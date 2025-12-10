package io.github.wj9806.jrest.client.http.encode;

/**
 * HTTP请求体编码器接口
 * 用于将Java对象编码为HTTP请求体内容
 */
public interface Encoder {
    
    /**
     * 将Java对象编码为字节数组
     * 
     * @param object 要编码的对象
     * @param contentType 内容类型
     * @return 编码后的字节数组
     * @throws Exception 编码过程中的异常
     */
    byte[] encode(Object object, String contentType) throws Exception;
    
    /**
     * 判断是否支持指定的内容类型
     * 
     * @param contentType 内容类型
     * @return 是否支持
     */
    boolean supports(String contentType);
}
