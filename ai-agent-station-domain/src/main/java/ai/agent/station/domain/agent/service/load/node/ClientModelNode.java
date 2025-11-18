package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.ClientModelVO;
import ai.agent.station.domain.agent.model.valobj.ClientToolMcpVO;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAiChatModel节点
 */
@Slf4j
@Service
public class ClientModelNode extends AbstractLoadSupport {

    @Resource
    private ClientAdvisorNode clientAdvisorNode;

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - OpenAiChatModel模型节点，开始加载OpenAiChatModel");
        Map<String, ClientModelVO> clientModelVOMap = dynamicContext.get(getDataName());
        for (Map.Entry<String, ClientModelVO> entry : clientModelVOMap.entrySet()) {
            String clientId = entry.getKey();
            ClientModelVO clientModelVO = entry.getValue();
            log.info("客户端ID：{}，需要加载的OpenAiChatModel ID：{}", clientId, clientModelVO.getModelId());
            // 防止多次构建同一个OpenAiChatModel和注册同一个Bean
            String beanName = getBeanName(clientModelVO.getModelId());
            if (checkBeanExist(beanName)) {
                log.info("存在相同的OpenAiChatModel Bean - 不处理");
                continue;
            }
            // 获取OpenAiChatModel的API
            OpenAiApi openAiApi = getBean(AgentEnum.CLIENT_API.getBeanName(clientModelVO.getApiId()));
            // 获取OpenAiChatModel的MCP工具集合
            List<McpSyncClient> mcpSyncClients = new ArrayList<>();
            for (String toolMcpId : clientModelVO.getToolMcpIdList()) {
                McpSyncClient mcpSyncClient = getBean(AgentEnum.CLIENT_TOOL_MCP.getBeanName(toolMcpId));
                mcpSyncClients.add(mcpSyncClient);
            }
            // 构建OpenAiChatModel
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(
                            OpenAiChatOptions.builder()
                                    .model(clientModelVO.getModelName())
                                    .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                                    .build())
                    .build();
            // 注册Bean对象
            registerBean(beanName, OpenAiChatModel.class, chatModel);
        }
        return router(loadCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return clientAdvisorNode;
    }

    @Override
    protected String getBeanName(String beanId) {
        return AgentEnum.CLIENT_MODEL.getBeanName(beanId);
    }

    @Override
    protected String getDataName() {
        return AgentEnum.CLIENT_MODEL.getDataName();
    }

}
