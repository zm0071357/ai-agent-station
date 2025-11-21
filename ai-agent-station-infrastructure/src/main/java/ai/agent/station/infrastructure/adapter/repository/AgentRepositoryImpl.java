package ai.agent.station.infrastructure.adapter.repository;

import ai.agent.station.domain.agent.adapter.repository.AgentRepository;
import ai.agent.station.domain.agent.model.valobj.*;
import ai.agent.station.domain.agent.model.valobj.enums.AdvisorTypeEnum;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.model.valobj.enums.ConfigTypeEnum;
import ai.agent.station.domain.agent.model.valobj.enums.McpTransportTypeEnum;
import ai.agent.station.infrastructure.dao.*;
import ai.agent.station.infrastructure.dao.po.*;
import ai.agent.station.types.common.Constants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class AgentRepositoryImpl implements AgentRepository {

    @Resource
    private AgentDao agentDao;

    @Resource
    private AgentConfigDao agentConfigDao;

    @Resource
    private ClientConfigDao clientConfigDao;

    @Resource
    private ClientModelDao clientModelDao;

    @Resource
    private ClientApiDao clientApiDao;

    @Resource
    private ClientToolMcpDao clientToolMcpDao;

    @Resource
    private ClientPromptDao clientPromptDao;

    @Resource
    private ClientAdvisorDao clientAdvisorDao;

    @Resource
    private ClientDao clientDao;

    @Override
    public AgentVO getAgentByAgentId(String agentId) {
        Agent agent = agentDao.getAgentByAgentId(agentId);
        return AgentVO.builder()
                .agentId(agentId)
                .agentName(agent.getAgentName())
                .agentType(agent.getAgentType())
                .description(agent.getDescription())
                .build();
    }

    @Override
    public List<String> getClientIdListByAgentId(String agentId) {
        return agentConfigDao.getClientIdListByAgentId(agentId);
    }

    @Override
    public Map<String, ClientModelVO> getClientModelMap(List<String> clientIdList) {
        // 查询客户端配置
        List<ClientConfig> clientModelConfigList = clientConfigDao.getConfig(
                AgentEnum.CLIENT.getType(),
                AgentEnum.CLIENT_MODEL.getType(),
                clientIdList
        );
        // 获取客户端模型配置Map
        Map<String, ClientModelVO> clientModelConfigMap = new HashMap<>();
        for (ClientConfig clientConfig : clientModelConfigList) {
            String clientId = clientConfig.getSourceId();
            String modelId = clientConfig.getTargetId();
            // 查询模型
            ClientModel clientModel = clientModelDao.getModelByModelId(modelId);
            String toolMcpId = clientModel.getToolMcpId();
            List<String> toolMcpIdList = List.of(toolMcpId.split(Constants.SPLIT));
            clientModelConfigMap.put(clientId, ClientModelVO.builder()
                    .modelId(clientModel.getModelId())
                    .apiId(clientModel.getApiId())
                    .toolMcpIdList(toolMcpIdList)
                    .modelName(clientModel.getModelName())
                    .modelDimensions(clientModel.getModelDimensions())
                    .modelType(clientModel.getModelType())
                    .build());
        }
        return clientModelConfigMap;
    }

    @Override
    public Map<String, ClientApiVO> getClientApiMap(List<String> clientIdList) {
        // 查询客户端模型配置
        Map<String, ClientModelVO> clientModelMap = this.getClientModelMap(clientIdList);
        // 获取客户端API配置Map
        Map<String, ClientApiVO> clientApiConfigMap = new HashMap<>();
        for (Map.Entry<String, ClientModelVO> entry : clientModelMap.entrySet()) {
            String clientId = entry.getKey();
            ClientModelVO clientModelVO = entry.getValue();
            ClientApi clientApi = clientApiDao.getApiByApiId(clientModelVO.getApiId());
            ClientApiVO clientApiVO = ClientApiVO.builder()
                    .apiId(clientApi.getApiId())
                    .baseUrl(clientApi.getBaseUrl())
                    .apiKey(clientApi.getApiKey())
                    .completionsPath(clientApi.getCompletionsPath())
                    .embeddingsPath(clientApi.getEmbeddingsPath())
                    .build();
            clientApiConfigMap.put(clientId, clientApiVO);
        }
        return clientApiConfigMap;
    }

    @Override
    public Map<String, List<ClientToolMcpVO>> getClientToolMcpMap(List<String> clientIdList) {
        // 查询客户端模型配置
        Map<String, ClientModelVO> clientModelMap = this.getClientModelMap(clientIdList);
        // 获取客户端API配置Map
        Map<String, List<ClientToolMcpVO>> clientToolMcpConfigMap = new HashMap<>();
        for (Map.Entry<String, ClientModelVO> entry : clientModelMap.entrySet()) {
            String clientId = entry.getKey();
            ClientModelVO clientModelVO = entry.getValue();
            // 获取MCP工具集合
            List<String> toolMcpIdList = clientModelVO.getToolMcpIdList();
            List<ClientToolMcpVO> clientToolMcpVOList = new ArrayList<>();
            for (String toolMcpId : toolMcpIdList) {
                ClientToolMcp clientToolMcp = clientToolMcpDao.getToolMcpByToolMcpId(toolMcpId);
                clientToolMcpVOList.add(getClientToolMcpVO(clientToolMcp));
            }
            clientToolMcpConfigMap.put(clientId, clientToolMcpVOList);
        }
        return clientToolMcpConfigMap;
    }

    /**
     * 处理MCP工具
     * @param clientToolMcp
     * @return
     */
    private ClientToolMcpVO getClientToolMcpVO(ClientToolMcp clientToolMcp) {
        ClientToolMcpVO clientToolMcpVO = ClientToolMcpVO.builder()
                .toolMcpId(clientToolMcp.getToolMcpId())
                .toolMcpName(clientToolMcp.getToolMcpName())
                .transportType(clientToolMcp.getTransportType())
                .transportConfig(clientToolMcp.getTransportConfig())
                .requestTimeout(clientToolMcp.getRequestTimeout())
                .build();
        try {
            if (clientToolMcp.getTransportType().equals(McpTransportTypeEnum.SSE.getType())) {
                // 解析SSE配置
                ObjectMapper objectMapper = new ObjectMapper();
                ClientToolMcpVO.TransportConfigSse transportConfigSse = objectMapper.readValue(clientToolMcp.getTransportConfig(), ClientToolMcpVO.TransportConfigSse.class);
                clientToolMcpVO.setTransportConfigSse(transportConfigSse);
            } else {
                // 解析STDIO配置
                Map<String, ClientToolMcpVO.TransportConfigStdio.Stdio> stdio = JSON.parseObject(clientToolMcp.getTransportConfig(), new TypeReference<>() {});
                ClientToolMcpVO.TransportConfigStdio transportConfigStdio = new ClientToolMcpVO.TransportConfigStdio();
                transportConfigStdio.setStdio(stdio);
                clientToolMcpVO.setTransportConfigStdio(transportConfigStdio);
            }
        } catch (Exception e) {
            log.error("解析传输配置失败: {}", e.getMessage(), e);
        }
        return clientToolMcpVO;
    }

    @Override
    public Map<String, ClientPromptVO> getClientPromptMap(List<String> clientIdList) {
        // 查询客户端配置
        List<ClientConfig> clientPromptConfigList = clientConfigDao.getConfig(
                AgentEnum.CLIENT.getType(),
                AgentEnum.CLIENT_PROMPT.getType(),
                clientIdList
        );
        // 获取客户端提示词配置Map
        Map<String, ClientPromptVO> clientPromptConfigMap = new HashMap<>();
        for (ClientConfig clientConfig : clientPromptConfigList) {
            String clientId = clientConfig.getSourceId();
            String promptId = clientConfig.getTargetId();
            ClientPrompt clientPrompt = clientPromptDao.getPromptByPromptId(promptId);
            ClientPromptVO clientPromptVO = ClientPromptVO.builder()
                    .promptId(clientPrompt.getPromptId())
                    .promptName(clientPrompt.getPromptName())
                    .promptContent(clientPrompt.getPromptContent())
                    .build();
            clientPromptConfigMap.put(clientId, clientPromptVO);
        }
        return clientPromptConfigMap;
    }

    @Override
    public Map<String, List<ClientAdvisorVO>> getClientAdvisorMap(List<String> clientIdList) {
        // 查询客户端配置
        List<ClientConfig> clientAdvisorConfigList = clientConfigDao.getConfig(
                AgentEnum.CLIENT.getType(),
                AgentEnum.CLIENT_ADVISOR.getType(),
                clientIdList
        );
        // 获取客户端Advisor配置Map
        Map<String, List<ClientAdvisorVO>> clientAdvisorConfigMap = new HashMap<>();
        for (ClientConfig clientConfig : clientAdvisorConfigList) {
            String clientId = clientConfig.getSourceId();
            String advisorIdExpr = clientConfig.getTargetId();
            // 获取Advisor配置集合
            List<ClientAdvisorVO> clientAdvisorVOList = new ArrayList<>();
            List<String> advisorIdList = List.of(advisorIdExpr.split(Constants.SPLIT));
            for (String advisorId : advisorIdList) {
                ClientAdvisor clientAdvisor = clientAdvisorDao.getAdvisorByAdvisorId(advisorId);
                clientAdvisorVOList.add(getClientAdvisorVO(clientAdvisor));
            }
            clientAdvisorConfigMap.put(clientId, clientAdvisorVOList);
        }
        return clientAdvisorConfigMap;
    }

    /**
     * 处理Advisor
     * @param clientAdvisor
     * @return
     */
    private ClientAdvisorVO getClientAdvisorVO(ClientAdvisor clientAdvisor) {
        ClientAdvisorVO.ChatMemory chatMemory = null;
        ClientAdvisorVO.RagAnswer ragAnswer = null;

        String extParam = clientAdvisor.getExtParam();
        if (extParam != null && !extParam.trim().isEmpty()) {
            try {
                if (clientAdvisor.getAdvisorType().equals(AdvisorTypeEnum.CHAT_MEMORY.getType())) {
                    // 解析chatMemory配置
                    chatMemory = JSON.parseObject(extParam, ClientAdvisorVO.ChatMemory.class);
                } else {
                    // 解析ragAnswer配置
                    ragAnswer = JSON.parseObject(extParam, ClientAdvisorVO.RagAnswer.class);
                }
            } catch (Exception e) {
                // 解析失败时忽略，使用默认值null
            }
        }

        return ClientAdvisorVO.builder()
                .advisorId(clientAdvisor.getAdvisorId())
                .advisorName(clientAdvisor.getAdvisorName())
                .advisorType(clientAdvisor.getAdvisorType())
                .orderNum(clientAdvisor.getOrderNum())
                .chatMemory(chatMemory)
                .ragAnswer(ragAnswer)
                .build();
    }

    @Override
    public List<ClientVO> getClientList(List<String> clientIdList) {
        List<ClientVO> clientVOList = new ArrayList<>();
        for (String clientId : clientIdList) {
            // 查询客户端
            Client client = clientDao.getClientByClientId(clientId);
            // 查询客户端配置集合
            List<ClientConfig> clientConfigList = clientConfigDao.getConfigByClientId(AgentEnum.CLIENT.getType(), clientId);
            String modelId = null;
            String promptId = null;
            List<String> advisorIdList = new ArrayList<>();
            for (ClientConfig clientConfig : clientConfigList) {
                if (clientConfig.getTargetType().equals(ConfigTypeEnum.MODEL.getType())) {
                    modelId = clientConfig.getTargetId();
                } else if (clientConfig.getTargetType().equals(ConfigTypeEnum.PROMPT.getType())) {
                    promptId = clientConfig.getTargetId();
                } else if (clientConfig.getTargetType().equals(ConfigTypeEnum.ADVISOR.getType())) {
                    String advisorIdExpr = clientConfig.getTargetId();
                    advisorIdList = List.of(advisorIdExpr.split(Constants.SPLIT));
                }
            }
            ClientVO clientVO = ClientVO.builder()
                    .clientId(client.getClientId())
                    .clientName(client.getClientName())
                    .description(client.getDescription())
                    .modelId(modelId)
                    .promptId(promptId)
                    .advisorIdList(advisorIdList)
                    .build();
            clientVOList.add(clientVO);
        }
        return clientVOList;
    }

}
