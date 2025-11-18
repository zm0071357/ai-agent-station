package ai.agent.station.domain.agent.adapter.repository;

import ai.agent.station.domain.agent.model.valobj.*;

import java.util.List;
import java.util.Map;

public interface AgentRepository {

    /**
     * 查询客户端模型配置Map
     * @param clientIdList 客户端ID集合
     * @return
     */
    Map<String, ClientModelVO> getClientModelMap(List<String> clientIdList);

    /**
     * 查询客户端API配置Map
     * @param clientIdList 客户端ID集合
     * @return
     */
    Map<String, ClientApiVO> getClientApiMap(List<String> clientIdList);

    /**
     * 查询客户端模型MCP工具配置Map
     * @param clientIdList 客户端ID集合
     * @return
     */
    Map<String, List<ClientToolMcpVO>> getClientToolMcpMap(List<String> clientIdList);

    /**
     * 查询客户端提示词配置Map
     * @param clientIdList 客户端ID集合
     * @return
     */
    Map<String, ClientPromptVO> getClientPromptMap(List<String> clientIdList);

    /**
     * 查询客户端Advisor配置Map
     * @param clientIdList 客户端ID集合
     * @return
     */
    Map<String, List<ClientAdvisorVO>> getClientAdvisorMap(List<String> clientIdList);

    /**
     * 查询客户端总配置集合
     * @param clientIdList 客户端ID集合
     * @return
     */
    List<ClientVO> getClientList(List<String> clientIdList);
}
