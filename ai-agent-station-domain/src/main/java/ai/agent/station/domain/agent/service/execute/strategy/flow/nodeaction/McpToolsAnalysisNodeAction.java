package ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * MCP工具助手状态图 - MCP工具分析节点
 */
@Slf4j
public class McpToolsAnalysisNodeAction extends AbstractFlowNodeAction {

    private final ChatClient mcpToolsManagerClient;

    public McpToolsAnalysisNodeAction(ChatClient mcpToolsManagerClient) {
        this.mcpToolsManagerClient = mcpToolsManagerClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("MCP工具任务助手状态图 - MCP工具分析节点");
        String prompt = state.value("prompt", "");
        String userId = state.value("userId", "");
        int currentStep = state.value("currentStep", 0);
        String key = state.value("key", "");
        log.info("MCP工具分析节点 - 任务：{}，用户：{}，当前执行步数：{}", prompt, userId, currentStep);
        // 提示词
        String mcpToolsAnalysisPrompt = String.format("""
                        # MCP工具能力分析任务
                        
                        ## 重要说明
                        **注意：本阶段仅进行MCP工具能力分析，不执行用户的实际请求。**\s
                        这是一个纯分析阶段，目的是评估可用工具的能力和适用性，为后续的执行规划提供依据。
                        
                        ## 用户请求
                        %s
                        
                        ## 分析要求
                        请基于上述实际的MCP工具信息，针对用户请求进行详细的工具能力分析（仅分析，不执行）：
                        
                        ### 1. 工具匹配分析
                        - 分析每个可用工具的核心功能和适用场景
                        - 评估哪些工具能够满足用户请求的具体需求
                        - 标注每个工具的匹配度（高/中/低）
                        
                        ### 2. 工具使用指南
                        - 提供每个相关工具的具体调用方式
                        - 说明必需的参数和可选参数
                        - 给出参数的示例值和格式要求
                        
                        ### 3. 执行策略建议
                        - 推荐最优的工具组合方案
                        - 建议工具的调用顺序和依赖关系
                        - 提供备选方案和降级策略
                        
                        ### 4. 注意事项
                        - 标注工具的使用限制和约束条件
                        - 提醒可能的错误情况和处理方式
                        - 给出性能优化建议
                        
                        ### 5. 分析总结
                        - 明确说明这是分析阶段，不要执行用的任何实际操作
                        - 总结工具能力评估结果
                        - 为后续执行阶段提供建议
                        
                        请确保分析结果准确、详细、可操作，并再次强调这仅是分析阶段。
                        """,
                prompt);

        // 大模型调用
        Flux<String> mcpToolsAnalysisFluxResult = mcpToolsManagerClient.prompt()
                .user(mcpToolsAnalysisPrompt)
                .stream()
                .content();

        // 收集片段组合成分析结果
        Mono<String> completeText = mcpToolsAnalysisFluxResult
                .collectList()
                .map(list -> String.join("", list));
        String mcpToolsAnalysisResult = completeText.block();

        assert mcpToolsAnalysisResult != null;
        log.info("MCP工具分析节点 - 任务：{}，用户：{}，MCP工具分析结果：\n{}", prompt, userId, mcpToolsAnalysisResult);

        // 执行步数增加
        currentStep ++;

        // 发送结果
        log.info("MCP工具分析节点 - 用户：{}，发送第 {} 步结果", userId, currentStep);
        sendResult(currentStep, mcpToolsAnalysisResult, userId, ResponseBodyEmitterManager.get(key));

        // 写入上下文
        return Map.of(
                "mcpToolsAnalysisResult", mcpToolsAnalysisResult,
                "currentStep", currentStep
        );
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
