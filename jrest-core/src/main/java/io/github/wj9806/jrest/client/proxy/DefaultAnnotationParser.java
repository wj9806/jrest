package io.github.wj9806.jrest.client.proxy;

import io.github.wj9806.jrest.client.http.MultipartFile;
import io.github.wj9806.jrest.client.http.DefaultMultipartFile;
import io.github.wj9806.jrest.client.annotation.*;
import io.github.wj9806.jrest.client.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认REST客户端注解解析器实现（单例模式）
 */
public class DefaultAnnotationParser implements AnnotationParser {
    
    // 单例实例
    private static final DefaultAnnotationParser INSTANCE = new DefaultAnnotationParser();
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultAnnotationParser.class);
    
    // 私有构造函数，防止外部实例化
    private DefaultAnnotationParser() {
    }
    
    /**
     * 获取单例实例
     * 
     * @return DefaultAnnotationParser单例
     */
    public static DefaultAnnotationParser getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String parseBaseUrl(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(RestClient.class)) {
            throw new IllegalArgumentException("Interface must be annotated with @RestClient");
        }
        RestClient restClientAnnotation = clazz.getAnnotation(RestClient.class);
        return restClientAnnotation.baseUrl();
    }
    
    @Override
    public ClientType parseClientType(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(RestClient.class)) {
            throw new IllegalArgumentException("Interface must be annotated with @RestClient");
        }
        RestClient restClientAnnotation = clazz.getAnnotation(RestClient.class);
        return restClientAnnotation.clientType();
    }
    
    @Override
    public HttpRequest parse(Method method, Object[] args, String baseUrl) {
        // 获取HTTP方法
        String httpMethod = getHttpMethod(method);
        if (httpMethod == null) {
            throw new IllegalArgumentException("Method must be annotated with HTTP method annotation");
        }
        
        // 构建请求URL
        String requestUrl = buildRequestUrl(method, args, baseUrl);
        
        // 解析参数
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> queryParams = new HashMap<>();
        Map<String, String> cookies = new HashMap<>();
        Object requestBody = null;
        Map<String, MultipartFile> multipartFiles = new HashMap<>();
        Map<String, Object> formData = new HashMap<>();
        boolean hasFormData = false;
        
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Object arg = args[i];
            
            for (Annotation annotation : annotations) {
                if (annotation instanceof PathParam) {
                    // 路径参数已经在buildRequestUrl中处理
                } else if (annotation instanceof QueryParam) {
                    QueryParam queryParam = (QueryParam) annotation;
                    String name = queryParam.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    queryParams.put(name, arg);
                } else if (annotation instanceof RequestBody) {
                    requestBody = arg;
                } else if (annotation instanceof Header) {
                    Header header = (Header) annotation;
                    String name = header.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    headers.put(name, arg != null ? arg.toString() : null);
                } else if (annotation instanceof Cookie) {
                    Cookie cookie = (Cookie) annotation;
                    String name = cookie.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    cookies.put(name, arg != null ? arg.toString() : null);
                } else if (annotation instanceof RequestPart) {
                    RequestPart requestPart = (RequestPart) annotation;
                    String name = requestPart.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    if (arg instanceof MultipartFile) {
                        multipartFiles.put(name, (MultipartFile) arg);
                        hasFormData = true;
                    } else if (arg instanceof java.io.File) {
                        // 支持File类型参数，自动转换为MultipartFile
                        multipartFiles.put(name, new DefaultMultipartFile(name, (java.io.File) arg));
                        hasFormData = true;
                    } else {
                        throw new IllegalArgumentException("RequestPart parameter must be of type MultipartFile or File");
                    }
                } else if (annotation instanceof FormField) {
                    FormField formField = (FormField) annotation;
                    String name = formField.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    formData.put(name, arg);
                    hasFormData = true;
                }
            }
        }
        
        // 创建HttpRequest对象
        HttpRequest.Builder builder = new HttpRequest.Builder()
                .url(requestUrl)
                .method(httpMethod)
                .headers(headers)
                .queryParams(queryParams)
                .cookies(cookies);
                
        // 设置请求体和表单数据
        if (hasFormData) {
            builder.formData(formData);
            if (!multipartFiles.isEmpty()) {
                builder.multipartFiles(multipartFiles);
            }
            // 如果有body且不是InputStream类型，将其转换为form字段
            if (requestBody != null && !(requestBody instanceof java.io.InputStream)) {
                builder.formField("body", requestBody);
            }
        } else if (requestBody != null) {
            builder.body(requestBody);
        }
        
        // 添加multipart文件
        if (!multipartFiles.isEmpty() && !hasFormData) {
            builder.multipartFiles(multipartFiles);
        }
        
        return builder.build();
    }
    
    /**
     * 获取HTTP方法
     */
    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GET.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(POST.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PUT.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DELETE.class)) {
            return "DELETE";
        }
        return null;
    }
    
    /**
     * 构建请求URL
     */
    private String buildRequestUrl(Method method, Object[] args, String baseUrl) {
        String path = "";
        
        // 获取路径
        if (method.isAnnotationPresent(GET.class)) {
            path = method.getAnnotation(GET.class).value();
        } else if (method.isAnnotationPresent(POST.class)) {
            path = method.getAnnotation(POST.class).value();
        } else if (method.isAnnotationPresent(PUT.class)) {
            path = method.getAnnotation(PUT.class).value();
        } else if (method.isAnnotationPresent(DELETE.class)) {
            path = method.getAnnotation(DELETE.class).value();
        }
        
        // 替换路径参数
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Object arg = args[i];
            
            for (Annotation annotation : annotations) {
                if (annotation instanceof PathParam) {
                    PathParam pathParam = (PathParam) annotation;
                    String paramName = pathParam.value();
                    path = path.replace("{" + paramName + "}", arg.toString());
                }
            }
        }
        
        // 构建完整URL
        String fullUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        if (!path.isEmpty() && !path.startsWith("/")) {
            fullUrl += path;
        } else {
            fullUrl += path.substring(1);
        }

        logger.debug("Built request URL: {}", fullUrl);
        return fullUrl;
    }
}
