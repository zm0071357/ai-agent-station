package ai.agent.station.domain.user.adapter.repository;

import ai.agent.station.domain.user.model.valobj.UserVO;

public interface UserRepository {

    /**
     * 用户黑名单校验
     * @param userId 用户ID
     * @return
     */
    Boolean checkUserBlack(String userId);

    /**
     * 用户剩余可调用次数校验
     * @param userId 用户ID
     * @return
     */
    Integer checkUserExecuteCount(String userId);

    /**
     * 更新用户剩余可调用次数
     * @param userId 用户ID
     * @param executeCount 可调用次数
     */
    void updateUserExecuteCount(String userId, int executeCount);

    /**
     * 获取用户
     * @param userId 用户ID
     * @param email 邮箱
     * @return
     */
    UserVO getUser(String userId, String email);

    /**
     * 注册
     * @param password 密码
     * @param email 邮箱
     * @return
     */
    String register(String password, String email);

}
