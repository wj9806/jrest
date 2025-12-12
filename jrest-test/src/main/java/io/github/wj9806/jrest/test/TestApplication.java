package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.spring.EnableRestClient;
import io.github.wj9806.jrest.test.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRestClient
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
