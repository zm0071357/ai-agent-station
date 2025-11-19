package ai.agent.station.domain.agent.service.execute.filter;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.service.execute.factory.DefaultLinkFactory;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static ai.agent.station.types.common.Constants.MAX_STEP;

/**
 * 执行步数校验节点
 */
@Slf4j
@Service
public class StepFilter implements LogicHandler<ChatRequestEntity, DefaultLinkFactory.DynamicContext, CheckRequestEntity> {

    @Override
    public CheckRequestEntity apply(ChatRequestEntity chatRequestEntity, DefaultLinkFactory.DynamicContext dynamicContext) throws Exception {
        log.info("调用模型校验责任链 - 执行步数校验节点，用户ID：{}", chatRequestEntity.getUserId());
        Integer maxStep = chatRequestEntity.getMaxStep();
        if (maxStep <= 0 || maxStep > MAX_STEP) {
            return CheckRequestEntity.builder()
                    .isPass(false)
                    .message("最大执行步数非法")
                    .build();
        }
        return CheckRequestEntity.builder()
                .isPass(true)
                .message("校验通过")
                .executeCount(dynamicContext.getExecuteCount())
                .build();
    }

}
