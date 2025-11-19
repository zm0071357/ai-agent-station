package ai.agent.station.domain.agent.service.execute.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResponseBodyEmitter统一管理
 */
@Slf4j
@Component
public class ResponseBodyEmitterManager {

    private static final Map<String, ResponseBodyEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * 创建ResponseBodyEmitter
     * @param userId 用户ID
     * @param timeOut 超时时间
     */
    public static ResponseBodyEmitter put(String userId, long timeOut) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(timeOut);
        emitterMap.put(userId, emitter);
        log.info("存储ResponseBodyEmitter，用户ID：{}", userId);
        return emitter;
    }

    /**
     * 获取ResponseBodyEmitter
     * @param userId 用户ID
     * @return ResponseBodyEmitter
     */
    public static ResponseBodyEmitter get(String userId) {
        return emitterMap.get(userId);
    }

    /**
     * 移除ResponseBodyEmitter
     * @param userId 用户ID
     */
    public static void remove(String userId) {
        ResponseBodyEmitter emitter = emitterMap.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("移除ResponseBodyEmitter，用户ID: {}", userId);
            } catch (Exception e) {
                log.error("移除ResponseBodyEmitter时出错，用户ID: {}", userId, e);
            }
        }
    }

    /**
     * 发送消息
     * @param userId 用户ID
     * @param message 信息
     */
    public static ResponseBodyEmitter send(String userId, String message) {
        ResponseBodyEmitter emitter = get(userId);
        if (emitter != null) {
            try {
                emitter.send(message);
            } catch (IOException e) {
                log.error("发送信息时出错，用户ID: {}", userId, e);
            }
        }
        return emitter;
    }

}
