package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.ClientConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClientConfigDao {

    /**
     * 获取客户端特定配置列表
     * @param sourceType 源类型
     * @param clientIdList 客户端ID集合
     * @return
     */
    List<ClientConfig> getConfig(@Param("sourceType") String sourceType,
                                 @Param("targetType") String targetType,
                                 @Param("clientIdList") List<String> clientIdList);

    /**
     * 获取单个客户端总配置
     * @param sourceType 源类型
     * @param clientId 客户端ID
     * @return
     */
    List<ClientConfig> getConfigByClientId(@Param("sourceType") String sourceType, @Param("sourceId") String clientId);

}
