package ai.agent.station.domain.agent.model.entity;

import ai.agent.station.domain.agent.service.load.advisor.RagAnswerAdvisor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import java.util.List;

/**
 * 校验对话请求实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckRequestEntity {

    /**
     * 请求是否通过
     */
    private Boolean isPass;

    /**
     * 信息
     */
    private String message;

    /**
     * 剩余可调用次数
     */
    private Integer executeCount;

    /**
     * ragAnswerAdvisor
     */
    private RagAnswerAdvisor ragAnswerAdvisor;

}
