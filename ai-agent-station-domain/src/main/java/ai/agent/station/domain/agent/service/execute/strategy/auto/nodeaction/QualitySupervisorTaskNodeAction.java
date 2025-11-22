package ai.agent.station.domain.agent.service.execute.strategy.auto.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.model.entity.SupervisionResultEntity;
import ai.agent.station.domain.agent.model.valobj.enums.SupervisionEnum;
import ai.agent.station.domain.agent.model.valobj.enums.SupervisionResultEnum;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static ai.agent.station.types.common.Constants.*;

/**
 * 任务助手状态图 - 质量监督节点
 */
@Slf4j
public class QualitySupervisorTaskNodeAction extends AbstractTaskNodeAction {

    private final ChatClient qualitySupervisorClient;

    public QualitySupervisorTaskNodeAction(ChatClient qualitySupervisorClient) {
        this.qualitySupervisorClient = qualitySupervisorClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("任务助手状态图 - 质量监督节点");
        String prompt = state.value("prompt", "");
        String userId = state.value("userId", "");
        int maxStep = state.value("maxStep", DEFAULT_MAX_STEP);
        int currentStep = state.value("currentStep", DEFAULT_CURRENT_STEP);
        String tag = state.value("tag", "");
        String key = state.value("key", "");
        log.info("质量监督节点 - 任务：{}，用户：{}，最大执行步数：{}，当前执行步数：{}", prompt, userId, maxStep, currentStep);

        // 提示词
        String precisionResult = state.value("precisionResult", "跳过当前节点，直接不通过");
        String supervisionPrompt = String.format("""
                    **用户原始需求:** %s
                    
                    **执行结果:** %s
                    
                    **监督要求:** 
                    请严格评估执行结果是否真正满足了用户的原始需求：
                    1. 检查是否直接回答了用户的问题
                    2. 评估内容的完整性和实用性
                    3. 确认是否提供了用户期望的具体结果（如学习计划、项目列表等）
                    4. 判断是否只是描述过程而没有给出实际答案
                    
                    **输出格式:**
                    请严格按照以下给定的格式和标签来组织你的回答，不要添加任何额外的解释、前言或总结。你的输出必须且只能是以下结构：
                    需求匹配度: [执行结果与用户原始需求的匹配程度分析]
                    内容完整性: [内容是否完整、具体、实用]
                    问题识别: [发现的问题和不足，特别是是否偏离了用户真正的需求]
                    改进建议: [具体的改进建议，确保能直接满足用户需求]
                    质量评分: [1-10分的质量评分]
                    是否通过: [通过/不通过/再优化]
                    """,
                prompt,
                precisionResult);

        // 大模型调用
        Flux<String> supervisionFluxResult = qualitySupervisorClient.prompt(supervisionPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, key)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .advisors(getRagAnswerAdvisorList(userId, tag))
                .stream()
                .content();

        // 收集片段组合成监督结果
        Mono<String> completeText = supervisionFluxResult
                .collectList()
                .map(list -> String.join("", list));
        String supervisionResult = completeText.block();
        assert supervisionResult != null;
        log.info("质量监督节点 - 任务：{}，用户：{}，质量监督结果：\n{}", prompt, userId, supervisionResult);

        // 执行步数增加
        currentStep ++;

        // 解析和发送结果
        log.info("质量监督节点 - 用户：{}，解析第 {} 步结果", userId, currentStep);
        parseResult(ResponseBodyEmitterManager.get(key), currentStep, supervisionResult, userId);

        // 获取枚举
        SupervisionResultEnum supervisionResultEnum = SupervisionResultEnum.get(currentStep >= maxStep, SupervisionEnum.getSupervision(supervisionResult));
        return supervisionResultEnum.getResult(SupervisionResultEntity.builder()
                .supervisionResult(supervisionResult)
                .currentStep(currentStep)
                .history(supervisionResult)
                .build());
    }

    @Override
    protected void parseResult(ResponseBodyEmitter emitter, int currentStep, String supervisionResult, String userId) {
        // 将分析结果分段
        String[] lines = supervisionResult.split("\n");
        // 子类型
        String subType = "";
        // 发送文本段
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.contains("质量评估:")) {
                // 发送前一个部分的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "supervision_assessment";
                sectionContent = new StringBuilder();
            } else if (line.contains("问题识别:")) {
                // 发送前一个部分的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "supervision_issues";
                sectionContent = new StringBuilder();
            } else if (line.contains("改进建议:")) {
                // 发送前一个部分的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "supervision_suggestions";
                sectionContent = new StringBuilder();
            } else if (line.contains("质量评分:")) {
                // 发送前一个部分的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "supervision_score";
                sectionContent = new StringBuilder();
                String score = line.substring(line.indexOf(":") + 1).trim();
                sectionContent.append(score);
            } else if (line.contains("是否通过:")) {
                // 发送前一个部分的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "supervision_pass";
                sectionContent = new StringBuilder();
                String status = line.substring(line.indexOf(":") + 1).trim();
                sectionContent.append(status);
            }

            // 收集当前部分的内容
            if (!subType.isEmpty()) {
                if (!sectionContent.isEmpty()) {
                    sectionContent.append("\n");
                }
                sectionContent.append(line);
            }

            switch (subType) {
                case "supervision_assessment" -> log.info("📋 {}", line);
                case "supervision_issues" -> log.info("⚠️ {}", line);
                case "supervision_suggestions" -> log.info("💡 {}", line);
                case "supervision_score" -> log.info("📝 {}", line);
                case "supervision_pass" -> log.info("✅ {}", line);
            }
        }
        // 发送最后一个部分的内容
        sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
    }

    @Override
    protected void sendResult(ResponseBodyEmitter emitter, int currentStep, String subType, String content, String userId) {
        if (!subType.isEmpty() && !content.isEmpty()) {
            ExecuteResultEntity executeResultEntity = ExecuteResultEntity.createSupervisionSubResult(currentStep, subType, content, userId);
            sendSseResult(emitter, executeResultEntity);
        }
    }

}
