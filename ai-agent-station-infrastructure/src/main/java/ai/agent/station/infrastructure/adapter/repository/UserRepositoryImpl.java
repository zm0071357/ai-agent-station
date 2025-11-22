package ai.agent.station.infrastructure.adapter.repository;

import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.domain.user.model.valobj.UserVO;
import ai.agent.station.infrastructure.dao.UserDao;
import ai.agent.station.infrastructure.dao.po.User;
import ai.agent.station.types.utils.AgronUtil;
import ai.agent.station.types.utils.ModuloUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserDao userDao;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Boolean checkUserBlack(String userId) {
        RBitSet bitSet = redissonClient.getBitSet("black");
        if (!bitSet.isExists()) {
            return false;
        }
        return bitSet.get(ModuloUtil.getIndexFromUserId(userId));
    }

    @Override
    public Integer checkUserExecuteCount(String userId) {
        User user = userDao.getUserByUserId(userId);
        return user.getExecuteCount();
    }

    @Override
    public void updateUserExecuteCount(String userId, int executeCount) {
        User user = new User();
        user.setUserId(userId);
        user.setExecuteCount(executeCount);
        Integer updateCount = userDao.updateUserExecuteCount(user);
        if (updateCount == 0) {
            throw new RuntimeException("更新记录为0");
        }
    }

    @Override
    public UserVO getUser(String userId, String email) {
        User userReq = new User();
        userReq.setUserId(userId);
        userReq.setUserEmail(email);
        User user = userDao.getUser(userReq);
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getUserEmail())
                .isBlack(user.getIsBlack())
                .password(user.getPassword())
                .build();
    }

    @Override
    public String register(String password, String email) {
        try {
            String userId = RandomUtil.randomNumbers(9);
            User userReq = new User();
            userReq.setUserId(userId);
            userReq.setUserName("浅度浏览" + userId);
            userReq.setPassword(AgronUtil.hashPassword(password));
            userReq.setUserEmail(email);
            userDao.insert(userReq);
            return userId;
        } catch (Exception e) {
            throw new RuntimeException("注册新增用户异常：" + e.getMessage());
        }
    }

}
