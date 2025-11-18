package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.ClientApiVO;
import ai.agent.station.domain.agent.model.valobj.ClientToolMcpVO;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * API节点
 */
@Slf4j
@Service
public class ClientApiNode extends AbstractLoadSupport {

    @Resource
    private ClientToolMcpNode clientToolMcpNode;

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - API节点，开始加载API");
        Map<String, ClientApiVO> clientApiVOMap = dynamicContext.get(getDataName());
        for (Map.Entry<String, ClientApiVO> entry : clientApiVOMap.entrySet()) {
            String clientId = entry.getKey();
            ClientApiVO clientApiVO = entry.getValue();
            log.info("客户端ID：{}，需要加载的API ID：{}", clientId, clientApiVO.getApiId());
            // 防止多次构建同一个API和注册同一个Bean
            String beanName = getBeanName(clientApiVO.getApiId());
            if (checkBeanExist(beanName)) {
                log.info("存在相同的API Bean - 不处理");
                continue;
            }
            // 构建API
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(clientApiVO.getBaseUrl())
                    .apiKey(clientApiVO.getApiKey())
                    .completionsPath(clientApiVO.getCompletionsPath())
                    .embeddingsPath(clientApiVO.getEmbeddingsPath())
                    .build();
            // 注册Bean
            registerBean(beanName, OpenAiApi.class, openAiApi);
        }
        return router(loadCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return clientToolMcpNode;
    }

    @Override
    protected String getBeanName(String beanId) {
        return AgentEnum.CLIENT_API.getBeanName(beanId);
    }

    @Override
    protected String getDataName() {
        return AgentEnum.CLIENT_API.getDataName();
    }

}
