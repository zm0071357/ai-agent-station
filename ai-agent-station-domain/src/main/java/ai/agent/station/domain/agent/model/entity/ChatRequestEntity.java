package ai.agent.station.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话请求实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestEntity {

    /**
     * 会话标识 - 用于获取对应ResponseBodyEmitter
     */
    private String key;


    /**
     * 用户ID
     */
    private String userId;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 提示词文本
     */
    private String prompt;

    /**
     * 提示词标签
     */
    private String tag;

    /**
     * 最大执行步数
     */
    private Integer maxStep;

}
