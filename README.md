<p align="center">
  <h1 align="center">🛡️ AntiCheat System</h1>
  <p align="center">
    <strong>AI 驱动的 Minecraft 反作弊系统</strong><br/>
    规则引擎实时检测 · 大模型智能研判 · WebSocket 毫秒级推送
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-green.svg" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.4-blue.svg" alt="Spring Boot 3.2"/>
  <img src="https://img.shields.io/badge/Vue.js-3.4-brightgreen.svg" alt="Vue.js 3"/>
  <img src="https://img.shields.io/badge/Spigot-1.20.4-yellow.svg" alt="Spigot 1.20.4"/>
  <img src="https://img.shields.io/badge/AI-DeepSeek-purple.svg" alt="DeepSeek"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-orange.svg" alt="MySQL 8.0"/>
  <img src="https://img.shields.io/badge/WebSocket-Real--time-red.svg" alt="WebSocket"/>
  <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="MIT"/>
</p>

<p align="center">
  <a href="#-项目背景">项目背景</a> ·
  <a href="#-技术架构">技术架构</a> ·
  <a href="#-核心特性">核心特性</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-项目结构">项目结构</a> ·
  <a href="#-技术栈">技术栈</a> ·
  <a href="#-文档">文档</a>
</p>

---

## 📖 项目背景

Minecraft 服务器长期面临作弊困扰——**飞行外挂、矿透、自动瞄准、杀戮光环**等严重影响游戏公平性。传统反作弊方案依赖硬编码规则，误判率高、难以应对新型作弊手段。

本项目探索了一种新思路：**规则引擎负责实时检测，AI 负责深度分析**。通过将大语言模型引入反作弊流程，系统可以对玩家历史行为进行上下文感知的综合研判，而非简单的单点规则触发。同时，完整的前后端分离架构和实时通信设计，让运维人员可以通过 Web 面板直观监控服务器安全状态。

### 🎯 解决的核心问题

- ❌ **传统方案**：硬编码规则 → 误判率高 → 新型作弊无法检测 → 缺乏可视化
- ✅ **本方案**：6 种检测器 + AI 二次研判 + 渐进惩罚 + 实时仪表盘

---

## 🏗️ 技术架构

```
┌──────────────────────────────────────────────────────────────────┐
│                   Minecraft Server (Spigot 1.20.4)                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     AntiCheat Plugin                        │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │ │
│  │  │ Fly     │ │ Speed   │ │ Aimbot  │ │ AutoClick│          │ │
│  │  │ Detector│ │ Detector│ │ Detector│ │ Detector │          │ │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘          │ │
│  │  ┌────┴───────────┴──────────┴──────────┴────┐            │ │
│  │  │  PunishmentManager · CacheManager          │            │ │
│  │  │  (渐进惩罚)         (封禁/白名单缓存)        │            │ │
│  │  └────────────────────┬───────────────────────┘            │ │
│  └───────────────────────┼────────────────────────────────────┘ │
└──────────────────────────┼──────────────────────────────────────┘
                           │ WebSocket + HTTP
        ┌──────────────────┴──────────────────┐
        ▼                                     ▼
┌───────────────────────┐        ┌──────────────────────────┐
│  Spring Boot Backend  │        │   DeepSeek AI Service     │
│  ┌─────────────────┐  │        │  ┌────────────────────┐  │
│  │ 40+ REST API    │  │  HTTP  │  │ 作弊行为智能分析     │  │
│  │ JWT + API Key   │  │◄─────►│  │ 举报内容自动研判     │  │
│  │ WebSocket 广播   │  │        │  │ 安全态势综合评估     │  │
│  │ 限流 + 审计日志  │  │        │  │ 封禁决策辅助        │  │
│  │ 12 张数据表      │  │        │  │ SSE 流式对话         │  │
│  └────────┬────────┘  │        │  └────────────────────┘  │
│           │           │        └──────────────────────────┘
│  ┌────────▼────────┐  │
│  │  MySQL 8.0      │  │
│  └─────────────────┘  │
└───────────────────────┘
            │ WebSocket (JWT)
            ▼
┌──────────────────────────────────────────────────────────────┐
│                  Vue.js 3 Admin Panel                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐   │
│  │ 实时仪表盘│ │ 玩家管理 │ │ 作弊记录 │ │ AI 分析助手   │   │
│  │ ECharts  │ │ 风险评分 │ │ 举报处理 │ │ SSE 流式对话  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘   │
│           Element Plus · 中英双语 · 暗色主题                  │
└──────────────────────────────────────────────────────────────┘
```

