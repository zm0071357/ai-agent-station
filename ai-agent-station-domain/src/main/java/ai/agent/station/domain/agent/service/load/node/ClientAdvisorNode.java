package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.ClientAdvisorVO;
import ai.agent.station.domain.agent.model.valobj.enums.AdvisorTypeEnum;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Advisor顾问节点
 */
@Slf4j
@Service
public class ClientAdvisorNode extends AbstractLoadSupport {

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private ClientNode clientNode;

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - Advisor顾问节点，开始加载Advisor");
        Map<String, List<ClientAdvisorVO>> clientAdvisorVOMap = dynamicContext.get(getDataName());
        for (Map.Entry<String, List<ClientAdvisorVO>> entry : clientAdvisorVOMap.entrySet()) {
            String clientId = entry.getKey();
            List<ClientAdvisorVO> clientAdvisorVOList = entry.getValue();
            for (ClientAdvisorVO clientAdvisorVO : clientAdvisorVOList) {
                log.info("客户端ID：{}，需要加载的Advisor ID：{}", clientId, clientAdvisorVO.getAdvisorId());
                // 防止多次构建同一个Advisor和注册同一个Bean
                String beanName = getBeanName(clientAdvisorVO.getAdvisorId());
                if (checkBeanExist(beanName)) {
                    log.info("存在相同的Advisor Bean - 不处理");
                    continue;
                }
                // 构建Advisor
                Advisor advisor = createAdvisor(clientAdvisorVO);
                // 注册Bean对象
                registerBean(beanName, Advisor.class, advisor);
            }
        }
        return router(loadCommandEntity, dynamicContext);
    }

    /**
     * 构建Advisor
     * @param clientAdvisorVO
     * @return
     */
    private Advisor createAdvisor(ClientAdvisorVO clientAdvisorVO) {
        AdvisorTypeEnum advisorTypeEnum = AdvisorTypeEnum.getByType(clientAdvisorVO.getAdvisorType());
        return advisorTypeEnum.createAdvisor(clientAdvisorVO, pgVectorStore);
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return clientNode;
    }

    @Override
    protected String getBeanName(String beanId) {
        return AgentEnum.CLIENT_ADVISOR.getBeanName(beanId);
    }

    @Override
    protected String getDataName() {
        return AgentEnum.CLIENT_ADVISOR.getDataName();
    }

}
