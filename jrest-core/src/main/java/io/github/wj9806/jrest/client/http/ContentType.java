package io.github.wj9806.jrest.client.http;

/**
 * Content-Type枚举
 */
public enum ContentType {
    /**
     * JSON格式
     */
    APPLICATION_JSON("application/json"),
    
    /**
     * XML格式
     */
    APPLICATION_XML("application/xml"),
    
    /**
     * Form表单格式
     */
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    
    /**
     * 多部分表单格式
     */
    MULTIPART_FORM_DATA("multipart/form-data"),
    
    /**
     * 文本格式
     */
    TEXT_PLAIN("text/plain"),
    
    /**
     * HTML格式
     */
    TEXT_HTML("text/html"),
    
    /**
     * 二进制流格式
     */
    APPLICATION_OCTET_STREAM("application/octet-stream");
    
    private final String value;
    
    ContentType(String value) {
        this.value = value;
    }
    
    /**
     * 获取Content-Type的字符串值
     * @return Content-Type字符串
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}