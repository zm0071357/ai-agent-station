package ai.agent.station.domain.agent.service.execute.filter;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.service.execute.factory.DefaultLinkFactory;
import ai.agent.station.types.common.Constants;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import com.alibaba.fastjson.JSON;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提示词校验节点
 */
@Slf4j
@Service
public class PromptFilter implements LogicHandler<ChatRequestEntity, DefaultLinkFactory.DynamicContext, CheckRequestEntity> {

    @Override
    public CheckRequestEntity apply(ChatRequestEntity chatRequestEntity, DefaultLinkFactory.DynamicContext dynamicContext) throws Exception {
        log.info("调用模型校验责任链 - 提示词校验节点，用户ID：{}", chatRequestEntity.getUserId());
        boolean isSensitive = SensitiveWordHelper.contains(chatRequestEntity.getPrompt());
        if (isSensitive) {
            List<String> wordList = SensitiveWordHelper.findAll(chatRequestEntity.getPrompt());
            return CheckRequestEntity.builder()
                    .isPass(false)
                    .message("敏感提示词拦截：" + String.join(Constants.COMMA, wordList))
                    .build();
        }
        return next(chatRequestEntity, dynamicContext);
    }

}
