package io.github.wj9806.jrest.client.proxy;

import io.github.wj9806.jrest.client.http.HttpClient;
import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * REST客户端调用处理器
 */
public class RestClientInvocationHandler implements InvocationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RestClientInvocationHandler.class);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final AnnotationParser annotationParser;
    
    public RestClientInvocationHandler(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.annotationParser = DefaultAnnotationParser.getInstance();
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果是Object类的方法，直接调用
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        
        logger.debug("Invoking method: {}", method.getName());
        
        // 使用注解解析器解析方法为HttpRequest
        HttpRequest httpRequest = annotationParser.parse(method, args, baseUrl);
        
        // 检查返回类型是否为Future（包括CompletableFuture）
        Class<?> returnType = method.getReturnType();
        if (Future.class.isAssignableFrom(returnType)) {
            // 异步请求处理
            Object result = handleAsyncRequest(httpRequest, method);
            // 确保返回非null值
            if (result == null) {
                logger.error("handleAsyncRequest returned null");
                return CompletableFuture.completedFuture("");
            }
            return result;
        } else {
            // 同步请求处理
            HttpResponse response = httpClient.exchange(httpRequest);
            return parseResponse(response, method);
        }
    }
    
    /**
     * 处理异步请求
     */
    private Object handleAsyncRequest(HttpRequest httpRequest, Method method) {
        // 检查参数
        if (httpRequest == null) {
            logger.error("HttpRequest is null");
            return CompletableFuture.completedFuture("");
        }
        
        if (method == null) {
            logger.error("Method is null");
            return CompletableFuture.completedFuture("");
        }
        
        // 发送异步HTTP请求
        CompletableFuture<HttpResponse> responseFuture = httpClient.exchangeAsync(httpRequest);
        
        // 检查responseFuture是否为null
        if (responseFuture == null) {
            logger.error("responseFuture is null");
            return CompletableFuture.completedFuture("");
        }
        
        // 返回一个新的CompletableFuture，它会在原始Future完成时解析响应
        return responseFuture.thenApply(response -> {
            try {
                // 检查响应是否为null
                if (response == null) {
                    logger.error("Response is null");
                    return "";
                }
                
                // 使用与同步请求相同的方法解析响应
                Object result = parseResponse(response, method);
                // 确保返回非null值
                return result != null ? result : "";
            } catch (Exception e) {
                logger.error("Error parsing async response", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 解析响应
     */
    private Object parseResponse(HttpResponse response, Method method) throws Exception {
        // 检查返回类型是否为Future（包括CompletableFuture）
        Class<?> returnType = method.getReturnType();
        if (Future.class.isAssignableFrom(returnType)) {
            // 获取泛型参数类型
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    // 获取Future的泛型参数类型
                    Type targetType = actualTypeArguments[0];

                    // 检查泛型参数是否为文件下载类型
                    if (targetType instanceof Class &&
                        ((Class<?>) targetType == byte[].class || (Class<?>) targetType == InputStream.class)) {
                        // 如果是文件下载类型，创建一个新的Method对象来模拟非异步方法
                        // 这样handleFileDownload就能正确识别返回类型
                        return handleFileDownload(response, method);
                    } else {
                        // 否则，解析为普通类型
                        return parseResponse(response, targetType);
                    }
                }
            }
            // 如果无法获取泛型参数，返回null
            return null;
        } else {
            // 首先检查是否为文件下载类型（byte[]或InputStream）
            Object fileResult = handleFileDownload(response, method);
            if (fileResult != null) {
                return fileResult;
            }

            if (returnType == void.class || returnType == Void.class) {
                // 如果返回类型是void，直接返回null
                return null;
            } else {
                // 否则，尝试将响应体转换为目标类型
                // 对于泛型类型，需要获取完整的泛型信息
                Type targetType = method.getGenericReturnType();
                return parseResponse(response, targetType);
            }
        }
    }
    
    /**
     * 解析响应
     */
    private Object parseResponse(HttpResponse response, Type targetType) throws Exception {
        // 如果目标类型是void，直接返回null
        if (targetType == void.class || targetType == Void.class) {
            return null;
        }
        
        String body = response.getBody();
        
        // 如果响应体为空，返回null
        if (body == null || body.isEmpty()) {
            return null;
        }
        
        // 从响应头中获取内容类型
        String contentType = "application/json"; // 默认值
        Map<String, String> headers = response.getHeaders();
        if (headers != null && headers.containsKey("Content-Type")) {
            contentType = headers.get("Content-Type");
        }
        
        // 使用解码器解析响应
        return httpClient.getCodecManager().selectDecoder(contentType)
                .decode(body, targetType, contentType);
    }
    
    /**
     * 处理文件下载响应
     */
    private Object handleFileDownload(HttpResponse response, Method method) throws Exception {
        Class<?> returnType = method.getReturnType();
        
        // 检查是否为异步返回类型（CompletableFuture）
        if (Future.class.isAssignableFrom(returnType)) {
            // 获取泛型参数类型
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    // 获取Future的泛型参数类型
                    Type targetType = actualTypeArguments[0];
                    if (targetType instanceof Class) {
                        Class<?> actualType = (Class<?>) targetType;
                        return handleFileDownloadByType(response, actualType);
                    }
                }
            }
        } else {
            // 同步返回类型
            return handleFileDownloadByType(response, returnType);
        }
        
        // 如果不是文件下载类型，返回null让其他方法处理
        return null;
    }
    
    /**
     * 根据实际类型处理文件下载响应
     */
    private Object handleFileDownloadByType(HttpResponse response, Class<?> actualType) throws Exception {
        // 根据返回类型处理响应
        if (actualType == byte[].class) {
            // 返回字节数组
            if (response.getBinaryBody() != null) {
                return response.getBinaryBody();
            } else if (response.getBody() != null) {
                return response.getBody().getBytes();
            } else {
                return new byte[0];
            }
        } else if (actualType == InputStream.class) {
            // 返回输入流
            return response.getBodyAsStream();
        }
        
        // 如果不是文件下载类型，返回null让其他方法处理
        return null;
    }
}