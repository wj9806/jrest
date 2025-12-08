package io.github.wj9806.jrest.client.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传接口定义
 */
public interface MultipartFile {
    
    /**
     * 获取文件名称
     * @return 文件名称
     */
    String getName();
    
    /**
     * 获取原始文件名称
     * @return 原始文件名称
     */
    String getOriginalFilename();
    
    /**
     * 获取文件内容类型
     * @return 文件内容类型
     */
    String getContentType();
    
    /**
     * 文件是否为空
     * @return 是否为空
     */
    boolean isEmpty();
    
    /**
     * 获取文件大小
     * @return 文件大小（字节）
     */
    long getSize();
    
    /**
     * 获取文件内容的字节数组
     * @return 文件内容字节数组
     * @throws IOException IO异常
     */
    byte[] getBytes() throws IOException;
    
    /**
     * 获取文件输入流
     * @return 文件输入流
     * @throws IOException IO异常
     */
    InputStream getInputStream() throws IOException;
    
    /**
     * 将文件内容传输到目标文件
     * @param dest 目标文件
     * @throws IOException IO异常
     */
    void transferTo(File dest) throws IOException;
}