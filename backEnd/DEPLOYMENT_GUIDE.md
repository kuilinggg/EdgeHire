# EdgeHire HR模块部署指南

## 环境要求

- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (用于前端)

## 后端部署步骤

### 1. 数据库准备

1. 创建数据库：
```sql
CREATE DATABASE EdgeHire CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 运行初始化脚本：
```bash
mysql -u root -p EdgeHire < database_init.sql
```

### 2. 配置文件设置

1. 复制配置模板：
```bash
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

2. 修改数据库配置：
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
    url: jdbc:mysql://localhost:3306/EdgeHire?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8
```

### 3. 编译和运行

1. 清理并编译：
```bash
mvn clean compile
```

2. 运行测试（可选）：
```bash
mvn test
```

3. 启动应用：
```bash
mvn spring-boot:run
```

或者打包后运行：
```bash
mvn clean package
java -jar target/EdgeHire-0.0.1-SNAPSHOT.jar
```

### 4. 验证部署

访问以下URL验证服务是否正常：

- 健康检查：`http://localhost:8080/actuator/health`（如果启用了actuator）
- API测试：`http://localhost:8080/api/hr/verify/1`

## 前端部署步骤

### 1. 安装依赖
```bash
cd frontEnd
npm install
```

### 2. 配置API地址

修改 `src/api/hr.js` 中的API_BASE地址：
```javascript
const API_BASE = 'http://your-backend-server:8080/api'
```

### 3. 启动开发服务器
```bash
npm run dev
```

### 4. 构建生产版本
```bash
npm run build
```

## 生产环境配置

### 1. 数据库优化

在 `application.yml` 中添加生产环境配置：

```yaml
spring:
  profiles:
    active: prod
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境不要使用update
    show-sql: false
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

logging:
  level:
    com.se.EdgeHire: INFO
    org.springframework: WARN
  file:
    name: logs/edgehire.log
```

### 2. 安全配置

1. 使用环境变量存储敏感信息：
```bash
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret
```

2. 在配置文件中引用：
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### 3. 反向代理配置（Nginx）

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 监控和日志

### 1. 应用监控

添加Spring Boot Actuator依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

配置监控端点：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### 2. 日志配置

在 `src/main/resources/logback-spring.xml` 中配置日志：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/edgehire.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/edgehire.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否启动
   - 验证用户名密码是否正确
   - 确认数据库URL格式正确

2. **端口冲突**
   - 修改 `application.yml` 中的端口：
   ```yaml
   server:
     port: 8081
   ```

3. **内存不足**
   - 增加JVM内存：
   ```bash
   java -Xmx2g -jar target/EdgeHire-0.0.1-SNAPSHOT.jar
   ```

4. **跨域问题**
   - 确保控制器上有 `@CrossOrigin(origins = "*")` 注解
   - 或者配置全局CORS

### 日志查看

查看应用日志：
```bash
tail -f logs/edgehire.log
```

查看错误日志：
```bash
grep ERROR logs/edgehire.log
```

## 性能优化

### 1. 数据库优化

- 为常用查询字段添加索引
- 使用连接池优化数据库连接
- 定期分析慢查询

### 2. 应用优化

- 启用缓存：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

- 配置缓存：
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

### 3. 前端优化

- 启用gzip压缩
- 使用CDN加速静态资源
- 实现懒加载和分页

## 备份策略

### 数据库备份

创建定时备份脚本：
```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u root -p EdgeHire > backup/edgehire_$DATE.sql
```

### 应用备份

备份配置文件和日志：
```bash
tar -czf backup/app_backup_$DATE.tar.gz src/main/resources/application.yml logs/
```
