package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.test.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserServiceTest {
    
    @Autowired
    private UserService userService;

    @Resource
    private CodecTest.TestClient testClient;

    @Test
    public void test() {
        String result = testClient.test();
        System.out.println(result);

        CodecTest.XmlUser user = testClient.getUser(1L);
        System.out.println(user);
    }
    
    @Test
    public void testUserServiceBeanExists() {
        // 测试UserService接口是否被正确实例化为Spring Bean
        assertNotNull(userService, "UserService bean should be initialized");
    }
    
}
