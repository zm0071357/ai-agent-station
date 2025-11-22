package ai.agent.station.api;

import ai.agent.station.api.dto.LoginRequestDTO;
import ai.agent.station.api.dto.RegisterRequestDTO;
import ai.agent.station.api.response.Response;

public interface IndexService {

    /**
     * 登录
     * @param loginRequestDTO
     * @return
     */
    Response<String> login(LoginRequestDTO loginRequestDTO);

    /**
     * 注册
     * @param registerRequestDTO
     * @return
     */
    Response<String> register(RegisterRequestDTO registerRequestDTO);

    /**
     * 获取验证码
     * @param email
     * @return
     */
    Response<String> getVerificationCode(String email);

    /**
     * 登出
     * @return
     */
    Response<String> logout();

}
