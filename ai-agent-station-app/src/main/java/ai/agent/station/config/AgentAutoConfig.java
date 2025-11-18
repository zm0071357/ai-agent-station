package ai.agent.station.config;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.common.Constants;
import ai.agent.station.types.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Slf4j
@Configuration
@EnableConfigurationProperties(AgentAutoConfigProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.agent.auto-config", name = "enabled", havingValue = "true")
public class AgentAutoConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private AgentAutoConfigProperties agentAutoConfigProperties;

    @Resource
    private DefaultLoadFactory defaultLoadFactory;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("Agent自动装配开始");
            String clientIdExpr = agentAutoConfigProperties.getClientIdListExpr();
            if (StringUtils.isEmpty(clientIdExpr)) {
                log.warn("客户端ID列表为空");
                return;
            }
            List<String> clientIdList = List.of(clientIdExpr.split(Constants.SPLIT));
            StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> loadHandler = defaultLoadFactory.loadHandler();
            LoadResEntity loadResEntity = loadHandler.apply(LoadCommandEntity.builder()
                            .clientIdList(clientIdList)
                            .build(),
                    new DefaultLoadFactory.DynamicContext());
            log.info("Agent自动装配结果：{}", loadResEntity.getMessage());
        } catch (Exception e) {
            log.error("Agent自动装配失败", e);
        }
    }
}
