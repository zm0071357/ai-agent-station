package ai.agent.station.domain.user.service.login.factory;

import ai.agent.station.domain.user.model.entity.LoginEntity;
import ai.agent.station.domain.user.model.entity.RegisterEntity;
import ai.agent.station.domain.user.service.login.filter.LoginPWFilter;
import ai.agent.station.domain.user.service.login.filter.LoginVCFilter;
import ai.agent.station.domain.user.service.login.filter.RegisterPWFilter;
import ai.agent.station.types.framework.link.multition.LinkArmory;
import ai.agent.station.types.framework.link.multition.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class DefaultRegisterFactory {

    @Bean("registerLogicLink")
    public BusinessLinkedList<RegisterEntity, DefaultRegisterFactory.DynamicContext, String> loginLogicLink(RegisterPWFilter registerPWFilter) {
        // 组装链
        LinkArmory<RegisterEntity, DefaultRegisterFactory.DynamicContext, String> linkArmory =
                new LinkArmory<>("注册校验责任链", registerPWFilter);
        // 链对象
        return linkArmory.getLogicLink();
    }


    /**
     * 动态上下文
     */
    @Data
    public static class DynamicContext {

    }

}
