package io.github.wj9806.jrest.client.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 原生HttpURLConnection实现
 */
public class NativeHttpClient extends AbstractHttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeHttpClient.class);
    private final ExecutorService executorService;
    
    public NativeHttpClient() {
        this(Executors.newFixedThreadPool(10));
    }
    
    public NativeHttpClient(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    @Override
    protected HttpResponse doExchange(HttpRequest httpRequest) throws IOException {
        // 构建带查询参数的URL
        StringBuilder urlBuilder = new StringBuilder(httpRequest.getUrl());
        if (httpRequest.getQueryParams() != null && !httpRequest.getQueryParams().isEmpty()) {
            urlBuilder.append("?");
            Set<Map.Entry<String, Object>> entries = httpRequest.getQueryParams().entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                          .append("=")
                          .append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"))
                          .append("&");
            }
            // 移除最后一个&符号
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        
        URL httpUrl = new URL(urlBuilder.toString());
        HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
        // 设置超时时间
        connection.setConnectTimeout(getConnectTimeout());
        connection.setReadTimeout(getReadTimeout());
        String method = httpRequest.getMethod().toUpperCase();
        connection.setRequestMethod(method);
        
        // 设置输出流（对于需要请求体的方法）
        if ("POST".equals(method) || "PUT".equals(method)) {
            connection.setDoOutput(true);
        }
        
        // 设置请求头
        if (httpRequest.getHeaders() != null && !httpRequest.getHeaders().isEmpty()) {
            httpRequest.getHeaders().forEach(connection::setRequestProperty);
        }
        
        // 设置Cookie
        if (httpRequest.getCookies() != null && !httpRequest.getCookies().isEmpty()) {
            StringBuilder cookieBuilder = new StringBuilder();
            for (Map.Entry<String, String> cookie : httpRequest.getCookies().entrySet()) {
                if (cookieBuilder.length() > 0) {
                    cookieBuilder.append(";");
                }
                cookieBuilder.append(cookie.getKey()).append("=").append(cookie.getValue());
            }
            connection.setRequestProperty("Cookie", cookieBuilder.toString());
        }
        
        // 设置请求体
        if (("POST".equals(method) || "PUT".equals(method))) {
            // 处理multipart/form-data请求
            if (httpRequest.isFormData()) {
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                
                try (OutputStream os = connection.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
                    
                    // 写入文件
                    for (Map.Entry<String, MultipartFile> entry : httpRequest.getMultipartFiles().entrySet()) {
                        String name = entry.getKey();
                        MultipartFile file = entry.getValue();
                        
                        writer.append("--").append(boundary).println();
                        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"").append(file.getOriginalFilename()).append("\"").println();
                        writer.append("Content-Type: ").append(file.getContentType() != null ? file.getContentType() : "application/octet-stream").println();
                        writer.append("Content-Transfer-Encoding: binary").println();
                        writer.println();
                        writer.flush();
                        
                        try (InputStream is = file.getInputStream()) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            os.flush();
                        }
                        
                        writer.println();
                        writer.flush();
                    }
                    
                    // 写入表单字段
                    if (httpRequest.hasFormData()) {
                        for (Map.Entry<String, Object> entry : httpRequest.getFormData().entrySet()) {
                            String name = entry.getKey();
                            Object value = entry.getValue();
                            
                            writer.append("--").append(boundary).println();
                            writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"") .println();
                            writer.println();
                            writer.print(value != null ? value.toString() : "");
                            writer.println();
                            writer.flush();
                        }
                    }
                    
                    // 结束边界
                    writer.append("--").append(boundary).append("--").println();
                    writer.flush();
                    
                } catch (IOException e) {
                    logger.error("Error writing multipart request", e);
                    throw e;
                }
            } 
            // 处理其他类型的请求体
            else if (httpRequest.getBody() != null) {
                if (httpRequest.getBody() instanceof InputStream) {
                    // 处理文件流请求体
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        InputStream is = (InputStream) httpRequest.getBody();
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    // 处理JSON请求体
                    String contentType = "application/json; charset=UTF-8";
                    // 使用编解码器管理器编码请求体
                    try {
                        byte[] encodedBody = getCodecManager().selectEncoder(contentType).encode(httpRequest.getBody(), contentType);
                        connection.setRequestProperty("Content-Type", contentType);

                        try (OutputStream os = connection.getOutputStream()) {
                            os.write(encodedBody);
                        }
                    } catch (Exception e) {
                        logger.error("Error encoding request body", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        
        logger.debug("Sending {} request to: {}", method, urlBuilder);
        
        return buildHttpResponse(connection);
    }
    
    /**
     * 构建HttpResponse对象
     */
    private HttpResponse buildHttpResponse(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        
        // 获取Content-Type
        String contentType = connection.getContentType();
        
        // 构建响应头
        Map<String, String> headers = new HashMap<>();
        int i = 1; // 从1开始，跳过第0个字段（状态行）
        String headerKey;
        while ((headerKey = connection.getHeaderFieldKey(i)) != null) {
            headers.put(headerKey, connection.getHeaderField(i));
            i++;
        }
        
        // 尝试获取相应的流
        InputStream is = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        
        // 如果流为null，返回空响应
        if (is == null) {
            connection.disconnect();
            return new HttpResponse(statusCode, "", headers);
        }
        
        try {
            // 判断是否为二进制数据（非文本类型）
            if (contentType != null && 
                (contentType.startsWith("application/octet-stream") || 
                 contentType.startsWith("image/") || 
                 contentType.startsWith("audio/") || 
                 contentType.startsWith("video/") || 
                 contentType.endsWith("pdf") || 
                 contentType.endsWith("zip") || 
                 contentType.endsWith("rar"))) {
                
                // 读取二进制数据
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] binaryBody = baos.toByteArray();
                
                logger.debug("Response status code: {}", statusCode);
                logger.debug("Response content type: {}", contentType);
                logger.debug("Response body size: {} bytes", binaryBody.length);
                
                connection.disconnect();
                return new HttpResponse(statusCode, binaryBody, headers);
            } else {
                // 读取文本数据
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBody.append(line);
                    }
                    String body = responseBody.toString();
                    
                    logger.debug("Response status code: {}", statusCode);
                    logger.debug("Response body: {}", body);
                    
                    connection.disconnect();
                    return new HttpResponse(statusCode, body, headers);
                }
            }
        } catch (Exception e) {
            logger.error("Error reading response body", e);
            connection.disconnect();
            return new HttpResponse(statusCode, "", headers);
        }
    }
    
    @Override
    protected CompletableFuture<HttpResponse> doExchangeAsync(HttpRequest httpRequest) {
        // 使用线程池执行异步请求
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                // 调用doExchange方法直接执行请求，不经过拦截器和重试逻辑（由父类executeAsync处理）
                HttpResponse response = doExchange(httpRequest);
                future.complete(response);
            } catch (IOException e) {
                logger.error("Error executing async request", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}