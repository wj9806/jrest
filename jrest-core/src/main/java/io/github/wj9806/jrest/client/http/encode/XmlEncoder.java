package io.github.wj9806.jrest.client.http.encode;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * XML编码器，将Java对象编码为XML格式
 */
public class XmlEncoder implements Encoder {
    
    private static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * 将Java对象编码为XML字节数组
     * 
     * @param object     要编码的对象
     * @param contentType 内容类型
     * @return XML字节数组
     * @throws Exception 编码过程中的异常
     */
    @Override
    public byte[] encode(Object object, String contentType) throws Exception {
        return xmlMapper.writeValueAsBytes(object);
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
}