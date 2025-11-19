package ai.agent.station.domain.agent.service.execute.factory;


import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.service.execute.filter.PromptFilter;
import ai.agent.station.domain.agent.service.execute.filter.StepFilter;
import ai.agent.station.domain.agent.service.execute.filter.UserFilter;
import ai.agent.station.types.framework.link.multition.LinkArmory;
import ai.agent.station.types.framework.link.multition.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * 责任链工厂
 */
@Slf4j
@Service
public class DefaultLinkFactory {

    @Bean("executeLogicLink")
    public BusinessLinkedList<ChatRequestEntity, DynamicContext, CheckRequestEntity> executeLogicLink(
            UserFilter userFilter, PromptFilter promptFilter, StepFilter stepFilter) {
        // 组装链
        LinkArmory<ChatRequestEntity, DynamicContext, CheckRequestEntity> linkArmory =
                new LinkArmory<>("调用模型校验责任链", userFilter, promptFilter, stepFilter);
        // 链对象
        return linkArmory.getLogicLink();
    }

    /**
     * 动态上下文
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        /**
         * 剩余可调用次数
         */
        private Integer executeCount;
    }

}