### 🔐 安全设计

```
请求 → RateLimit 限流 → API Key 过滤器(插件) → JWT 过滤器(管理员) → Controller
                              │                        │
                        常数时间比较              BCrypt 密码哈希
                       MessageDigest.isEqual       jjwt 0.12.3
```

---

## ✨ 核心特性

### 🎮 六种作弊检测器

所有检测器继承 `AbstractDetector`，通过 Bukkit 事件系统监听，支持权限绕过、创造模式豁免、运行时开关。

<table>
<tr>
  <td width="50%">

#### 🕊️ 飞行检测 (Fly)
- 监控空中悬停 tick 数和移动轨迹
- 排除水中、攀爬、药水效果干扰
- 检测瞬间飞行和异常上升速度

  </td>
  <td width="50%">

#### 🏃 速度检测 (Speed)
- 计算水平移动速度
- 区分行走/冲刺/飞行模式
- 阈值：行走 5.0 m/s、冲刺 7.0 m/s

  </td>
</tr>
<tr>
  <td>

#### 🎯 瞄准辅助 (Aimbot)
- 监控视角旋转角速度
- 检测异常 snap 旋转（>80°）
- 追踪攻击目标一致性

  </td>
  <td>

#### 🖱️ 自动点击 (AutoClick)
- 统计每秒点击次数
- 阈值 > 15 CPS 触发告警
- 区分左右键操作

  </td>
</tr>
<tr>
  <td>

#### ⚔️ 杀戮光环 (KillAura)
- 短时间窗口内攻击多目标
- 1 秒内攻击 3+ 不同实体即告警
- 结合瞄准检测交叉验证

  </td>
  <td>

#### ⛏️ 透视检测 (X-Ray)
- 统计矿脉挖掘中稀有矿石比例
- 稀有矿石占比 > 15% 触发
- 累积 50 块以上开始分析

  </td>
</tr>
</table>

### ⚡ 渐进式惩罚

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  第 1 次触发  │ ──► │  第 2 次触发  │ ──► │  第 3 次触发  │
│  游戏内警告   │     │  临时封禁 1h  │     │  永久封禁     │
│  + Title 提示 │     │  + 踢出服务器 │     │  + 踢出服务器 │
└──────────────┘     └──────────────┘     └──────────────┘
```

- **3 秒冷却**防止重复触发
- **白名单豁免**信任玩家自动跳过
- **自动解封** — `PunishmentScheduler` 每分钟检查过期封禁
- **降级策略** — 后端不可用时支持 `allow` / `deny` / `whitelist_only` 三级

### 🤖 AI 智能分析

集成 DeepSeek 大模型，提供四种分析模式和交互式对话：

| 分析模式 | 触发场景 | 输出结果 |
|---------|---------|---------|
| **作弊研判** | 分析玩家检测历史 | `CLEAN` / `SUSPICIOUS` / `LIKELY` / `CONFIRMED` + 置信度 |
| **举报分析** | 评估举报内容和证据 | `CONFIRM` / `WARN` / `REJECT` |
| **态势评估** | 服务器整体安全概览 | 威胁洞察、风险趋势、优化建议 |
| **封禁复审** | 评估已有封禁合理性 | 恰当 / 过严 / 过轻 |
| **AI 助手** | 交互式分析对话 | SSE 流式响应，多轮会话记忆 |

**工程细节**：5 分钟响应缓存、30 分钟会话 TTL、多层 JSON 解析（```json → 纯 JSON → 正则回退）、温度 0.3 确保输出稳定性。

### 📡 实时告警

```
插件检测作弊 ──WebSocket──▶ 后端持久化 + 广播 ──WebSocket──▶ 前端实时刷新
```

