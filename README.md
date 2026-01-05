# 智能考试系统

目前没有数据库以及前端脚本及代码，待我论文通过后一并奉上，感谢大佬们的批评与指正。
感谢尚硅谷的课程给予我很大帮助！！

基于Spring Boot + MyBatis Plus开发的智能在线考试系统，支持AI智能组卷、自动判卷等功能。

## 技术栈

- **后端框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis Plus
- **缓存**: Redis
- **文件存储**: MinIO
- **AI服务**: 阿里云千问大模型
- **API文档**: Knife4j
- **其他**: Maven, Lombok

## 项目结构

```
exam-system-server/
├── src/
│   └── main/
│       ├── java/com/exam/
│       │   ├── controller/     # 控制层
│       │   ├── service/        # 服务层
│       │   ├── entity/         # 实体类
│       │   ├── mapper/         # 数据访问层
│       │   ├── config/         # 配置类
│       │   └── util/           # 工具类
│       └── resources/
│           ├── mapper/         # MyBatis XML映射文件
│           └── application.yml # 应用配置文件
└── pom.xml                     # Maven依赖配置
```

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- MinIO (可选，用于文件存储)

### 环境配置

1. **复制环境变量模板文件**:
   ```bash
   cp .env.example .env
   ```

2. **编辑 `.env` 文件**，填入实际配置信息：
   ```env
   # Redis配置
   REDIS_HOST=your-redis-host
   REDIS_PORT=6379
   REDIS_DB=0

   # 数据库配置
   DB_URL=jdbc:mysql://your-mysql-host:3306/exam_system?useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
   DB_USERNAME=your-username
   DB_PASSWORD=your-password

   # 千问AI配置
   QWEN_API_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
   QWEN_API_KEY=your-actual-api-key-here
   QWEN_MODEL=qwen-plus

   # MinIO配置
   MINIO_ENDPOINT=http://your-minio-host:9000
   MINIO_BUCKET=exam-system
   MINIO_USERNAME=your-username
   MINIO_PASSWORD=your-password
   ```

3. **导入数据库**:
   ```bash
   # 创建数据库
   CREATE DATABASE exam_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

   # 导入数据库脚本
   mysql -u root -p exam_system < <database-script>.sql
   ```

### 运行项目

1. **使用Maven编译**:
   ```bash
   mvn clean install
   ```

2. **运行项目**:
   ```bash
   mvn spring-boot:run
   ```

   或者直接运行打包后的jar文件：
   ```bash
   java -jar target/exam-system-server-1.0.0.jar
   ```

3. **访问API文档**:
   项目启动后，访问: `http://localhost:8080/doc.html`

## 主要功能

### 题目管理
- 选择题、判断题、简答题等多种题型
- 支持题目分类、难度设置
- AI辅助生成题目

### 试卷管理
- 手动组卷
- AI智能组卷（根据难度、知识点自动生成）
- 试卷发布与管理

### 在线考试
- 在线答题
- 自动计时
- 防作弊监控（窗口切换检测）

### 自动判卷
- 客观题自动判卷
- 主观题AI智能判卷
- 成绩分析与统计

### 文件管理
- MinIO对象存储
- 支持大文件上传（最大150MB）
- 图片、视频等多媒体资源

## API接口文档

启动项目后访问: `http://localhost:8080/doc.html`

查看完整的API文档，包括：
- 用户管理
- 题目管理
- 试卷管理
- 考试管理
- 文件上传下载
- 等等...

## 配置说明

### application.yml 配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 服务端口 | 8080 |
| spring.datasource.* | 数据库配置 | - |
| spring.data.redis.* | Redis配置 | - |
| qwen.api.* | 千问AI配置 | - |
| minio.* | MinIO配置 | - |

### 环境变量优先级

环境变量 > application.yml 默认值

例如：
```bash
# 设置数据库密码环境变量，会覆盖application.yml中的默认值
export DB_PASSWORD=your_password
```

## 开发规范

### 代码规范
- 使用Lombok简化代码
- 统一使用驼峰命名
- 控制器返回统一包装类Result
- 使用MyBatis Plus进行数据操作

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

## 常见问题

### 1. 数据库连接失败
检查 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 配置是否正确

### 2. Redis连接失败
检查 `REDIS_HOST`, `REDIS_PORT` 配置是否正确，Redis服务是否启动

### 3. MinIO文件上传失败
检查MinIO服务是否正常，`MINIO_ENDPOINT`、`MINIO_USERNAME`、`MINIO_PASSWORD` 配置是否正确

### 4. AI功能无法使用
检查 `QWEN_API_KEY` 是否配置正确，是否有足够的额度

## 安全建议

1. **不要将 `.env` 文件提交到版本控制系统**
2. 生产环境使用强密码
3. 定期更新依赖包
4. 启用HTTPS（生产环境）
5. 配置防火墙规则，限制数据库、Redis等服务的访问

## 首页
<img width="2549" height="1242" alt="image" src="https://github.com/user-attachments/assets/7229aaeb-333a-4250-bbef-5b6ebd6dbd63" />


## 管理员页面
<img width="2549" height="1242" alt="image" src="https://github.com/user-attachments/assets/c9181429-3b24-4d52-8a7c-7c32f03e3eda" />

