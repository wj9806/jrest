package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.proxy.ClientType;
import io.github.wj9806.jrest.client.annotation.*;
import java.util.List;

/**
 * GitHub API客户端接口
 */
@RestClient(baseUrl = "https://api.github.com", clientType = ClientType.NATIVE)
public interface GitHubClient {
    
    /**
     * 获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @GET("/users/{username}")
    User getUser(@PathParam("username") String username);
    
    /**
     * 获取用户仓库列表
     * 
     * @param username 用户名
     * @param perPage 每页数量
     * @param page 页码
     * @return 仓库列表
     */
    @GET("/users/{username}/repos")
    List<Repository> getRepositories(@PathParam("username") String username,
                                     @QueryParam("per_page") int perPage,
                                     @QueryParam("page") int page);

}