- **15 秒心跳**保活，指数退避重连（5s → 10s → 20s → 40s → 60s）
- **通知规则引擎** — 自定义条件触发告警（如"检测到 KillAura 且严重度 > 5"）
- **最大 100 并发**，64KB 消息上限
- **后端 → 插件**反向指令：远程踢人、封禁、解封、刷新缓存、同步配置

---

## 🚀 快速开始

### 前置要求

| 环境 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 运行后端和插件 |
| Node.js | 18+ | 前端开发 |
| MySQL | 8.0+ | 数据持久化 |
| Maven | 3.6+ | Java 构建 |
| Spigot/Paper | 1.20.4 | Minecraft 服务端 |

### 🐳 Docker 一键启动（推荐）

```bash
# 克隆项目
git clone https://github.com/wzbis666/Anti-cheat-system.git
cd Anti-cheat-system

# 启动全部服务（MySQL + 后端 + 前端）
docker compose up -d

# 访问管理面板
open http://localhost:3030
```

### 🔧 手动构建

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE anticheat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 构建后端
cd backend
mvn clean package -DskipTests

# 3. 构建插件
cd ..
mvn clean package -DskipTests

# 4. 安装前端依赖
cd frontend
npm install

# 5. 配置环境变量（生产环境务必修改）
export DB_PASSWORD=your_db_password
export JWT_SECRET=$(openssl rand -base64 32)
export API_KEY=$(openssl rand -base64 32)
export AI_API_KEY=your_deepseek_api_key

# 6. 启动后端
cd backend
java -jar target/anticheat-backend-1.0-SNAPSHOT.jar &

# 7. 启动前端
cd frontend
npm run dev
```

访问 `http://localhost:3030` 打开管理面板。

### 📦 安装插件

将 `target/AntiCheatPlugin-1.0-SNAPSHOT.jar` 复制到 Spigot 服务器的 `plugins/` 目录，重启服务器。确保插件 `config.yml` 中的 `api.key` 与后端配置一致。

---

## 📁 项目结构

```
anticheatsystem/
│
├── src/                                    # 🔌 Minecraft 插件
│   └── main/java/com/anticheat/
│       ├── AntiCheatPlugin.java            # 插件主入口
│       ├── AntiCheatWebSocketClient.java   # WS 客户端（重连/心跳/消息路由）
│       ├── AbstractDetector.java           # 检测器抽象基类
│       ├── CacheManager.java               # 封禁/白名单本地缓存（30s 刷新）
│       ├── ConfigManager.java              # 配置管理（支持后端同步）
│       ├── PunishmentManager.java          # 渐进式惩罚引擎
│       ├── PlayerJoinListener.java         # 入服校验（封禁检查 + 降级）
│       ├── FlyDetector.java                # 飞行检测
│       ├── SpeedDetector.java              # 速度检测
│       ├── AimbotDetector.java             # 自瞄检测
│       ├── AutoClickDetector.java          # 连点检测
│       ├── KillAuraDetector.java           # 杀戮光环检测
│       └── XrayDetector.java               # 透视检测
│
├── backend/                                # ☕ Spring Boot 后端
│   └── src/main/java/com/anticheat/backend/
│       ├── controller/                     # 12 个 REST 控制器
│       │   ├── AiController.java           # AI 分析接口
│       │   ├── AuthController.java         # 认证（登录/注册/JWT）
│       │   ├── CheatRecordController.java  # 作弊记录 CRUD
│       │   ├── PunishmentController.java   # 封禁管理
│       │   ├── ReportController.java       # 举报处理
│       │   ├── PlayerController.java       # 玩家管理
│       │   ├── WhitelistController.java    # 白名单管理
│       │   ├── AppealController.java       # 申诉审核
│       │   ├── AuditLogController.java     # 审计日志
│       │   ├── SettingsController.java     # 系统设置
│       │   ├── StatsController.java        # 统计数据
│       │   └── NotificationRuleController.java  # 通知规则
│       ├── service/                        # 业务逻辑层
│       ├── repository/                     # JPA Repository
│       ├── model/                          # 12 个 JPA 实体
│       ├── dto/                            # 请求/响应 DTO
│       ├── security/                       # JWT + API Key + 限流
│       ├── handler/                        # WebSocket 处理器
│       ├── ai/                             # DeepSeek 集成
│       ├── scheduler/                      # 封禁自动解封定时任务
│       └── config/                         # Security, CORS, Swagger, WebSocket
│
├── frontend/                               # 🎨 Vue.js 3 前端
│   └── src/
│       ├── views/                          # 11 个页面组件
│       │   ├── Dashboard.vue               # 实时仪表盘 + ECharts
│       │   ├── Players.vue                 # 玩家列表 + 风险评分
│       │   ├── Cheats.vue                  # 作弊记录时间线
│       │   ├── Reports.vue                 # 举报处理工作流
│       │   ├── Punishments.vue             # 封禁管理
│       │   ├── Whitelist.vue               # 白名单管理
│       │   ├── Appeals.vue                 # 申诉审核
│       │   ├── AuditLog.vue                # 审计日志
│       │   ├── Settings.vue                # 系统设置
│       │   ├── Profile.vue                 # 个人中心
│       │   └── Login.vue                   # 登录页
│       ├── components/AiAssistant.vue      # AI 对话助手
│       ├── api/index.js                    # Axios 封装 + 全部 API
│       ├── router/                         # 路由守卫
│       └── locales/                        # 中英文 i18n
│
├── docker-compose.yml                      # Docker 一键部署
├── CLAUDE.md                               # AI 开发指南
├── docs/                                   # 设计文档
│   ├── system-design.md                    # 系统设计
│   ├── database-design.md                  # 数据库设计
│   ├── deployment-guide.md                 # 部署指南
│   └── user-guide.md                       # 用户手册
├── scripts/                                # 测试脚本
├── pom.xml                                 # 插件 Maven 配置
└── backend/pom.xml                         # 后端 Maven 配置
```

