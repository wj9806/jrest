package io.github.wj9806.jrest.client.http.decode;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;

/**
 * XML解码器，将XML格式的响应体解码为Java对象
 */
public class XmlDecoder implements Decoder {
    
    private static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * 将XML字节数组解码为Java对象
     * 
     * @param bytes       XML字节数组
     * @param targetType  目标类型
     * @param contentType 内容类型
     * @param <T>         目标类型泛型
     * @return 解码后的Java对象
     * @throws Exception 解码过程中的异常
     */
    @Override
    public <T> T decode(byte[] bytes, Type targetType, String contentType) throws Exception {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        
        return decodeBytes(bytes, targetType);
    }

    /**
     * 将XML字符串解码为Java对象
     * 
     * @param content     XML字符串
     * @param targetType  目标类型
     * @param contentType 内容类型
     * @param <T>         目标类型泛型
     * @return 解码后的Java对象
     * @throws Exception 解码过程中的异常
     */
    @Override
    public <T> T decode(String content, Type targetType, String contentType) throws Exception {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        return decodeBytes(content.getBytes(StandardCharsets.UTF_8), targetType);
    }

    /**
     * 判断是否支持指定的内容类型
     * 
     * @param contentType 内容类型
     * @return 是否支持XML内容类型
     */
    @Override
    public boolean supports(String contentType) {
        return contentType != null && (contentType.startsWith("application/xml") || 
               contentType.startsWith("text/xml") || 
               contentType.endsWith("+xml"));
    }

    /**
     * 实际执行解码操作
     * 
     * @param bytes      字节数组
     * @param targetType 目标类型
     * @param <T>        目标类型泛型
     * @return 解码后的对象
     * @throws Exception 解码异常
     */
    @SuppressWarnings("unchecked")
    private <T> T decodeBytes(byte[] bytes, Type targetType) throws Exception {
        if (targetType instanceof Class) {
            return xmlMapper.readValue(bytes, (Class<T>) targetType);
        } else if (targetType instanceof ParameterizedType) {
            return xmlMapper.readValue(bytes, xmlMapper.getTypeFactory().constructType(targetType));
        } else {
            throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
    }
}