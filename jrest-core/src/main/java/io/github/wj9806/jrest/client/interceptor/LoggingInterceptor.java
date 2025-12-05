package io.github.wj9806.jrest.client.interceptor;

import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志拦截器，用于打印请求和响应信息
 */
public class LoggingInterceptor implements HttpRequestInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public void beforeRequest(HttpRequest httpRequest) {
        logger.info("[REQUEST] {} {}", httpRequest.getMethod(), httpRequest.getUrl());
        
        // 打印请求头
        if (httpRequest.getHeaders() != null && !httpRequest.getHeaders().isEmpty()) {
            logger.info("[REQUEST HEADERS]");
            httpRequest.getHeaders().forEach((key, value) -> 
                logger.info("  {}: {}", key, value));
        }
        
        // 打印请求参数
        if (httpRequest.getQueryParams() != null && !httpRequest.getQueryParams().isEmpty()) {
            logger.info("[REQUEST PARAMS]");
            httpRequest.getQueryParams().forEach((key, value) -> 
                logger.info("  {}: {}", key, value));
        }
        
        // 打印请求体（注意：请求体可能很大，这里可以根据需要调整日志级别）
        if (httpRequest.getBody() != null) {
            logger.debug("[REQUEST BODY] {}", httpRequest.getBody());
        }
    }
    
    @Override
    public void afterResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        logger.info("[RESPONSE] {} {}", httpResponse.getStatusCode(), httpResponse.getHeaders().get("Content-Type"));
        
        // 打印响应头
        if (httpResponse.getHeaders() != null && !httpResponse.getHeaders().isEmpty()) {
            logger.info("[RESPONSE HEADERS]");
            httpResponse.getHeaders().forEach((key, value) -> 
                logger.info("  {}: {}", key, value));
        }
        
        // 打印响应体（注意：响应体可能很大，这里可以根据需要调整日志级别）
        if (httpResponse.getBody() != null) {
            logger.debug("[RESPONSE BODY] {}", httpResponse.getBody());
        }
    }
}
