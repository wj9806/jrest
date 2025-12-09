package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.PathParam;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.http.DefaultRetryer;
import io.github.wj9806.jrest.client.http.Retryer;
import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;
import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.http.HttpResponse;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步重试功能测试类
 */
public class AsyncRetryTest {

    // 用于记录请求次数的拦截器
    private static class RequestCountInterceptor implements HttpRequestInterceptor {
        private final List<Long> requestTimes = new ArrayList<>();
        
        @Override
        public void beforeRequest(HttpRequest httpRequest) {
            // 记录请求时间
            requestTimes.add(System.currentTimeMillis());
            System.out.println("Request " + requestTimes.size() + " sent at: " + requestTimes.get(requestTimes.size() - 1));
        }
        
        public int getRequestCount() {
            return requestTimes.size();
        }
        
        public List<Long> getRequestTimes() {
            return requestTimes;
        }
    }
    
    /**
     * 定义异步测试客户端接口
     */
    @RestClient(baseUrl = "http://localhost:8080", clientType = ClientType.NATIVE)
    interface AsyncTestClient {
        
        /**
         * 异步获取指定状态码的响应
         */
        @GET("/status/{code}")
        CompletableFuture<String> asyncGetStatus(@PathParam("code") int code);
    }

    @Test
    public void testAsyncRetry() throws InterruptedException, ExecutionException, TimeoutException {
        // 创建请求计数拦截器
        RequestCountInterceptor countInterceptor = new RequestCountInterceptor();
        
        // 创建自定义重试策略：最多重试3次，遇到5xx错误重试
        Retryer retryer = new DefaultRetryer.Builder()
                .maxRetries(3)
                .initialDelay(100)  // 初始延迟100ms
                .maxDelay(1000)     // 最大延迟1000ms
                .addRetryStatusCode(500)  // 添加500状态码重试
                .addRetryStatusCode(502)  // 添加502状态码重试
                .addRetryStatusCode(503)  // 添加503状态码重试
                .addRetryStatusCode(504)  // 添加504状态码重试
                .build();
        
        // 创建JRestClientFactory实例，配置重试策略和请求计数拦截器
        JRestClientFactory factory = new JRestClientFactory.Builder()
                .addInterceptor(countInterceptor)
                .retryer(retryer)
                .build();
        
        // 创建异步测试客户端代理
        AsyncTestClient client = factory.createProxy(AsyncTestClient.class);
        System.out.println("client = " + client);
        
        // 发送异步请求，模拟503错误（服务不可用）
        System.out.println("Sending async request with 503 status code...");
        CompletableFuture<String> future = client.asyncGetStatus(503);
        System.out.println("future = " + future);
        
        try {
            // 设置超时时间，避免无限等待
            String result = future.get(10, TimeUnit.SECONDS);
            System.out.println("Request completed successfully: " + result);
        } catch (ExecutionException e) {
            // 预期会捕获到异常，因为httpbin.org会返回503错误且不会自动恢复
            System.out.println("Request failed as expected: " + e.getMessage());
        }
        
        // 验证重试次数：初始请求 + 最多3次重试 = 最多4次请求
        int requestCount = countInterceptor.getRequestCount();
        System.out.println("Total requests sent: " + requestCount);
        
        // 验证至少发送了2次请求（初始请求 + 至少1次重试）
        assert requestCount >= 2;
        
        // 验证请求之间有延迟（重试间隔）
        List<Long> requestTimes = countInterceptor.getRequestTimes();
        for (int i = 1; i < requestTimes.size(); i++) {
            long delay = requestTimes.get(i) - requestTimes.get(i-1);
            System.out.println("Delay between request " + i + " and " + (i+1) + ": " + delay + " ms");
            // 验证延迟至少大于初始延迟的一部分（允许有一些误差）
            assert delay >= 50; // 至少50ms延迟
        }
        
        System.out.println("Async retry test completed successfully!");
    }
    
    @Test
    public void testAsyncRetryWithSuccess() throws InterruptedException, ExecutionException {
        // 创建请求计数拦截器
        RequestCountInterceptor countInterceptor = new RequestCountInterceptor();
        
        // 创建自定义重试策略：最多重试3次，遇到5xx错误重试
        Retryer retryer = new DefaultRetryer.Builder()
                .maxRetries(3)
                .initialDelay(100)
                .build();
        
        // 创建JRestClientFactory实例，配置重试策略和请求计数拦截器
        JRestClientFactory factory = new JRestClientFactory.Builder()
                .addInterceptor(countInterceptor)
                .retryer(retryer)
                .build();
        
        // 创建异步测试客户端代理
        AsyncTestClient client = factory.createProxy(AsyncTestClient.class);
        System.out.println("client = " + client);
        
        // 发送异步请求，使用200状态码（成功）
        System.out.println("Sending async request with 200 status code...");
        CompletableFuture<String> future = client.asyncGetStatus(200);
        System.out.println("future = " + future);
        
        // 获取结果
        String result = future.get();
        System.out.println("Request completed successfully: " + result);
        
        // 验证只发送了1次请求（因为请求成功，不需要重试）
        int requestCount = countInterceptor.getRequestCount();
        System.out.println("Total requests sent: " + requestCount);
        assert requestCount == 1;
        
        System.out.println("Async retry with success test completed successfully!");
    }
}