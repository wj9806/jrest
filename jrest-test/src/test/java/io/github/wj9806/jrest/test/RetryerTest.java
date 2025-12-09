package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.http.DefaultRetryer;
import io.github.wj9806.jrest.client.http.Retryer;
import org.junit.Test;

import java.io.IOException;

public class RetryerTest {

    @Test
    public void testRetryerLogic() {
        // 创建自定义重试策略
        Retryer retryer = new DefaultRetryer.Builder()
                .maxRetries(3)
                .build();

        // 测试重试条件
        System.out.println("Testing retry conditions...");
        System.out.println("Should retry for IOException: " + retryer.shouldRetry(null, null, new IOException("Connection reset"), 0));
        System.out.println("Max retries: " + retryer.getMaxRetries());
        System.out.println("Delay for retry 1: " + retryer.getDelay(1) + " ms");
        System.out.println("Delay for retry 2: " + retryer.getDelay(2) + " ms");
        System.out.println("Delay for retry 3: " + retryer.getDelay(3) + " ms");

        // 测试延迟计算
        System.out.println("\nTesting delay calculation...");
        for (int i = 1; i <= 3; i++) {
            System.out.println("Delay for retry " + i + ": " + retryer.getDelay(i) + " ms");
        }

        // 测试最大重试次数
        System.out.println("\nTesting max retries...");
        System.out.println("Max retries: " + retryer.getMaxRetries());
        
        System.out.println("\nRetryer logic test completed successfully!");
    }
}