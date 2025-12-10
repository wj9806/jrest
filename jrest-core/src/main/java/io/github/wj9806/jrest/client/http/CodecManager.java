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
     * 为指定的内容类型选择合适的编码器
     * 
     * @param contentType 内容类型
     * @return 合适的编码器，如果没有找到则返回null
     */
    public Encoder selectEncoder(String contentType) {
        for (Encoder encoder : encoders) {
            if (encoder.supports(contentType)) {
                return encoder;
            }
        }
        
        // 如果没有找到匹配的编码器，返回第一个编码器（作为默认）
        return encoders.isEmpty() ? null : encoders.get(0);
    }
    
    /**
     * 为指定的内容类型选择合适的解码器
     * 
     * @param contentType 内容类型
     * @return 合适的解码器，如果没有找到则返回null
     */
    public Decoder selectDecoder(String contentType) {
        for (Decoder decoder : decoders) {
            if (decoder.supports(contentType)) {
                return decoder;
            }
        }
        
        // 如果没有找到匹配的解码器，返回第一个解码器（作为默认）
        return decoders.isEmpty() ? null : decoders.get(0);
    }
}
