package ai.agent.station.domain.agent.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 配置类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ConfigTypeEnum {

    MODEL("model"),
    PROMPT("prompt"),
    ADVISOR("advisor"),
    ;

    private String type;

}
