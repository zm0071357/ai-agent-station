package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserDao {

    /**
     * 通过账号获取用户
     * @param userId
     * @return
     */
    User getUserByUserId(@Param("userId") String userId);

    /**
     * 新增用户
     * @param user
     * @return
     */
    void insert(User user);

    /**
     * 更新用户积分点数
     * @param userReq
     * @return
     */
    Integer updatePoints(User userReq);

    /**
     * 更新用户剩余可调用次数
     * @param user
     * @return
     */
    Integer updateUserExecuteCount(User user);

    /**
     * 获取用户
     * @param userReq
     * @return
     */
    User getUser(User userReq);
}
