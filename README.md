# JRest - 轻量级REST客户端框架

JRest是一个基于Java的轻量级REST客户端框架，通过注解和代理模式简化REST API的调用。

## 特性

- 🌟 **注解驱动**：使用简洁的注解定义REST接口
- 🔄 **多种客户端实现**：支持原生HTTP客户端和Apache HttpClient
- ⏱️ **超时控制**：可配置连接超时和读取超时
- 🔌 **拦截器支持**：灵活的请求拦截器机制
- 🔁 **重试机制**：可配置的重试策略
- 📦 **编解码支持**：内置JSON等多种编解码器
- 📝 **异步支持**：支持异步HTTP请求
- 🎨 **简洁API**：简单易用的API设计

## 快速开始

### 添加依赖

在您的Maven项目中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.wj9806</groupId>
    <artifactId>jrest-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 定义REST接口

创建一个接口并使用`@RestClient`注解标记：

```java
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.annotation.Get;
import io.github.wj9806.jrest.client.annotation.Path;

@RestClient(baseUrl = "https://api.github.com")
public interface GitHubClient {
    
    @Get("/users/{username}")
    User getUser(@Path("username") String username);
}
```

### 创建客户端实例

使用`JRestClientFactory`创建客户端代理：

```java
import io.github.wj9806.jrest.client.JRestClientFactory;

// 创建工厂实例
JRestClientFactory factory = new JRestClientFactory.Builder()
    .connectTimeout(10000)  // 设置连接超时为10秒
    .readTimeout(15000)     // 设置读取超时为15秒
    .build();

// 创建客户端代理
GitHubClient client = factory.createProxy(GitHubClient.class);

// 调用API
User user = client.getUser("octocat");
System.out.println(user.getName());
```

## 核心功能

### 1. 超时配置

```java
JRestClientFactory factory = new JRestClientFactory.Builder()
    .connectTimeout(10000)  // 连接超时（毫秒）
    .readTimeout(15000)     // 读取超时（毫秒）
    .build();
```

### 2. 拦截器

创建自定义拦截器：

```java
public class LoggingInterceptor implements HttpRequestInterceptor {
    @Override
    public void beforeRequest(HttpRequest httpRequest) {
        System.out.println("Request: " + request.getMethod() + " " + request.getUrl());
        return true;
    }
    
    @Override
    public void afterResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        System.out.println("Response: " + response.getStatusCode());
    }

}
```

添加拦截器：

```java
JRestClientFactory factory = new JRestClientFactory.Builder()
    .addInterceptor(new LoggingInterceptor())
    .build();
```

### 3. 重试策略

```java
Retryer retryer = new Retryer.Builder()
    .maxAttempts(3)
    .retryPolicy(RetryPolicy.DEFAULT)
    .build();

JRestClientFactory factory = new JRestClientFactory.Builder()
    .retryer(retryer)
    .build();
```

### 4. 客户端类型选择

```java
// 使用Apache HttpClient
@RestClient(baseUrl = "https://api.github.com", clientType = ClientType.APACHE)
public interface GitHubClient {
    // ...
}
```

## 支持的注解

### 类级别注解

- `@RestClient`：标记接口为REST客户端，指定基础URL和客户端类型

### 方法级别注解

- `@Get`：GET请求
- `@Post`：POST请求
- `@Put`：PUT请求
- `@Delete`：DELETE请求
- `@Patch`：PATCH请求
- `@Headers`：设置请求头
- `@Consumes`：指定请求内容类型
- `@Produces`：指定响应内容类型

### 参数级别注解

- `@Path`：路径参数
- `@Query`：查询参数
- `@Body`：请求体
- `@Header`：请求头参数

## 异步支持

```java
@RestClient(baseUrl = "https://api.github.com")
public interface GitHubAsyncClient {
    
    @Get("/users/{username}")
    Future<User> getUserAsync(@Path("username") String username);
}
```

使用异步客户端：

```java
GitHubAsyncClient client = factory.createProxy(GitHubAsyncClient.class);
Future<User> future = client.getUserAsync("octocat");

// 处理异步结果
User user = future.get();
```

## 文件上传

```java
@RestClient(baseUrl = "https://example.com")
public interface FileUploadClient {
    
    @Post("/upload")
    String uploadFile(@RequestPart MultipartFile file, @FormField("description") String description);
}
```

## 项目结构

```
jrest/
├── jrest-core/           # 核心库
├── jrest-test/           # 测试模块
└── README.md            # 项目文档
```

## 依赖

- Apache HttpClient 4.5.13
- Jackson 2.12.5
- SLF4J 1.7.32
