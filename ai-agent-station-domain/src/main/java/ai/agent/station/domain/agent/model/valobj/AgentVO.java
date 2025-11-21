package ai.agent.station.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent - 值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentVO {

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * Agent名称
     */
    private String agentName;

    /**
     * Agent类型
     */
    private String agentType;

    /**
     * 描述
     */
    private String description;


}
