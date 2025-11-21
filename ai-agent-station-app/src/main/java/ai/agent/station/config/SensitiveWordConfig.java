package ai.agent.station.config;

import ai.agent.station.infrastructure.sensitive.CustomWordAllow;
import ai.agent.station.infrastructure.sensitive.CustomWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 敏感词过滤配置
 */
@Slf4j
@Configuration
public class SensitiveWordConfig {

    @Bean(value = "sensitiveWordBs")
    public SensitiveWordBs sensitiveWordBs() {
        log.info("构建敏感词库");
        return SensitiveWordBs.newInstance()
                //.wordDeny(new CustomWordDeny())
                //.wordAllow(new CustomWordAllow())
                .init();
    }

}
