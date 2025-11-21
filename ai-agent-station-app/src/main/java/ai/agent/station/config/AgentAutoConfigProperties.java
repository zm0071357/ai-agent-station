package ai.agent.station.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.agent.auto-config")
public class AgentAutoConfigProperties {

    /**
     * 是否启用Agent自动装配
     */
    private boolean enabled = false;

    /**
     * Agent ID列表表达式
     */
    private String agentIdListExpr;

}
