package io.github.wj9806.jrest.client.http;

import java.io.IOException;

/**
 * 重试策略接口，用于决定是否应该重试失败的HTTP请求
 */
public interface Retryer {
    
    /**
     * 决定是否应该重试请求
     *
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象，如果请求没有到达服务器则为null
     * @param exception 异常对象，如果没有异常则为null
     * @param retryCount 当前重试次数
     * @return 是否应该重试
     */
    boolean shouldRetry(HttpRequest httpRequest, HttpResponse httpResponse, IOException exception, int retryCount);
    
    /**
     * 获取重试之间的延迟时间（毫秒）
     *
     * @param retryCount 当前重试次数
     * @return 延迟时间（毫秒）
     */
    long getDelay(int retryCount);
    
    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    int getMaxRetries();
}