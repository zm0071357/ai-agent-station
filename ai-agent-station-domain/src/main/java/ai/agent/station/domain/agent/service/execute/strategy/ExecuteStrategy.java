package ai.agent.station.domain.agent.service.execute.strategy;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface ExecuteStrategy {

    /**
     * 调用
     * @param chatRequestEntity
     * @param agentTypeEnum
     * @param emitter
     */
    void execute(ChatRequestEntity chatRequestEntity, AgentTypeEnum agentTypeEnum, ResponseBodyEmitter emitter) throws Exception;

}
