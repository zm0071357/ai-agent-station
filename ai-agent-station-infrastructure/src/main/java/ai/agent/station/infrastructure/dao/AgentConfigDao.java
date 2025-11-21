package ai.agent.station.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentConfigDao {

    /**
     * 根据Agent ID查询对应客户端ID集合
     * @param agentId Agent ID
     * @return
     */
    List<String> getClientIdListByAgentId(@Param("agentId") String agentId);

}
