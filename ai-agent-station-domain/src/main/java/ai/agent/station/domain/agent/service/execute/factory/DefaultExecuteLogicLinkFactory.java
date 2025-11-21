package ai.agent.station.domain.agent.service.execute.factory;


import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import ai.agent.station.domain.agent.service.execute.filter.PromptFilter;
import ai.agent.station.domain.agent.service.execute.filter.RagFilter;
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

/**
 * 责任链工厂
 */
@Slf4j
@Service
public class DefaultExecuteLogicLinkFactory {

    @Bean("autoExecuteLogicLink")
    public BusinessLinkedList<ChatRequestEntity, DynamicContext, CheckRequestEntity> autoExecuteLogicLink(
            StepFilter stepFilter, UserFilter userFilter, PromptFilter promptFilter, RagFilter ragFilter) {
        // 组装链
        LinkArmory<ChatRequestEntity, DynamicContext, CheckRequestEntity> linkArmory =
                new LinkArmory<>("调用Auto型Agent校验责任链", stepFilter, userFilter, promptFilter, ragFilter);
        // 链对象
        return linkArmory.getLogicLink();
    }

    @Bean("flowExecuteLogicLink")
    public BusinessLinkedList<ChatRequestEntity, DynamicContext, CheckRequestEntity> flowExecuteLogicLink(
            UserFilter userFilter, PromptFilter promptFilter) {
        // 组装链
        LinkArmory<ChatRequestEntity, DynamicContext, CheckRequestEntity> linkArmory =
                new LinkArmory<>("调用Flow型Agent校验责任链", userFilter, promptFilter);
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
         * Agent类型枚举
         */
        private AgentTypeEnum agentTypeEnum;

        /**
         * 最大执行步数
         */
        private Integer maxStep;

        /**
         * 剩余可调用次数
         */
        private Integer executeCount;
    }

}
