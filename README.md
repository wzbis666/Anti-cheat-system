# Minecraft Anti-Cheat System

一个功能完整的 Minecraft 服务器反作弊系统，包含插件端、后端服务和前端管理界面。

## ✨ 功能特性

### 🎮 插件端检测
- **飞行作弊检测** - 检测玩家非法飞行行为
- **速度作弊检测** - 检测异常移动速度
- **自动点击检测** - 检测非法连点器
- **瞄准辅助检测** - 检测 AimBot 行为
- **杀戮光环检测** - 检测 KillAura 行为
- **透视作弊检测** - 检测 X-Ray 透视

### 🖥️ 后端服务
- RESTful API 接口
- WebSocket 实时通信
- JWT 身份认证
- MySQL 数据库存储
- 渐进式惩罚系统

### 📊 前端管理
- 实时监控面板
- 玩家管理
- 作弊记录查看
- 封禁管理
- 白名单管理
- AI 辅助分析

## 🛠️ 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| 插件 | Spigot API | 1.20.4 |
| 后端 | Spring Boot | 3.2.x |
| 前端 | Vue.js | 3.x |
| 数据库 | MySQL | 8.0+ |
| 认证 | JWT | 0.12.x |

## 📁 项目结构

```
Anti-cheat-system/
├── backend/           # Spring Boot 后端
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/          # Vue 前端
│   ├── src/
│   ├── public/
│   └── package.json
├── src/               # Minecraft 插件
│   ├── main/java/
│   └── main/resources/
├── docs/              # 文档
├── scripts/           # 辅助脚本
└── pom.xml            # 插件构建配置
```

## 🚀 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- MySQL 8.0+

### 1. 配置数据库

创建 MySQL 数据库：
```sql
CREATE DATABASE anticheat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动后端服务

```bash
cd backend
mvn clean package -DskipTests
java -jar target/anticheat-backend-1.0-SNAPSHOT.jar
```

### 3. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

### 4. 安装插件

将构建生成的 `AntiCheatPlugin-1.0-SNAPSHOT.jar` 放入 Spigot 服务器的 `plugins/` 目录。

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/anticheat
    username: root
    password: your_password

jwt:
  secret: your_jwt_secret
  expiration: 86400000

api:
  key: your_api_key
```

### 插件配置 (config.yml)

```yaml
api:
  key: your_api_key

websocket:
  host: localhost
  port: 8080

detection:
  fly: true
  speed: true
  autoclick: true
  aimbot: true
  killaura: true
  xray: true
```

## 🌐 访问地址

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:3000 |
| 后端 API | http://localhost:8080 |
| Swagger Docs | http://localhost:8080/swagger-ui.html |

## 🔑 默认账号

```
用户名: admin
密码: admin123
```

> **注意**: 首次登录后请立即修改密码！

## 📜 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

如有问题请在 GitHub Issues 中提出。