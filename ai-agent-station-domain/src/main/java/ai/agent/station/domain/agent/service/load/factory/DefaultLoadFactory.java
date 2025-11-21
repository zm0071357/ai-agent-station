package ai.agent.station.domain.agent.service.load.factory;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.service.load.node.RootNode;
import ai.agent.station.types.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载数据工厂
 */
@Service
public class DefaultLoadFactory {

    private final RootNode rootNode;

    public DefaultLoadFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<LoadCommandEntity, DynamicContext, LoadResEntity> loadHandler(){
        return rootNode;
    }

    /**
     * 动态上下文
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        /**
         * Agent类型
         */
        private String agentType;

        /**
         * 数据聚合Map
         */
        private Map<String, Object> dataMap = new HashMap<>();

        /**
         * ChatClient对话客户端Map
         */
        private Map<String, ChatClient> chatClientMap = new HashMap<>();

        public <T> void set(String key, T value) {
            dataMap.put(key, value);
        }

        public <T> T get(String key) {
            return (T) dataMap.get(key);
        }

    }

}
