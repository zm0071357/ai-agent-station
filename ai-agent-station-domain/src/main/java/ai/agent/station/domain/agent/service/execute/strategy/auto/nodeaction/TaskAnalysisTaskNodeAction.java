package ai.agent.station.domain.agent.service.execute.strategy.auto.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static ai.agent.station.types.common.Constants.*;

/**
 * 任务助手状态图 - 任务分析节点
 */
@Slf4j
public class TaskAnalysisTaskNodeAction extends AbstractTaskNodeAction {

    private final ChatClient taskAnalysisClient;

    public TaskAnalysisTaskNodeAction(ChatClient taskAnalysisClient) {
        this.taskAnalysisClient = taskAnalysisClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("任务助手状态图 - 任务分析节点");
        String prompt = state.value("prompt", "");
        String userId = state.value("userId", "");
        int maxStep = state.value("maxStep", DEFAULT_MAX_STEP);
        int currentStep = state.value("currentStep", DEFAULT_CURRENT_STEP);
        String tag = state.value("tag", "");
        String key = state.value("key", "");
        log.info("任务分析节点 - 任务：{}，用户：{}，最大执行步数：{}，当前执行步数：{}", prompt, userId, maxStep, currentStep);
        // 提示词
        List<String> historyList = state.value("history", List.of());
        String history = String.join("\n", historyList);
        String analysisPrompt = String.format("""
                    **原始用户需求:** %s
                    
                    **历史质量监督:** %s
                        
                    **分析要求:**
                    请深入分析用户的具体需求，制定明确的执行策略：
                    1. 理解用户真正想要什么（如：具体的学习计划、项目列表、技术方案等）
                    2. 分析需要哪些具体的执行步骤（如：搜索信息、检索项目、生成内容等）
                    3. 制定能够产生实际结果的执行策略
                    4. 确保策略能够直接回答用户的问题
                        
                    **输出格式要求:**
                    请严格按照以下给定的格式和标签来组织你的回答，不要添加任何额外的解释、前言或总结。你的输出必须且只能是以下结构：
                    任务状态分析: [当前任务完成情况的详细分析]
                    执行历史评估: [对已完成工作的质量和效果评估]
                    下一步策略: [具体的执行计划，包括需要调用的工具和生成的内容]
                    """,
                prompt,
                history);

        // 大模型调用
        Flux<String> analysisFluxResult = taskAnalysisClient.prompt(analysisPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, key)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .advisors(getRagAnswerAdvisorList(userId, tag))
                .stream()
                .content();

        // 收集片段组合成分析结果
        Mono<String> completeText = analysisFluxResult
                .collectList()
                .map(list -> String.join("", list));
        String analysisResult = completeText.block();

        assert analysisResult != null;
        log.info("任务分析节点 - 任务：{}，用户：{}，任务分析结果：\n{}", prompt, userId, analysisResult);

        // 执行步数增加
        currentStep ++;

        // 是否继续执行和是否执行完成
        String isContinue;
        String isCompleted;
        if (currentStep >= maxStep) {
            log.info("任务分析节点 - 任务：{}，用户：{}，最大执行步数：{}，当前执行步数：{}，已达最大执行步数，进入结果总结节点", prompt, userId, maxStep, currentStep);
            isContinue = "NO";
            isCompleted = "YES";
        } else {
            log.info("任务分析节点 - 任务：{}，用户：{}，最大执行步数：{}，当前执行步数：{}，继续执行，进入任务执行节点", prompt, userId, maxStep, currentStep);
            isContinue = "YES";
            isCompleted = "NO";
        }

        // 解析和发送结果
        log.info("任务分析节点 - 用户：{}，解析第 {} 步结果", userId, currentStep);
        parseResult(ResponseBodyEmitterManager.get(key), currentStep, analysisResult, userId);

        // 写入上下文
        return Map.of(
                "analysisResult", analysisResult,
                "currentStep", currentStep,
                "isContinue", isContinue,
                "isCompleted", isCompleted
        );
    }

    @Override
    protected void parseResult(ResponseBodyEmitter emitter, int currentStep, String analysisResult, String userId) {
        // 将分析结果分段
        String[] lines = analysisResult.split("\n");
        // 子类型
        String subType = "";
        // 发送文本段
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            // 去除字符串两端的空白字符
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.contains("任务状态分析:")) {
                // 发送上一个section的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "analysis_status";
                sectionContent = new StringBuilder();
            } else if (line.contains("执行历史评估:")) {
                // 发送上一个section的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "analysis_history";
                sectionContent = new StringBuilder();
            } else if (line.contains("下一步策略:")) {
                // 发送上一个section的内容
                sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
                subType = "analysis_strategy";
                sectionContent = new StringBuilder();
            }

            // 收集当前section的内容
            if (!subType.isEmpty()) {
                sectionContent.append(line).append("\n");
                switch (subType) {
                    case "analysis_status" -> log.info("📊 {}", line);
                    case "analysis_history" -> log.info("📈 {}", line);
                    case "analysis_strategy" -> log.info("🚀 {}", line);
                }
            }
        }
        // 发送最后一个section的内容
        sendResult(emitter, currentStep, subType, sectionContent.toString(), userId);
    }

    @Override
    protected void sendResult(ResponseBodyEmitter emitter, int currentStep, String subType, String content, String userId) {
        if (!subType.isEmpty() && !content.isEmpty()) {
            ExecuteResultEntity executeResultEntity = ExecuteResultEntity.createAnalysisSubResult(currentStep, subType, content, userId);
            sendSseResult(emitter, executeResultEntity);
        }
    }

}
