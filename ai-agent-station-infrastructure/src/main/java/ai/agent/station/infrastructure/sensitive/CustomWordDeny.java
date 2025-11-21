package ai.agent.station.infrastructure.sensitive;

import com.github.houbb.sensitive.word.api.IWordDeny;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义敏感词黑名单
 */
@Service
public class CustomWordDeny implements IWordDeny {
    @Override
    public List<String> deny() {
        return List.of();
    }
}
