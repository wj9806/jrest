package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.PathParam;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 异步GitHub API请求测试类
 */
public class AsyncGitHubTest {
    
    /**
     * 定义异步GitHub客户端接口
     */
    @RestClient(baseUrl = "https://api.github.com", clientType = ClientType.NATIVE)
    interface AsyncGitHubClient {
        
        /**
         * 异步获取用户信息
         */
        @GET("/users/{username}")
        CompletableFuture<User> asyncGetUser(@PathParam("username") String username);
        
        /**
         * 异步获取用户仓库列表
         */
        @GET("/users/{username}/repos")
        CompletableFuture<List<Repository>> asyncGetRepositories(@PathParam("username") String username);
        
        /**
         * 异步获取GitHub API状态
         */
        @GET("/status")
        CompletableFuture<String> asyncGetStatus();
    }
    
    @Test
    public void testAsyncGetUser() throws InterruptedException, ExecutionException {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        // 创建异步GitHub客户端代理
        AsyncGitHubClient client = factory.createProxy(AsyncGitHubClient.class);
        
        // 发送异步GET请求获取用户信息
        CompletableFuture<User> future = client.asyncGetUser("octocat");
        
        // 验证future不为null
        assert future != null;
        
        // 等待请求完成并获取结果
        User user = future.get();
        
        // 验证响应不为null
        assert user != null;
        assert "octocat".equals(user.getLogin());
        
        // 打印用户信息
        System.out.println("Async User: " + user);
    }
    
    @Test
    public void testAsyncGetRepositories() throws InterruptedException, ExecutionException {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        // 创建异步GitHub客户端代理
        AsyncGitHubClient client = factory.createProxy(AsyncGitHubClient.class);
        
        // 发送异步GET请求获取仓库列表
        CompletableFuture<List<Repository>> future = client.asyncGetRepositories("octocat");
        
        // 验证future不为null
        assert future != null;
        
        // 等待请求完成并获取结果
        List<Repository> repositories = future.get();
        
        // 验证响应不为null
        assert repositories != null;
        assert repositories.size() > 0;
        
        // 打印前5个仓库
        System.out.println("Async Repositories (First 5):");
        for (int i = 0; i < Math.min(5, repositories.size()); i++) {
            System.out.println(repositories.get(i));
        }
    }
    
    @Test
    public void testAsyncGetStatus() throws InterruptedException, ExecutionException {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        // 创建异步GitHub客户端代理
        AsyncGitHubClient client = factory.createProxy(AsyncGitHubClient.class);
        
        // 发送异步GET请求获取GitHub API状态
        CompletableFuture<String> future = client.asyncGetStatus();
        
        // 验证future不为null
        assert future != null;
        
        // 等待请求完成并获取结果
        String status = future.get();
        
        // 验证响应不为null
        assert status != null;
        
        // 打印状态
        System.out.println("Async GitHub API Status: " + status);
    }
    
    @Test
    public void testMultipleAsyncRequests() throws InterruptedException, ExecutionException {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        // 创建异步GitHub客户端代理
        AsyncGitHubClient client = factory.createProxy(AsyncGitHubClient.class);
        
        // 发送多个异步请求
        CompletableFuture<User> userFuture = client.asyncGetUser("octocat");
        CompletableFuture<List<Repository>> reposFuture = client.asyncGetRepositories("octocat");
        CompletableFuture<String> statusFuture = client.asyncGetStatus();
        
        // 等待所有请求完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(userFuture, reposFuture, statusFuture);
        allOf.get();
        
        // 验证所有请求都成功完成
        assert userFuture.isDone() && !userFuture.isCompletedExceptionally();
        assert reposFuture.isDone() && !reposFuture.isCompletedExceptionally();
        assert statusFuture.isDone() && !statusFuture.isCompletedExceptionally();
        
        System.out.println("Multiple Async GitHub Requests Test Completed Successfully");
    }
}