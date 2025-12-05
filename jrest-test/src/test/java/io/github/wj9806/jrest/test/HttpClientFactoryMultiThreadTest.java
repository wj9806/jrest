package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.http.HttpClientFactory;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 测试HttpClientFactory在多线程环境下的单例实现
 */
public class HttpClientFactoryMultiThreadTest {

    /**
     * 简单测试HttpClientFactory的单例实现
     */
    @Test
    public void testSimpleHttpClientFactory() {
        // 测试HttpClientFactory类加载器
        ClassLoader factoryClassLoader1 = HttpClientFactory.class.getClassLoader();
        ClassLoader factoryClassLoader2 = HttpClientFactory.class.getClassLoader();
        System.out.println("HttpClientFactory类加载器1: " + factoryClassLoader1);
        System.out.println("HttpClientFactory类加载器2: " + factoryClassLoader2);
        System.out.println("HttpClientFactory类加载器是否相同: " + (factoryClassLoader1 == factoryClassLoader2));
        
        // 验证每个ClientType的HttpClient实例是同一个对象
        Object nativeClient1 = HttpClientFactory.createHttpClient(ClientType.NATIVE);
        Object nativeClient2 = HttpClientFactory.createHttpClient(ClientType.NATIVE);
        
        // 测试Native HttpClient类加载器
        ClassLoader nativeClientClassLoader1 = nativeClient1.getClass().getClassLoader();
        ClassLoader nativeClientClassLoader2 = nativeClient2.getClass().getClassLoader();
        System.out.println("NativeHttpClient类加载器1: " + nativeClientClassLoader1);
        System.out.println("NativeHttpClient类加载器2: " + nativeClientClassLoader2);
        System.out.println("NativeHttpClient类加载器是否相同: " + (nativeClientClassLoader1 == nativeClientClassLoader2));
        
        System.out.println("简单测试 - Native实例1: " + nativeClient1.hashCode() + ", 实例: " + nativeClient1);
        System.out.println("简单测试 - Native实例2: " + nativeClient2.hashCode() + ", 实例: " + nativeClient2);
        System.out.println("httpClient1类: " + nativeClient1.getClass());
        System.out.println("httpClient2类: " + nativeClient2.getClass());
        System.out.println("httpClient1和httpClient2是否是同一个类: " + (nativeClient1.getClass() == nativeClient2.getClass()));
        
        assertSame("Native HttpClient实例应该是同一个对象", nativeClient1, nativeClient2);
        
        Object apacheClient1 = HttpClientFactory.createHttpClient(ClientType.APACHE);
        Object apacheClient2 = HttpClientFactory.createHttpClient(ClientType.APACHE);
        
        // 测试Apache HttpClient类加载器
        ClassLoader apacheClientClassLoader1 = apacheClient1.getClass().getClassLoader();
        ClassLoader apacheClientClassLoader2 = apacheClient2.getClass().getClassLoader();
        System.out.println("ApacheHttpClient类加载器1: " + apacheClientClassLoader1);
        System.out.println("ApacheHttpClient类加载器2: " + apacheClientClassLoader2);
        System.out.println("ApacheHttpClient类加载器是否相同: " + (apacheClientClassLoader1 == apacheClientClassLoader2));
        
        System.out.println("简单测试 - Apache实例1: " + apacheClient1.hashCode() + ", 实例: " + apacheClient1);
        System.out.println("简单测试 - Apache实例2: " + apacheClient2.hashCode() + ", 实例: " + apacheClient2);
        
        assertSame("Apache HttpClient实例应该是同一个对象", apacheClient1, apacheClient2);
        
        // 验证不同ClientType的HttpClient实例是不同的对象
        assertNotSame("Native和Apache HttpClient实例应该是不同的对象", nativeClient1, apacheClient1);
        
        System.out.println("简单测试通过！");
    }
    
    /**
     * 测试多线程环境下，每个ClientType的HttpClient实例只会被创建一次
     */
    @Test
    public void testHttpClientFactoryMultiThread() throws InterruptedException {
        // 定义线程数量
        int threadCount = 10;
        
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // 提交多个线程同时请求Native类型的HttpClient
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    Object nativeClient = HttpClientFactory.createHttpClient(ClientType.NATIVE);
                    System.out.println("线程[" + threadId + "]获取到的Native实例: " + nativeClient.hashCode());
                    System.out.println("线程[" + threadId + "] - HttpClientFactory类加载器: " + HttpClientFactory.class.getClassLoader());
                    System.out.println("线程[" + threadId + "] - NativeHttpClient类加载器: " + nativeClient.getClass().getClassLoader());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        latch.await(10, TimeUnit.SECONDS);
        
        // 关闭线程池
        executorService.shutdown();
        
        // 验证每个ClientType的HttpClient实例是同一个对象
        Object nativeClient1 = HttpClientFactory.createHttpClient(ClientType.NATIVE);
        Object nativeClient2 = HttpClientFactory.createHttpClient(ClientType.NATIVE);
        System.out.println("测试主线程获取到的Native实例1: " + nativeClient1.hashCode());
        System.out.println("测试主线程获取到的Native实例2: " + nativeClient2.hashCode());
        System.out.println("测试主线程 - HttpClientFactory类加载器: " + HttpClientFactory.class.getClassLoader());
        System.out.println("测试主线程 - NativeHttpClient类加载器: " + nativeClient1.getClass().getClassLoader());
        assertSame("Native HttpClient实例应该是同一个对象", nativeClient1, nativeClient2);
        
        System.out.println("多线程测试通过！");
    }
}