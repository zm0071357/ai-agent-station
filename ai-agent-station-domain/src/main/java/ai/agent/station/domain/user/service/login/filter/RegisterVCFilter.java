package ai.agent.station.domain.user.service.login.filter;

import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.domain.user.model.entity.RegisterEntity;
import ai.agent.station.domain.user.service.login.factory.DefaultRegisterFactory;
import ai.agent.station.types.framework.link.multition.handler.LogicHandler;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterVCFilter implements LogicHandler<RegisterEntity, DefaultRegisterFactory.DynamicContext, String> {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserRepository userRepository;

    @Override
    public String apply(RegisterEntity registerEntity, DefaultRegisterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("注册校验责任链 - 验证码校验节点，用户邮箱：{}", registerEntity.getEmail());
        String email = registerEntity.getEmail();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new RuntimeException("邮箱格式不正确");
        }
        String verificationCode = redissonClient.getBucket("Verification_Code_" + email).get().toString();
        if (verificationCode == null) {
            throw new RuntimeException("验证码已失效，请重新获取");
        }
        if (!registerEntity.getVerificationCode().equals(verificationCode)) {
            throw new RuntimeException("验证码错误");
        }
        log.info("注册校验责任链 - 校验通过，进行注册，用户邮箱：{}", email);
        // 注册
        return userRepository.register(registerEntity.getPassword(), email);
    }

}
