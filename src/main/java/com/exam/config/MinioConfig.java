package com.exam.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 * 用于配置MinIO对象存储服务
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    
    public String endpoint;      // MinIO服务器地址
    public String username;      // 用户名 
    public String password;      // 密码
    public String bucketName;    // 存储桶名称
    public Long urlExpiry;       // URL过期时间
    
    /**
     * 创建MinIO客户端Bean
     * @return MinIO客户端实例
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)      // 设置MinIO服务器地址
                .credentials(username, password)  // 设置认证凭据
                .build();
    }
} 