package ai.agent.station.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 客户端模型配置 - 值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientModelVO {

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * API ID
     */
    private String apiId;

    /**
     * MCP工具ID集合
     */
    private List<String> toolMcpIdList;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型向量维度
     */
    private Integer modelDimensions;

    /**
     * 模型类型：openai、deepseek、claude
     */
    private String modelType;

}
