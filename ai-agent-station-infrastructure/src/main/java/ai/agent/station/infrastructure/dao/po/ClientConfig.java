package ai.agent.station.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户端配置表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 源类型（model、client）
     */
    private String sourceType;

    /**
     * 源ID
     */
    private String sourceId;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 扩展参数（JSON格式）
     */
    private String extParam;

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
