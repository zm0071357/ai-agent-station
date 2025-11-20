package ai.agent.station.domain.agent.service.execute.filter;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.service.execute.factory.DefaultLinkFactory;
import ai.agent.station.domain.agent.service.execute.manager.RagAnswerAdvisorManager;
import ai.agent.station.domain.agent.service.load.advisor.RagAnswerAdvisor;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

import static ai.agent.station.types.common.Constants.TAG_LIST;

/**
 * RAG校验节点
 */
@Slf4j
@Service
public class RagFilter implements LogicHandler<ChatRequestEntity, DefaultLinkFactory.DynamicContext, CheckRequestEntity> {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private PgVectorStore pgVectorStore;

    @Override
    public CheckRequestEntity apply(ChatRequestEntity chatRequestEntity, DefaultLinkFactory.DynamicContext dynamicContext) throws Exception {
        log.info("调用模型校验责任链 - RAG校验节点，用户ID：{}", chatRequestEntity.getUserId());
        if (StringUtils.isNotBlank(chatRequestEntity.getTag())) {
            log.info("调用模型校验责任链 - RAG校验节点，用户ID：{}，标签：{}", chatRequestEntity.getUserId(), chatRequestEntity.getTag());
            List<String> tagList = redissonClient.getList(TAG_LIST + chatRequestEntity.getUserId());
            if (!tagList.contains(chatRequestEntity.getTag())) {
                return CheckRequestEntity.builder()
                        .isPass(false)
                        .message("标签不存在")
                        .build();
            }
            RagAnswerAdvisor ragAnswerAdvisor = RagAnswerAdvisorManager.getOrCreateRagAnswerAdvisor(
                    RagAnswerAdvisorManager.getId(chatRequestEntity.getUserId(), chatRequestEntity.getTag()),
                    chatRequestEntity.getTag(),
                    pgVectorStore
            );
            log.info("ragAnswerAdvisor：{}", ragAnswerAdvisor);
        }
        return CheckRequestEntity.builder()
                .isPass(true)
                .message("校验通过")
                .executeCount(dynamicContext.getExecuteCount())
                .build();
    }

}
