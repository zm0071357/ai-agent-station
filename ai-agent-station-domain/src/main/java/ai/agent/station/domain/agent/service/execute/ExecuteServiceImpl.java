package ai.agent.station.domain.agent.service.execute;

import ai.agent.station.domain.agent.adapter.repository.AgentRepository;
import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.valobj.AgentVO;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import ai.agent.station.domain.agent.service.execute.strategy.ExecuteStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;

@Slf4j
@Service
public class ExecuteServiceImpl implements ExecuteService {

    @Resource
    private AgentRepository agentRepository;

    @Resource
    private Map<String, ExecuteStrategy> executeStrategyMap;

    @Override
    public void execute(ChatRequestEntity chatRequestEntity, ResponseBodyEmitter emitter) throws Exception {
        // 获取Agent
        AgentVO agentVO = agentRepository.getAgentByAgentId(chatRequestEntity.getAgentId());
        // 根据Agent类型获取对应处理策略 - 获取对应服务
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.getByType(agentVO.getAgentType());
        log.info("用户ID：{}，Agent ID：{}，Agent 类型：{}", chatRequestEntity.getUserId(), chatRequestEntity.getAgentId(), agentTypeEnum.getType());
        ExecuteStrategy executeStrategy = executeStrategyMap.get(agentTypeEnum.getStrategy());
        // 执行调用策略
        executeStrategy.execute(chatRequestEntity, agentTypeEnum, emitter);
    }

}
