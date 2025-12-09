package io.github.wj9806.jrest.client.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * HTTP响应封装
 */
public class HttpResponse {
    private int statusCode;
    private String body;
    private byte[] binaryBody;
    private Map<String, String> headers;

    public HttpResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public HttpResponse(int statusCode, byte[] binaryBody, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.binaryBody = binaryBody;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public byte[] getBinaryBody() {
        return binaryBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBodyAsStream() {
        if (binaryBody != null) {
            return new ByteArrayInputStream(binaryBody);
        } else if (body != null) {
            return new ByteArrayInputStream(body.getBytes());
        } else {
            return new ByteArrayInputStream(new byte[0]);
        }
    }
}
