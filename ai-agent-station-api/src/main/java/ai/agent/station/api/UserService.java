package ai.agent.station.api;

import ai.agent.station.api.dto.UserInfoResponseDTO;
import ai.agent.station.api.response.Response;

public interface UserService {

    /**
     * 获取用户信息
     * @return
     */
    Response<UserInfoResponseDTO> userInfo();

}
