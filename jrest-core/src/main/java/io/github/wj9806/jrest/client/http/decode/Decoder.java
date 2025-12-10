package io.github.wj9806.jrest.client.http.decode;

import java.lang.reflect.Type;

/**
 * HTTP响应体解码器接口
 * 用于将HTTP响应体内容解码为Java对象
 */
public interface Decoder {
    
    /**
     * 将HTTP响应体字节数组解码为Java对象
     * 
     * @param bytes 响应体字节数组
     * @param targetType 目标类型
     * @param contentType 内容类型
     * @param <T> 目标类型泛型
     * @return 解码后的Java对象
     * @throws Exception 解码过程中的异常
     */
    <T> T decode(byte[] bytes, Type targetType, String contentType) throws Exception;
    
    /**
     * 将HTTP响应体字符串解码为Java对象
     * 
     * @param content 响应体字符串
     * @param targetType 目标类型
     * @param contentType 内容类型
     * @param <T> 目标类型泛型
     * @return 解码后的Java对象
     * @throws Exception 解码过程中的异常
     */
    <T> T decode(String content, Type targetType, String contentType) throws Exception;
    
    /**
     * 判断是否支持指定的内容类型
     * 
     * @param contentType 内容类型
     * @return 是否支持
     */
    boolean supports(String contentType);
}
