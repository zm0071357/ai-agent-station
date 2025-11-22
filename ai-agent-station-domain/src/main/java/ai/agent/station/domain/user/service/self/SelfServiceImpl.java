package ai.agent.station.domain.user.service.self;

import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.domain.user.model.valobj.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SelfServiceImpl implements SelfService {

    @Resource
    private UserRepository userRepository;

    @Override
    public UserVO userInfo(String userId) {
        return null;
    }

}
