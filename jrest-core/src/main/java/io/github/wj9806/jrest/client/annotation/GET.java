package io.github.wj9806.jrest.client.annotation;

import io.github.wj9806.jrest.client.http.ContentType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP GET方法注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    /**
     * 请求路径
     */
    String value() default "";
    
    /**
     * 请求的Content-Type
     */
    ContentType consumes() default ContentType.APPLICATION_JSON;
}