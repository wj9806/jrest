package io.github.wj9806.jrest.spring.annotation;

import io.github.wj9806.jrest.client.annotation.AnnotationParser;
import io.github.wj9806.jrest.client.annotation.DefaultAnnotationParser;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.http.HttpRequest;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring MVC注解解析器
 * 解析Spring MVC风格的注解(@GetMapping, @PostMapping, @PathVariable, @RequestParam等)
 */
public class SpringMvcAnnotationParser implements AnnotationParser {

    private static final Logger logger = LoggerFactory.getLogger(SpringMvcAnnotationParser.class);
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private static DefaultAnnotationParser defaultAnnotationParser = DefaultAnnotationParser.getInstance();
    private Environment environment;

    @Override
    public String parseBaseUrl(Class<?> clazz) {
        // 解析类上的@RestClient注解
        if (clazz.isAnnotationPresent(RestClient.class)) {
            RestClient restClientAnnotation = clazz.getAnnotation(RestClient.class);
            String baseUrl = restClientAnnotation.baseUrl();
            // 解析属性占位符
            if (environment != null && baseUrl != null && !baseUrl.isEmpty()) {
                return environment.resolvePlaceholders(baseUrl);
            }
            return baseUrl;
        }
        return null;
    }
    
    /**
     * 设置Environment对象用于解析属性占位符
     * @param environment Environment对象
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public ClientType parseClientType(Class<?> clazz) {
        // 解析类上的@RestClient注解
        if (clazz.isAnnotationPresent(RestClient.class)) {
            RestClient restClientAnnotation = clazz.getAnnotation(RestClient.class);
            return restClientAnnotation.clientType();
        }
        return ClientType.NATIVE;
    }

    @Override
    public HttpRequest parse(Method method, Object[] args, String baseUrl) {
        // 获取HTTP方法和路径
        HttpMethodInfo httpMethodInfo = getHttpMethodInfo(method);
        if (httpMethodInfo == null) {
            //找不到Spring注解,尝试使用默认注解解析器
            return defaultAnnotationParser.parse(method, args, baseUrl);
        }

        // 获取类级别的RequestMapping路径
        String classPath = getClassRequestMappingPath(method.getDeclaringClass());
        
        // 合并类路径和方法路径
        String fullPath = mergePaths(classPath, httpMethodInfo.path);

        // 构建请求URL
        String requestUrl = buildRequestUrl(method, args, baseUrl, fullPath);

        // 解析参数
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> queryParams = new HashMap<>();
        Map<String, String> cookies = new HashMap<>();
        Object requestBody = null;

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Object arg = args[i];

            for (Annotation annotation : annotations) {
                if (annotation instanceof PathVariable) {
                    // 路径参数已经在buildRequestUrl中处理
                } else if (annotation instanceof RequestParam) {
                    RequestParam requestParam = (RequestParam) annotation;
                    String name = requestParam.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    queryParams.put(name, arg);
                } else if (annotation instanceof RequestBody) {
                    requestBody = arg;
                } else if (annotation instanceof RequestHeader) {
                    RequestHeader requestHeader = (RequestHeader) annotation;
                    String name = requestHeader.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    headers.put(name, arg != null ? arg.toString() : null);
                } else if (annotation instanceof CookieValue) {
                    CookieValue cookieValue = (CookieValue) annotation;
                    String name = cookieValue.value();
                    // 如果没有指定value，则使用参数名称
                    if (name.isEmpty()) {
                        name = method.getParameters()[i].getName();
                    }
                    cookies.put(name, arg != null ? arg.toString() : null);
                }
            }
        }

        // 创建HttpRequest对象
        HttpRequest.Builder builder = new HttpRequest.Builder()
                .url(requestUrl)
                .method(httpMethodInfo.method)
                .headers(headers)
                .queryParams(queryParams)
                .cookies(cookies)
                .body(requestBody);

        logger.debug("Built HttpRequest: {}", builder.build());
        return builder.build();
    }

    /**
     * 获取HTTP方法和路径信息
     */
    private HttpMethodInfo getHttpMethodInfo(Method method) {
        // 先检查RequestMapping注解
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            RequestMethod[] methods = requestMapping.method();
            String[] paths = requestMapping.path();
            return new HttpMethodInfo(
                    methods.length > 0 ? methods[0].name() : "GET",
                    paths.length > 0 ? paths[0] : ""
            );
        }

