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
    private Map<String, String> cookies;
    private Object body;
    private Map<String, MultipartFile> multipartFiles;
    private Map<String, Object> formData;
    private boolean isFormData;

    /**
     * 检查是否为form-data请求
     * @return 是否为form-data请求
     */
    public boolean isFormData() {
        return isFormData || hasMultipartFiles() || (formData != null && !formData.isEmpty());
    }

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.cookies = builder.cookies;
        this.body = builder.body;
        this.multipartFiles = builder.multipartFiles;
        this.formData = builder.formData;
        this.isFormData = builder.isFormData;
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
     * 获取Cookie
     * @return Cookie
     */
    public Map<String, String> getCookies() {
        return cookies;
    }
    
    /**
     * 获取multipart文件
     * @return multipart文件
     */
    public Map<String, MultipartFile> getMultipartFiles() {
        return multipartFiles;
    }
    
    /**
     * 是否包含multipart文件
     * @return 是否包含multipart文件
     */
    public boolean hasMultipartFiles() {
        return multipartFiles != null && !multipartFiles.isEmpty();
    }
    
    /**
     * 获取表单数据
     * @return 表单数据
     */
    public Map<String, Object> getFormData() {
        return formData;
    }
    
    /**
     * 是否包含表单数据
     * @return 是否包含表单数据
     */
    public boolean hasFormData() {
        return formData != null && !formData.isEmpty();
    }

    /**
     * HttpRequest构建器
     */
    public static class Builder {
        private String url;
        private String method;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, Object> queryParams = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
        private Object body;
        private Map<String, MultipartFile> multipartFiles = new HashMap<>();
        private Map<String, Object> formData = new HashMap<>();
        private boolean isFormData = false;
        
        /**
         * 设置为form-data请求
         * @return Builder实例
         */
        public Builder formData() {
            this.isFormData = true;
            return this;
        }
        
        /**
         * 添加表单字段
         * @param name 字段名
         * @param value 字段值
         * @return Builder实例
         */
        public Builder formField(String name, Object value) {
            this.formData.put(name, value);
            this.isFormData = true;
            return this;
        }
        
        /**
         * 设置表单数据
         * @param formData 表单数据
         * @return Builder实例
         */
        public Builder formData(Map<String, Object> formData) {
            this.formData.putAll(formData);
            this.isFormData = true;
            return this;
        }

        public Builder() {}
        
        /**
         * 从现有HttpRequest创建Builder实例
         * @param request 现有HttpRequest实例
         * @return Builder实例
         */
        public static Builder newBuilder(HttpRequest request) {
            Builder builder = new Builder();
            builder.url(request.getUrl());
            builder.method(request.getMethod());
            builder.headers(new HashMap<>(request.getHeaders()));
            builder.queryParams(new HashMap<>(request.getQueryParams()));
            builder.cookies(new HashMap<>(request.getCookies()));
            builder.body(request.getBody());
            builder.multipartFiles(new HashMap<>(request.getMultipartFiles()));
            builder.formData(new HashMap<>(request.getFormData()));
            builder.isFormData = request.isFormData();
            return builder;
        }

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
         * 添加multipart文件
         * @param name 参数名称
         * @param file 文件
         * @return Builder实例
         */
        public Builder addMultipartFile(String name, MultipartFile file) {
            this.multipartFiles.put(name, file);
            return this;
        }
        
        /**
         * 设置multipart文件
         * @param multipartFiles multipart文件
         * @return Builder实例
         */
        public Builder multipartFiles(Map<String, MultipartFile> multipartFiles) {
            this.multipartFiles.putAll(multipartFiles);
            return this;
        }
        
        /**
         * 添加Cookie
         * @param name Cookie名称
         * @param value Cookie值
         * @return Builder实例
         */
        public Builder cookie(String name, String value) {
            this.cookies.put(name, value);
            return this;
        }
        
        /**
         * 设置Cookie
         * @param cookies Cookie
         * @return Builder实例
         */
        public Builder cookies(Map<String, String> cookies) {
            this.cookies.putAll(cookies);
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