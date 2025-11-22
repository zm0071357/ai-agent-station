package ai.agent.station.api.dto;

import lombok.Getter;

@Getter
public class LoginRequestDTO {

    /**
     * 用户ID
     */
    private String userId;

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
