package ai.agent.station.domain.user.service.mail;

public interface MailService {

    /**
     * 发送欢迎通知
     * @param email
     * @param userId
     */
    void sendWelMail(String email, String userId);

    /**
     * 发送验证码通知
     * @param email
     * @param verificationCode
     */
    void sendVCMail(String email, String verificationCode);

    /**
     * 发送黑名单通知
     * @param email
     * @param userId
     */
    void sendBlackMail(String email, String userId);

}
