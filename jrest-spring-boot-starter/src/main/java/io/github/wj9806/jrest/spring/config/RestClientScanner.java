package io.github.wj9806.jrest.spring.config;

import io.github.wj9806.jrest.client.annotation.RestClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * RestClient扫描器
 * 扫描指定包下的@RestClient注解接口并注册为Spring Bean
 */
public class RestClientScanner extends ClassPathBeanDefinitionScanner {

    public RestClientScanner(BeanDefinitionRegistry registry) {
        super(registry);
        // 添加注解过滤器，只扫描@RestClient注解的接口
        addIncludeFilter(new AnnotationTypeFilter(RestClient.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        // 执行实际的扫描
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No RestClient was found in '" + String.join(",", basePackages) + "' package. Please check your configuration.");
        } else {
            // 处理扫描到的BeanDefinition
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        // 只处理接口类型
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            AbstractBeanDefinition definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();

            if (beanClassName == null) {
                continue;
            }

            // 获取@RestClient注解的属性
            try {
                Class<?> clientInterface = ClassUtils.forName(beanClassName, this.getClass().getClassLoader());
                RestClient restClientAnnotation = clientInterface.getAnnotation(RestClient.class);

                if (restClientAnnotation == null) {
                    continue;
                }

                // 设置Bean的类型为RestClientFactoryBean
                definition.setBeanClass(RestClientFactoryBean.class);

                // 设置构造函数参数，传入接口类型的类名
                definition.getConstructorArgumentValues().addIndexedArgumentValue(0, beanClassName);

                // 添加Bean的依赖
                definition.setDependsOn("jRestClientFactory");

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to process RestClient: " + beanClassName, e);
            }
        }
    }
}
