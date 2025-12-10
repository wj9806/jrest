package io.github.wj9806.jrest.client.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Apache HttpClient实现
 */
public class ApacheHttpClient extends AbstractHttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpClient.class);
    private final CloseableHttpClient httpClient;
    private final CloseableHttpAsyncClient asyncHttpClient;

    public ApacheHttpClient() {
        this.httpClient = HttpClients.createDefault();
        this.asyncHttpClient = HttpAsyncClients.createDefault();
        this.asyncHttpClient.start();
    }
    
    public ApacheHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        this.asyncHttpClient = HttpAsyncClients.createDefault();
        this.asyncHttpClient.start();
    }
    
    public ApacheHttpClient(CloseableHttpClient httpClient, CloseableHttpAsyncClient asyncHttpClient) {
        this.httpClient = httpClient;
        this.asyncHttpClient = asyncHttpClient;
        if (!asyncHttpClient.isRunning()) {
            this.asyncHttpClient.start();
        }
    }
    
    /**
     * 构建Apache HttpRequest对象
     */
    private HttpRequestBase buildHttpRequest(HttpRequest httpRequest) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(httpRequest.getUrl());
        
        // 添加查询参数
        if (httpRequest.getQueryParams() != null && !httpRequest.getQueryParams().isEmpty()) {
            Set<Map.Entry<String, Object>> entries = httpRequest.getQueryParams().entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        
        // 根据HTTP方法创建相应的请求对象
        HttpRequestBase requestBase;
        switch (httpRequest.getMethod().toUpperCase()) {
            case "GET":
                requestBase = new HttpGet(uriBuilder.build());
                break;
            case "POST":
                HttpPost httpPost = new HttpPost(uriBuilder.build());
                // 设置POST请求体
                if (httpRequest.isFormData()) {
                    setMultipartRequestBody(httpPost, httpRequest.getBody(), httpRequest.getFormData(), httpRequest.getMultipartFiles());
                } else {
                    setRequestBody(httpPost, httpRequest.getBody());
                }
                requestBase = httpPost;
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uriBuilder.build());
                // 设置PUT请求体
                if (httpRequest.isFormData()) {
                    setMultipartRequestBody(httpPut, httpRequest.getBody(), httpRequest.getFormData(), httpRequest.getMultipartFiles());
                } else {
                    setRequestBody(httpPut, httpRequest.getBody());
                }
                requestBase = httpPut;
                break;
            case "DELETE":
                requestBase = new HttpDelete(uriBuilder.build());
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + httpRequest.getMethod());
        }
        
        // 添加请求头
        if (httpRequest.getHeaders() != null && !httpRequest.getHeaders().isEmpty()) {
            httpRequest.getHeaders().forEach(requestBase::addHeader);
        }
        
        // 添加Cookie
        if (httpRequest.getCookies() != null && !httpRequest.getCookies().isEmpty()) {
            StringBuilder cookieBuilder = new StringBuilder();
            for (Map.Entry<String, String> cookie : httpRequest.getCookies().entrySet()) {
                if (cookieBuilder.length() > 0) {
                    cookieBuilder.append(";").append(" ");
                }
                cookieBuilder.append(cookie.getKey()).append("=").append(cookie.getValue());
            }
            requestBase.addHeader("Cookie", cookieBuilder.toString());
        }
        
        return requestBase;
    }
    
    @Override
    protected HttpResponse doExchange(HttpRequest httpRequest) throws IOException {
        try {
            HttpRequestBase requestBase = buildHttpRequest(httpRequest);
            
            logger.debug("Sending {} request to: {}", httpRequest.getMethod(), requestBase.getURI());
            
            try (CloseableHttpResponse response = httpClient.execute(requestBase)) {
                return buildHttpResponse(response);
            }
            
        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax: {}", httpRequest.getUrl(), e);
            throw new IOException("Invalid URI syntax", e);
        } catch (Exception e) {
            logger.error("Error building or executing request", e);
            throw new IOException("Error processing request", e);
        }
    }
    
    /**
     * 设置请求体
     */
    private void setRequestBody(HttpEntityEnclosingRequestBase request, Object body) throws Exception {
        if (body != null) {
            // 获取内容类型
            String contentType = "application/json";
            if (request.getFirstHeader("Content-Type") != null) {
                contentType = request.getFirstHeader("Content-Type").getValue();
            }
            
            // 使用编码器编码请求体
            byte[] bytes = getCodecManager().selectEncoder(contentType).encode(body, contentType);
            request.setEntity(new StringEntity(new String(bytes, StandardCharsets.UTF_8), "UTF-8"));
            
            // 只有当请求没有设置Content-Type时，才添加默认的JSON Content-Type
            if (request.getFirstHeader("Content-Type") == null) {
                request.addHeader("Content-Type", contentType);
            }
        }
    }

    /**
     * 设置multipart请求体
     */
    private void setMultipartRequestBody(HttpEntityEnclosingRequestBase request, Object body, Map<String, Object> formData, Map<String, MultipartFile> multipartFiles) throws IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // 添加表单字段
        if (formData != null && !formData.isEmpty()) {
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    builder.addTextBody(name, value.toString(), ContentType.TEXT_PLAIN);
                }
            }
        }

        // 添加文件
        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            for (Map.Entry<String, MultipartFile> entry : multipartFiles.entrySet()) {
                String name = entry.getKey();
                MultipartFile file = entry.getValue();

                ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
                if (file.getContentType() != null) {
                    try {
                        contentType = ContentType.parse(file.getContentType());
                    } catch (Exception e) {
                        logger.debug("Invalid content type: {}", file.getContentType(), e);
                    }
                }

                // 不使用try-with-resources，让MultipartEntityBuilder自己管理InputStream的关闭
                InputStream is = file.getInputStream();
                builder.addBinaryBody(
                    name,
                    is,
                    contentType,
                    file.getOriginalFilename()
                );
            }
        }

        // 设置实体
        request.setEntity(builder.build());
    }
    
    @Override
    protected CompletableFuture<HttpResponse> doExchangeAsync(HttpRequest httpRequest) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        
        try {
            HttpRequestBase requestBase = buildHttpRequest(httpRequest);
            
            logger.debug("Sending async {} request to: {}", httpRequest.getMethod(), requestBase.getURI());
            
            // 异步执行请求
            asyncHttpClient.execute(requestBase, new FutureCallback<org.apache.http.HttpResponse>() {
                @Override
                public void completed(org.apache.http.HttpResponse response) {
                    try {
                        HttpResponse httpResponse = buildHttpResponse(response);
                        future.complete(httpResponse);
                    } catch (IOException e) {
                        future.completeExceptionally(e);
                    }
                }
                
                @Override
                public void failed(Exception ex) {
                    future.completeExceptionally(ex);
                }
                
                @Override
                public void cancelled() {
                    future.cancel(true);
                }
            });
            
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 从org.apache.http.HttpResponse构建自定义HttpResponse
     */
    private HttpResponse buildHttpResponse(org.apache.http.HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String body = entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
        
        // 构建响应头
        Map<String, String> headers = new HashMap<>();
        for (Header header : response.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        
        logger.debug("Response status code: {}", statusCode);
        logger.debug("Response body: {}", body);
        
        return new HttpResponse(statusCode, body, headers);
    }
}