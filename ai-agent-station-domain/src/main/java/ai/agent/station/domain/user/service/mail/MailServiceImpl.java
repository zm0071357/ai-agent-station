package ai.agent.station.domain.user.service.mail;

import ai.agent.station.types.utils.JavaMailUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailUtil javaMailUtil;

    @Override
    public void sendWelMail(String email, String userId) {
        execute(email, javaMailUtil.WelTitle, javaMailUtil.getSendWelContent(userId));
    }

    @Override
    public void sendVCMail(String email, String verificationCode) {
        execute(email, javaMailUtil.VCTitle, javaMailUtil.getSendVCContent(verificationCode));
    }

    @Override
    public void sendBlackMail(String email, String userId) {
        execute(email, javaMailUtil.BlackTitle, javaMailUtil.getSendBlackContent(userId));
    }

    /**
     * 执行发送
     * @param email 邮箱
     * @param title 标题
     * @param content 内容
     */
    public void execute(String email, String title, String content) {
        try {
            int tryCount = 0;
            while (tryCount < 3) {
                Boolean isSuccess = javaMailUtil.sendMimeMessage(email, title, content, true);
                if (isSuccess) {
                    log.info("异步发送通知完成，用户邮箱：{}", email);
                    break;
                }
                tryCount ++;
            }
        } catch (Exception e) {
            log.info("异步发送通知异常，用户邮箱：{}", email, e);
        }
    }

}
