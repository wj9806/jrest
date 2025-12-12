package io.github.wj9806.jrest.client.http;

import io.github.wj9806.jrest.client.http.decode.Decoder;
import io.github.wj9806.jrest.client.http.decode.JacksonDecoder;
import io.github.wj9806.jrest.client.http.decode.XmlDecoder;
import io.github.wj9806.jrest.client.http.encode.Encoder;
import io.github.wj9806.jrest.client.http.encode.JacksonEncoder;
import io.github.wj9806.jrest.client.http.encode.XmlEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 编解码器管理器，用于管理和选择合适的编解码器
 */
public class CodecManager {
    
    private final List<Encoder> encoders;
    private final List<Decoder> decoders;
    
    /**
     * 默认构造函数，添加默认的Jackson编解码器
     */
    public CodecManager() {
        this.encoders = new ArrayList<>();
        this.decoders = new ArrayList<>();
        
        // 添加默认的Jackson编解码器
        addEncoder(new JacksonEncoder());
        addEncoder(new XmlEncoder());
        addDecoder(new JacksonDecoder());
        addDecoder(new XmlDecoder());
    }
    
    /**
     * 构造函数，使用自定义的编解码器列表
     * 
     * @param encoders 编码器列表
     * @param decoders 解码器列表
     */
    public CodecManager(List<Encoder> encoders, List<Decoder> decoders) {
        this.encoders = new ArrayList<>(encoders);
        this.decoders = new ArrayList<>(decoders);
    }
    
    /**
     * 添加编码器
     * 
     * @param encoder 编码器
     */
    public void addEncoder(Encoder encoder) {
        encoders.add(encoder);
    }
    
    /**
     * 添加解码器
     * 
     * @param decoder 解码器
     */
    public void addDecoder(Decoder decoder) {
        decoders.add(decoder);
    }
    
    /**
     * 获取所有编码器
     * 
     * @return 编码器列表
     */
    public List<Encoder> getEncoders() {
        return Collections.unmodifiableList(encoders);
    }
    
    /**
     * 获取所有解码器
     * 
     * @return 解码器列表
     */
    public List<Decoder> getDecoders() {
        return Collections.unmodifiableList(decoders);
    }
    
    /**
     * 内容类型匹配工具方法，支持通配符
     * 
     * @param pattern 匹配模式，支持通配符如application/*
     * @param contentType 实际内容类型
     * @return 是否匹配
     */
    private boolean matchesContentType(String pattern, String contentType) {
        if (pattern == null || contentType == null) {
            return false;
        }
        
        // 移除参数部分（如;charset=utf-8）
        String basePattern = pattern.split(";").length > 0 ? pattern.split(";" )[0].trim() : pattern.trim();
        String baseContentType = contentType.split(";").length > 0 ? contentType.split(";" )[0].trim() : contentType.trim();
        
        // 完全匹配
        if (basePattern.equals(baseContentType)) {
            return true;
        }
        
        // 通配符匹配（如application/*）
        if (basePattern.endsWith("/*")) {
            String type = basePattern.substring(0, basePattern.length() - 2);
            return baseContentType.startsWith(type + "/");
        }
        
        // 子类型通配符匹配（如*/*）
        if (basePattern.equals("*/*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 为指定的内容类型选择合适的编码器
     * 
     * @param contentType 内容类型
     * @return 合适的编码器，如果没有找到则返回null
     */
    public Encoder selectEncoder(String contentType) {
        Encoder defaultEncoder = encoders.isEmpty() ? null : encoders.get(0);
        if (contentType == null) {
            return defaultEncoder;
        }
        
        // 优先选择完全匹配的编码器
        for (Encoder encoder : encoders) {
            if (encoder.supports(contentType)) {
                return encoder;
            }
        }
        
        // 如果没有完全匹配，尝试使用通配符匹配
        for (Encoder encoder : encoders) {
            // 检查编码器支持的内容类型与实际内容类型是否匹配
            // 这里我们需要获取编码器支持的所有内容类型，所以需要修改编码器接口
            // 作为临时解决方案，我们可以检查编码器的类名来确定其支持的内容类型
            String encoderType = encoder.getClass().getSimpleName().toLowerCase();
            if ((encoderType.contains("json") && matchesContentType("application/json", contentType)) ||
                (encoderType.contains("xml") && matchesContentType("application/xml", contentType))) {
                return encoder;
            }
        }
        
        // 如果没有找到匹配的编码器，返回第一个编码器（作为默认）
        return defaultEncoder;
    }
    
    /**
     * 为指定的内容类型选择合适的解码器
     * 
     * @param contentType 内容类型
     * @return 合适的解码器，如果没有找到则返回null
     */
    public Decoder selectDecoder(String contentType) {
        Decoder defaultDecoder = decoders.isEmpty() ? null : decoders.get(0);
        if (contentType == null) {
            return defaultDecoder;
        }
        
        // 优先选择完全匹配的解码器
        for (Decoder decoder : decoders) {
            if (decoder.supports(contentType)) {
                return decoder;
            }
        }
        
        // 如果没有完全匹配，尝试使用通配符匹配
        for (Decoder decoder : decoders) {
            // 检查解码器支持的内容类型与实际内容类型是否匹配
            String decoderType = decoder.getClass().getSimpleName().toLowerCase();
            if ((decoderType.contains("json") && matchesContentType("application/json", contentType)) ||
                (decoderType.contains("xml") && matchesContentType("application/xml", contentType))) {
                return decoder;
            }
        }
        
        // 如果没有找到匹配的解码器，返回第一个解码器（作为默认）
        return defaultDecoder;
    }
}
