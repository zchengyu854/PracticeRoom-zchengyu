package com.exam.service;

import com.exam.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传服务
 * 支持MinIO和本地文件存储两种方式
 */
@Slf4j
@Service
public class FileUploadService {

    @Autowired(required = false)  // MinIO可能未配置，设置为非必须
    private MinioClient minioClient;  // MinIO客户端
    
    @Autowired(required = false)  // MinIO可能未配置，设置为非必须
    private MinioConfig minioConfig;  // MinIO配置

    @Value("${file.upload.path:./uploads/}")  // 本地文件存储路径
    private String localUploadPath;
    
    @Value("${file.upload.url-prefix:http://localhost:8080/files/}")  // 文件访问URL前缀
    private String fileUrlPrefix;

    /**
     * 上传文件（自动选择MinIO或本地存储）
     * @param file 上传的文件
     * @param folder 文件夹名称（如：banners, avatars等）
     * @return 包含文件信息的结果Map
     */
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            String url;
            // 如果MinIO配置可用，优先使用MinIO  // 优先使用MinIO存储
            if (minioClient != null && minioConfig != null) {
                url = uploadToMinio(file, folder);
            } else {
                // 否则使用本地文件存储  // 降级使用本地存储
                log.info("MinIO未配置，使用本地文件存储");
                url = uploadToLocal(file, folder);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());
            return result;
            
        } catch (Exception e) {
            log.error("MinIO上传失败，降级使用本地存储: {}", e.getMessage());
            // MinIO失败时降级到本地存储  // 容错处理
            try {
                String url = uploadToLocal(file, folder);
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", file.getSize());
                result.put("contentType", file.getContentType());
                return result;
                
            } catch (Exception localException) {
                log.error("本地文件上传也失败: {}", localException.getMessage());
                throw new RuntimeException("文件上传失败: " + localException.getMessage());
            }
        }
    }

    /**
     * 上传到MinIO
     */
    private String uploadToMinio(MultipartFile file, String folder) throws Exception {
        log.info("开始上传文件到MinIO: 文件名={}, 大小={}, 类型={}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        // 检查存储桶是否存在，不存在则创建  // 确保存储桶存在
        ensureBucketExists();
        
        // 生成唯一文件名  // 构建文件路径
        String fileName = generateFileName(file.getOriginalFilename());
        String objectName = folder + "/" + fileName;
        log.info("生成的对象名称: {}", objectName);
        
        // 获取文件输入流  // 准备文件流
        InputStream inputStream = file.getInputStream();
        
        // 上传文件到MinIO  // 执行文件上传
        log.info("正在上传文件到MinIO存储桶: {}", minioConfig.bucketName);
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(minioConfig.bucketName)
                .object(objectName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        log.info("文件上传到MinIO完成");
        
        // 生成访问URL  // 生成文件访问地址
        String url = generateFileUrl(objectName);
        
        log.info("文件上传到MinIO成功: {}", url);
        return url;
    }

    /**
     * 上传到本地文件系统
     */
    private String uploadToLocal(MultipartFile file, String folder) throws Exception {
        // 创建上传目录  // 确保目录存在
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        Path uploadDir = Paths.get(localUploadPath, folder, datePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 生成唯一文件名  // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        
        // 保存文件  // 写入文件
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 生成访问URL  // 构建访问地址
        String relativePath = folder + "/" + datePath + "/" + fileName;
        String url = fileUrlPrefix + relativePath;
        
        log.info("文件上传到本地成功: {}", url);
        return url;
    }

    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateFileName(String originalFilename) {
        // 获取文件扩展名  // 提取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成日期路径  // 按日期分组存储
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        
        // 生成UUID文件名  // 使用UUID确保唯一性
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return datePath + "/" + uuid + extension;
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            // 检查存储桶是否存在  // 检查存储桶
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.bucketName)
                    .build()
            );
            
            // 如果不存在则创建  // 创建存储桶
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioConfig.bucketName)
                        .build()
                );
                log.info("存储桶创建成功: {}", minioConfig.bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储桶操作失败: " + e.getMessage());
        }
    }

    /**
     * 生成文件访问URL
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    private String generateFileUrl(String objectName) {
        try {
            // 生成预签名URL  // 生成临时访问地址
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.bucketName)
                    .object(objectName)
                    .expiry(Math.toIntExact(minioConfig.urlExpiry), TimeUnit.SECONDS)
                    .build()
            );
        } catch (Exception e) {
            log.error("生成文件URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param objectName 对象名称  
     */
    public void deleteFile(String objectName) {
        try {
            if (minioClient != null && minioConfig != null) {
                // 从MinIO删除文件  // 删除文件
                minioClient.removeObject(
                    RemoveObjectArgs.builder()
                        .bucket(minioConfig.bucketName)
                        .object(objectName)
                        .build()
                );
                log.info("文件从MinIO删除成功: {}", objectName);
            } else {
                // 从本地删除文件  // 删除本地文件
                Path filePath = Paths.get(localUploadPath, objectName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("本地文件删除成功: {}", objectName);
                }
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
} 