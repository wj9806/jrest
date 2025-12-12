package io.github.wj9806.jrest.spring.test;

import io.github.wj9806.jrest.spring.EnableRestClient;
import io.github.wj9806.jrest.spring.annotation.SpringMvcAnnotationParserConfiguration;
import io.github.wj9806.jrest.spring.test.client.GitHubClient2;
import io.github.wj9806.jrest.spring.test.entity.Repository;
import io.github.wj9806.jrest.spring.test.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringMvcAnnotationParserConfiguration.class)
@EnableRestClient(basePackages = "io.github.wj9806.jrest.spring.test.client")
public class SpringMvcAnnotationParserTest {

    @Autowired
    private GitHubClient2 gitHubClient2;

    @Test
    public void testGetUser() throws Exception {
        // 调用getUser方法获取用户信息
        User user = gitHubClient2.getUser("octocat");

        // 打印用户信息
        System.out.println("User: " + user);

        // 验证用户信息是否正确
        assert user != null;
        assert "octocat".equals(user.getLogin());
    }

    @Test
    public void testGetRepositories() throws Exception {
        // 调用getRepositories方法获取仓库列表
        List<Repository> repositories = gitHubClient2.getRepositories("octocat", 5, 1);

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