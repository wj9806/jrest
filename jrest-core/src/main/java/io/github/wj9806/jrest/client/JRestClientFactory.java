package io.github.wj9806.jrest.client;

import io.github.wj9806.jrest.client.http.CodecManager;
import io.github.wj9806.jrest.client.http.HttpClient;
import io.github.wj9806.jrest.client.http.HttpClientFactory;
import io.github.wj9806.jrest.client.http.Retryer;
import io.github.wj9806.jrest.client.http.decode.Decoder;
import io.github.wj9806.jrest.client.http.encode.Encoder;
import io.github.wj9806.jrest.client.interceptor.GlobalInterceptorManager;
import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;
import io.github.wj9806.jrest.client.proxy.AnnotationParser;
import io.github.wj9806.jrest.client.proxy.ClientType;
import io.github.wj9806.jrest.client.proxy.DefaultAnnotationParser;
import io.github.wj9806.jrest.client.proxy.RestClientInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * REST客户端代理工厂
 */
public class JRestClientFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(JRestClientFactory.class);
    private final AnnotationParser annotationParser;
    private final List<HttpRequestInterceptor> interceptors;
    private final Retryer retryer;
    private final CodecManager codecManager;
    private final int connectTimeout;
    private final int readTimeout;

    /**
     * 私有构造函数，通过Builder创建实例
     */
    private JRestClientFactory(Builder builder) {
        this.annotationParser = builder.annotationParser;
        this.interceptors = new ArrayList<>(builder.interceptors);
        this.retryer = builder.retryer;
        this.codecManager = builder.codecManager;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
    }

    /**
     * 创建REST客户端代理实例
     * 
     * @param clazz 接口类
     * @param <T>   接口类型
     * @return 代理实例
     */
    public <T> T createProxy(Class<T> clazz) {
        // 使用注解解析器解析RestClient注解
        String baseUrl = annotationParser.parseBaseUrl(clazz);
        ClientType clientType = annotationParser.parseClientType(clazz);
        
        logger.debug("Creating proxy for interface: {}, baseUrl: {}, clientType: {}", 
                clazz.getName(), baseUrl, clientType);
        
        // 创建HttpClient实例
        HttpClient httpClient = HttpClientFactory.createHttpClient(clientType);
        
        // 设置超时时间
        httpClient.setConnectTimeout(connectTimeout);
        httpClient.setReadTimeout(readTimeout);
        
        // 设置重试策略
        if (retryer != null) {
            httpClient.setRetryer(retryer);
        }
        
        // 设置编解码器管理器
        if (codecManager != null) {
            httpClient.setCodecManager(codecManager);
        }
        
        // 添加全局拦截器
        for (HttpRequestInterceptor interceptor : interceptors) {
            httpClient.addInterceptor(interceptor);
        }
        GlobalInterceptorManager.getInstance().getGlobalInterceptors().forEach(httpClient::addInterceptor);
        
        // 创建代理实例
        Object client = Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new RestClientInvocationHandler(baseUrl, httpClient)
        );
        
        return clazz.cast(client);
    }
    
    /**
     * JRestClientFactory建造者类
     */
    public static class Builder {
        private AnnotationParser annotationParser = DefaultAnnotationParser.getInstance();
        private final List<HttpRequestInterceptor> interceptors = new ArrayList<>();
        private Retryer retryer;
        private CodecManager codecManager;
        private int connectTimeout = 30000;
        private int readTimeout = 30000;

        /**
         * 设置注解解析器
         * 
         * @param annotationParser 注解解析器实例
         * @return Builder实例
         */
        public Builder annotationParser(AnnotationParser annotationParser) {
            this.annotationParser = annotationParser;
            return this;
        }
        
        /**
         * 设置编解码器管理器
         * 
         * @param codecManager 编解码器管理器实例
         * @return Builder实例
         */
        public Builder codecManager(CodecManager codecManager) {
            this.codecManager = codecManager;
            return this;
        }
        
        /**
         * 设置连接超时时间
         * 
         * @param connectTimeout 连接超时时间（毫秒）
         * @return Builder实例
         */
        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        
        /**
         * 设置读取超时时间
         * 
         * @param readTimeout 读取超时时间（毫秒）
         * @return Builder实例
         */
        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
        
        /**
         * 添加编码器
         * 
         * @param encoder 编码器实例
         * @return Builder实例
         */
        public Builder addEncoder(Encoder encoder) {
            if (codecManager == null) {
                codecManager = new CodecManager();
            }
            codecManager.addEncoder(encoder);
            return this;
        }
        
        /**
         * 添加解码器
         * 
         * @param decoder 解码器实例
         * @return Builder实例
         */
        public Builder addDecoder(Decoder decoder) {
            if (codecManager == null) {
                codecManager = new CodecManager();
            }
            codecManager.addDecoder(decoder);
            return this;
        }

        /**
         * 添加请求拦截器
         * 
         * @param interceptor 请求拦截器
         * @return Builder实例
         */
        public Builder addInterceptor(HttpRequestInterceptor interceptor) {
            if (interceptor != null) {
                this.interceptors.add(interceptor);
            }
            return this;
        }

        /**
         * 设置重试策略
         * 
         * @param retryer 重试策略
         * @return Builder实例
         */
        public Builder retryer(Retryer retryer) {
            this.retryer = retryer;
            return this;
        }

        /**
         * 构建JRestClientFactory实例
         * 
         * @return JRestClientFactory实例
         */
        public JRestClientFactory build() {
            return new JRestClientFactory(this);
        }
    }
}