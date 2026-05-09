# 🎮 Minecraft Anti-Cheat System

一个完整的 Minecraft 服务器反作弊解决方案，包含插件端检测、后端服务和现代化前端管理界面。

![Java](https://img.shields.io/badge/Java-17+-green.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-blue.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📖 目录

- [功能特性](#-功能特性)
- [技术架构](#-技术架构)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [配置指南](#-配置指南)
- [API 文档](#-api-文档)
- [许可证](#-许可证)

---

## ✨ 功能特性

### 🔍 作弊检测模块

| 检测类型 | 说明 | 阈值配置 |
|---------|------|---------|
| **飞行检测** | 检测非法飞行和悬空 | 最大悬空时间、最大悬停次数 |
| **速度检测** | 检测异常移动速度 | 最大行走/奔跑/飞行速度 |
| **自动点击** | 检测连点器使用 | 最大点击次数/秒 |
| **瞄准辅助** | 检测 AimBot 行为 | 最大视角变化 |
| **杀戮光环** | 检测 KillAura | 短时间攻击目标数 |
| **透视检测** | 检测 X-Ray 行为 | 稀有矿物比例 |

### 📊 管理功能

- **实时监控** - WebSocket 实时接收作弊警报
- **玩家管理** - 玩家数据查看与搜索
- **作弊记录** - 完整的作弊行为历史
- **封禁管理** - 临时/永久封禁
- **白名单** - 白名单玩家管理
- **举报系统** - 玩家自助举报
- **申诉系统** - 封禁申诉处理
- **审计日志** - 操作日志记录
- **AI 辅助** - DeepSeek AI 智能分析

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Minecraft Server                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              AntiCheatPlugin (Bukkit)                │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │    │
│  │  │FlyDetect│ │SpeedDet│ │ClickDet│ │AimDetect│   │    │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘   │    │
│  │       └──────────┬┴──────────┬┴───────────┘        │    │
│  │                  ▼                               │    │
│  │         CheatWebSocketClient                     │    │
│  └─────────────────────┬───────────────────────────┘    │
└────────────────────────┼─────────────────────────────────┘
                         │ WebSocket / HTTP
                         ▼
┌───────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                       │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐  │
│  │ REST API  │ │WebSocket  │ │  Security │ │   AI     │  │
│  │           │ │ Handler   │ │  (JWT)    │ │ Service  │  │
│  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └─────┬─────┘  │
│        └─────────────┼─────────────┼─────────────┘        │
│                      ▼                                     │
│  ┌─────────────────────────────────────────────────────┐  │
│  │                    MySQL Database                    │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
                         │
                         │ HTTP / WebSocket
                         ▼
┌───────────────────────────────────────────────────────────┐
│                    Vue.js Frontend                          │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │Dashboard│ │ Players │ │ Cheats  │ │Settings │        │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
└───────────────────────────────────────────────────────────┘
```

### 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **Minecraft 插件** | Spigot API | 1.20.4 |
| **后端框架** | Spring Boot | 3.2.x |
| **安全认证** | JWT | 0.12.x |
| **数据库** | MySQL | 8.0+ |
| **前端框架** | Vue.js | 3.x |
| **构建工具** | Vite | 5.x |
| **API 文档** | Swagger/OpenAPI | 3.0 |

---

## 📁 项目结构

```
anticheatsystem/
├── src/                          # Minecraft 插件源码
│   └── main/java/com/anticheat/
│       ├── AntiCheatPlugin.java   # 插件主类
│       ├── AntiCheatCommand.java  # 指令处理
│       ├── AntiCheatWebSocketClient.java  # WebSocket 客户端
│       ├── CacheManager.java      # 缓存管理
│       ├── ConfigManager.java     # 配置管理
│       ├── HttpHelper.java       # HTTP 请求
│       ├── PunishmentManager.java # 惩罚管理
│       ├── detector/              # 检测器模块
│       │   ├── AbstractDetector.java
│       │   ├── FlyDetector.java
│       │   ├── SpeedDetector.java
│       │   ├── AutoClickDetector.java
│       │   ├── AimbotDetector.java
│       │   ├── KillAuraDetector.java
│       │   └── XrayDetector.java
│       └── listener/             # 事件监听器
│           ├── PlayerJoinListener.java
│           └── PlayerQuitListener.java
│
├── backend/                      # Spring Boot 后端
│   └── src/main/java/com/anticheat/backend/
│       ├── controller/           # REST 控制器
│       ├── service/              # 业务逻辑
│       ├── repository/           # 数据访问
│       ├── model/                # 数据模型
│       ├── dto/                  # 数据传输对象
│       ├── security/             # 安全认证
│       ├── config/               # 配置类
│       ├── handler/              # WebSocket 处理器
│       ├── ai/                   # AI 服务
│       └── scheduler/            # 定时任务
│
├── frontend/                     # Vue.js 前端
│   ├── src/
│   │   ├── views/               # 页面组件
│   │   ├── components/          # 公共组件
│   │   ├── api/                 # API 调用
│   │   ├── router/              # 路由配置
│   │   ├── locales/             # 国际化
│   │   └── assets/              # 静态资源
│   └── package.json
│
├── docs/                        # 项目文档
├── scripts/                     # 辅助脚本
└── pom.xml                      # Maven 主配置
```

---

## 🚀 快速开始

### 环境要求

| 环境 | 版本要求 |
|------|---------|
| Java | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |
| Spigot/Paper | 1.20.4 |

### 1. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE anticheat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 构建后端

```bash
cd backend
mvn clean package -DskipTests
```

### 3. 构建插件

```bash
cd ..
mvn clean package -DskipTests
# 输出: target/AntiCheatPlugin-1.0-SNAPSHOT.jar
```

### 4. 启动服务

```bash
# 启动后端
cd backend
java -jar target/anticheat-backend-1.0-SNAPSHOT.jar

# 新终端 - 启动前端
cd frontend
npm install
npm run dev
```

### 5. 安装插件

将 `AntiCheatPlugin-1.0-SNAPSHOT.jar` 复制到 Spigot 服务器的 `plugins/` 目录，重启服务器。

---

## ⚙️ 配置指南

### 后端配置 (backend/src/main/resources/application.yml)

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:anticheat}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}

api:
  key: ${API_KEY:your-api-key}

ai:
  enabled: ${AI_ENABLED:true}
  provider: ${AI_PROVIDER:deepseek}
  api-key: ${AI_API_KEY:}
  base-url: ${AI_BASE_URL:https://api.deepseek.com}
```

### 插件配置 (src/main/resources/config.yml)

```yaml
api:
  key: your-api-key

websocket:
  host: localhost
  port: 8080

detection:
  fly: true
  speed: true
  autoclick: true
  fly_permission: true
  aimbot: true
  killaura: true
  xray: true

thresholds:
  max-walk-speed: 5.0
  max-sprint-speed: 7.0
  max-fly-speed: 12.0
  max-clicks-per-second: 15
  max-yaw-change: 160.0
  max-pitch-change: 90.0

punishment:
  cooldown: 3000
  warning-threshold: 2
  temp-ban-threshold: 4
  perm-ban-threshold: 6
  temp-ban-duration: 86400000
```

---

## 📚 API 文档

启动后端后访问 Swagger UI：
```
http://localhost:8080/swagger-ui.html
```

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/change-password | 修改密码 |

### 管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/players | 玩家列表 |
| GET | /api/cheat-records | 作弊记录 |
| POST | /api/punishments | 创建封禁 |
| DELETE | /api/punishments/{id} | 解除封禁 |
| GET | /api/whitelist | 白名单列表 |
| POST | /api/whitelist | 添加白名单 |
| DELETE | /api/whitelist/{id} | 移除白名单 |

### WebSocket

```
ws://localhost:8080/cheat-ws
```

---

## 🌐 访问地址

| 服务 | 地址 |
|------|------|
| 前端管理界面 | http://localhost:3000 |
| 后端 API | http://localhost:8080 |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |

### 默认账号

```
用户名: admin
密码: admin123
```

> ⚠️ **安全提示**: 首次登录后请立即修改默认密码！

---

## 📜 许可证

本项目采用 [MIT 许可证](LICENSE)开源。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📞 联系方式

- **GitHub Issues**: [https://github.com/wzbis666/Anti-cheat-system/issues](https://github.com/wzbis666/Anti-cheat-system/issues)
