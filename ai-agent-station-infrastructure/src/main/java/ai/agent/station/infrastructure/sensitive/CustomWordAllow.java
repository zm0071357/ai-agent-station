package ai.agent.station.infrastructure.sensitive;

import com.github.houbb.sensitive.word.api.IWordAllow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 敏感词白名单
 */
@Service
public class CustomWordAllow implements IWordAllow {
    @Override
    public List<String> allow() {
        return List.of();
    }
}
