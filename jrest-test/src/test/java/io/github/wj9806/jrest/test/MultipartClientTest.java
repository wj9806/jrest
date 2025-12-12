package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.http.DefaultMultipartFile;
import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.http.MultipartFile;
import io.github.wj9806.jrest.client.annotation.*;
import io.github.wj9806.jrest.client.proxy.ClientType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MultipartClientTest {

    /**
     * 定义带有Header注解的测试接口
     */
    @RestClient(baseUrl = "http://localhost:8080/", clientType = ClientType.NATIVE)
    interface MultipartClient {

        @POST("/file/upload")
        String upload(@RequestPart MultipartFile file, @FormField("description") String description);
        
        @POST("/file/upload")
        String uploadWithFile(@RequestPart File file, @FormField("description") String description);
    }

    @Test
    public void test() throws IOException {
        JRestClientFactory factory = new JRestClientFactory.Builder().build();

        MultipartClient multipartClient = factory.createProxy(MultipartClient.class);

        // Create a temporary file for testing
        File tempFile = File.createTempFile("test-upload", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Test file content");
        }

        String result = multipartClient.upload(new DefaultMultipartFile("file", tempFile), "Test description");
        System.out.println(result);

        // Clean up
        tempFile.delete();
    }
    
    @Test
    public void testFileParam() throws IOException {
        JRestClientFactory factory = new JRestClientFactory.Builder().build();

        MultipartClient multipartClient = factory.createProxy(MultipartClient.class);

        // Create a temporary file for testing
        File tempFile = File.createTempFile("test-upload-file", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Test file content for File parameter");
        }

        String result = multipartClient.uploadWithFile(tempFile, "Test description with File parameter");
        System.out.println("File param test result: " + result);

        // Clean up
        tempFile.delete();
    }

}
