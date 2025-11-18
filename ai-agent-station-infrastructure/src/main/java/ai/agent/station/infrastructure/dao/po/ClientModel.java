package ai.agent.station.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户端模型配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientModel {

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * API ID
     */
    private String apiId;

    /**
     * MCP工具ID
     */
    private String toolMcpId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型向量维度
     */
    private Integer modelDimensions;

    /**
     * 模型类型：openai、deepseek等
     */
    private String modelType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
