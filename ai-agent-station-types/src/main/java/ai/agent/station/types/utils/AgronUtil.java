package ai.agent.station.types.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

/**
 * 加密工具类
 */
@Component
public class AgronUtil {

    /**
     * 加密
     * @param password
     * @return
     */
    public static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        return argon2.hash(10, 65536, 1, password.toCharArray());
    }

    /**
     * 解密
     * @param hash
     * @param password
     * @return
     */
    public static boolean verifyPassword(String hash, String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        return argon2.verify(hash, password.toCharArray());
    }
}
