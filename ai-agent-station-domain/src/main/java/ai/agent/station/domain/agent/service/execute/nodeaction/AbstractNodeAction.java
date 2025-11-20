package ai.agent.station.domain.agent.service.execute.nodeaction;

import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.service.execute.manager.RagAnswerAdvisorManager;
import ai.agent.station.domain.agent.service.load.advisor.RagAnswerAdvisor;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象NodeAction
 */
@Slf4j
public abstract class AbstractNodeAction implements NodeAction {

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
     * 解析结果
     * @param emitter 流式处理
     * @param currentStep 当前执行步数
     * @param result 结果
     * @param userId 用户ID
     */
    protected abstract void parseResult(ResponseBodyEmitter emitter, int currentStep, String result, String userId);

    /**
     * 发送子结果
     * @param emitter 流式处理
     * @param currentStep 当前执行步数
     * @param subType 子类型标识
     * @param content 子内容
     * @param userId 用户ID
     */
    protected abstract void sendResult(ResponseBodyEmitter emitter, int currentStep, String subType, String content, String userId);

    /**
     * 通用获取RagAnswerAdvisor集合
     * @param userId 用户ID
     * @param tag 知识库标签
     * @return
     */
    protected List<Advisor> getRagAnswerAdvisorList(String userId, String tag) {
        List<Advisor> advisorList = new ArrayList<>();
        if (StringUtils.isNotBlank(tag)) {
            RagAnswerAdvisor ragAnswerAdvisor = RagAnswerAdvisorManager.getRagAnswerAdvisor(RagAnswerAdvisorManager.getId(userId, tag));
            advisorList.add(ragAnswerAdvisor);
        }
        return advisorList;
    }
}
