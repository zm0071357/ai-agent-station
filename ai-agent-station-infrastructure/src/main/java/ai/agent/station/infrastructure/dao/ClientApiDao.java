package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.ClientApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientApiDao {

    /**
     * 根据API ID获取API配置
     * @param apiId API ID
     * @return
     */
    ClientApi getApiByApiId(@Param("apiId") String apiId);
}
