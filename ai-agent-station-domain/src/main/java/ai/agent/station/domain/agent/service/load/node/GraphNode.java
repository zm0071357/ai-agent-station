package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import ai.agent.station.domain.agent.service.execute.strategy.auto.nodeaction.QualitySupervisorTaskNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.auto.nodeaction.TaskAnalysisTaskNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction.McpTaskExecutionNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction.McpTaskParseStepsNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction.McpTaskPlanningNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction.McpToolsAnalysisNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.auto.nodeaction.ResultSummaryTaskNodeAction;
import ai.agent.station.domain.agent.service.execute.strategy.auto.nodeaction.TaskPrecisionTaskNodeAction;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Graph状态图节点
 */
@Slf4j
@Service
public class GraphNode extends AbstractLoadSupport {

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - Graph状态图节点，开始加载Graph");
        if (dynamicContext.getAgentType().equals(AgentTypeEnum.AUTO.getType())) {
            // 加载任务助手状态图
            loadTaskAssistantGraph(dynamicContext);
        } else if (dynamicContext.getAgentType().equals(AgentTypeEnum.FLOW.getType())) {
            // 加载MCP任务助手状态图
            loadMcpTaskAssistantGraph(dynamicContext);
        }
        return LoadResEntity.builder()
                .message("加载完成")
                .build();
    }

    /**
     * 加载TaskAssistantGraph
     * @param dynamicContext
     * @throws Exception
     */
    private void loadTaskAssistantGraph(DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - Graph状态图节点，开始加载TaskAssistantGraph");
        Map<String, ChatClient> chatClientMap = dynamicContext.getChatClientMap();
        ChatClient taskAnalysisClient = chatClientMap.get("taskAnalysisClient");
        ChatClient taskPrecisionClient = chatClientMap.get("taskPrecisionClient");
        ChatClient qualitySupervisorClient = chatClientMap.get("qualitySupervisorClient");
        ChatClient resultSummaryClient = chatClientMap.get("resultSummaryClient");

        // 定义数据处理策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("prompt", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("userId", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("maxStep", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("currentStep", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("analysisResult", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("precisionResult", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("supervisionResult", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("summaryResult", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("isContinue", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("isPass", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("history", KeyStrategy.APPEND);
            keyStrategyHashMap.put("isCompleted", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("advisorList", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("key", KeyStrategy.REPLACE);
            return keyStrategyHashMap;
        };

        // 定义状态图
        String beanName = "taskAssistantGraph";
        StateGraph stateGraph = new StateGraph(beanName, keyStrategyFactory);

        // 新增处理节点
        stateGraph.addNode("taskAnalysisNodeAction", AsyncNodeAction.node_async(new TaskAnalysisTaskNodeAction(taskAnalysisClient)));
        stateGraph.addNode("taskPrecisionNodeAction", AsyncNodeAction.node_async(new TaskPrecisionTaskNodeAction(taskPrecisionClient)));
        stateGraph.addNode("qualitySupervisorNodeAction", AsyncNodeAction.node_async(new QualitySupervisorTaskNodeAction(qualitySupervisorClient)));
        stateGraph.addNode("resultSummaryNodeAction", AsyncNodeAction.node_async(new ResultSummaryTaskNodeAction(resultSummaryClient)));

        // 新增边
        // 起始边 - 任务分析
        stateGraph.addEdge(StateGraph.START, "taskAnalysisNodeAction");
        // 任务分析 - 可继续（任务执行）、不可继续（结果总结）
        stateGraph.addConditionalEdges("taskAnalysisNodeAction",
                AsyncEdgeAction.edge_async(state -> state.value("isContinue", "NO")),
                Map.of(
                        "YES", "taskPrecisionNodeAction",
                        "NO", "resultSummaryNodeAction"
                ));
        // 任务执行 - 可继续（质量监督）、不可继续（结果总结）
        stateGraph.addConditionalEdges("taskPrecisionNodeAction",
                AsyncEdgeAction.edge_async(state -> state.value("isContinue", "NO")),
                Map.of(
                        "YES", "qualitySupervisorNodeAction",
                        "NO", "resultSummaryNodeAction"
                ));
        // 质量监督 - 通过（结果总结）、不通过（任务分析）、再优化（任务执行）、已达最大步数（结果总结）
        stateGraph.addConditionalEdges("qualitySupervisorNodeAction",
                AsyncEdgeAction.edge_async(state -> state.value("isPass", "YES")),
                Map.of(
                        "YES", "resultSummaryNodeAction",
                        "NO", "taskAnalysisNodeAction",
                        "REOPTIMIZATION", "taskPrecisionNodeAction",
                        "MAXSTEP", "resultSummaryNodeAction"
                ));
        // 结果总结 - 结束边
        stateGraph.addEdge("resultSummaryNodeAction", StateGraph.END);
        // 编译
        CompiledGraph compiledGraph = stateGraph.compile();
        // 注册Bean
        registerBean(beanName, CompiledGraph.class, compiledGraph);
        log.info("Agent构建 - Graph状态图节点，加载TaskAssistantGraph完成");
    }

    /**
     * 加载McpTaskAssistantGraph
     * @param dynamicContext
     * @throws Exception
     */
    private void loadMcpTaskAssistantGraph(DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - Graph状态图节点，开始加载FlowAssistantGraph");
        Map<String, ChatClient> chatClientMap = dynamicContext.getChatClientMap();
        ChatClient mcpToolsAnalysisClient = chatClientMap.get("mcpToolsAnalysisClient");
        ChatClient mcpTaskPlanningClient = chatClientMap.get("mcpTaskPlanningClient");
        ChatClient mcpTaskExecutorClient = chatClientMap.get("mcpTaskExecutorClient");

        // 定义数据处理策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("prompt", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("userId", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("currentStep", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("mcpToolsAnalysisResult", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("mcpToolsTaskPlanningResult", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("stepsMap", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("executeCount", KeyStrategy.REPLACE);
            keyStrategyHashMap.put("key", KeyStrategy.REPLACE);
            return keyStrategyHashMap;
        };

        // 定义状态图
        String beanName = "mcpTaskAssistantGraph";
        StateGraph stateGraph = new StateGraph(beanName, keyStrategyFactory);

        // 新增处理节点
        stateGraph.addNode("mcpToolsAnalysisNodeAction", AsyncNodeAction.node_async(new McpToolsAnalysisNodeAction(mcpToolsAnalysisClient)));
        stateGraph.addNode("mcpTaskPlanningNodeAction", AsyncNodeAction.node_async(new McpTaskPlanningNodeAction(mcpTaskPlanningClient)));
        stateGraph.addNode("mcpTaskParseStepsNodeAction", AsyncNodeAction.node_async(new McpTaskParseStepsNodeAction()));
        stateGraph.addNode("mcpTaskExecutionNodeAction", AsyncNodeAction.node_async(new McpTaskExecutionNodeAction(mcpTaskExecutorClient)));

        // 新增边
        stateGraph.addEdge(StateGraph.START, "mcpToolsAnalysisNodeAction");
        stateGraph.addEdge("mcpToolsAnalysisNodeAction", "mcpTaskPlanningNodeAction");
        stateGraph.addEdge("mcpTaskPlanningNodeAction", "mcpTaskParseStepsNodeAction");
        stateGraph.addEdge("mcpTaskParseStepsNodeAction", "mcpTaskExecutionNodeAction");
        stateGraph.addEdge("mcpTaskExecutionNodeAction", StateGraph.END);

        // 编译
        CompiledGraph compiledGraph = stateGraph.compile();

        // 注册Bean
        registerBean(beanName, CompiledGraph.class, compiledGraph);
        log.info("Agent构建 - Graph状态图节点，加载FlowAssistantGraph完成");
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return defaultStrategyHandler;
    }

}
