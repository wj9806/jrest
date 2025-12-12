package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.Header;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.junit.jupiter.api.Test;

/**
 * Header注解测试类
 */
public class HeaderAnnotationTest {

    /**
     * 定义带有Header注解的测试接口
     */
    @RestClient(baseUrl = "https://httpbin.org", clientType = ClientType.NATIVE)
    interface HeaderTestClient {
        
        @GET("/get")
        String getWithHeaders(
                @Header("Authorization") String token,
                @Header("User-Agent") String userAgent
        );
    }

    @Test
    public void testHeaderAnnotation() {
        // 创建JRestClientFactory实例
        JRestClientFactory factory = new JRestClientFactory.Builder()
                .build();

        // 创建代理实例
        HeaderTestClient client = factory.createProxy(HeaderTestClient.class);

        // 调用接口
        String response = client.getWithHeaders("Bearer token123", "JRest-Client");

        // 验证响应
        assert response != null;
        System.out.println("Response: " + response);
    }
}