package ai.agent.station.domain.user.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 - 值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户绑定邮箱
     */
    private String email;

    /**
     * 是否黑名单
     * 0 否
     * 1 是
     */
    private Integer isBlack;

    /**
     * 可调用次数 默认10次
     */
    private Integer executeCount;

    /**
     * 积分 - 用于后续营销动作
     */
    private Integer points;

    /**
     * 密码
     */
    private String password;

}
