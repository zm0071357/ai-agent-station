package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.ClientToolMcpVO;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.model.valobj.enums.McpTransportTypeEnum;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * MCP工具节点
 */
@Slf4j
@Service
public class ClientToolMcpNode extends AbstractLoadSupport {

    @Resource
    private ClientModelNode clientModelNode;

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - MCP工具节点，开始加载MCP工具");
        Map<String, List<ClientToolMcpVO>> clientToolMcpVOMap = dynamicContext.get(getDataName());
        for (Map.Entry<String, List<ClientToolMcpVO>> entry : clientToolMcpVOMap.entrySet()) {
            String clientId = entry.getKey();
            List<ClientToolMcpVO> clientToolMcpVOList = entry.getValue();
            for (ClientToolMcpVO clientToolMcpVO : clientToolMcpVOList) {
                log.info("客户端ID：{}，需要加载的MCP工具ID：{}", clientId, clientToolMcpVO.getToolMcpId());
                // 防止多次构建同一个MCP工具和注册同一个Bean
                String beanName = getBeanName(clientToolMcpVO.getToolMcpId());
                if (checkBeanExist(beanName)) {
                    log.info("存在相同的MCP工具Bean - 不处理");
                    continue;
                }
                // 构建MCP工具
                McpSyncClient mcpSyncClient = createMcpSyncClient(clientToolMcpVO);
                // 注册Bean
                registerBean(beanName, McpSyncClient.class, mcpSyncClient);
            }
        }
        return router(loadCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return clientModelNode;
    }

    @Override
    protected String getBeanName(String beanId) {
        return AgentEnum.CLIENT_TOOL_MCP.getBeanName(beanId);
    }

    @Override
    protected String getDataName() {
        return AgentEnum.CLIENT_TOOL_MCP.getDataName();
    }

    /**
     * 构建MCP工具
     * @param clientToolMcpVO
     * @return
     */
    private McpSyncClient createMcpSyncClient(ClientToolMcpVO clientToolMcpVO) {
        // SSE
        if (clientToolMcpVO.getTransportType().equals(McpTransportTypeEnum.SSE.getType())) {
            ClientToolMcpVO.TransportConfigSse transportConfigSse = clientToolMcpVO.getTransportConfigSse();
            String originalBaseUri = transportConfigSse.getBaseUri();
            String baseUri;
            String sseEndpoint;
            int queryParamStartIndex = originalBaseUri.indexOf("sse");
            if (queryParamStartIndex != -1) {
                baseUri = originalBaseUri.substring(0, queryParamStartIndex - 1);
                sseEndpoint = originalBaseUri.substring(queryParamStartIndex - 1);
            } else {
                baseUri = originalBaseUri;
                sseEndpoint = transportConfigSse.getSseEndpoint();
            }
            sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;
            HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                    .builder(baseUri) // 使用截取后的 baseUri
                    .sseEndpoint(sseEndpoint) // 使用截取或默认的 sseEndpoint
                    .build();
            McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(clientToolMcpVO.getRequestTimeout())).build();
            var init_sse = mcpSyncClient.initialize();
            log.info("Tool SSE MCP Initialized {}", init_sse);
            return mcpSyncClient;
        } else {
            // STDIO
            ClientToolMcpVO.TransportConfigStdio transportConfigStdio = clientToolMcpVO.getTransportConfigStdio();
            Map<String, ClientToolMcpVO.TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
            ClientToolMcpVO.TransportConfigStdio.Stdio stdio = stdioMap.get(clientToolMcpVO.getToolMcpName());
            // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
            var stdioParams = ServerParameters.builder(stdio.getCommand())
                    .args(stdio.getArgs())
                    .env(stdio.getEnv())
                    .build();
            var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams, new JacksonMcpJsonMapper(new ObjectMapper())))
                    .requestTimeout(Duration.ofSeconds(clientToolMcpVO.getRequestTimeout())).build();
            var init_stdio = mcpClient.initialize();
            log.info("Tool Stdio MCP Initialized {}", init_stdio);
            return mcpClient;
        }
    }
}
