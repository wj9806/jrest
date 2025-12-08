package io.github.wj9806.jrest.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Apache HttpClient实现
 */
public class ApacheHttpClient extends AbstractHttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient;

    public ApacheHttpClient() {
        this.httpClient = HttpClients.createDefault();
    }
    
    public ApacheHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    @Override
    protected HttpResponse doExchange(HttpRequest httpRequest) throws IOException {
        try {
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
                        cookieBuilder.append(";");
                    }
                    cookieBuilder.append(cookie.getKey()).append("=").append(cookie.getValue());
                }
                requestBase.addHeader("Cookie", cookieBuilder.toString());
            }
            
            logger.debug("Sending {} request to: {}", httpRequest.getMethod(), requestBase.getURI());
            CloseableHttpResponse response = httpClient.execute(requestBase);
            
            return buildHttpResponse(response);
            
        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax: {}", httpRequest.getUrl(), e);
            throw new IOException("Invalid URI syntax", e);
        }
    }
    
    /**
     * 设置请求体
     */
    private void setRequestBody(HttpEntityEnclosingRequestBase request, Object body) throws IOException {
        if (body != null) {
            // 处理JSON请求体
            String jsonBody = objectMapper.writeValueAsString(body);
            request.setEntity(new StringEntity(jsonBody, "UTF-8"));
            request.addHeader("Content-Type", "application/json");
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
    
    /**
     * 构建HttpResponse对象
     */
    private HttpResponse buildHttpResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String body = entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
        
        // 构建响应头
        Map<String, String> headers = new HashMap<>();
        for (org.apache.http.Header header : response.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        
        logger.debug("Response status code: {}", statusCode);
        logger.debug("Response body: {}", body);
        
        response.close();
        
        return new HttpResponse(statusCode, body, headers);
    }
}