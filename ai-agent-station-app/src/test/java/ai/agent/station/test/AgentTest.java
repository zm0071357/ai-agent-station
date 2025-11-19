package ai.agent.station.test;

import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.service.execute.ExecuteService;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgentTest {

    @Resource
    private DefaultLoadFactory defaultLoadFactory;

    @Resource
    private ExecuteService executeService;

    @Test
    public void test_loadData() throws Exception {
        StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> loadHandler =
                defaultLoadFactory.loadHandler();
        LoadResEntity loadResEntity = loadHandler.apply(
                LoadCommandEntity.builder()
                        .clientIdList((Arrays.asList("3101","3102","3103","3104")))
                        .build(),
                new DefaultLoadFactory.DynamicContext());
        log.info("测试结果：{}", JSON.toJSONString(loadResEntity));
    }

    @Test
    public void test_execute() throws Exception {
        String userId = "399547479";
        ResponseBodyEmitter emitter = ResponseBodyEmitterManager.put(userId, Long.MAX_VALUE);
        executeService.execute(
                ChatRequestEntity.builder()
                        .userId("399547479")
                        .prompt("写一个龟兔赛跑的故事，结局是兔子要赢")
                        .maxStep(3)
                        .build(),
                emitter
        );
    }

}
