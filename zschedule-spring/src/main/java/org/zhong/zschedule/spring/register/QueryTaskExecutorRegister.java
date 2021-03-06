/*
 *  Created by ZhongWenjie on 2019-01-25 9:52
 */

package org.zhong.zschedule.spring.register;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.zhong.zschedule.core.Util;
import org.zhong.zschedule.core.config.QueryTaskExecutorConfig;
import org.zhong.zschedule.core.executor.QueryTaskExecutor;
import org.zhong.zschedule.core.loader.ScheduledExecutorServiceLoader;
import org.zhong.zschedule.core.executor.DefaultQueryTaskExecutor;
import org.zhong.zschedule.core.task.QueryTask;
import org.zhong.zschedule.spring.component.DefaultQueryTaskExecutorComponent;
import org.zhong.zschedule.spring.component.QueryTaskExecutorComponent;

import java.security.AccessControlContext;
import java.util.Map;

@Component
public class QueryTaskExecutorRegister implements BeanFactoryPostProcessor {

    public static final String DEFAULT = "default";

    private ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        final DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;
        final AccessControlContext accessControlContext = defaultListableBeanFactory.getAccessControlContext();

        this.configurableListableBeanFactory = configurableListableBeanFactory;
        configurableListableBeanFactory.registerSingleton(DEFAULT, doBuild(new DefaultQueryTaskExecutorComponent()));
        Map<String, QueryTaskExecutorComponent> beans = configurableListableBeanFactory.getBeansOfType(QueryTaskExecutorComponent.class);
        if (Util.valid(beans) ) {
            for (QueryTaskExecutorComponent component : beans.values()) {
                QueryTaskExecutor queryTaskExecutor = doBuild(component);
                configurableListableBeanFactory.registerSingleton(component.getId(), queryTaskExecutor);
            }
        }
    }

    private QueryTaskExecutor doBuild(QueryTaskExecutorComponent queryTaskExecutorComponent) {
        QueryTaskExecutorConfig queryTaskExecutorConfig =
                configurableListableBeanFactory.getBean(queryTaskExecutorComponent.getQueryTaskExecutorConfigClass());
        ScheduledExecutorServiceLoader scheduledExecutorServiceLoader = configurableListableBeanFactory.getBean(
                queryTaskExecutorComponent.getScheduledExecutorServiceLoaderClass()
        );
        QueryTask queryTask = configurableListableBeanFactory.getBean(
                queryTaskExecutorComponent.getQueryTaskClass()
        );
        return new DefaultQueryTaskExecutor(
                queryTaskExecutorConfig, scheduledExecutorServiceLoader, queryTask
        );
    }

}
