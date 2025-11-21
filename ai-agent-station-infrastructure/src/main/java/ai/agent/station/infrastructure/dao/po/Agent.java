package ai.agent.station.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Agent {

    /**
     * 主键ID
     */
    private Long id;

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

    /**
     * 状态(0:禁用,1:启用)
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
