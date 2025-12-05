package io.github.wj9806.jrest.client.http;

import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * 抽象HTTP客户端接口
 */
public interface HttpClient {
    
    /**
     * 发送HTTP请求
     * 
     * @param httpRequest HTTP请求对象
     * @return 响应结果
     * @throws IOException IO异常
     */
    HttpResponse exchange(HttpRequest httpRequest) throws IOException;
    
    /**
     * 添加请求拦截器
     * 
     * @param interceptor 请求拦截器
     */
    void addInterceptor(HttpRequestInterceptor interceptor);
    
    /**
     * 获取所有请求拦截器
     * 
     * @return 请求拦截器列表
     */
    List<HttpRequestInterceptor> getInterceptors();
    
    /**
     * 清除所有请求拦截器
     */
    void clearInterceptors();
}