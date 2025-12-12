package io.github.wj9806.jrest.spring.test.client;

import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.proxy.ClientType;
import io.github.wj9806.jrest.spring.test.entity.Repository;
import io.github.wj9806.jrest.spring.test.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * GitHub API客户端接口
 */
@RestClient(baseUrl = "https://api.github.com", clientType = ClientType.NATIVE)
@RequestMapping("users")
public interface GitHubClient2 {
    
    /**
     * 获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("{username}")
    User getUser(@PathVariable String username);
    
    /**
     * 获取用户仓库列表
     * 
     * @param username 用户名
     * @param perPage 每页数量
     * @param page 页码
     * @return 仓库列表
     */
    @GetMapping("/{username}/repos")
    List<Repository> getRepositories(@PathVariable String username,
                                     @RequestParam("per_page") int perPage,
                                     @RequestParam("page") int page);

}