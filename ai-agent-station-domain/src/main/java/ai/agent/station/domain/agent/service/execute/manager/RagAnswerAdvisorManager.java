package ai.agent.station.domain.agent.service.execute.manager;

import ai.agent.station.domain.agent.service.load.advisor.RagAnswerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RagAnswerAdvisor统一管理
 */
@Slf4j
@Component
public class RagAnswerAdvisorManager {

    private static final Map<String, RagAnswerAdvisor> ragAnswerAdvisorMap = new ConcurrentHashMap<>();

    public static String getId(String userId, String tag) {
        return "rag_answer_advisor_" + userId + "_" + tag;
    }

    public static void createRagAnswerAdvisor(String id, String tag, PgVectorStore pgVectorStore) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(5)
                .filterExpression("knowledge == '" + tag + "'")
                .build();
        RagAnswerAdvisor ragAnswerAdvisor = new RagAnswerAdvisor(pgVectorStore, searchRequest);
        ragAnswerAdvisorMap.put(id, ragAnswerAdvisor);
    }

    public static RagAnswerAdvisor getRagAnswerAdvisor(String id) {
        return ragAnswerAdvisorMap.get(id);
    }

    public static RagAnswerAdvisor getOrCreateRagAnswerAdvisor(String id, String tag, PgVectorStore pgVectorStore) {
        RagAnswerAdvisor ragAnswerAdvisor = ragAnswerAdvisorMap.get(id);
        if (ragAnswerAdvisor == null) {
            log.info("ragAnswerAdvisor不存在，id：{}", id);
            createRagAnswerAdvisor(id, tag, pgVectorStore);
        }
        return ragAnswerAdvisorMap.get(id);
    }

}
