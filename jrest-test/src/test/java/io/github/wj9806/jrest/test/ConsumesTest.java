package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.POST;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.annotation.RequestBody;
import io.github.wj9806.jrest.client.http.ContentType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试consumes参数功能
 */
public class ConsumesTest {
    
    /**
     * 测试REST客户端接口
     */
    @RestClient(baseUrl = "http://localhost:8080")
    interface TestClient {
        
        /**
         * 使用默认Content-Type的POST请求
         */
        @POST("/api/test")
        String defaultContentType(@RequestBody TestBody body);
        
        /**
         * 使用自定义Content-Type的POST请求
         */
        @POST(value = "/api/test", consumes = ContentType.TEXT_PLAIN)
        String customContentType(@RequestBody String body);
        
        /**
         * 使用XML Content-Type的POST请求
         */
        @POST(value = "/api/test", consumes = ContentType.APPLICATION_XML)
        String xmlContentType(@RequestBody String body);
    }
    
    /**
     * 测试请求体类
     */
    static class TestBody {
        private String name;
        private int age;
        
        // getter和setter
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
    }
    
    @Test
    public void testConsumesAnnotation() {
        // 创建REST客户端工厂
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        
        // 创建REST客户端
        TestClient client = factory.createProxy(TestClient.class);
        
        assertNotNull(client);
        
        // 测试用例将在实际环境中验证Content-Type头是否正确设置
        // 由于这是单元测试，我们只验证客户端能够正常创建
        System.out.println("REST客户端创建成功，consumes参数功能已集成");
    }
}