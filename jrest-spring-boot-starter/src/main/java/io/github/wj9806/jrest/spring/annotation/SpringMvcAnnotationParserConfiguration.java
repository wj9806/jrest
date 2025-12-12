package io.github.wj9806.jrest.spring.annotation;

import io.github.wj9806.jrest.client.annotation.AnnotationParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Spring MVC注解解析器配置类
 * 自动注册SpringMvcAnnotationParser作为默认的注解解析器
 */
@Configuration
public class SpringMvcAnnotationParserConfiguration {

    /**
     * 注册SpringMvcAnnotationParser作为默认的注解解析器
     *
     * @return SpringMvcAnnotationParser实例
     */
    @Bean
    public AnnotationParser springMvcAnnotationParser(Environment environment) {
        SpringMvcAnnotationParser parser = new SpringMvcAnnotationParser();
        parser.setEnvironment(environment);
        return parser;
    }
}
