package ai.agent.station.domain.user.service.login;

import ai.agent.station.domain.user.model.entity.LoginEntity;
import ai.agent.station.domain.user.model.entity.RegisterEntity;

public interface LoginService {

    /**
     * 登录
     * @param loginEntity
     * @return
     */
    String login(LoginEntity loginEntity) throws Exception;

    /**
     * 注册
     * @param registerEntity
     * @return
     */
    String register(RegisterEntity registerEntity) throws Exception;

    /**
     * 获取验证码
     * @param email 邮箱
     * @return
     */
    void getVerificationCode(String email);

}
