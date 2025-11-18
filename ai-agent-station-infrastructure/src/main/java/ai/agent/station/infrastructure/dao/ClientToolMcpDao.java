package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.ClientToolMcp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientToolMcpDao {

    /**
     * 根据MCP工具ID获取客户端MCP工具配置
     * @param toolMcpId MCP工具ID
     * @return
     */
    ClientToolMcp getToolMcpByToolMcpId(@Param("toolMcpId") String toolMcpId);

}
