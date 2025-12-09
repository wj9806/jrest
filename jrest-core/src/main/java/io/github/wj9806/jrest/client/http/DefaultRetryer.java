package io.github.wj9806.jrest.client.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 默认重试策略实现
 * <p>
 * 支持对5xx状态码、连接超时、未知主机等情况进行重试
 * 使用指数退避算法计算延迟时间
 */
public class DefaultRetryer implements Retryer {
    
    // 默认最大重试次数
    private static final int DEFAULT_MAX_RETRIES = 3;
    
    // 默认初始延迟时间（毫秒）
    private static final long DEFAULT_INITIAL_DELAY = 100;
    
    // 默认最大延迟时间（毫秒）
    private static final long DEFAULT_MAX_DELAY = 10000;
    
    // 默认需要重试的HTTP状态码
    private static final Set<Integer> DEFAULT_RETRY_STATUS_CODES;
    
    // 默认需要重试的异常类型
    private static final Set<Class<? extends IOException>> DEFAULT_RETRY_EXCEPTIONS;
    
    static {
        Set<Integer> statusCodes = new HashSet<>();
        statusCodes.add(500); // 服务器内部错误
        statusCodes.add(502); // 网关错误
        statusCodes.add(503); // 服务不可用
        statusCodes.add(504); // 网关超时
        DEFAULT_RETRY_STATUS_CODES = Collections.unmodifiableSet(statusCodes);
        
        Set<Class<? extends IOException>> exceptions = new HashSet<>();
        exceptions.add(SocketTimeoutException.class); // 连接超时
        exceptions.add(UnknownHostException.class); // 未知主机
        exceptions.add(ConnectException.class); // 连接失败
        DEFAULT_RETRY_EXCEPTIONS = Collections.unmodifiableSet(exceptions);
    }
    
    // 最大重试次数
    private final int maxRetries;
    
    // 初始延迟时间
    private final long initialDelay;
    
    // 最大延迟时间
    private final long maxDelay;
    
    // 需要重试的HTTP状态码
    private final Set<Integer> retryStatusCodes;
    
    // 需要重试的异常类型
    private final Set<Class<? extends IOException>> retryExceptions;
    
    /**
     * 默认构造函数
     */
    public DefaultRetryer() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY, DEFAULT_MAX_DELAY, DEFAULT_RETRY_STATUS_CODES, DEFAULT_RETRY_EXCEPTIONS);
    }
    
    /**
     * 构造函数
     *
     * @param maxRetries 最大重试次数
     * @param initialDelay 初始延迟时间（毫秒）
     * @param maxDelay 最大延迟时间（毫秒）
     * @param retryStatusCodes 需要重试的HTTP状态码
     * @param retryExceptions 需要重试的异常类型
     */
    public DefaultRetryer(int maxRetries, long initialDelay, long maxDelay, Set<Integer> retryStatusCodes, Set<Class<? extends IOException>> retryExceptions) {
        this.maxRetries = maxRetries;
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
        this.retryStatusCodes = retryStatusCodes != null ? Collections.unmodifiableSet(new HashSet<>(retryStatusCodes)) : DEFAULT_RETRY_STATUS_CODES;
        this.retryExceptions = retryExceptions != null ? Collections.unmodifiableSet(new HashSet<>(retryExceptions)) : DEFAULT_RETRY_EXCEPTIONS;
    }
    
    @Override
    public boolean shouldRetry(HttpRequest httpRequest, HttpResponse httpResponse, IOException exception, int retryCount) {
        // 如果重试次数超过最大重试次数，则不重试
        if (retryCount >= maxRetries) {
            return false;
        }
        
        // 如果有异常，检查是否是可重试的异常类型
        if (exception != null) {
            return isRetryableException(exception);
        }
        
        // 如果有响应，检查状态码是否需要重试
        if (httpResponse != null) {
            return retryStatusCodes.contains(httpResponse.getStatusCode());
        }
        
        // 其他情况不重试
        return false;
    }
    
    @Override
    public long getDelay(int retryCount) {
        // 指数退避算法：initialDelay * 2^retryCount
        long delay = initialDelay * (1L << retryCount);
        // 限制最大延迟时间
        return Math.min(delay, maxDelay);
    }
    
    @Override
    public int getMaxRetries() {
        return maxRetries;
    }
    
    /**
     * 检查异常是否是可重试的类型
     *
     * @param exception 异常对象
     * @return 是否是可重试的异常类型
     */
    private boolean isRetryableException(IOException exception) {
        // 检查异常类型是否在需要重试的异常集合中
        for (Class<? extends IOException> exceptionClass : retryExceptions) {
            if (exceptionClass.isInstance(exception)) {
                return true;
            }
        }
        
        // 其他情况不重试
        return false;
    }
    
    /**
     * 创建DefaultRetryer的构建器
     *
     * @return 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * DefaultRetryer构建器
     */
    public static class Builder {
        
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private long initialDelay = DEFAULT_INITIAL_DELAY;
        private long maxDelay = DEFAULT_MAX_DELAY;
        private Set<Integer> retryStatusCodes = new HashSet<>(DEFAULT_RETRY_STATUS_CODES);
        private Set<Class<? extends IOException>> retryExceptions = new HashSet<>(DEFAULT_RETRY_EXCEPTIONS);
        
        /**
         * 设置最大重试次数
         *
         * @param maxRetries 最大重试次数
         * @return Builder实例
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        /**
         * 设置初始延迟时间
         *
         * @param initialDelay 初始延迟时间（毫秒）
         * @return Builder实例
         */
        public Builder initialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }
        
        /**
         * 设置最大延迟时间
         *
         * @param maxDelay 最大延迟时间（毫秒）
         * @return Builder实例
         */
        public Builder maxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }
        
        /**
         * 添加需要重试的HTTP状态码
         *
         * @param statusCode HTTP状态码
         * @return Builder实例
         */
        public Builder addRetryStatusCode(int statusCode) {
            this.retryStatusCodes.add(statusCode);
            return this;
        }
        
        /**
         * 设置需要重试的HTTP状态码
         *
         * @param retryStatusCodes 需要重试的HTTP状态码
         * @return Builder实例
         */
        public Builder retryStatusCodes(Set<Integer> retryStatusCodes) {
            this.retryStatusCodes.clear();
            if (retryStatusCodes != null) {
                this.retryStatusCodes.addAll(retryStatusCodes);
            }
            return this;
        }
        
        /**
         * 添加需要重试的异常类型
         *
         * @param exceptionClass 异常类型
         * @return Builder实例
         */
        public Builder addRetryException(Class<? extends IOException> exceptionClass) {
            this.retryExceptions.add(exceptionClass);
            return this;
        }
        
        /**
         * 设置需要重试的异常类型（可变参数版本）
         *
         * @param retryExceptions 需要重试的异常类型
         * @return Builder实例
         */
        @SafeVarargs
        public final Builder retryExceptions(Class<? extends IOException>... retryExceptions) {
            this.retryExceptions.clear();
            if (retryExceptions != null) {
                this.retryExceptions.addAll(Arrays.asList(retryExceptions));
            }
            return this;
        }
        
        /**
         * 构建DefaultRetryer实例
         *
         * @return DefaultRetryer实例
         */
        public DefaultRetryer build() {
            return new DefaultRetryer(maxRetries, initialDelay, maxDelay, retryStatusCodes, retryExceptions);
        }
    }
}