package ai.agent.station.domain.user.adapter.repository;

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
}
