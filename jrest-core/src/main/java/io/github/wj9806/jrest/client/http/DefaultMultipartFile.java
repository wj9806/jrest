package io.github.wj9806.jrest.client.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

/**
 * MultipartFile接口的默认实现
 */
public class DefaultMultipartFile implements MultipartFile {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultMultipartFile.class);
    
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;
    private final InputStream inputStream;
    private final File file;
    private final Long size;
    
    /**
     * 从文件创建MultipartFile
     * @param name 参数名称
     * @param file 文件
     */
    public DefaultMultipartFile(String name, File file) {
        this.name = name;
        this.file = file;
        this.originalFilename = file.getName();
        this.contentType = getContentTypeFromFile(file);
        this.content = null;
        this.inputStream = null;
        this.size = file.length();
    }
    
    /**
     * 从输入流创建MultipartFile
     * @param name 参数名称
     * @param originalFilename 原始文件名称
     * @param contentType 内容类型
     * @param inputStream 输入流
     */
    public DefaultMultipartFile(String name, String originalFilename, String contentType, InputStream inputStream) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.file = null;
        this.content = null;
        this.size = null;
    }
    
    /**
     * 从字节数组创建MultipartFile
     * @param name 参数名称
     * @param originalFilename 原始文件名称
     * @param contentType 内容类型
     * @param content 字节数组内容
     */
    public DefaultMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
        this.inputStream = null;
        this.file = null;
        this.size = (long) content.length;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public boolean isEmpty() {
        if (content != null) {
            return content.length == 0;
        }
        if (size != null) {
            return size == 0;
        }
        return true;
    }
    
    @Override
    public long getSize() {
        if (content != null) {
            return content.length;
        }
        if (size != null) {
            return size;
        }
        throw new UnsupportedOperationException("Cannot determine file size");
    }
    
    @Override
    public byte[] getBytes() throws IOException {
        if (content != null) {
            return content;
        }
        if (file != null) {
            return Files.readAllBytes(file.toPath());
        }
        if (inputStream != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }
        throw new IOException("No content available");
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        if (file != null) {
            return new FileInputStream(file);
        }
        if (content != null) {
            return new ByteArrayInputStream(content);
        }
        throw new IOException("No content available");
    }
    
    @Override
    public void transferTo(File dest) throws IOException {
        if (content != null) {
            Files.write(dest.toPath(), content);
        } else if (file != null) {
            Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } else if (inputStream != null) {
            try (OutputStream outputStream = new FileOutputStream(dest)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            throw new IOException("No content available");
        }
    }
    
    /**
     * 从文件获取内容类型
     * @param file 文件
     * @return 内容类型
     */
    private String getContentTypeFromFile(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            logger.debug("Could not determine file content type for {}", file.getName(), e);
            return "application/octet-stream";
        }
    }
}