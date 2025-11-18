package ai.agent.station.domain.agent.service.load;

import ai.agent.station.domain.agent.adapter.repository.AgentRepository;
import ai.agent.station.domain.agent.model.valobj.*;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class LoadDataServiceImpl implements LoadDataService {

    @Resource
    private AgentRepository agentRepository;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(List<String> clientIdList, Map<String, Object> dataMap) {

        // 查询Client - Model
        CompletableFuture<Map<String, ClientModelVO>> clientModelMapFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.getClientModelMap(clientIdList), threadPoolExecutor);

        // 查询Client - Api
        CompletableFuture<Map<String, ClientApiVO>> clientApiMapFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.getClientApiMap(clientIdList), threadPoolExecutor);

        // 查询Client - ToolMcp
        CompletableFuture<Map<String, List<ClientToolMcpVO>>> clientToolMcpMapFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.getClientToolMcpMap(clientIdList), threadPoolExecutor);

        // 查询Client - Prompt
        CompletableFuture<Map<String, ClientPromptVO>> clientPromptMapFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.getClientPromptMap(clientIdList), threadPoolExecutor);

        // 查询Client - Advisor
        CompletableFuture<Map<String, List<ClientAdvisorVO>>> clientAdvisorMapFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.getClientAdvisorMap(clientIdList), threadPoolExecutor);

        // 查询Client
        CompletableFuture<List<ClientVO>> clientListFuture = CompletableFuture.supplyAsync(
                () -> agentRepository.getClientList(clientIdList), threadPoolExecutor);

        // 所有任务完成 - 存入dataMap
        CompletableFuture.allOf(
                clientModelMapFuture, clientApiMapFuture, clientToolMcpMapFuture,
                clientPromptMapFuture, clientAdvisorMapFuture, clientListFuture
        ).thenRun(() -> {
            dataMap.put(AgentEnum.CLIENT_MODEL.getDataName(), clientModelMapFuture.join());
            dataMap.put(AgentEnum.CLIENT_API.getDataName(), clientApiMapFuture.join());
            dataMap.put(AgentEnum.CLIENT_TOOL_MCP.getDataName(), clientToolMcpMapFuture.join());
            dataMap.put(AgentEnum.CLIENT_PROMPT.getDataName(), clientPromptMapFuture.join());
            dataMap.put(AgentEnum.CLIENT_ADVISOR.getDataName(), clientAdvisorMapFuture.join());
            dataMap.put(AgentEnum.CLIENT.getDataName(), clientListFuture.join());
        }).join();
    }

}