---

## 🛠️ 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| **游戏插件** | Spigot API 1.20.4 · Java-WebSocket 1.5.4 · Gson 2.10 | Bukkit 事件驱动检测 |
| **后端核心** | Spring Boot 3.2.4 · Spring Security 6 · JPA/Hibernate | REST API · 认证鉴权 |
| **数据库** | MySQL 8.0 · HikariCP | 连接池 · JPA 自动建表 |
| **安全** | JWT (jjwt 0.12.3) · BCrypt · API Key · IP 限流 | 双认证体系 |
| **实时通信** | Spring WebSocket · 心跳保活 · 指数退避重连 | 毫秒级推送 |
| **AI 集成** | DeepSeek API (OpenAI 兼容) · SSE 流式 · 响应缓存 | 智能分析 |
| **前端** | Vue.js 3.4 · Element Plus 2.5 · ECharts 5.4 · Axios | 管理面板 |
| **国际化** | vue-i18n 9.14 | 中文 / English |
| **构建部署** | Maven 3.6+ · Vite 5.2 · Docker Compose · GitHub Actions CI | 自动化 |
| **文档** | SpringDoc OpenAPI 2.3 (Swagger) | API 文档 |

---

## 📊 项目指标

| 指标 | 数值 |
|------|------|
| 作弊检测类型 | 6 种 |
| REST API 端点 | 40+ |
| 数据库表 | 12 张 |
| 前端页面 | 11 个 |
| 后端单元测试 | 28 个 |
| WebSocket 延迟 | < 100ms（局域网） |
| API 响应时间 | p50 < 50ms, p99 < 200ms |

---

## 📚 文档

- [系统设计文档](docs/system-design.md) — 完整架构设计、模块划分、核心流程
- [数据库设计](docs/database-design.md) — ER 图、表结构、索引策略
- [部署指南](docs/deployment-guide.md) — Docker Compose 生产部署
- [用户手册](docs/user-guide.md) — 管理面板使用说明
- [测试报告](TEST_REPORT.md) — 47 项审查结果与问题分级
- [CLAUDE.md](CLAUDE.md) — AI 辅助开发指南

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

<p align="center">
  <sub>Built with ❤️ by <a href="https://github.com/wzbis666">wzbis666</a></sub>
</p>
