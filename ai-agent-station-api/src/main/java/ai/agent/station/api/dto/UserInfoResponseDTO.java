package ai.agent.station.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDTO {

    /**
     * 账号
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户绑定邮箱
     */
    private String userEmail;

    /**
     * 可调用次数
     */
    private Integer executeCount;

    /**
     * 积分 - 用于后续营销动作
     */
    private Integer points;

}
