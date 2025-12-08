package io.github.wj9806.jrest.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * multipart/form-data 请求中的文件参数注解
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestPart {
    
    /**
     * 参数名称
     * @return 参数名称
     */
    String value() default "";
}