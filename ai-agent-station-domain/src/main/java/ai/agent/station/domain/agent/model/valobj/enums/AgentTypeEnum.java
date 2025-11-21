package ai.agent.station.domain.agent.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agent类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AgentTypeEnum {

    AUTO("AUTO", "autoExecuteStrategy"),
    FLOW("FLOW", "flowExecuteStrategy"),
    ;

    private String type;

    private String strategy;

    public static AgentTypeEnum getByType(String type) {
        switch (type) {
            case "AUTO":
                return AUTO;
            case "FLOW":
                return FLOW;
            default:
                throw new RuntimeException("不存在的Agent类型");
        }
    }

}
