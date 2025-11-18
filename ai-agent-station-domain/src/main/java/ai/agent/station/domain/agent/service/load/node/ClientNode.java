package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.model.valobj.ClientPromptVO;
import ai.agent.station.domain.agent.model.valobj.ClientVO;
import ai.agent.station.domain.agent.model.valobj.enums.AgentEnum;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client客户端节点
 */
@Slf4j
@Service
public class ClientNode extends AbstractLoadSupport {

    @Resource
    private GraphNode graphNode;

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - Client节点，开始加载ChatClient");
        List<ClientVO> clientVOList = dynamicContext.get(getDataName());
        Map<String, ClientPromptVO> clientPromptVOMap = dynamicContext.get(AgentEnum.CLIENT_PROMPT.getDataName());
        for (ClientVO clientVO : clientVOList) {
            log.info("加载ChatClient - {}", clientVO.getClientName());
            String clientId = clientVO.getClientId();
            ClientPromptVO clientPromptVO = clientPromptVOMap.get(clientId);
            // 提示词
            String defaultSystem = "Ai 智能体 \r\n" + clientPromptVO.getPromptContent();
            // 模型
            OpenAiChatModel openAiChatModel = getBean(clientVO.getModelBeanName());
            // Advisor
            List<Advisor> advisorList = new ArrayList<>();
            List<String> advisorBeanNameList = clientVO.getAdvisorBeanNameList();
            for (String advisorBeanName : advisorBeanNameList) {
                advisorList.add(getBean(advisorBeanName));
            }
            // 构建ChatClient对话客户端
            ChatClient chatClient = ChatClient.builder(openAiChatModel)
                    .defaultSystem(defaultSystem)
                    .defaultAdvisors(advisorList.toArray(new Advisor[]{}))
                    .build();
            // 注册Bean
            String beanName = getBeanName(clientVO.getClientName());
            registerBean(beanName, ChatClient.class, chatClient);
            // 写入动态上下文
            dynamicContext.getChatClientMap().put(clientVO.getClientName(), getBean(beanName));
        }
        return router(loadCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity requestParameter, DefaultLoadFactory.DynamicContext dynamicContext) {
        return graphNode;
    }

    @Override
    protected String getBeanName(String name) {
        return AgentEnum.CLIENT.getBeanName(name);
    }

    @Override
    protected String getDataName() {
        return AgentEnum.CLIENT.getDataName();
    }

}
