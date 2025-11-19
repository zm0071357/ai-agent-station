package ai.agent.station.domain.agent.service.execute.filter;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.service.execute.factory.DefaultLinkFactory;
import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户校验节点
 */
@Slf4j
@Service
public class UserFilter implements LogicHandler<ChatRequestEntity, DefaultLinkFactory.DynamicContext, CheckRequestEntity> {

    @Resource
    private UserRepository userRepository;

    @Override
    public CheckRequestEntity apply(ChatRequestEntity chatRequestEntity, DefaultLinkFactory.DynamicContext dynamicContext) throws Exception {
        log.info("调用模型校验责任链 - 用户校验节点，用户ID：{}", chatRequestEntity.getUserId());
        // 用户黑名单校验
        Boolean isBlack = userRepository.checkUserBlack(chatRequestEntity.getUserId());
        if (isBlack) {
            return CheckRequestEntity.builder()
                    .isPass(false)
                    .message("用户在黑名单中")
                    .build();
        }
        // 用户剩余可调用次数校验
        Integer executeCount = userRepository.checkUserExecuteCount(chatRequestEntity.getUserId());
        if (executeCount <= 0) {
            return CheckRequestEntity.builder()
                    .isPass(false)
                    .message("用户可调用次数不足")
                    .build();
        }
        // 将剩余可调用次数写入动态上下文
        dynamicContext.setExecuteCount(executeCount);
        return next(chatRequestEntity, dynamicContext);
    }

}
