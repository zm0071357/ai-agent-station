package ai.agent.station.domain.user.service.login.filter;

import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.domain.user.model.entity.LoginEntity;
import ai.agent.station.domain.user.model.valobj.UserVO;
import ai.agent.station.domain.user.service.login.factory.DefaultLoginFactory;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import ai.agent.station.types.utils.AgronUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginPWFilter implements LogicHandler<LoginEntity, DefaultLoginFactory.DynamicContext, String> {

    @Resource
    private UserRepository userRepository;

    @Override
    public String apply(LoginEntity loginEntity, DefaultLoginFactory.DynamicContext dynamicContext) throws Exception {
        log.info("登录校验责任链 - 账密校验节点，用户ID：{}", loginEntity.getUserId());
        UserVO userVO = userRepository.getUser(loginEntity.getUserId(), loginEntity.getEmail());
        if (userVO == null) {
            throw new RuntimeException("请检查账号、密码、邮箱是否输入正确");
        }
        if (AgronUtil.verifyPassword(loginEntity.getPassword(), userVO.getPassword())) {
            throw new RuntimeException("请检查账号、密码是否输入正确");
        }
        if (userVO.getIsBlack() == 1) {
            throw new RuntimeException("用户处于黑名单，不可登录");
        }
        return next(loginEntity, dynamicContext);
    }

}
