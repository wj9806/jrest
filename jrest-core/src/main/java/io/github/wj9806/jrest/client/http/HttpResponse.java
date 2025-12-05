package io.github.wj9806.jrest.client.http;

import java.util.Map;

/**
 * HTTP响应封装
 */
public class HttpResponse {
    private int statusCode;
    private String body;
    private Map<String, String> headers;

    public HttpResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
