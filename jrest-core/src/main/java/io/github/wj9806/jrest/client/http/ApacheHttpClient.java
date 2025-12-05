package io.github.wj9806.jrest.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
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
                    setRequestBody(httpPost, httpRequest.getBody());
                    requestBase = httpPost;
                    break;
                case "PUT":
                    HttpPut httpPut = new HttpPut(uriBuilder.build());
                    // 设置PUT请求体
                    setRequestBody(httpPut, httpRequest.getBody());
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
            String jsonBody = objectMapper.writeValueAsString(body);
            request.setEntity(new StringEntity(jsonBody, "UTF-8"));
            request.addHeader("Content-Type", "application/json");
        }
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