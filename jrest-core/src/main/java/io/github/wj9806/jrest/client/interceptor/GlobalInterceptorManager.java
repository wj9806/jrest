package io.github.wj9806.jrest.client.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 全局拦截器管理器
 * 用于注册和管理全局HTTP请求拦截器
 */
public class GlobalInterceptorManager {
    
    private static final GlobalInterceptorManager INSTANCE = new GlobalInterceptorManager();
    
    private final List<HttpRequestInterceptor> globalInterceptors = new ArrayList<>();
    
    private GlobalInterceptorManager() {
        // 单例模式
    }
    
    /**
     * 获取全局拦截器管理器实例
     * 
     * @return 全局拦截器管理器实例
     */
    public static GlobalInterceptorManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 添加全局请求拦截器
     * 
     * @param interceptor 请求拦截器
     */
    public void addGlobalInterceptor(HttpRequestInterceptor interceptor) {
        if (interceptor != null) {
            globalInterceptors.add(interceptor);
        }
    }
    
    /**
     * 获取所有全局请求拦截器
     * 
     * @return 全局请求拦截器列表
     */
    public List<HttpRequestInterceptor> getGlobalInterceptors() {
        return Collections.unmodifiableList(globalInterceptors);
    }
    
    /**
     * 清除所有全局请求拦截器
     */
    public void clearGlobalInterceptors() {
        globalInterceptors.clear();
    }
}
