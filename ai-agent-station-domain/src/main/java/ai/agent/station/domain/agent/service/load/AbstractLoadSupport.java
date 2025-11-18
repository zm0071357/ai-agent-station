package ai.agent.station.domain.agent.service.load;

import ai.agent.station.domain.agent.adapter.repository.AgentRepository;
import ai.agent.station.domain.agent.model.entity.LoadCommandEntity;
import ai.agent.station.domain.agent.model.entity.LoadResEntity;
import ai.agent.station.domain.agent.service.load.factory.DefaultLoadFactory;
import ai.agent.station.types.framework.tree.multithread.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 加载数据 - 策略路由
 */
@Slf4j
public abstract class AbstractLoadSupport extends AbstractMultiThreadStrategyRouter<LoadCommandEntity, DefaultLoadFactory.DynamicContext, LoadResEntity> {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private AgentRepository agentRepository;

    @Override
    protected void multiThread(LoadCommandEntity loadCommandEntity, DefaultLoadFactory.DynamicContext dynamicContext) throws Exception {
        // 空实现 - 需要进行异步加载的子类手动做对应实现
        // 其他不需要的子类就不用实现，只需要实现处理节点的方法
    }

    /**
     * 通用注册Bean
     * @param beanName  Bean名称
     * @param beanClass Bean类型
     * @param <T>       Bean类型
     */
    protected synchronized <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        // 注册Bean
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> beanInstance);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        // 如果Bean已存在，先移除
        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
        }

        // 注册新的Bean
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
        log.info("成功注册Bean: {}", beanName);
    }

    /**
     * 通用校验Bean是否存在
     * @param beanName Bean名称
     * @return
     */
    protected boolean checkBeanExist(String beanName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        return beanFactory.containsBeanDefinition(beanName);
    }

    /**
     * 通用获取Bean
     * @param beanName Bean名称
     * @return
     * @param <T> Bean类型
     */
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 获取Bean名称
     * @param id 组件ID
     * @return
     */
    protected String getBeanName(String id) {
        return null;
    }

    /**
     * 获取数据名称
     * @return
     */
    protected String getDataName() {
        return null;
    }

}
