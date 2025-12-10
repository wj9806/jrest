# Spring MVC XML接口示例

## 1. 依赖配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>spring-mvc-xml-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    
    <properties>
        <java.version>8</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Jackson XML支持 -->
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 2. 实体类 (User.java)

```java
package com.example.springmvxml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JacksonXmlRootElement(localName = "user")
public class User {
    
    @JacksonXmlProperty(localName = "id")
    private Long id;
    
    @JacksonXmlProperty(localName = "name")
    private String name;
    
    @JacksonXmlProperty(localName = "email")
    private String email;
    
    // 构造函数、getter和setter
    public User() {
    }
    
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
```

## 3. Controller (UserController.java)

```java
package com.example.springmvxml.controller;

import com.example.springmvxml.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    /**
     * 接收XML格式的用户信息并返回
     * 
     * @param user XML格式的用户对象
     * @return 响应结果
     */
    @PostMapping(
        value = "/create", 
        consumes = {"application/xml", "text/xml"}, 
        produces = {"application/xml", "text/xml"}
    )
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 这里可以添加保存用户的逻辑
        System.out.println("接收到XML用户信息: " + user);
        
        // 返回创建的用户信息
        return ResponseEntity.ok(user);
    }
    
    /**
     * 获取用户信息（返回XML格式）
     * 
     * @param id 用户ID
     * @return XML格式的用户信息
     */
    @GetMapping(
        value = "/{id}", 
        produces = {"application/xml", "text/xml"}
    )
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // 这里可以添加从数据库获取用户的逻辑
        User user = new User(id, "张三", "zhangsan@example.com");
        
        return ResponseEntity.ok(user);
    }
}
```

## 4. 主应用类 (Application.java)

```java
package com.example.springmvxml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 5. 测试XML请求

### 使用curl测试POST请求（XML格式）

```bash
curl -X POST "http://localhost:8080/api/users/create" \
     -H "Content-Type: application/xml" \
     -H "Accept: application/xml" \
     -d '<user><id>1</id><name>张三</name><email>zhangsan@example.com</email></user>'
```

### 使用curl测试GET请求（返回XML格式）

```bash
curl -X GET "http://localhost:8080/api/users/1" \
     -H "Accept: application/xml"
```

## 6. 配置说明

1. **Jackson XML支持**：通过添加`jackson-dataformat-xml`依赖，Spring Boot会自动配置XML消息转换器。

2. **@RequestBody注解**：用于接收XML格式的请求体，Spring会自动将其转换为Java对象。

3. **consumes和produces属性**：用于指定接口接受和返回的内容类型，这里设置为`application/xml`和`text/xml`。

4. **Jackson XML注解**：
   - `@JacksonXmlRootElement`：指定XML根元素名称
   - `@JacksonXmlProperty`：指定XML元素名称

这个示例展示了如何在Spring MVC中接收和处理XML格式的请求，以及如何返回XML格式的响应。