        // 检查GetMapping注解
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            return new HttpMethodInfo("GET", getMapping.value().length > 0 ? getMapping.value()[0] : "");
        }

        // 检查PostMapping注解
        if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            return new HttpMethodInfo("POST", postMapping.value().length > 0 ? postMapping.value()[0] : "");
        }

        // 检查PutMapping注解
        if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            return new HttpMethodInfo("PUT", putMapping.value().length > 0 ? putMapping.value()[0] : "");
        }

        // 检查DeleteMapping注解
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            return new HttpMethodInfo("DELETE", deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "");
        }

        // 检查PatchMapping注解
        if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
            return new HttpMethodInfo("PATCH", patchMapping.value().length > 0 ? patchMapping.value()[0] : "");
        }

        return null;
    }

    /**
     * 获取类级别的RequestMapping路径
     */
    private String getClassRequestMappingPath(Class<?> clazz) {
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
            if (requestMapping.value().length > 0) {
                return requestMapping.value()[0];
            }
            if (requestMapping.path().length > 0) {
                return requestMapping.path()[0];
            }
        }
        return "";
    }
    
    /**
     * 合并类路径和方法路径
     */
    private String mergePaths(String classPath, String methodPath) {
        StringBuilder fullPath = new StringBuilder();
        
        if (!classPath.isEmpty()) {
            fullPath.append(classPath.startsWith("/") ? classPath : "/" + classPath);
        }
        
        if (!methodPath.isEmpty()) {
            if (methodPath.startsWith("/")) {
                fullPath.append(methodPath);
            } else {
                if (fullPath.length() > 0 && !classPath.endsWith("/")) {
                    fullPath.append("/");
                }
                fullPath.append(methodPath);
            }
        }
        
        return fullPath.toString();
    }
    
    /**
     * 构建请求URL
     */
    private String buildRequestUrl(Method method, Object[] args, String baseUrl, String path) {
        // 替换路径参数
        String processedPath = path;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        
        // 获取参数名称数组
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Object arg = args[i];

            for (Annotation annotation : annotations) {
                if (annotation instanceof PathVariable) {
                    PathVariable pathVariable = (PathVariable) annotation;
                    String paramName = pathVariable.value();
                    
                    // 如果没有指定value，则使用参数名称
                    if (paramName.isEmpty()) {
                        if (parameterNames != null && i < parameterNames.length) {
                            paramName = parameterNames[i];
                        } else {
                            // 作为最后 fallback，使用参数索引
                            paramName = "arg" + i;
                        }
                    }
                    
                    // 确保参数名称两边有大括号
                    String placeholder = "{" + paramName + "}";
                    if (processedPath.contains(placeholder)) {
                        processedPath = processedPath.replace(placeholder, arg != null ? arg.toString() : "");
                    }
                }
            }
        }

        // 构建完整URL
        StringBuilder fullUrl = new StringBuilder(baseUrl);
        
        if (!processedPath.isEmpty()) {
            if (!baseUrl.endsWith("/") && !processedPath.startsWith("/")) {
                fullUrl.append("/");
            } else if (baseUrl.endsWith("/") && processedPath.startsWith("/")) {
                processedPath = processedPath.substring(1);
            }
            fullUrl.append(processedPath);
        }

        String finalUrl = fullUrl.toString();
        logger.debug("Built request URL: {}", finalUrl);
        return finalUrl;
    }

    /**
     * HTTP方法信息内部类
     */
    private static class HttpMethodInfo {
        private String method;
        private String path;

        public HttpMethodInfo(String method, String path) {
            this.method = method;
            this.path = path;
        }
    }
}
