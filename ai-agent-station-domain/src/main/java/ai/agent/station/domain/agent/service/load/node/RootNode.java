package ai.agent.station.domain.agent.service.load.node;

import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.service.load.AbstractLoadSupport;
import ai.agent.station.domain.agent.service.load.LoadDataService;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 根节点 - 加载数据
 */
@Slf4j
@Service
public class RootNode extends AbstractLoadSupport {

    @Resource
    private LoadDataService loadDataService;

    @Resource
    private ClientApiNode clientApiNode;

    @Override
    protected void multiThread(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        loadDataService.loadData(loadCommandEntity.getClientIdList(), dynamicContext.getDataMap());
    }

    @Override
    protected LoadResEntity doApply(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Agent构建 - 根节点，开始加载所需数据");
        return router(loadCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> get(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) {
        return clientApiNode;
    }

}
