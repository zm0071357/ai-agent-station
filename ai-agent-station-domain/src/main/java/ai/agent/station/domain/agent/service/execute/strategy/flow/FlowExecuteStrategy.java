package ai.agent.station.domain.agent.service.execute.strategy.flow;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import ai.agent.station.domain.agent.service.execute.factory.DefaultExecuteLogicLinkFactory;
import ai.agent.station.domain.agent.service.execute.strategy.AbstractExecuteStrategy;
import ai.agent.station.types.framework.link.multition.chain.BusinessLinkedList;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service("flowExecuteStrategy")
public class FlowExecuteStrategy extends AbstractExecuteStrategy {

    @Lazy
    @Resource
    private CompiledGraph mcpTaskAssistantGraph;

    @Resource
    private BusinessLinkedList<ChatRequestEntity, DefaultExecuteLogicLinkFactory.DynamicContext, CheckRequestEntity> flowExecuteLogicLink;

    @Override
    protected CheckRequestEntity checkRequest(ChatRequestEntity chatRequestEntity, AgentTypeEnum agentTypeEnum) throws Exception {
        DefaultExecuteLogicLinkFactory.DynamicContext dynamicContext = new DefaultExecuteLogicLinkFactory.DynamicContext();
        dynamicContext.setAgentTypeEnum(agentTypeEnum);
        return flowExecuteLogicLink.apply(chatRequestEntity,dynamicContext);
    }

    @Override
    protected Map<String, Object> invoke(ChatRequestEntity chatRequestEntity) {
        HashMap<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("prompt", chatRequestEntity.getPrompt());
        requestParamMap.put("userId", chatRequestEntity.getUserId());
        Optional<OverAllState> overAllState = mcpTaskAssistantGraph.invoke(requestParamMap);
        Map<String, Object> map = overAllState.map(OverAllState::data).orElse(Map.of());
        log.info("用户请求：{}，返回结果：{}", JSON.toJSONString(chatRequestEntity), JSON.toJSONString(map));
        return map;
    }

}
