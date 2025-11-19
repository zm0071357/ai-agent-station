package ai.agent.station.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /**
     * 自增ID
     */
    private Long id;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
