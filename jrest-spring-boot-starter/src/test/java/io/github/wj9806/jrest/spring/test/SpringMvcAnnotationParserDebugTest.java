package io.github.wj9806.jrest.spring.test;

import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.proxy.ClientType;
import io.github.wj9806.jrest.spring.EnableRestClient;
import io.github.wj9806.jrest.spring.annotation.SpringMvcAnnotationParserConfiguration;
import io.github.wj9806.jrest.spring.test.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringMvcAnnotationParserConfiguration.class)
@EnableRestClient(basePackages = "io.github.wj9806.jrest.spring.test")
@Import(SpringMvcAnnotationParserDebugTest.TestBeanFactoryPostProcessor.class)
public class SpringMvcAnnotationParserDebugTest {

    public static class TestBeanFactoryPostProcessor  implements BeanFactoryPostProcessor{
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            MutablePropertySources propertySources = environment.getPropertySources();
            Map<String, Object> map = new HashMap<>();
            map.put("github.url", "https://api.github.com");
            MapPropertySource propertiesPropertySource = new MapPropertySource("test", map);
            propertySources.addFirst(propertiesPropertySource);
        }
    }

    @Autowired
    private TestClient testClient;

    @Test
    public void testParseMethod() throws Exception {
        // 调用getUser方法获取用户信息
        User user = testClient.getUser("octocat");

        // 打印用户信息
        System.out.println("User: " + user);

        // 验证用户信息是否正确
        assert user != null;
        assert "octocat".equals(user.getLogin());
    }

    @RestClient(baseUrl = "${github.url}", clientType = ClientType.NATIVE)
    @RequestMapping("users")
    interface TestClient {
        @GetMapping("{username}")
        User getUser(@PathVariable String username);
    }
}
