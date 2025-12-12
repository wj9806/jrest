package io.github.wj9806.jrest.spring.config;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.spring.annotation.SpringMvcAnnotationParserConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * RestClient自动配置类
 * Spring Boot启动时自动配置RestClient相关的Bean
 */
@Configuration
@Import(SpringMvcAnnotationParserConfiguration.class)
public class RestClientAutoConfiguration {

    /**
     * 自动配置JRestClientFactory Bean
     * 当容器中没有JRestClientFactory Bean时才会创建
     */
    @Bean
    @ConditionalOnMissingBean(JRestClientFactory.class)
    public JRestClientFactoryFactoryBean jRestClientFactory() {
        return new JRestClientFactoryFactoryBean();
    }

}
