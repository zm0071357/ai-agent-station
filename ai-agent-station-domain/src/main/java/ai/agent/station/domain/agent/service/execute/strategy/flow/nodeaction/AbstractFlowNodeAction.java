package ai.agent.station.domain.agent.service.execute.strategy.flow.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;

@Slf4j
public abstract class AbstractFlowNodeAction implements NodeAction {

    /**
     * 通用发送SSE结果
     * @param emitter 流式输出处理
     * @param executeResultEntity 阶段处理结果
     */
    protected void sendSseResult(ResponseBodyEmitter emitter, ExecuteResultEntity executeResultEntity) {
        try {
            if (emitter != null) {
                // 发送SSE格式的数据
                String sseData = "data: " + JSON.toJSONString(executeResultEntity) + "\n\n";
                emitter.send(sseData);
            }
        } catch (IOException e) {
            log.error("发送SSE结果失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 抽象发送结果方法
     * @param currentStep 当前执行步数
     * @param result 结果
     * @param userId 用户ID
     */
    protected abstract void sendResult(Integer currentStep, String result, String userId, ResponseBodyEmitter emitter);

}
