package ai.agent.station.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 密码 - 加密后
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String verificationCode;

}
