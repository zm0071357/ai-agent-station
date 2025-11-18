package ai.agent.station.domain.agent.model.valobj;

import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端总配置 - 值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientVO {

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 描述
     */
    private String description;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 提示词ID
     */
    private String promptId;

    /**
     * 顾问ID集合
     */
    private List<String> advisorIdList;

    /**
     * 获取模型Bean名称
     * @return
     */
    public String getModelBeanName() {
        return AgentEnum.CLIENT_MODEL.getBeanName(modelId);
    }

    /**
     * 获取Advisor Bean名称集合
     * @return
     */
    public List<String> getAdvisorBeanNameList() {
        List<String> advisorBeanNameList = new ArrayList<>();
        for (String advisorId : advisorIdList) {
            advisorBeanNameList.add(AgentEnum.CLIENT_ADVISOR.getBeanName(advisorId));
        }
        return advisorBeanNameList;
    }

}
