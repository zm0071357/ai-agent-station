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
     * @param key 会话标识
     * @param timeOut 超时时间
     */
    public static ResponseBodyEmitter put(String key, long timeOut) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(timeOut);
        emitterMap.put(key, emitter);
        log.info("存储ResponseBodyEmitter，会话标识：{}", key);
        return emitter;
    }

    /**
     * 获取ResponseBodyEmitter
     * @param key 会话标识
     * @return ResponseBodyEmitter
     */
    public static ResponseBodyEmitter get(String key) {
        return emitterMap.get(key);
    }

    /**
     * 移除ResponseBodyEmitter
     * @param key 会话标识
     */
    public static void remove(String key) {
        ResponseBodyEmitter emitter = emitterMap.remove(key);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("移除ResponseBodyEmitter，会话标识: {}", key);
            } catch (Exception e) {
                log.error("移除ResponseBodyEmitter时出错，会话标识: {}", key, e);
            }
        }
    }

    /**
     * 发送消息
     * @param key 会话标识
     * @param message 信息
     */
    public static ResponseBodyEmitter send(String key, String message) {
        ResponseBodyEmitter emitter = get(key);
        if (emitter != null) {
            try {
                emitter.send(message);
            } catch (IOException e) {
                log.error("发送信息时出错，会话标识: {}", key, e);
            }
        }
        return emitter;
    }

}
