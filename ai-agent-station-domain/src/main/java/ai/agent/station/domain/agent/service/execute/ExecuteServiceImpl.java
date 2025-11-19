package ai.agent.station.domain.agent.service.execute;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.service.execute.factory.DefaultLinkFactory;
import ai.agent.station.domain.user.adapter.repository.UserRepository;
import ai.agent.station.types.framework.link.multition.chain.BusinessLinkedList;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ExecuteServiceImpl implements ExecuteService {

    @Lazy
    @Resource
    private CompiledGraph taskAssistantGraph;

    @Resource
    private UserRepository userRepository;

    @Resource
    private BusinessLinkedList<ChatRequestEntity, DefaultLinkFactory.DynamicContext, CheckRequestEntity> executeLogicLink;

    @Override
    public void execute(ChatRequestEntity chatRequestEntity) throws Exception {
        // 责任链过滤
        CheckRequestEntity checkRequestEntity = executeLogicLink.apply(
                chatRequestEntity,
                new DefaultLinkFactory.DynamicContext()
        );
        if (checkRequestEntity.getIsPass()) {
            log.info("校验通过，用户ID：{}", chatRequestEntity.getUserId());
            // 调用大模型执行任务
            HashMap<String, Object> requestParamMap = new HashMap<>();
            requestParamMap.put("prompt", chatRequestEntity.getPrompt());
            requestParamMap.put("userId", chatRequestEntity.getUserId());
            requestParamMap.put("maxStep", chatRequestEntity.getMaxStep());
            Optional<OverAllState> overAllState = taskAssistantGraph.invoke(requestParamMap);
            Map<String, Object> map = overAllState.map(OverAllState::data).orElse(Map.of());
            log.info("用户请求：{}，返回结果：{}", JSON.toJSONString(chatRequestEntity), JSON.toJSONString(map));
            // 更新用户可调用次数
            userRepository.updateUserExecuteCount(chatRequestEntity.getUserId(), -Integer.parseInt(map.get("executeCount").toString()));
        } else {
            log.info("校验不通过：{}，用户ID：{}", JSON.toJSONString(checkRequestEntity), chatRequestEntity.getUserId());
        }

    }

}
