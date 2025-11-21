package ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP工具助手状态图 - 规划步骤解析节点
 */
@Slf4j
public class McpTaskParseStepsNodeAction extends AbstractFlowNodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("MCP工具任务助手状态图 - 规划步骤解析节点");
        String prompt = state.value("prompt", "null");
        String userId = state.value("userId", "null");
        String mcpToolsTaskPlanningResult = state.value("mcpToolsTaskPlanningResult", "");
        int currentStep = state.value("currentStep", 2);
        log.info("规划步骤解析节点 - 任务：{}，用户：{}，当前执行步数：{}", prompt, userId, currentStep);
        if (StringUtils.isBlank(mcpToolsTaskPlanningResult)) {
            log.warn("MCP工具任务助手状态图 - 规划步骤解析节点，规划结果为空，无法解析步骤，任务：{}，用户ID：{}", prompt, userId);
            throw new RuntimeException("规划结果为空，无法解析步骤");
        }
        Map<String, String> stepsMap = parseExecutionSteps(mcpToolsTaskPlanningResult);
        log.info("MCP工具任务助手状态图 - 规划步骤解析节点，任务：{}，用户ID：{}，成功解析 {} 个执行步骤：{}", prompt, userId, stepsMap.size(), JSON.toJSONString(stepsMap));

        StringBuilder parseResult = new StringBuilder();
        parseResult.append("## 步骤解析结果\n\n");
        parseResult.append(String.format("成功解析 %d 个执行步骤：\n\n", stepsMap.size()));

        for (Map.Entry<String, String> entry : stepsMap.entrySet()) {
            parseResult.append(String.format("- **%s**: %s\n",
                    entry.getKey(),
                    entry.getValue().split("\n")[0]));
        }

        // 执行步数增加
        currentStep ++;

        // 发送结果
        log.info("规划步骤解析节点 - 用户：{}，发送第 {} 步结果", userId, currentStep);
        sendResult(currentStep, parseResult.toString(), userId, ResponseBodyEmitterManager.get(userId));

        // 写入上下文
        return Map.of(
                "stepsMap", stepsMap,
                "currentStep", currentStep
        );
    }

    /**
     * 解析执行步骤
     * @param mcpToolsTaskPlanningResult 任务规划结果
     * @return
     */
    private Map<String, String> parseExecutionSteps(String mcpToolsTaskPlanningResult) {
        Map<String, String> stepsMap = new HashMap<>();
        if (mcpToolsTaskPlanningResult == null || mcpToolsTaskPlanningResult.trim().isEmpty()) {
            return stepsMap;
        }
        try {
            // 使用正则表达式匹配步骤标题和详细内容
            Pattern stepPattern = Pattern.compile("### (第\\d+步：[^\\n]+)([\\s\\S]*?)(?=### 第\\d+步：|$)");
            Matcher matcher = stepPattern.matcher(mcpToolsTaskPlanningResult);
            while (matcher.find()) {
                String stepTitle = matcher.group(1).trim();
                String stepContent = matcher.group(2).trim();
                // 提取步骤编号
                Pattern numberPattern = Pattern.compile("第(\\d+)步：");
                Matcher numberMatcher = numberPattern.matcher(stepTitle);
                if (numberMatcher.find()) {
                    String stepNumber = "第" + numberMatcher.group(1) + "步";
                    String fullStepInfo = stepTitle + "\n" + stepContent;
                    stepsMap.put(stepNumber, fullStepInfo);
                    log.debug("解析步骤: {} -> {}", stepNumber, stepTitle);
                }
            }
            // 如果没有匹配到详细步骤，尝试匹配简单的步骤列表
            if (stepsMap.isEmpty()) {
                Pattern simpleStepPattern = Pattern.compile("\\[ \\] (第\\d+步：[^\\n]+)");
                Matcher simpleMatcher = simpleStepPattern.matcher(mcpToolsTaskPlanningResult);
                while (simpleMatcher.find()) {
                    String stepTitle = simpleMatcher.group(1).trim();
                    Pattern numberPattern = Pattern.compile("第(\\d+)步：");
                    Matcher numberMatcher = numberPattern.matcher(stepTitle);
                    if (numberMatcher.find()) {
                        String stepNumber = "第" + numberMatcher.group(1) + "步";
                        stepsMap.put(stepNumber, stepTitle);
                        log.debug("解析简单步骤: {} -> {}", stepNumber, stepTitle);
                    }
                }
            }
            log.info("成功解析 {} 个执行步骤", stepsMap.size());
        } catch (Exception e) {
            log.error("解析规划结果时发生错误", e);
        }
        return stepsMap;
    }

    @Override
    protected void sendResult(Integer currentStep, String result, String userId, ResponseBodyEmitter emitter) {
        ExecuteResultEntity executeResultEntity = ExecuteResultEntity.createAnalysisSubResult(
                currentStep,
                "analysis_progress",
                result,
                userId);
        sendSseResult(emitter, executeResultEntity);
    }

}
