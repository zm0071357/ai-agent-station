package ai.agent.station.domain.agent.service.execute.strategy;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.CheckRequestEntity;
import ai.agent.station.domain.agent.model.entity.ExecuteResultEntity;
import ai.agent.station.domain.agent.model.valobj.enums.AgentTypeEnum;
import ai.agent.station.domain.user.adapter.repository.UserRepository;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;

/**
 * 执行策略抽象类
 */
@Slf4j
public abstract class AbstractExecuteStrategy implements ExecuteStrategy {

    @Resource
    protected UserRepository userRepository;

    @Override
    public void execute(ChatRequestEntity chatRequestEntity, AgentTypeEnum agentTypeEnum, ResponseBodyEmitter emitter) throws Exception {
        // 责任链校验
        CheckRequestEntity checkRequestEntity = checkRequest(chatRequestEntity, agentTypeEnum);
        if (checkRequestEntity.getIsPass()) {
            log.info("校验通过，用户ID：{}", chatRequestEntity.getUserId());
            // 调用大模型执行任务
            Map<String, Object> map = invoke(chatRequestEntity);
            // 更新用户可调用次数
            userRepository.updateUserExecuteCount(chatRequestEntity.getUserId(), -Integer.parseInt(map.get("executeCount").toString()));
            // 发送请求完成结果
            ExecuteResultEntity executeResultEntity = ExecuteResultEntity.createCompleteResult(chatRequestEntity.getUserId());
            String sseData = "data: " + JSON.toJSONString(executeResultEntity) + "\n\n";
            emitter.send(sseData);
        } else {
            log.info("校验不通过：{}，用户ID：{}", JSON.toJSONString(checkRequestEntity), chatRequestEntity.getUserId());
            // 发送请求错误结果
            ExecuteResultEntity executeResultEntity = ExecuteResultEntity.createErrorResult(checkRequestEntity.getMessage(), chatRequestEntity.getUserId());
            String sseData = "data: " + JSON.toJSONString(executeResultEntity) + "\n\n";
            emitter.send(sseData);
        }
    }

    /**
     * 抽象责任链校验方法
     * @param chatRequestEntity
     * @param agentTypeEnum
     * @return
     * @throws Exception
     */
    protected abstract CheckRequestEntity checkRequest(ChatRequestEntity chatRequestEntity, AgentTypeEnum agentTypeEnum) throws Exception;

    /**
     * 抽象调用大模型执行任务方法
     * @param chatRequestEntity
     * @return
     */
    protected abstract Map<String, Object> invoke(ChatRequestEntity chatRequestEntity);

}
