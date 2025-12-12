package io.github.wj9806.jrest.spring.config;

import io.github.wj9806.jrest.spring.EnableRestClient;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RestClient扫描配置器
 * 扫描指定包下的@RestClient注解并注册为Spring Bean
 */
public class RestClientScannerConfigurer implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取EnableRestClient注解的属性
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableRestClient.class.getName())
        );

        if (attributes == null) {
            throw new IllegalArgumentException("EnableRestClient annotation not found");
        }

        // 获取要扫描的包路径
        List<String> basePackages = new ArrayList<>();
        
        // 从basePackages属性获取
        for (String pkg : attributes.getStringArray("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        
        // 从basePackageClasses属性获取
        for (Class<?> clazz : attributes.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        
        // 如果没有指定包，则扫描注解所在类的包
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }

        // 创建RestClient扫描器
        RestClientScanner scanner = new RestClientScanner(registry);
        
        // 开始扫描
        scanner.doScan(StringUtils.toStringArray(basePackages));
    }
}
