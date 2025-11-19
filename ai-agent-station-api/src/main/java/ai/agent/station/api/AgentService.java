package ai.agent.station.api;

import ai.agent.station.api.dto.ChatRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface AgentService {

    /**
     * 流式对话
     * @param chatRequestDTO
     * @param response
     * @return
     */
    ResponseBodyEmitter agentChat(ChatRequestDTO chatRequestDTO, HttpServletResponse response);

}
