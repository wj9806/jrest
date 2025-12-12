package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.http.DefaultRetryer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class RetryExceptionTest {

    @Test
    public void testDefaultRetryExceptions() {
        // 创建默认的DefaultRetryer实例
        DefaultRetryer retryer = new DefaultRetryer();
        
        // 验证默认情况下是否包含SocketTimeoutException和UnknownHostException
        System.out.println("Default retry exceptions:");
        System.out.println("SocketTimeoutException: " + isExceptionRetryable(retryer, new SocketTimeoutException()));
        System.out.println("UnknownHostException: " + isExceptionRetryable(retryer, new UnknownHostException()));
        System.out.println("IOException: " + isExceptionRetryable(retryer, new IOException()));
    }

    @Test
    public void testCustomRetryExceptions() {
        // 创建自定义的DefaultRetryer实例，添加特定的异常类型
        DefaultRetryer retryer = new DefaultRetryer.Builder()
                .addRetryException(IOException.class) // 添加通用IOException
                .addRetryException(UnknownHostException.class) // 已存在，但再次添加不会有影响
                .build();
        
        // 验证自定义配置是否生效
        System.out.println("\nCustom retry exceptions:");
        System.out.println("SocketTimeoutException: " + isExceptionRetryable(retryer, new SocketTimeoutException()));
        System.out.println("UnknownHostException: " + isExceptionRetryable(retryer, new UnknownHostException()));
        System.out.println("IOException: " + isExceptionRetryable(retryer, new IOException()));
    }

    @Test
    public void testSetRetryExceptions() {
        // 创建自定义的DefaultRetryer实例，替换默认的异常类型列表
        DefaultRetryer retryer = new DefaultRetryer.Builder()
                .retryExceptions(IOException.class) // 只保留IOException
                .build();
        
        // 验证替换是否生效
        System.out.println("\nSet retry exceptions:");
        System.out.println("SocketTimeoutException: " + isExceptionRetryable(retryer, new SocketTimeoutException()));
        System.out.println("UnknownHostException: " + isExceptionRetryable(retryer, new UnknownHostException()));
        System.out.println("IOException: " + isExceptionRetryable(retryer, new IOException()));
    }

    /**
     * 检查异常是否可重试
     */
    private boolean isExceptionRetryable(DefaultRetryer retryer, IOException e) {
        // 使用反射调用isRetryableException方法
        try {
            java.lang.reflect.Method method = DefaultRetryer.class.getDeclaredMethod("isRetryableException", IOException.class);
            method.setAccessible(true);
            return (boolean) method.invoke(retryer, e);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}