package ai.agent.station.config;

import ai.agent.station.domain.agent.adapter.repository.AgentRepository;
import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.AgentVO;
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

    @Resource
    private AgentRepository agentRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("Agent自动装配开始");
            String agentIdListExpr = agentAutoConfigProperties.getAgentIdListExpr();
            if (StringUtils.isEmpty(agentIdListExpr)) {
                log.warn("Agent ID列表为空");
                return;
            }
            List<String> agentIdList = List.of(agentIdListExpr.split(Constants.SPLIT));

            for (String agentId : agentIdList) {
                log.info("装配Agent - Agent ID：{}", agentId);
                List<String> clientIdList = agentRepository.getClientIdListByAgentId(agentId);
                AgentVO agentVO = agentRepository.getAgentByAgentId(agentId);
                StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> loadHandler = defaultLoadFactory.loadHandler();
                DefaultLoadFactory.DynamicContext dynamicContext = new DefaultLoadFactory.DynamicContext();
                dynamicContext.setAgentType(agentVO.getAgentType());
                LoadResEntity loadResEntity = loadHandler.apply(
                        LoadCommandEntity.builder()
                                .clientIdList(clientIdList)
                                .build(),
                        dynamicContext);
                log.info("装配Agent - Agent ID：{}，结果：{}", agentId, loadResEntity.getMessage());
            }
        } catch (Exception e) {
            log.error("Agent自动装配失败", e);
        }
    }
}
