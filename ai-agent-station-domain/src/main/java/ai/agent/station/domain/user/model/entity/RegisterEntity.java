package ai.agent.station.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterEntity {

    /**
     * 密码
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
