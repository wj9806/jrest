package io.github.wj9806.jrest.client.http.decode;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 基于Jackson的JSON解码器实现
 */
public class JacksonDecoder implements Decoder {
    
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    private final ObjectMapper objectMapper;
    
    /**
     * 默认构造函数，使用默认的ObjectMapper
     */
    public JacksonDecoder() {
        this.objectMapper = DEFAULT_OBJECT_MAPPER;
    }
    
    /**
     * 构造函数，使用自定义的ObjectMapper
     * 
     * @param objectMapper 自定义的ObjectMapper
     */
    public JacksonDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public <T> T decode(byte[] bytes, Type targetType, String contentType) throws Exception {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        
        String content = new String(bytes, StandardCharsets.UTF_8);
        return decode(content, targetType, contentType);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(String content, Type targetType, String contentType) throws Exception {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        // 如果目标类型是String，直接返回
        if (targetType == String.class) {
            return (T) content;
        }
        
        // 否则使用Jackson进行JSON反序列化

        return objectMapper.readValue(content, new TypeReference<T>() {
            @Override
            public Type getType() {
                return targetType;
            }
        });
    }
    
    @Override
    public boolean supports(String contentType) {
        return contentType != null && (contentType.equals("application/json") || contentType.startsWith("application/json;"));
    }
}
