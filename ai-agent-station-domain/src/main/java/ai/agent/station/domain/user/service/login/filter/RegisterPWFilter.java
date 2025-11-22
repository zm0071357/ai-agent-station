package ai.agent.station.domain.user.service.login.filter;

import ai.agent.station.domain.user.model.entity.RegisterEntity;
import ai.agent.station.domain.user.service.login.factory.DefaultRegisterFactory;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterPWFilter implements LogicHandler<RegisterEntity, DefaultRegisterFactory.DynamicContext, String> {

    @Override
    public String apply(RegisterEntity registerEntity, DefaultRegisterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("注册校验责任链 - 密码校验节点，注册邮箱：{}", registerEntity.getEmail());
        String password = registerEntity.getPassword();
        // 密码需满足包含数字、大小写字母、8位以上
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!password.matches(passwordPattern)) {
            throw new IllegalArgumentException("密码必须包含数字、大小写字母，且至少8位以上");
        }
        return next(registerEntity, dynamicContext);
    }


}
