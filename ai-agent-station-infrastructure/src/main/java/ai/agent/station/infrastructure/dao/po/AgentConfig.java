package ai.agent.station.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent配置表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 智能体ID
     */
    private String agentId;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 序列号(执行顺序)
     */
    private Integer sequence;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
