package ai.agent.station.domain.agent.model.valobj.enums;

import ai.agent.station.domain.agent.model.valobj.ClientAdvisorVO;
import ai.agent.station.domain.agent.service.load.advisor.RagAnswerAdvisor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AdvisorTypeEnum {

    CHAT_MEMORY("ChatMemory") {
        @Override
        public Advisor createAdvisor(ClientAdvisorVO clientAdvisorVO, PgVectorStore pgVectorStore) {
            ClientAdvisorVO.ChatMemory chatMemory = clientAdvisorVO.getChatMemory();
            return PromptChatMemoryAdvisor.builder(
                    MessageWindowChatMemory.builder()
                            .maxMessages(chatMemory.getMaxMessages())
                            .build()
            ).build();
        }
    },
    RAG_ANSWER("RagAnswer") {
        @Override
        public Advisor createAdvisor(ClientAdvisorVO clientAdvisorVO, PgVectorStore pgVectorStore) {
            ClientAdvisorVO.RagAnswer ragAnswer = clientAdvisorVO.getRagAnswer();
            return new RagAnswerAdvisor(pgVectorStore, SearchRequest.builder()
                    .topK(ragAnswer.getTopK())
                    .filterExpression(ragAnswer.getFilterExpression())
                    .build());
        }
    },
    ;

    private String type;

    // 静态Map缓存，用于快速查找
    private static final Map<String, AdvisorTypeEnum> TYPE_MAP = new HashMap<>();

    // 静态初始化块，在类加载时初始化Map
    static {
        for (AdvisorTypeEnum advisorTypeEnum : values()) {
            TYPE_MAP.put(advisorTypeEnum.getType(), advisorTypeEnum);
        }
    }

    /**
     * 创建Advisor
     * @param clientAdvisorVO
     * @param pgVectorStore
     * @return
     */
    public abstract Advisor createAdvisor(ClientAdvisorVO clientAdvisorVO, PgVectorStore pgVectorStore);

    /**
     * 根据类型获取对应的枚举
     * @param type 类型
     * @return
     */
    public static AdvisorTypeEnum getByType(String type) {
        AdvisorTypeEnum advisorTypeEnum = TYPE_MAP.get(type);
        if (advisorTypeEnum == null) {
            throw new RuntimeException("err! advisorType " + type + " not exist!");
        }
        return advisorTypeEnum;
    }
}
