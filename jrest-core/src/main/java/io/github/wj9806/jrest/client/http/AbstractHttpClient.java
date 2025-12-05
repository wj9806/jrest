package io.github.wj9806.jrest.client.http;

import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * HttpClient抽象基类，提供拦截器管理功能
 */
public abstract class AbstractHttpClient implements HttpClient {
    
    private final List<HttpRequestInterceptor> interceptors = new ArrayList<>();
    
    @Override
    public HttpResponse exchange(HttpRequest httpRequest) throws IOException {
        // 请求前拦截 - 按order升序执行
        for (HttpRequestInterceptor interceptor : interceptors) {
            interceptor.beforeRequest(httpRequest);
        }
        
        // 执行实际请求
        HttpResponse httpResponse = doExchange(httpRequest);
        
        // 响应后拦截 - 按order降序执行
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).afterResponse(httpRequest, httpResponse);
        }
        
        return httpResponse;
    }
    
    /**
     * 执行实际的HTTP请求，由子类实现
     * 
     * @param httpRequest HTTP请求对象
     * @return 响应结果
     * @throws IOException IO异常
     */
    protected abstract HttpResponse doExchange(HttpRequest httpRequest) throws IOException;
    
    @Override
    public void addInterceptor(HttpRequestInterceptor interceptor) {
        if (interceptor != null) {
            interceptors.add(interceptor);
            // 按order值升序排序
            interceptors.sort(Comparator.comparingInt(HttpRequestInterceptor::order));
        }
    }
    
    @Override
    public List<HttpRequestInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
    
    @Override
    public void clearInterceptors() {
        interceptors.clear();
    }
}
