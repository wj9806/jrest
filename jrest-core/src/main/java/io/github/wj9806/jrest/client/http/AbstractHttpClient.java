package io.github.wj9806.jrest.client.http;

import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient抽象基类，提供拦截器管理功能
 */
public abstract class AbstractHttpClient implements HttpClient {
    
    private final List<HttpRequestInterceptor> interceptors = new ArrayList<>();
    
    // 重试策略
    private Retryer retryer;
    
    // 默认重试策略
    private static final Retryer DEFAULT_RETRYER = new DefaultRetryer();
    
    // 编解码器管理器
    private CodecManager codecManager;
    
    // 默认编解码器管理器
    private static final CodecManager DEFAULT_CODEC_MANAGER = new CodecManager();
    
    // 默认连接超时时间（毫秒）
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    
    // 默认读取超时时间（毫秒）
    private static final int DEFAULT_READ_TIMEOUT = 30000;
    
    // 连接超时时间（毫秒）
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    
    // 读取超时时间（毫秒）
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    
    @Override
    public HttpResponse exchange(HttpRequest httpRequest) throws IOException {
        int retryCount = 0;
        HttpResponse httpResponse = null;

        // 获取重试策略，如果没有设置则使用默认重试策略
        Retryer currentRetryer = getRetryer();
        
        while (true) {
            try {
                // 请求前拦截 - 按order升序执行
                for (HttpRequestInterceptor interceptor : interceptors) {
                    interceptor.beforeRequest(httpRequest);
                }
                
                // 执行实际请求
                httpResponse = doExchange(httpRequest);
                
                // 响应后拦截 - 按order降序执行
                for (int i = interceptors.size() - 1; i >= 0; i--) {
                    interceptors.get(i).afterResponse(httpRequest, httpResponse);
                }
                
                // 检查是否需要重试
                if (!currentRetryer.shouldRetry(httpRequest, httpResponse, null, retryCount)) {
                    return httpResponse;
                }
                
            } catch (IOException e) {
                // 检查是否需要重试
                if (!currentRetryer.shouldRetry(httpRequest, null, e, retryCount)) {
                    throw e;
                }
            }
            
            // 递增重试次数
            retryCount++;
            
            // 获取延迟时间
            long delay = currentRetryer.getDelay(retryCount);
            
            try {
                // 等待延迟时间
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("Retry interrupted", ie);
            }
        }
    }
    
    /**
     * 执行实际的HTTP请求，由子类实现
     * 
     * @param httpRequest HTTP请求对象
     * @return 响应结果
     * @throws IOException IO异常
     */
    protected abstract HttpResponse doExchange(HttpRequest httpRequest) throws IOException;
    
    /**
     * 异步执行实际的HTTP请求，由子类实现
     * 
     * @param httpRequest HTTP请求对象
     * @return 包含响应结果的CompletableFuture
     */
    protected abstract CompletableFuture<HttpResponse> doExchangeAsync(HttpRequest httpRequest);
    
    /**
     * 调度执行器服务，用于异步请求和重试
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    
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
    
    @Override
    public void setRetryer(Retryer retryer) {
        this.retryer = retryer;
    }
    
    @Override
    public CompletableFuture<HttpResponse> exchangeAsync(HttpRequest httpRequest) {
        // 创建一个新的CompletableFuture用于异步执行
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        
        // 启动异步执行
        executeAsync(httpRequest, future, 0);
        
        return future;
    }
    
    /**
     * 异步执行HTTP请求，并处理重试逻辑
     */
    private void executeAsync(HttpRequest httpRequest, CompletableFuture<HttpResponse> future, int retryCount) {
        // 如果future已被取消，则不再执行
        if (future.isCancelled()) {
            return;
        }
        
        // 获取重试策略
        Retryer currentRetryer = getRetryer();
        
        // 创建请求副本，避免并发修改问题
        HttpRequest requestCopy = HttpRequest.Builder.newBuilder(httpRequest).build();
        
        // 请求前拦截 - 按order升序执行
        for (HttpRequestInterceptor interceptor : interceptors) {
            interceptor.beforeRequest(requestCopy);
        }
        
        // 异步执行实际请求
        doExchangeAsync(requestCopy)
            .thenApply(httpResponse -> {
                // 响应后拦截 - 按order降序执行
                for (int i = interceptors.size() - 1; i >= 0; i--) {
                    interceptors.get(i).afterResponse(requestCopy, httpResponse);
                }
                return httpResponse;
            })
            .thenAccept(httpResponse -> {
                // 检查是否需要重试
                if (!currentRetryer.shouldRetry(requestCopy, httpResponse, null, retryCount)) {
                    future.complete(httpResponse);
                } else {
                    // 需要重试，安排下次重试
                    scheduleRetry(requestCopy, future, retryCount + 1);
                }
            })
            .exceptionally(ex -> {
                // 提取原始的IOException
                Throwable cause = ex.getCause();
                IOException exception;
                if (cause instanceof IOException) {
                    exception = (IOException) cause;
                } else if (ex instanceof IOException) {
                    exception = (IOException) ex;
                } else {
                    exception = new IOException(ex);
                }
                
                // 检查是否需要重试
                if (!currentRetryer.shouldRetry(requestCopy, null, exception, retryCount)) {
                    future.completeExceptionally(exception);
                } else {
                    // 需要重试，安排下次重试
                    scheduleRetry(requestCopy, future, retryCount + 1);
                }
                
                return null;
            });
    }
    
    /**
     * 安排重试
     */
    private void scheduleRetry(HttpRequest httpRequest, CompletableFuture<HttpResponse> future, int retryCount) {
        // 获取延迟时间
        long delay = getRetryer().getDelay(retryCount);
        
        // 安排延迟后的重试
        scheduler.schedule(() -> executeAsync(httpRequest, future, retryCount), delay, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public Retryer getRetryer() {
        return retryer != null ? retryer : DEFAULT_RETRYER;
    }
    
    @Override
    public void setCodecManager(CodecManager codecManager) {
        this.codecManager = codecManager;
    }
    
    @Override
    public CodecManager getCodecManager() {
        return codecManager != null ? codecManager : DEFAULT_CODEC_MANAGER;
    }
    
    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    @Override
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    @Override
    public int getReadTimeout() {
        return readTimeout;
    }
}
