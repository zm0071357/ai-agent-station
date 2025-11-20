package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.service.execute.nodeaction.QualitySupervisorNodeAction;
import ai.agent.station.domain.agent.service.execute.nodeaction.ResultSummaryNodeAction;
import ai.agent.station.domain.agent.service.execute.nodeaction.TaskAnalysisNodeAction;
import ai.agent.station.domain.agent.service.execute.nodeaction.TaskPrecisionNodeAction;
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
            return keyStrategyHashMap;
        };

        // 定义状态图
        String beanName = "taskAssistantGraph";
        StateGraph stateGraph = new StateGraph(beanName, keyStrategyFactory);

        // 新增处理节点
        stateGraph.addNode("taskAnalysisNodeAction", AsyncNodeAction.node_async(new TaskAnalysisNodeAction(taskAnalysisClient)));
        stateGraph.addNode("taskPrecisionNodeAction", AsyncNodeAction.node_async(new TaskPrecisionNodeAction(taskPrecisionClient)));
        stateGraph.addNode("qualitySupervisorNodeAction", AsyncNodeAction.node_async(new QualitySupervisorNodeAction(qualitySupervisorClient)));
        stateGraph.addNode("resultSummaryNodeAction", AsyncNodeAction.node_async(new ResultSummaryNodeAction(resultSummaryClient)));

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

        return LoadResEntity.builder()
                .message("加载完成")
                .build();
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return defaultStrategyHandler;
    }

}
