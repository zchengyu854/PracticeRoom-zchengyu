# 项目配置指南

## 配置文件说明

本项目使用多个配置文件来管理不同环境的配置，确保敏感信息不会泄露到版本控制系统中。

### 配置文件列表

1. **application.yml** (Git管理)
   - 主配置文件，使用环境变量作为默认值
   - 适合公共配置，不包含敏感信息
   - 默认值适用于生产环境或演示环境

2. **application-local.yml** (Git忽略)
   - 本地开发环境配置
   - 包含实际的数据库密码、API密钥等敏感信息
   - 每个开发者可以有自己的配置

3. **application-prod.yml** (Git忽略)
   - 生产环境配置
   - 包含生产环境的实际配置信息

4. **.env.example** (Git管理)
   - 环境变量示例文件
   - 展示所有可配置的环境变量
   - 不包含实际的敏感信息

5. **.env** (Git忽略)
   - 实际的环境变量配置
   - 包含真实的敏感信息
   - 每个环境有自己的配置

## 环境配置切换

### 1. 本地开发环境

**方式一：使用application-local.yml**
```bash
java -jar exam-system-server.jar --spring.profiles.active=local
```

**方式二：使用环境变量**
```bash
# 导入环境变量
export DB_USERNAME=root
export DB_PASSWORD=your_password
export QWEN_API_KEY=your_api_key

# 启动应用
java -jar exam-system-server.jar
```

**方式三：使用.env文件配合Spring Boot**
在pom.xml中添加依赖：
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>java-dotenv</artifactId>
    <version>5.2.2</version>
</dependency>
```

### 2. 生产环境

使用环境变量方式部署（推荐）：

```bash
# Docker部署示例
docker run -d \
  -p 8080:8080 \
  -e DB_URL=jdbc:mysql://prod-db:3306/exam_system \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=prod_password \
  -e REDIS_HOST=prod-redis \
  -e QWEN_API_KEY=prod_api_key \
  -e MINIO_ENDPOINT=http://prod-minio:9000 \
  exam-system-server
```

**Kubernetes ConfigMap示例：**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: exam-system-config
data:
  REDIS_HOST: "prod-redis"
  REDIS_PORT: "6379"
  QWEN_API_BASE_URL: "https://dashscope.aliyuncs.com/compatible-mode/v1"
  QWEN_MODEL: "qwen-plus"
---
apiVersion: v1
kind: Secret
metadata:
  name: exam-system-secrets
type: Opaque
stringData:
  DB_PASSWORD: "your-prod-password"
  QWEN_API_KEY: "your-prod-api-key"
  MINIO_PASSWORD: "your-minio-password"
```

## 敏感信息检查清单

在提交代码到Git之前，请确保：

- [ ] application.yml中不包含真实的密码、API密钥
- [ ] .gitignore中包含敏感配置文件
- [ ] .env文件未被添加到Git
- [ ] application-local.yml和application-prod.yml未被提交
- [ ] SQL脚本中不包含真实的生产数据（除非是初始化脚本）
- [ ] 日志文件、临时文件未被提交

## 常见配置项说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://host:port/database?params
    username: your_username
    password: your_password
```

**参数说明：**
- `useUnicode=true`: 使用Unicode编码
- `characterEncoding=utf8`: 字符编码
- `allowPublicKeyRetrieval=true`: 允许公钥检索
- `useSSL=false`: 不使用SSL（开发环境）
- `serverTimezone=Asia/Shanghai`: 服务器时区

### Redis配置
```yaml
spring:
  data:
    redis:
      host: redis_host
      port: 6379
      database: 0
      password: your_redis_password  # 如果Redis设置了密码
```

### MinIO配置
```yaml
minio:
  endpoint: http://minio_host:9000
  bucket-name: your_bucket_name
  username: your_username
  password: your_password
  url-expiry: 604800  # URL过期时间，单位秒
```

### 千问AI配置
```yaml
qwen:
  api:
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: your_api_key
    model: qwen-plus  # 或 qwen-turbo, qwen-max
    max-tokens: 2000
    temperature: 0.3  # 0-1之间，越低越稳定
```

## 配置文件优先级

Spring Boot 配置文件加载优先级（从高到低）：

1. 命令行参数
2. 环境变量
3. application-{profile}.yml (激活的profile)
4. application.yml

例如：
```bash
# 命令行参数优先级最高
java -jar app.jar --server.port=9090
```

## 日志配置

开发环境使用debug级别，生产环境使用info级别：

```yaml
logging:
  level:
    com.exam: debug  # 开发环境
    # com.exam: info  # 生产环境
```

## 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 150MB      # 单个文件最大大小
      max-request-size: 150MB   # 单次请求最大大小
```

## 安全建议

1. **不要在代码中硬编码敏感信息**
   - ❌ String password = "123456";
   - ✅ String password = System.getenv("DB_PASSWORD");

2. **使用专门的密钥管理服务**（生产环境）
   - HashiCorp Vault
   - AWS Secrets Manager
   - Azure Key Vault
   - 阿里云KMS

3. **定期轮换密钥**
   - 数据库密码
   - API密钥
   - MinIO访问密钥

4. **使用最小权限原则**
   - 数据库用户只授予必要的权限
   - 不要使用root账户运行应用

5. **加密传输**
   - 生产环境使用HTTPS
   - 数据库连接使用SSL

## 故障排查

### 问题1: 找不到配置文件

**症状**: 启动时报错找不到配置

**解决方案**:
- 确认配置文件在 `src/main/resources/` 目录
- 检查文件名是否正确（application.yml）
- 检查YAML格式是否正确（缩进、冒号等）

### 问题2: 环境变量未生效

**症状**: 环境变量设置后，应用仍使用默认值

**解决方案**:
- 确认环境变量名称正确（大小写敏感）
- Windows: `set VARIABLE=value`
- Linux/Mac: `export VARIABLE=value`
- 重启应用以加载新的环境变量

### 问题3: 数据库连接失败

**症状**: 启动时报数据库连接错误

**检查项**:
- 数据库地址、端口是否正确
- 数据库服务是否启动
- 用户名、密码是否正确
- 防火墙是否允许连接
- 数据库是否已创建

## 参考链接

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [MyBatis Plus配置](https://baomidou.com/pages/56bac0/)
- [Redis配置](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [MinIO Java SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html)
