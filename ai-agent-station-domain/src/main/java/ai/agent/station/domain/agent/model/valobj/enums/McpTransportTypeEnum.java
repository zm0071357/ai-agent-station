package ai.agent.station.domain.agent.model.valobj.enums;

import lombok.*;

/**
 * MCP工具类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum McpTransportTypeEnum {

    SSE("sse"),
    STDIO("stdio"),
    ;

    private String type;
}
