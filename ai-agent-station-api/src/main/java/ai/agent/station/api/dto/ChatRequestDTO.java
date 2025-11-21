package ai.agent.station.api.dto;

import lombok.Getter;

/**
 * 对话请求体
 */
@Getter
public class ChatRequestDTO {

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
     * 知识库标签
     */
    private String tag;

    /**
     * 最大执行步数
     */
    private Integer maxStep;

}
