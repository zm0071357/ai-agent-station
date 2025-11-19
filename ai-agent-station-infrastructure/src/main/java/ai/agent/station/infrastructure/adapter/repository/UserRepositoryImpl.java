package ai.agent.station.infrastructure.adapter.repository;

import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.infrastructure.dao.UserDao;
import ai.agent.station.infrastructure.dao.po.User;
import ai.agent.station.types.utils.ModuloUtil;
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

}
