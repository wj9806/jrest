package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.interceptor.LoggingInterceptor;
import io.github.wj9806.jrest.client.JRestClientFactory;
import org.junit.Test;

import java.util.List;

/**
 * GitHubClient测试类
 */
public class GitHubClientTest {
    
    @Test
    public void testGetUser() {
        // 使用建造者模式创建JRestClientFactory实例，并通过建造者添加LoggingInterceptor
        JRestClientFactory factory = new JRestClientFactory.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();
        
        // 创建GitHubClient代理实例
        GitHubClient gitHubClient = factory.createProxy(GitHubClient.class);
        
        // 调用getUser方法获取用户信息
        User user = gitHubClient.getUser("octocat");
        
        // 打印用户信息
        System.out.println("User: " + user);
        
        // 验证用户信息是否正确
        assert user != null;
        assert "octocat".equals(user.getLogin());
    }
    
    @Test
    public void testGetRepositories() {
        // 使用建造者模式创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        // 创建GitHubClient代理实例
        GitHubClient gitHubClient = factory.createProxy(GitHubClient.class);
        
        // 调用getRepositories方法获取仓库列表
        List<Repository> repositories = gitHubClient.getRepositories("octocat", 5, 1);
        
        // 打印仓库列表
        System.out.println("Repositories:");
        for (Repository repo : repositories) {
            System.out.println(repo);
        }
        
        // 验证仓库列表是否正确
        assert repositories != null;
        assert repositories.size() > 0;
    }
}
