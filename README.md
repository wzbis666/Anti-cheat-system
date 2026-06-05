# 🛡️ AntiCheat System

<div align="center">

**Minecraft 服务器反作弊管理面板** · 开箱即用 · Docker 一键部署

[![Java](https://img.shields.io/badge/Java-17+-4B8BBE.svg?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F.svg?style=flat-square&logo=spring)](https://spring.io/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.4-4FC08D.svg?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-COLS.svg?style=flat-square)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-Join-5865F2.svg?style=flat-square&logo=discord)](https://discord.gg)

</div>

---

## ✨ 功能特性

### 🎮 作弊检测

| 检测器 | 说明 | 阈值 |
|--------|------|------|
| 🕊️ 飞行 | 空中悬停、移动轨迹分析 | 10 tick |
| 🏃 速度 | 行走/冲刺/飞行速度 | 5.0 / 7.0 / 12.0 m/s |
| 🎯 自瞄 | 视角旋转角速度检测 | >160°/s |
| 🖱️ 连点 | 点击频率统计 | >15 CPS |
| ⚔️ 杀戮光环 | 多目标攻击分析 | 1s 内 3+ 目标 |
| ⛏️ 透视 | 稀有矿石挖掘比例 | >15% |

### 🤖 AI 智能分析

集成 DeepSeek 大模型，提供作弊研判、举报分析、态势评估功能。

### 📊 管理面板

- **实时仪表盘** - ECharts 可视化 + WebSocket 实时推送
- **玩家管理** - 风险评分、入服检测、封禁记录
- **作弊记录** - 时间线追溯、证据保存
- **举报处理** - 工作流审核、批量操作
- **申诉管理** - 封禁复审、玩家申诉
- **审计日志** - 操作记录、追责溯源

### 🔐 安全特性

- JWT + BCrypt 双认证
- API Key 插件通信验证
- IP 限流防暴力破解
- 敏感信息环境变量隔离

---

## 🚀 快速开始

### 🐳 Docker 一键部署（推荐）

```bash
# 克隆项目
git clone https://github.com/wzbis666/Anti-cheat-system.git
cd Anti-cheat-system

# 配置环境变量
cp .env.example .env
# 编辑 .env 填入您的配置

# 启动全部服务
docker compose up -d

# 访问管理面板
open http://localhost:3030
# 默认账号: admin / (生产环境请修改)
```

### 📋 前置要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| Docker | 20.10+ | 容器化部署 |
| Docker Compose | 2.0+ | 服务编排 |
| MySQL | 8.0+ | 已包含在 Docker 中 |

### 🔧 手动部署

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE anticheat CHARACTER SET utf8mb4;"

# 2. 配置环境变量
export DB_PASSWORD=your_secure_password
export JWT_SECRET=$(openssl rand -base64 64)
export API_KEY=$(openssl rand -hex 32)
export AI_API_KEY=your_deepseek_key  # 可选

# 3. 构建后端
cd backend && mvn clean package -DskipTests && cd ..

# 4. 构建前端
cd frontend && npm install && npm run build && cd ..

# 5. 运行
java -jar backend/target/anticheat-backend-1.0-SNAPSHOT.jar
```

### 📦 安装插件

1. 构建插件: `mvn clean package -DskipTests`
2. 将 `target/AntiCheatPlugin-1.0-SNAPSHOT.jar` 复制到服务器 `plugins/` 目录
3. 重启服务器
4. 修改插件 `config.yml` 中的 `api.key` 与后端一致

---

## ⚙️ 配置说明

### 环境变量

| 变量 | 必填 | 说明 | 示例 |
|------|------|------|------|
| `DB_PASSWORD` | ✅ | 数据库密码 | `SecurePass123!` |
| `JWT_SECRET` | ✅ | JWT 签名密钥 (≥32字符) | `openssl rand -base64 64` |
| `API_KEY` | ✅ | 插件通信密钥 (≥32字符) | `openssl rand -hex 32` |
| `AI_API_KEY` | ❌ | DeepSeek API Key | `sk-xxxx...` |
| `AI_ENABLED` | ❌ | 启用 AI 功能 | `true`/`false` |

### 插件配置 (config.yml)

```yaml
api:
  key: YOUR_API_KEY_HERE  # 与后端 API_KEY 一致

detection:
  fly: true
  speed: true
  autoclick: true
  aimbot: true
  killaura: true
  xray: true

fallback:
  strategy: allow  # 后端不可用时: allow/deny/whitelist_only
```

---

## 🏗️ 系统架构

```
                    ┌─────────────────────────────────┐
                    │     Minecraft Server (Spigot)     │
                    │  ┌─────────────────────────────┐ │
                    │  │     AntiCheat Plugin         │ │
                    │  │  6 Detector + PunishmentMgr  │ │
                    │  └──────────────┬──────────────┘ │
                    └─────────────────┼─────────────────┘
                                      │ WebSocket/HTTP
                                      ▼
┌──────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │ REST API    │  │ WebSocket   │  │ AI (DeepSeek)   │ │
│  │ 40+ 端点    │  │ 实时推送    │  │ 智能分析        │ │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘ │
│         │                 │                  │          │
│         └─────────────────┬──────────────────┘          │
│                           ▼                             │
│                   ┌───────────────┐                     │
│                   │   MySQL 8.0   │                     │
│                   └───────────────┘                     │
└──────────────────────────────────────────────────────────┘
                            │
                            ▼ WebSocket (JWT)
┌──────────────────────────────────────────────────────────┐
│                   Frontend (Vue.js 3)                     │
│  Dashboard · Players · Cheats · Reports · Punishments  │
│  Appeals · Whitelist · Audit Log · Settings · AI Chat   │
└──────────────────────────────────────────────────────────┘
```

---

## 📁 项目结构

```
anticheatsystem/
├── src/                    # Minecraft 插件
│   └── main/java/com/anticheat/
│       ├── AntiCheatPlugin.java
│       ├── *Detector.java   # 6 种检测器
│       ├── PunishmentManager.java
│       └── ...
├── backend/                # Spring Boot 后端
│   └── src/main/java/com/anticheat/backend/
│       ├── controller/     # REST API
│       ├── service/        # 业务逻辑
│       ├── security/       # JWT + API Key
│       └── ...
├── frontend/               # Vue.js 3 前端
│   └── src/
│       ├── views/          # 页面组件
│       ├── api/            # Axios 封装
│       └── locales/        # 中英文 i18n
├── docker-compose.yml       # Docker 部署
├── .env.example            # 环境变量示例
├── CONTRIBUTING.md         # 贡献指南
└── SECURITY.md             # 安全政策
```

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 插件 | Spigot API 1.20.4 · Java-WebSocket 1.5.4 |
| 后端 | Spring Boot 3.2 · Spring Security 6 · JPA |
| 数据库 | MySQL 8.0 · HikariCP |
| 安全 | JWT (jjwt 0.12.3) · BCrypt |
| 实时 | Spring WebSocket · 心跳重连 |
| AI | DeepSeek API · SSE 流式响应 |
| 前端 | Vue.js 3.4 · Element Plus 2.5 · ECharts 5.4 |
| 部署 | Docker Compose · GitHub Actions |

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何参与贡献。

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

<div align="center">

**Made with ❤️ by [wzbis666](https://github.com/wzbis666)**

</div>
