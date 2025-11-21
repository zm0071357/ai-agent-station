package ai.agent.station.domain.agent.service.execute.filter;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import ai.agent.station.domain.agent.service.execute.factory.DefaultExecuteLogicLinkFactory;
import ai.agent.station.types.common.Constants;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提示词校验节点
 */
@Slf4j
@Service
public class PromptFilter implements LogicHandler<ChatRequestEntity, DefaultExecuteLogicLinkFactory.DynamicContext, CheckRequestEntity> {

    @Resource
    private SensitiveWordBs sensitiveWordBs;

    @Override
    public CheckRequestEntity apply(ChatRequestEntity chatRequestEntity, DefaultExecuteLogicLinkFactory.DynamicContext dynamicContext) throws Exception {
        log.info("调用模型校验责任链 - 提示词校验节点，用户ID：{}", chatRequestEntity.getUserId());
        boolean isSensitive = sensitiveWordBs.contains(chatRequestEntity.getPrompt());
        if (isSensitive) {
            List<String> wordList = sensitiveWordBs.findAll(chatRequestEntity.getPrompt());
            return CheckRequestEntity.builder()
                    .isPass(false)
                    .message("敏感提示词拦截：" + String.join(Constants.COMMA, wordList))
                    .build();
        }
        if (dynamicContext.getAgentTypeEnum().equals(AgentTypeEnum.FLOW)) {
            return CheckRequestEntity.builder()
                    .isPass(true)
                    .message("校验通过")
                    .executeCount(dynamicContext.getExecuteCount())
                    .build();
        }
        return next(chatRequestEntity, dynamicContext);
    }

}
