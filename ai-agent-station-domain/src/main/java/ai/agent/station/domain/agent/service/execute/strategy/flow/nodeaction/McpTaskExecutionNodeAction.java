package ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP工具任务助手状态图 - MCP任务执行节点
 */
@Slf4j
public class McpTaskExecutionNodeAction extends AbstractFlowNodeAction {

    private final ChatClient mcpTaskExecutorClient;

    public McpTaskExecutionNodeAction(ChatClient mcpTaskExecutorClient) {
        this.mcpTaskExecutorClient = mcpTaskExecutorClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        try {
            log.info("MCP工具任务助手状态图 - MCP任务执行节点");
            String prompt = state.value("prompt", "null");
            String userId = state.value("userId", "null");
            Map<String, String> stepsMap = state.value("stepsMap", new HashMap<>());
            int currentStep = state.value("currentStep", 3);
            log.info("MCP任务执行节点 - 任务：{}，用户：{}，当前执行步数：{}", prompt, userId, currentStep);

            // 按顺序执行规划步骤
            Map<String, Object> resultMap = executeStepsInOrder(mcpTaskExecutorClient, stepsMap, prompt);
            resultMap.put("executeCount", currentStep);

            // 执行步数增加
            currentStep ++;

            // 发送结果
            log.info("MCP任务规划节点 - 用户：{}，发送第 {} 步结果", userId, currentStep);
            sendResult(currentStep, "已完成所有规划步骤的执行", userId, ResponseBodyEmitterManager.get(userId));

            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 顺序执行任务步骤
     * @param mcpTaskExecutorClient 客户端
     * @param stepsMap 步骤Map
     * @param prompt 用户提示词
     * @return
     */
    private Map<String, Object> executeStepsInOrder(ChatClient mcpTaskExecutorClient, Map<String, String> stepsMap, String prompt) {
        // 按步骤编号排序执行
        List<Integer> stepNumbers = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        for (String stepKey : stepsMap.keySet()) {
            try {
                // 从"第1步"、"第2步"等格式中提取数字
                Pattern numberPattern = Pattern.compile("第(\\d+)步");
                Matcher matcher = numberPattern.matcher(stepKey);
                if (matcher.find()) {
                    stepNumbers.add(Integer.parseInt(matcher.group(1)));
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析步骤编号: {}", stepKey);
            }
        }
        // 排序步骤编号
        stepNumbers.sort(Integer::compareTo);
        // 按顺序执行每个步骤
        for (Integer stepNumber : stepNumbers) {
            String stepKey = "第" + stepNumber + "步";
            String stepContent = "";
            // 查找匹配的步骤内容
            for (Map.Entry<String, String> entry : stepsMap.entrySet()) {
                if (entry.getKey().startsWith(stepKey)) {
                    stepContent = entry.getValue();
                    break;
                }
            }
            if (stepContent != null) {
                // 执行单个任务
                String result = "这是第0步，所以没有结果";
                result = executeStep(mcpTaskExecutorClient, stepNumber, stepKey, stepContent, prompt, result);
                resultMap.put("step" + stepNumber, result);
            } else {
                log.warn("未找到步骤内容: {}", stepKey);
            }
        }
        return resultMap;
    }

    /**
     * 执行单个任务
     * @param mcpTaskExecutorClient 客户端
     * @param stepNumber 当前步骤数
     * @param stepKey 步骤编号
     * @param stepContent 任务
     * @param prompt 提示词
     * @param result 上一步执行结果
     * @return
     */
    private String executeStep(ChatClient mcpTaskExecutorClient, Integer stepNumber, String stepKey, String stepContent, String prompt, String result) {
        try {
            log.info("\n--- 开始执行 {} ---", stepKey);
            log.info("步骤内容: {}", stepContent);
            String mcpTaskExecutionPrompt = getMcpTaskExecutionPrompt(stepContent, prompt, result);
            Flux<String> mcpTaskExecutionFluxResult = mcpTaskExecutorClient.prompt()
                    .user(mcpTaskExecutionPrompt)
                    .stream()
                    .content();
                    // 收集片段组合成分析结果
            Mono<String> completeText = mcpTaskExecutionFluxResult
                    .collectList()
                    .map(list -> String.join("", list));
            String mcpTaskExecutionResult = completeText.block();
            assert mcpTaskExecutionResult != null;
            log.info("步骤 {} 执行结果: {}", stepNumber, mcpTaskExecutionResult);
            // 短暂延迟，避免请求过于频繁
            Thread.sleep(1000);
            return mcpTaskExecutionResult;
        } catch (Exception e) {
            log.error("执行步骤 {} 时发生错误: {}", stepNumber, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取提示词
     * @param stepContent 当前步骤任务
     * @param prompt 用户提示词
     * @param result 上一步执行结果
     * @return
     */
    private String getMcpTaskExecutionPrompt(String stepContent, String prompt, String result) {
        return "你是一个智能执行助手，需要执行以下步骤:\n\n" +
                "**步骤内容:**\n" +
                stepContent + "\n\n" +
                "**用户原始请求:**\n" +
                prompt + "\n\n" +
                "**上一步的执行结果**\n" +
                result + "\n\n" +
                "**执行要求:**\n" +
                "1. 仔细分析步骤内容，理解需要执行的具体任务\n" +
                "2. 如果涉及MCP工具调用，请使用相应的工具\n" +
                "3. 提供详细的执行过程和结果\n" +
                "4. 如果遇到问题，请说明具体的错误信息\n" +
                "5. **重要**: 执行完成后，必须在回复末尾明确输出执行结果，格式如下:\n" +
                "   ```\n" +
                "   === 执行结果 ===\n" +
                "   状态: [成功/失败]\n" +
                "   结果描述: [具体的执行结果描述]\n" +
                "   输出数据: [如果有具体的输出数据，请在此列出]\n" +
                "   ```\n\n" +
                "请开始执行这个步骤，并严格按照要求提供详细的执行报告和结果输出。";
    }

    @Override
    protected void sendResult(Integer currentStep, String result, String userId, ResponseBodyEmitter emitter) {
        ExecuteResultEntity executeResultEntity = ExecuteResultEntity.createExecutionResult(
                currentStep,
                result,
                userId);
        sendSseResult(emitter, executeResultEntity);
    }

}
