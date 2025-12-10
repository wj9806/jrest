package io.github.wj9806.jrest.client.http.encode;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/**
 * 基于Jackson的JSON编码器实现
 */
public class JacksonEncoder implements Encoder {
    
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    private final ObjectMapper objectMapper;
    
    /**
     * 默认构造函数，使用默认的ObjectMapper
     */
    public JacksonEncoder() {
        this.objectMapper = DEFAULT_OBJECT_MAPPER;
    }
    
    /**
     * 构造函数，使用自定义的ObjectMapper
     * 
     * @param objectMapper 自定义的ObjectMapper
     */
    public JacksonEncoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public byte[] encode(Object object, String contentType) throws Exception {
        if (object == null) {
            return new byte[0];
        }
        
        // 如果是String类型，直接返回字节数组
        if (object instanceof String) {
            return ((String) object).getBytes(StandardCharsets.UTF_8);
        }
        
        // 否则使用Jackson进行JSON序列化
        return objectMapper.writeValueAsBytes(object);
    }
    
    @Override
    public boolean supports(String contentType) {
        return contentType != null && (contentType.equals("application/json") || contentType.startsWith("application/json;"));
    }
}
