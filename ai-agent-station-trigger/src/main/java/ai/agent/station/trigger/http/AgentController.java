package ai.agent.station.trigger.http;

import ai.agent.station.api.AgentService;
import ai.agent.station.api.dto.ChatRequestDTO;
import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.service.execute.ExecuteService;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/agent")
public class AgentController implements AgentService {

    @Resource
    private ExecuteService executeService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @PostMapping("/chat")
    @Override
    public ResponseBodyEmitter agentChat(@RequestBody ChatRequestDTO chatRequestDTO, HttpServletResponse response) {
        try {
            // 构建流式输出对象
            ResponseBodyEmitter emitter = ResponseBodyEmitterManager.put(chatRequestDTO.getUserId(), Long.MAX_VALUE);
            // 参数校验
            if (StringUtils.isBlank(chatRequestDTO.getUserId()) || StringUtils.isBlank(chatRequestDTO.getPrompt()) ||
                    chatRequestDTO.getMaxStep() == null) {
                return ResponseBodyEmitterManager.send(chatRequestDTO.getUserId(), "参数非法");
            }
            // 异步执行Agent
            threadPoolExecutor.execute(() -> {
                try {
                    executeService.execute(
                            ChatRequestEntity.builder()
                                    .userId(chatRequestDTO.getUserId())
                                    .prompt(chatRequestDTO.getPrompt())
                                    .maxStep(chatRequestDTO.getMaxStep())
                                    .build(),
                            emitter);
                } catch (Exception e) {
                    log.error("Agent执行异常：{}", e.getMessage(), e);
                    try {
                        emitter.send("Agent执行异常：" + e.getMessage());
                    } catch (Exception ex) {
                        log.error("发送异常信息失败：{}", ex.getMessage(), ex);
                    }
                } finally {
                    ResponseBodyEmitterManager.remove(chatRequestDTO.getUserId());
                }
            });
            return emitter;
        } catch (Exception e) {
            log.error("Agent请求处理异常：{}", e.getMessage(), e);
            return ResponseBodyEmitterManager.send(chatRequestDTO.getUserId(), "请求处理失败：" + e.getMessage());
        }
    }

}
