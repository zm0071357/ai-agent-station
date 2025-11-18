package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.ClientModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientModelDao {

    /**
     * 根据ID获取客户端模型配置
     * @param modelId 模型ID
     * @return
     */
    ClientModel getModelByModelId(@Param("modelId") String modelId);
}
