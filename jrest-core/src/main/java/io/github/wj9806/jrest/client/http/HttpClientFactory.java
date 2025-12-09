package io.github.wj9806.jrest.client.http;

import io.github.wj9806.jrest.client.proxy.ClientType;

/**
 * HttpClient工厂类
 */
public class HttpClientFactory {

    // 使用volatile关键字确保多线程环境下的可见性
    private static volatile HttpClient nativeClient;
    private static volatile HttpClient apacheClient;
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private HttpClientFactory() {
    }
    
    /**
     * 获取HttpClient实例
     * 同一种ClientType的Client只创建一次
     * 使用双重检查锁定（Double-Checked Locking）确保线程安全和性能
     * 
     * @param clientType 客户端类型枚举
     * @return HttpClient实例
     */
    public static HttpClient createHttpClient(ClientType clientType) {
        HttpClient httpClient;
        switch (clientType) {
            case APACHE:
                if (apacheClient == null) {
                    synchronized (HttpClientFactory.class) {
                        if (apacheClient == null) {
                            apacheClient = new ApacheHttpClient();
                        }
                    }
                }
                httpClient = apacheClient;
                break;
            case NATIVE:
            default:
                if (nativeClient == null) {
                    synchronized (HttpClientFactory.class) {
                        if (nativeClient == null) {
                            nativeClient = new NativeHttpClient();
                        }
                    }
                }
                httpClient = nativeClient;
        }
        
        return httpClient;
    }
    
}