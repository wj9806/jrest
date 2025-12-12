package io.github.wj9806.jrest.spring.config;

import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.JRestClientFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * RestClient工厂Bean
 * 用于创建RestClient代理实例
 */
public class RestClientFactoryBean<T> implements FactoryBean<T>, InitializingBean {

    private Class<T> restClientInterface;
    @Autowired
    private JRestClientFactory restClientFactory;

    public RestClientFactoryBean(String restClientInterfaceName) {
        try {
            this.restClientInterface = (Class<T>) ClassUtils.forName(restClientInterfaceName, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load RestClient interface class: " + restClientInterfaceName, e);
        }
    }

    public RestClientFactoryBean(Class<T> restClientInterface) {
        this.restClientInterface = restClientInterface;
    }

    @Override
    public T getObject() throws Exception {
        Assert.notNull(restClientInterface, "RestClient interface must not be null");
        Assert.notNull(restClientFactory, "JRestClientFactory must not be null");

        // 检查接口是否被@RestClient注解标记
        RestClient restClientAnnotation = restClientInterface.getAnnotation(RestClient.class);
        Assert.notNull(restClientAnnotation, "Interface must be annotated with @RestClient");

        // 使用JRestClientFactory创建代理实例
        return restClientFactory.createProxy(restClientInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return restClientInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 只在实际创建代理时才检查restClientInterface是否为null
        // 这样可以支持创建模板RestClientFactoryBean
        if (restClientInterface != null) {
            Assert.isTrue(restClientInterface.isInterface(), "RestClient must be an interface");
        }
    }

    // Getter and Setter
    public void setRestClientFactory(JRestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }
}
