package io.github.wj9806.jrest.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * 原生HttpURLConnection实现
 */
public class NativeHttpClient extends AbstractHttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeHttpClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
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
        
        // 设置请求体
        if (("POST".equals(method) || "PUT".equals(method)) && httpRequest.getBody() != null) {
            String jsonBody = objectMapper.writeValueAsString(httpRequest.getBody());
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        
        logger.debug("Sending {} request to: {}", method, urlBuilder.toString());
        
        return buildHttpResponse(connection);
    }
    
    /**
     * 构建HttpResponse对象
     */
    private HttpResponse buildHttpResponse(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        
        // 读取响应体
        String body = null;
        try (InputStream is = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseBody.append(line);
            }
            body = responseBody.toString();
            
        } catch (IOException e) {
            logger.error("Error reading response body", e);
        }
        
        // 构建响应头
        Map<String, String> headers = new HashMap<>();
        int i = 1; // 从1开始，跳过第0个字段（状态行）
        String headerKey;
        while ((headerKey = connection.getHeaderFieldKey(i)) != null) {
            headers.put(headerKey, connection.getHeaderField(i));
            i++;
        }
        
        logger.debug("Response status code: {}", statusCode);
        logger.debug("Response body: {}", body);
        
        connection.disconnect();
        
        return new HttpResponse(statusCode, body, headers);
    }
}