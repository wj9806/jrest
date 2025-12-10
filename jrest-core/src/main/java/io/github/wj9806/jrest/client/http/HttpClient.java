package io.github.wj9806.jrest.client.http;

import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
     * 异步发送HTTP请求
     * 
     * @param httpRequest HTTP请求对象
     * @return 包含响应结果的CompletableFuture
     */
    CompletableFuture<HttpResponse> exchangeAsync(HttpRequest httpRequest);
    
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
    
    /**
     * 设置重试策略
     * 
     * @param retryer 重试策略
     */
    void setRetryer(Retryer retryer);
    
    /**
     * 获取重试策略
     * 
     * @return 重试策略
     */
    Retryer getRetryer();
    
    /**
     * 设置编解码器管理器
     * 
     * @param codecManager 编解码器管理器
     */
    void setCodecManager(CodecManager codecManager);
    
    /**
     * 获取编解码器管理器
     * 
     * @return 编解码器管理器
     */
    CodecManager getCodecManager();
    
    /**
     * 设置连接超时时间
     * 
     * @param connectTimeout 连接超时时间（毫秒）
     */
    void setConnectTimeout(int connectTimeout);
    
    /**
     * 获取连接超时时间
     * 
     * @return 连接超时时间（毫秒）
     */
    int getConnectTimeout();
    
    /**
     * 设置读取超时时间
     * 
     * @param readTimeout 读取超时时间（毫秒）
     */
    void setReadTimeout(int readTimeout);
    
    /**
     * 获取读取超时时间
     * 
     * @return 读取超时时间（毫秒）
     */
    int getReadTimeout();
}