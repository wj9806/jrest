package io.github.wj9806.jrest.client.annotation;

import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.proxy.ClientType;

import java.lang.reflect.Method;

/**
 * REST客户端注解解析器接口
 * 将接口和方法注解解析成配置和HttpRequest对象
 */
public interface AnnotationParser {
    
    /**
     * 解析RestClient注解，获取基础URL
     *
     * @param clazz 接口类
     * @return 基础URL
     */
    String parseBaseUrl(Class<?> clazz);
    
    /**
     * 解析RestClient注解，获取客户端类型
     *
     * @param clazz 接口类
     * @return 客户端类型
     */
    ClientType parseClientType(Class<?> clazz);
    
    /**
     * 将方法解析成HttpRequest
     *
     * @param method 方法对象
     * @param args 方法参数
     * @param baseUrl 基础URL
     * @return HttpRequest对象
     */
    HttpRequest parse(Method method, Object[] args, String baseUrl);
}
