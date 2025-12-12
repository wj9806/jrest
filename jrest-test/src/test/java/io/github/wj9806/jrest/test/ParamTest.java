package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.RestClient;
import org.junit.jupiter.api.Test;

public class ParamTest {

    @RestClient(baseUrl = "http://localhost:8080")
    interface UserService {
        @GET("/getUser")
        Result<User> getUser();
    }

    @Test
    public void test() {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder().build();

        // 创建异步GitHub客户端代理
        UserService client = factory.createProxy(UserService.class);

        Result<User> result = client.getUser();
        System.out.println(result.getData().getClass());
    }

}
