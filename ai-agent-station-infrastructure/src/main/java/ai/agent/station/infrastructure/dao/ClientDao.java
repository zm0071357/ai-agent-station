package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.Client;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientDao {

    /**
     * 根据客户端ID获取客户端
     * @param clientId 客户端ID
     * @return
     */
    Client getClientByClientId(@Param("clientId") String clientId);

}
