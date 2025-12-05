package io.github.wj9806.jrest.client.proxy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wj9806.jrest.client.annotation.*;
import io.github.wj9806.jrest.client.http.HttpClient;
import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * REST客户端调用处理器
 */
public class RestClientInvocationHandler implements InvocationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RestClientInvocationHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final AnnotationParser annotationParser;
    
    public RestClientInvocationHandler(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.annotationParser = DefaultAnnotationParser.getInstance();
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果是Object类的方法，直接调用
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        
        logger.debug("Invoking method: {}", method.getName());
        
        // 使用注解解析器解析方法为HttpRequest
        HttpRequest httpRequest = annotationParser.parse(method, args, baseUrl);
        
        // 发送HTTP请求
        HttpResponse response = httpClient.exchange(httpRequest);
        
        // 解析响应
        return parseResponse(response, method);
    }
    
    /**
     * 解析响应
     */
    private Object parseResponse(HttpResponse response, Method method) throws Exception {
        String body = response.getBody();
        
        // 如果返回类型是void，直接返回null
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        
        // 如果返回类型是String，直接返回响应体
        if (returnType == String.class) {
            return body;
        }
        
        // 否则，将JSON响应体转换为目标类型
        if (body != null && !body.isEmpty()) {
            // 使用TypeReference处理泛型类型
            TypeReference<?> typeReference = new TypeReference<Object>() {
                @Override
                public java.lang.reflect.Type getType() {
                    return method.getGenericReturnType();
                }
            };
            return objectMapper.readValue(body, typeReference);
        }
        
        return null;
    }
}