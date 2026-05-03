# 部署文档

## 1. 部署概述

本文档提供 AntiCheat 反作弊系统的完整部署指南，包括开发环境部署和生产环境部署。

### 1.1 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        生产环境架构                              │
└─────────────────────────────────────────────────────────────────┘

                    ┌─────────────┐
                    │   Nginx     │
                    │  反向代理   │
                    └──────┬──────┘
                           │
           ┌───────────────┴───────────────┐
           │                               │
           ▼                               ▼
    ┌─────────────┐                 ┌─────────────┐
    │  Frontend   │                 │   Backend   │
    │  静态资源   │                 │  Spring Boot│
    │  :80/443    │                 │   :8080     │
    └─────────────┘                 └──────┬──────┘
                                           │
                                           ▼
                                    ┌─────────────┐
                                    │   MySQL     │
                                    │   :3306     │
                                    └─────────────┘
```

### 1.2 环境要求

| 组件 | 开发环境 | 生产环境 |
|------|----------|----------|
| JDK | 17+ | 17+ |
| Node.js | 18+ | - |
| MySQL | - | 8.0+ |
| Nginx | - | 1.20+ |
| 内存 | 4GB+ | 8GB+ |
| 存储 | 10GB | 50GB+ |

---

## 2. 开发环境部署

### 2.1 后端部署

#### 2.1.1 安装依赖

```bash
# 确保已安装 JDK 17+
java -version

# 进入后端目录
cd backend

# 安装 Maven 依赖
mvn clean install -DskipTests
```

#### 2.1.2 配置文件

编辑 `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/anticheat;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: YourSuperSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong
  expiration: 86400000
```

#### 2.1.3 启动服务

```bash
# 方式一：Maven 启动
mvn spring-boot:run

# 方式二：JAR 启动
java -jar target/anticheat-backend-1.0-SNAPSHOT.jar
```

### 2.2 前端部署

#### 2.2.1 安装依赖

```bash
# 确保已安装 Node.js 18+
node -version
npm -version

# 进入前端目录
cd frontend

# 安装依赖
npm install
```

#### 2.2.2 配置文件

创建 `.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws/cheats
```

#### 2.2.3 启动服务

```bash
# 开发模式启动
npm run dev

# 访问 http://localhost:3000
```

---

## 3. 生产环境部署

### 3.1 数据库部署

#### 3.1.1 安装 MySQL

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# 启动 MySQL
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### 3.1.2 创建数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE anticheat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'anticheat'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';

-- 授权
GRANT ALL PRIVILEGES ON anticheat.* TO 'anticheat'@'localhost';
FLUSH PRIVILEGES;
```

#### 3.1.3 执行初始化脚本

```bash
mysql -u anticheat -p anticheat < docs/database-design.sql
```

### 3.2 后端部署

#### 3.2.1 构建生产包

```bash
cd backend

# 构建跳过测试
mvn clean package -DskipTests

# 生成的 JAR 文件
# target/anticheat-backend-1.0-SNAPSHOT.jar
```

#### 3.2.2 生产配置

创建 `application-prod.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/anticheat?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: anticheat
    password: YourStrongPassword123!
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

jwt:
  secret: ${JWT_SECRET:YourProductionSecretKeyMustBeAtLeast256BitsLongForSecurity}
  expiration: 86400000

logging:
  level:
    root: INFO
    com.anticheat: DEBUG
  file:
    name: /var/log/anticheat/application.log
```

#### 3.2.3 Systemd 服务配置

创建 `/etc/systemd/system/anticheat-backend.service`:

```ini
[Unit]
Description=AntiCheat Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=anticheat
Group=anticheat
WorkingDirectory=/opt/anticheat/backend
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar anticheat-backend-1.0-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=anticheat-backend

[Install]
WantedBy=multi-user.target
```

启动服务:

```bash
# 重载 systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start anticheat-backend

# 设置开机自启
sudo systemctl enable anticheat-backend

# 查看状态
sudo systemctl status anticheat-backend
```

### 3.3 前端部署

#### 3.3.1 构建生产包

创建 `.env.production`:

