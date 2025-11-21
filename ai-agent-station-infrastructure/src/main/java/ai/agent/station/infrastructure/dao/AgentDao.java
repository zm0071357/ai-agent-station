package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.Agent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentDao {

    /**
     * 根据Agent ID获取Agent
     * @param agentId Agent ID
     * @return
     */
    Agent getAgentByAgentId(String agentId);

}
