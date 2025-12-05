package io.github.wj9806.jrest.client.annotation;

import io.github.wj9806.jrest.client.proxy.ClientType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口为REST客户端
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestClient {
    /**
     * 基础URL
     */
    String baseUrl() default "";
    
    /**
     * 客户端实现类型
     */
    ClientType clientType() default ClientType.NATIVE;
}