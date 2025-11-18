package ai.agent.station.domain.agent.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AgentEnum {

    CLIENT_API("对话API", "api", "client_api_", "client_api_data_map"),
    CLIENT_MODEL("对话模型", "model", "client_model_", "client_model_data_map"),
    CLIENT_PROMPT("提示词", "prompt", "client_prompt_", "client_system_prompt_data_map"),
    CLIENT_TOOL_MCP("mcp工具", "tool_mcp", "client_tool_mcp_", "client_tool_mcp_data_map"),
    CLIENT_ADVISOR("顾问角色", "advisor", "client_advisor_", "client_advisor_data_map"),
    CLIENT("客户端", "client", "client_", "client_data_list"),
    GRAPH("状态图", "graph", "graph_", "client_graph_list"),
    ;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * Bean 对象名称标签
     */
    private String beanNameTag;

    /**
     * 数据名称
     */
    private String dataName;

    // 静态Map用于O(1)时间复杂度查找
    private static final Map<String, AgentEnum> TYPE_MAP = new HashMap<>();

    // 静态初始化块，在类加载时构建Map
    static {
        for (AgentEnum enumVO : AgentEnum.values()) {
            TYPE_MAP.put(enumVO.getType(), enumVO);
        }
    }

    /**
     * 根据类型获取对应的枚举
     * @param type 类型
     * @return
     */
    public static AgentEnum getByType(String type) {
        if (type == null) {
            return null;
        }
        AgentEnum result = TYPE_MAP.get(type);
        if (result == null) {
            throw new RuntimeException("type value " + type + " not exist!");
        }
        return result;
    }

    /**
     * 获取Bean名称
     * @param id Bean ID
     * @return
     */
    public String getBeanName(String id) {
        return this.beanNameTag + id;
    }

}
