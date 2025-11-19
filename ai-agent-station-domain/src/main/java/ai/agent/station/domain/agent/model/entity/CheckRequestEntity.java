package ai.agent.station.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 校验对话请求实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckRequestEntity {

    /**
     * 请求是否通过
     */
    private Boolean isPass;

    /**
     * 信息
     */
    private String message;

    /**
     * 剩余可调用次数
     */
    private Integer executeCount;

}
