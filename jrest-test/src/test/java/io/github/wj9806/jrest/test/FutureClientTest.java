package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.PathParam;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Future返回值支持测试
 */
public class FutureClientTest {

    @RestClient(baseUrl = "https://api.github.com", clientType = ClientType.NATIVE)
    interface FutureGitHubClient {
        
        @GET("/users/{username}")
        Future<User> getUser(@PathParam("username") String username);
        
        @GET("/users/{username}/repos")
        Future<List<Repository>> getRepositories(@PathParam("username") String username);
    }


    @Test
    public void testFutureGetUser() throws ExecutionException, InterruptedException {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        System.out.println("Creating proxy...");
        FutureGitHubClient client = factory.createProxy(FutureGitHubClient.class);
        
        System.out.println("Calling getUser...");
        Future<User> future = client.getUser("octocat");
        
        System.out.println("Future obtained: " + future.getClass().getName());
        assertNotNull(future);
        
        System.out.println("Calling future.get()...");
        User user = future.get();
        System.out.println("Future.get() returned: " + user);
        
        assertNotNull(user);
        assertEquals("octocat", user.getLogin());
        
        System.out.println("Future User: " + user);
    }

    @Test
    public void testFutureGetRepositories() throws ExecutionException, InterruptedException {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        FutureGitHubClient client = factory.createProxy(FutureGitHubClient.class);
        Future<List<Repository>> future = client.getRepositories("octocat");
        
        assertNotNull(future);
        
        List<Repository> repos = future.get();
        assertNotNull(repos);
        assertFalse(repos.isEmpty());
        
        System.out.println("Future Repositories: " + repos.size() + " repositories found");
    }
}