package io.github.wj9806.jrest.client.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求封装
 */
public class HttpRequest {
    private String url;
    private String method;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private Object body;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.body = builder.body;
    }

    /**
     * 获取请求URL
     * @return 请求URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取HTTP方法
     * @return HTTP方法
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取请求头
     * @return 请求头
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 获取查询参数
     * @return 查询参数
     */
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    /**
     * 获取请求体
     * @return 请求体
     */
    public Object getBody() {
        return body;
    }

    /**
     * HttpRequest构建器
     */
    public static class Builder {
        private String url;
        private String method;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, Object> queryParams = new HashMap<>();
        private Object body;

        public Builder() {}

        /**
         * 设置请求URL
         * @param url 请求URL
         * @return Builder实例
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * 设置HTTP方法
         * @param method HTTP方法
         * @return Builder实例
         */
        public Builder method(String method) {
            this.method = method;
            return this;
        }

        /**
         * 设置GET方法
         * @return Builder实例
         */
        public Builder get() {
            this.method = "GET";
            return this;
        }

        /**
         * 设置POST方法
         * @return Builder实例
         */
        public Builder post() {
            this.method = "POST";
            return this;
        }

        /**
         * 设置PUT方法
         * @return Builder实例
         */
        public Builder put() {
            this.method = "PUT";
            return this;
        }

        /**
         * 设置DELETE方法
         * @return Builder实例
         */
        public Builder delete() {
            this.method = "DELETE";
            return this;
        }

        /**
         * 添加请求头
         * @param name 头名称
         * @param value 头值
         * @return Builder实例
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * 设置请求头
         * @param headers 请求头
         * @return Builder实例
         */
        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加查询参数
         * @param name 参数名
         * @param value 参数值
         * @return Builder实例
         */
        public Builder queryParam(String name, Object value) {
            this.queryParams.put(name, value);
            return this;
        }

        /**
         * 设置查询参数
         * @param queryParams 查询参数
         * @return Builder实例
         */
        public Builder queryParams(Map<String, Object> queryParams) {
            this.queryParams.putAll(queryParams);
            return this;
        }

        /**
         * 设置请求体
         * @param body 请求体
         * @return Builder实例
         */
        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        /**
         * 构建HttpRequest实例
         * @return HttpRequest实例
         */
        public HttpRequest build() {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL must not be null or empty");
            }
            if (method == null || method.isEmpty()) {
                throw new IllegalArgumentException("HTTP method must not be null or empty");
            }
            return new HttpRequest(this);
        }
    }
}