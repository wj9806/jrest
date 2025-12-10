package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.http.encode.XmlEncoder;
import io.github.wj9806.jrest.client.http.decode.XmlDecoder;
import org.junit.Test;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.Assert.*;

/**
 * 测试XML编解码器功能
 */
public class XmlCodecTest {

    @JacksonXmlRootElement(localName = "user")
    static class User {
        @JacksonXmlProperty(localName = "id")
        private Long id;
        
        @JacksonXmlProperty(localName = "name")
        private String name;
        
        @JacksonXmlProperty(localName = "email")
        private String email;
        
        public User() {}
        
        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @Test
    public void testXmlEncoder() throws Exception {
        // 创建XML编码器
        XmlEncoder encoder = new XmlEncoder();
        
        // 创建测试用户对象
        User user = new User(1L, "张三", "zhangsan@example.com");
        
        // 测试编码功能
        byte[] xmlBytes = encoder.encode(user, "application/xml");
        assertNotNull(xmlBytes);
        assertTrue(xmlBytes.length > 0);
        
        // 打印XML内容
        String xml = new String(xmlBytes, "UTF-8");
        System.out.println("编码后的XML: " + xml);
        
        // 验证XML内容
        assertTrue(xml.contains("<user"));
        assertTrue(xml.contains("<id>1</id>"));
        assertTrue(xml.contains("<name>张三</name>"));
        assertTrue(xml.contains("<email>zhangsan@example.com</email>"));
        assertTrue(xml.contains("</user>"));
        
        // 测试contentType支持
        assertTrue(encoder.supports("application/xml"));
        assertTrue(encoder.supports("text/xml"));
        assertTrue(encoder.supports("application/atom+xml"));
        assertFalse(encoder.supports("application/json"));
    }

    @Test
    public void testXmlDecoder() throws Exception {
        // 创建XML解码器
        XmlDecoder decoder = new XmlDecoder();
        
        // XML字符串
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><user><id>1</id><name>张三</name><email>zhangsan@example.com</email></user>";
        
        // 测试从字符串解码
        User userFromString = decoder.decode(xml, User.class, "application/xml");
        assertNotNull(userFromString);
        assertEquals(1L, userFromString.getId().longValue());
        assertEquals("张三", userFromString.getName());
        assertEquals("zhangsan@example.com", userFromString.getEmail());
        
        // 测试从字节数组解码
        User userFromBytes = decoder.decode(xml.getBytes("UTF-8"), User.class, "application/xml");
        assertNotNull(userFromBytes);
        assertEquals(1L, userFromBytes.getId().longValue());
        assertEquals("张三", userFromBytes.getName());
        assertEquals("zhangsan@example.com", userFromBytes.getEmail());
        
        // 测试contentType支持
        assertTrue(decoder.supports("application/xml"));
        assertTrue(decoder.supports("text/xml"));
        assertTrue(decoder.supports("application/rss+xml"));
        assertFalse(decoder.supports("application/json"));
    }

    @Test
    public void testXmlCodecRoundTrip() throws Exception {
        // 创建编解码器
        XmlEncoder encoder = new XmlEncoder();
        XmlDecoder decoder = new XmlDecoder();
        
        // 原始对象
        User originalUser = new User(2L, "李四", "lisi@example.com");
        
        // 编码为XML
        byte[] xmlBytes = encoder.encode(originalUser, "application/xml");
        
        // 解码为对象
        User decodedUser = decoder.decode(xmlBytes, User.class, "application/xml");
        
        // 验证编码解码后的对象与原始对象相同
        assertNotNull(decodedUser);
        assertEquals(originalUser.getId(), decodedUser.getId());
        assertEquals(originalUser.getName(), decodedUser.getName());
        assertEquals(originalUser.getEmail(), decodedUser.getEmail());
        
        System.out.println("XML编解码往返测试成功！");
    }
}