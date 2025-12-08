package io.github.wj9806.jrest.client.annotation;

import java.lang.annotation.*;

/**
 * 表单字段注解，用于标记方法参数作为表单字段
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormField {
    /**
     * 表单字段名称
     * @return 字段名称
     */
    String value() default "";
}
