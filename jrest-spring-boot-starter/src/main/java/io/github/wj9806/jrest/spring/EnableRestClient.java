package io.github.wj9806.jrest.spring;

import io.github.wj9806.jrest.spring.config.RestClientAutoConfiguration;
import io.github.wj9806.jrest.spring.config.RestClientScannerConfigurer;
import org.springframework.context.annotation.Import;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用JRest客户端功能
 * 扫描并注册所有被@RestClient注解标记的接口
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RestClientScannerConfigurer.class, RestClientAutoConfiguration.class})
public @interface EnableRestClient {
    
    /**
     * 指定要扫描的包路径
     * 如果不指定，则扫描注解所在类的包及其子包
     */
    String[] basePackages() default {};
    
    /**
     * 指定要扫描的类
     * 通过类来间接指定扫描路径
     */
    Class<?>[] basePackageClasses() default {};
}
