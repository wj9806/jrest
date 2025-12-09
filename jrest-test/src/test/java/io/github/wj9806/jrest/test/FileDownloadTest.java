package io.github.wj9806.jrest.test;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.RestClient;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件下载测试
 */
public class FileDownloadTest {
    
    /**
     * 文件下载服务接口
     */
    @RestClient(baseUrl = "http://localhost:8080")
    public interface FileDownloadService {
        
        /**
         * 下载文件，返回字节数组
         */
        @GET("/test/download")
        byte[] downloadFileAsBytes();
        
        /**
         * 下载文件，返回InputStream
         */
        @GET("/test/download")
        InputStream downloadFileAsStream();
    }
    
    @Test
    public void testFileDownloadAsBytes() throws IOException {
        // 创建客户端工厂
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        FileDownloadService service = factory.createProxy(FileDownloadService.class);
        
        // 下载文件
        byte[] bytes = service.downloadFileAsBytes();
        
        // 保存文件
        Path path = Paths.get("test-download.pdf");
        Files.write(path, bytes);
        
        System.out.println("File downloaded as bytes, size: " + bytes.length + " bytes");
        System.out.println("File saved to: " + path.toAbsolutePath());
    }
    
    @Test
    public void testFileDownloadAsStream() throws IOException {
        // 创建客户端工厂
        JRestClientFactory factory = new JRestClientFactory.Builder().build();
        FileDownloadService service = factory.createProxy(FileDownloadService.class);
        
        // 下载文件
        try (InputStream inputStream = service.downloadFileAsStream();
             OutputStream outputStream = Files.newOutputStream(Paths.get("test-download-stream.pdf"))) {
            
            // 保存文件
            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            System.out.println("File downloaded as stream, size: " + totalBytes + " bytes");
            System.out.println("File saved to: " + Paths.get("test-download-stream.pdf").toAbsolutePath());
        }
    }
}