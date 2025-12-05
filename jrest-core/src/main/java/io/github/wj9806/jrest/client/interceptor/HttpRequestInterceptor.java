package io.github.wj9806.jrest.client.interceptor;

import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.http.HttpResponse;

/**
 * HTTP请求拦截器接口
 * 用于在发送请求前和收到响应后进行拦截处理
 */
public interface HttpRequestInterceptor {
    
    /**
     * 获取拦截器的执行顺序
     * 数字越小，前置拦截优先级越高
     * 后置拦截的顺序与前置相反
     * 
     * @return 执行顺序，默认0
     */
    default int order() {
        return 0;
    }
    
    /**
     * 在发送请求前拦截
     * 
     * @param httpRequest HTTP请求对象，可修改
     */
    default void beforeRequest(HttpRequest httpRequest) {
        // 默认空实现
    }
    
    /**
     * 在收到响应后拦截
     * 
     * @param httpRequest HTTP请求对象
     * @param httpResponse HTTP响应对象，可修改
     */
    default void afterResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        // 默认空实现
    }
}
