package ai.agent.station.domain.agent.service.execute;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface ExecuteService {

    /**
     * 调用
     * @param chatRequestEntity
     * @param emitter
     */
    void execute(ChatRequestEntity chatRequestEntity, ResponseBodyEmitter emitter) throws Exception;

}
