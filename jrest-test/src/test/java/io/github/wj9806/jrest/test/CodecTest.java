package io.github.wj9806.jrest.test;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.PathParam;
import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.http.ContentType;
import io.github.wj9806.jrest.client.http.decode.Decoder;
import io.github.wj9806.jrest.client.http.encode.Encoder;
import io.github.wj9806.jrest.client.http.decode.JacksonDecoder;
import io.github.wj9806.jrest.client.http.encode.JacksonEncoder;
import io.github.wj9806.jrest.client.http.CodecManager;
import lombok.Data;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.*;

public class CodecTest {

    @RestClient(baseUrl = "http://localhost:8080")
    interface TestClient {
        @GET("/test/test")
        String test();

        @GET(value = "{id}", consumes = ContentType.APPLICATION_XML)
        XmlUser getUser(@PathParam("id") Long id);
    }

    @Test
    public void testCodec() throws Exception {
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        TestClient client = factory.createProxy(TestClient.class);
        String result = client.test();
        System.out.println(result);

        XmlUser user = client.getUser(1l);
        System.out.println(user);
    }


    @Test
    public void testJacksonEncoder() throws Exception {
        JacksonEncoder encoder = new JacksonEncoder();
        TestBean bean = new TestBean("test", 123);
        
        byte[] bytes = encoder.encode(bean, "application/json");
        assertNotNull(bytes);
        
        String json = new String(bytes);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));
        
        assertTrue(encoder.supports("application/json"));
        assertTrue(encoder.supports("application/json;charset=UTF-8"));
        assertFalse(encoder.supports("text/plain"));
    }

    @Test
    public void testJacksonDecoder() throws Exception {
        JacksonDecoder decoder = new JacksonDecoder();
        String json = "{\"name\":\"test\",\"value\":123}";
        
        TestBean bean = (TestBean) decoder.decode(json.getBytes(), TestBean.class, "application/json");
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals(123, bean.getValue());
        
        // 测试字符串直接返回
        String stringValue = (String) decoder.decode("test string".getBytes(), String.class, "application/json");
        assertEquals("test string", stringValue);
        
        assertTrue(decoder.supports("application/json"));
        assertTrue(decoder.supports("application/json;charset=UTF-8"));
        assertFalse(decoder.supports("text/plain"));
    }

    @Test
    public void testCodecManager() throws Exception {
        CodecManager codecManager = new CodecManager();
        
        // 测试获取编码器
        Encoder jsonEncoder = codecManager.selectEncoder("application/json");
        assertNotNull(jsonEncoder);
        assertTrue(jsonEncoder instanceof JacksonEncoder);
        
        // 测试获取解码器
        Decoder jsonDecoder = codecManager.selectDecoder("application/json");
        assertNotNull(jsonDecoder);
        assertTrue(jsonDecoder instanceof JacksonDecoder);
        
        // 测试获取不存在的编码器时返回第一个编码器作为默认值
        Encoder defaultEncoder = codecManager.selectEncoder("text/plain");
        assertNotNull(defaultEncoder);
        assertTrue(defaultEncoder instanceof JacksonEncoder);
        
        // 测试获取不存在的解码器时返回第一个解码器作为默认值
        Decoder defaultDecoder = codecManager.selectDecoder("text/plain");
        assertNotNull(defaultDecoder);
        assertTrue(defaultDecoder instanceof JacksonDecoder);
        
        // 测试自定义编解码器
        TestEncoder testEncoder = new TestEncoder();
        TestDecoder testDecoder = new TestDecoder();
        
        codecManager.addEncoder(testEncoder);
        codecManager.addDecoder(testDecoder);
        
        assertEquals(testEncoder, codecManager.selectEncoder("text/test"));
        assertEquals(testDecoder, codecManager.selectDecoder("text/test"));
    }

    @Data
    @JacksonXmlRootElement(localName = "user")
    static class XmlUser {
        @JacksonXmlProperty(localName = "id")
        private Long id;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "email")
        private String email;
    }

    static class TestBean {
        private String name;
        private int value;
        
        public TestBean() {}
        
        public TestBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
    
   public static class TestEncoder implements Encoder {
        @Override
        public byte[] encode(Object object, String contentType) throws Exception {
            return object.toString().getBytes();
        }
        
        @Override
        public boolean supports(String contentType) {
            return "text/test".equals(contentType);
        }
    }
    
   public static class TestDecoder implements Decoder {
        @Override
        public <T> T decode(byte[] bytes, Type targetType, String contentType) throws Exception {
            return (T) new String(bytes);
        }
        
        @Override
        public <T> T decode(String content, Type targetType, String contentType) throws Exception {
            return (T) content;
        }
        
        @Override
        public boolean supports(String contentType) {
            return "text/test".equals(contentType);
        }
    }
}