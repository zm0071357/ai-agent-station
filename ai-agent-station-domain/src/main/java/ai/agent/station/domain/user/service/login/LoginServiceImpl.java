package ai.agent.station.domain.user.service.login;

import ai.agent.station.domain.user.model.entity.LoginEntity;
import ai.agent.station.domain.user.model.entity.RegisterEntity;
import ai.agent.station.domain.user.service.login.factory.DefaultLoginFactory;
import ai.agent.station.domain.user.service.login.factory.DefaultRegisterFactory;
import ai.agent.station.domain.user.service.mail.MailService;
import ai.agent.station.types.framework.link.multition.chain.BusinessLinkedList;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private MailService mailService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BusinessLinkedList<LoginEntity, DefaultLoginFactory.DynamicContext, String> loginLogicLink;

    @Resource
    private BusinessLinkedList<RegisterEntity, DefaultRegisterFactory.DynamicContext, String> registerLogicLink;

    @Override
    public String login(LoginEntity loginEntity) throws Exception {
        return loginLogicLink.apply(loginEntity, new DefaultLoginFactory.DynamicContext());
    }

    @Override
    public String register(RegisterEntity registerEntity) throws Exception {
        // 注册获取账号
        String userId = registerLogicLink.apply(registerEntity, new DefaultRegisterFactory.DynamicContext());
        // 异步发送欢迎通知
        threadPoolExecutor.execute(() -> mailService.sendWelMail(registerEntity.getEmail(), userId));
        // 登录
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    @Override
    public void getVerificationCode(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new RuntimeException("邮箱格式不正确");
        }
        String verificationCodeKey = "Verification_Code_" + email;
        String requestLastTimeKey = "Verification_Code_Request_Time_" + email;
        String verificationCode = (String) redissonClient.getBucket(verificationCodeKey).get();
        Long requestLastTime = (Long) redissonClient.getBucket(requestLastTimeKey).get();
        long currentTime = System.currentTimeMillis();

        if (StringUtils.isBlank(verificationCode) || requestLastTime == null) {
            log.info("验证码已过期/未发送过验证码 - 发送验证码，用户邮箱：{}", email);
            sendVerificationCode(verificationCodeKey, requestLastTimeKey, email);
        } else if (currentTime - requestLastTime > 60000) {
            log.info("前后请求验证码间隔超过1分钟 - 发送验证码，用户邮箱：{}", email);
            sendVerificationCode(verificationCodeKey, requestLastTimeKey, email);
        } else {
            log.info("重复请求发送验证码 - 用户邮箱：{}", email);
        }
    }

    /**
     * 发送验证码
     * @param verificationCodeKey 验证码Key
     * @param requestLastTimeKey 请求时间Key
     * @param email 邮箱
     */
    public void sendVerificationCode(String verificationCodeKey, String requestLastTimeKey, String email) {
        // 生成验证码
        String verificationCode = RandomUtil.randomNumbers(6);
        // 缓存验证码
        redissonClient.getBucket(verificationCodeKey).set(verificationCode, 3, TimeUnit.MINUTES);
        // 缓存请求时间
        redissonClient.getBucket(requestLastTimeKey).set(System.currentTimeMillis(), 3, TimeUnit.MINUTES);
        // 异步发送验证码通知
        threadPoolExecutor.execute(() -> mailService.sendVCMail(email, verificationCode));
    }

}
