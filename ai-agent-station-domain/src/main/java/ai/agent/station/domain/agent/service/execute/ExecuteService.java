package ai.agent.station.domain.agent.service.execute;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;

public interface ExecuteService {

    /**
     * 调用
     * @param chatRequestEntity
     */
    void execute(ChatRequestEntity chatRequestEntity) throws Exception;

}