```env
VITE_API_BASE_URL=https://your-domain.com/api
VITE_WS_URL=wss://your-domain.com/ws/cheats
```

构建:

```bash
cd frontend

# 构建生产版本
npm run build

# 生成的文件在 dist/ 目录
```

#### 3.3.2 Nginx 配置

创建 `/etc/nginx/sites-available/anticheat`:

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL 证书配置
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 前端静态文件
    root /opt/anticheat/frontend/dist;
    index index.html;
    
    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # WebSocket 代理
    location /ws/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 86400;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1000;
}
```

启用配置:

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/anticheat /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

### 3.4 SSL 证书配置

使用 Let's Encrypt 免费证书:

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期测试
sudo certbot renew --dry-run
```

---

## 4. Minecraft 插件部署

### 4.1 构建插件

```bash
# 进入插件目录
cd anticheatsystem

# 使用 Gradle 构建
./gradlew shadowJar

# 生成的插件文件
# build/libs/AntiCheatPlugin-1.0-SNAPSHOT-all.jar
```

### 4.2 安装插件

```bash
# 复制到服务器 plugins 目录
cp build/libs/AntiCheatPlugin-1.0-SNAPSHOT-all.jar /path/to/server/plugins/

# 重启 Minecraft 服务器
```

### 4.3 配置插件

编辑 `plugins/AntiCheatPlugin/config.yml`:

```yaml
# 后端 API 地址
backend:
  url: http://localhost:8080
  ws-url: ws://localhost:8080/ws/cheats

# 检测配置
detection:
  enabled: true
  # 检测间隔 (tick)
  interval: 20
  
# 自动踢出配置
auto-kick:
  enabled: true
  # 风险分阈值
  threshold: 80

# 举报命令配置
report:
  enabled: true
  cooldown: 60
```

---

## 5. Docker 部署 (可选)

### 5.1 Docker Compose 文件

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: anticheat-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: anticheat
      MYSQL_USER: anticheat
      MYSQL_PASSWORD: YourStrongPassword123!
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docs/database-design.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - anticheat-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: anticheat-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JWT_SECRET: YourProductionSecretKeyMustBeAtLeast256BitsLongForSecurity
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: anticheat
      DB_USER: anticheat
      DB_PASSWORD: YourStrongPassword123!
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    networks:
      - anticheat-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: anticheat-frontend
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
    networks:
      - anticheat-network

volumes:
  mysql_data:

networks:
  anticheat-network:
    driver: bridge
```

### 5.2 后端 Dockerfile

创建 `backend/Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/anticheat-backend-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 5.3 前端 Dockerfile

创建 `frontend/Dockerfile`:

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
```

### 5.4 启动服务

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

---

## 6. 运维管理

### 6.1 日志管理

```bash
# 查看后端日志
tail -f /var/log/anticheat/application.log

# 查看 Nginx 日志
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 6.2 备份策略

```bash
# 数据库备份脚本
#!/bin/bash
BACKUP_DIR=/backup/anticheat
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

mysqldump -u anticheat -p'YourStrongPassword123!' anticheat > $BACKUP_DIR/anticheat_$DATE.sql

# 保留最近 7 天的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

### 6.3 监控告警

建议使用 Prometheus + Grafana 进行监控:

- JVM 内存使用率
- 数据库连接数
- API 响应时间
- 错误率统计

---

## 7. 故障排查

### 7.1 常见问题

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 无法连接数据库 | 密码错误/服务未启动 | 检查配置/启动MySQL |
| 前端无法访问API | CORS配置错误 | 检查SecurityConfig |
| WebSocket连接失败 | 代理配置错误 | 检查Nginx配置 |
| Token验证失败 | 密钥不一致 | 检查JWT配置 |

### 7.2 健康检查

```bash
# 检查后端服务
curl http://localhost:8080/api/auth/validate

# 检查数据库连接
mysql -u anticheat -p -e "SELECT 1"

# 检查 Nginx 状态
sudo systemctl status nginx
```

---

## 8. 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| 1.0 | 2024-04-11 | 初始版本 | AntiCheat Team |
