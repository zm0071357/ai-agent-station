package ai.agent.station.domain.user.service.self;

import ai.agent.station.domain.user.model.valobj.UserVO;

public interface SelfService {

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return
     */
    UserVO userInfo(String userId);
}
