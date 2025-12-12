package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;
import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器排序测试
 */
public class InterceptorOrderTest {

    // 用于记录拦截器执行顺序的列表
    private static final List<String> beforeRequestOrder = new ArrayList<>();
    private static final List<String> afterResponseOrder = new ArrayList<>();

    @Test
    public void testInterceptorOrder() {
        // 创建四个具有不同order值的拦截器
        HttpRequestInterceptor interceptorA = new TestInterceptor("A", 10);
        HttpRequestInterceptor interceptorB = new TestInterceptor("B", 20);
        HttpRequestInterceptor interceptorC = new TestInterceptor("C", 30);
        HttpRequestInterceptor interceptorD = new TestInterceptor("D", 40);

        // 使用建造者模式创建JRestClientFactory实例，并添加四个拦截器（顺序打乱）
        JRestClientFactory factory = new JRestClientFactory.Builder()
                .addInterceptor(interceptorC)
                .addInterceptor(interceptorA)
                .addInterceptor(interceptorD)
                .addInterceptor(interceptorB)
                .build();

        // 创建GitHubClient代理实例
        GitHubClient gitHubClient = factory.createProxy(GitHubClient.class);

        // 调用方法触发拦截器执行
        User user = gitHubClient.getUser("octocat");

        // 验证拦截器执行顺序
        System.out.println("Before Request Order: " + beforeRequestOrder);
        System.out.println("After Response Order: " + afterResponseOrder);

        // 验证前置拦截顺序是A、B、C、D（按order升序）
        assert beforeRequestOrder.get(0).equals("A");
        assert beforeRequestOrder.get(1).equals("B");
        assert beforeRequestOrder.get(2).equals("C");
        assert beforeRequestOrder.get(3).equals("D");

        // 验证后置拦截顺序是D、C、B、A（按order降序）
        assert afterResponseOrder.get(0).equals("D");
        assert afterResponseOrder.get(1).equals("C");
        assert afterResponseOrder.get(2).equals("B");
        assert afterResponseOrder.get(3).equals("A");
    }

    /**
     * 测试用拦截器
     */
    private static class TestInterceptor implements HttpRequestInterceptor {
        private final String name;
        private final int order;

        public TestInterceptor(String name, int order) {
            this.name = name;
            this.order = order;
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public void beforeRequest(HttpRequest httpRequest) {
            System.out.println("Before Request Interceptor " + name + " (order: " + order + ")");
            beforeRequestOrder.add(name);
        }

        @Override
        public void afterResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
            System.out.println("After Response Interceptor " + name + " (order: " + order + ")");
            afterResponseOrder.add(name);
        }
    }
}
