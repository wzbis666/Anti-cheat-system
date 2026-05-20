# AI 驱动的 Minecraft 反作弊系统

> 融合传统规则引擎与大语言模型智能分析的全栈反作弊解决方案。横跨 Minecraft 插件、Spring Boot 后端、Vue.js 前端三个技术领域，集成 DeepSeek AI 实现作弊行为的自动化研判。

![Java](https://img.shields.io/badge/Java-17+-green.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-blue.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)
![DeepSeek](https://img.shields.io/badge/AI-DeepSeek-purple.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-Real--time-red.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

---

## 目录

- [项目背景](#项目背景)
- [技术架构](#技术架构)
- [核心功能](#核心功能)
  - [六种作弊检测器](#六种作弊检测器)
  - [渐进式惩罚系统](#渐进式惩罚系统)
  - [AI 智能分析](#ai-智能分析)
  - [实时告警与通知](#实时告警与通知)
  - [管理后台](#管理后台)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [工程实践](#工程实践)
- [技术栈](#技术栈)
- [相关文档](#相关文档)
- [许可证](#许可证)

---

## 项目背景

Minecraft 服务器长期受作弊问题困扰——飞行、透视、自动瞄准等外挂严重影响游戏公平性。传统反作弊方案依赖硬编码规则，误判率高、难以应对新型作弊手段。

这个项目探索了一种新思路：**让规则引擎负责实时检测，让 AI 负责深度分析**。通过将 LLM 引入反作弊流程，系统可以对玩家的历史行为进行上下文感知的综合研判，而不仅仅是单点规则的触发。同时，完整的前后端分离架构和实时通信设计，让运维人员可以通过 Web 面板直观地监控服务器安全状态。

---

## 技术架构

```
┌──────────────────────────────────────────────────────────────┐
│                    Minecraft Server (Spigot 1.20.4)           │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                   AntiCheatPlugin                        │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐  │ │
│  │  │ 飞行检测 │ │ 速度检测 │ │ 自瞄检测 │ │ 连点检测   │  │ │
│  │  │ KillAura │ │ X-Ray    │ │ 渐进惩罚 │ │ 白名单豁免 │  │ │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────┬─────┘  │ │
│  │       └─────────────┴────────────┴──────────────┘        │ │
│  │                        │ WebSocket / HTTP                │ │
│  └────────────────────────┼────────────────────────────────┘ │
└───────────────────────────┼──────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌──────────────────────────┐    ┌──────────────────────────────┐
│   Spring Boot Backend    │    │     DeepSeek AI Service       │
│   ┌──────────────────┐   │    │   ┌──────────────────────┐   │
│   │ 40+ REST API     │   │    │   │ 作弊行为智能分析      │   │
│   │ JWT + API Key 认证│   │◄──►│   │ 举报内容自动研判      │   │
│   │ WebSocket 实时推送│   │    │   │ 安全态势综合评估      │   │
│   │ 限流 + 审计日志  │   │    │   │ 封禁决策辅助          │   │
│   │ 12 张数据表      │   │    │   │ SSE 流式对话          │   │
│   └──────┬───────────┘   │    │   └──────────────────────┘   │
│          │               │    └──────────────────────────────┘
│   ┌──────▼───────────┐   │
│   │   MySQL 8.0+     │   │
│   └──────────────────┘   │
└──────────────────────────┘
                            │ HTTP / WebSocket
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                   Vue.js 3 Admin Panel                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐    │
│  │ 实时仪表盘│ │ 玩家管理 │ │ 作弊记录 │ │ AI 分析助手  │    │
│  │ ECharts  │ │ 风险评分 │ │ 举报处理 │ │ 流式对话     │    │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘    │
│              Element Plus  ·  中英双语  ·  暗色主题           │
└──────────────────────────────────────────────────────────────┘
```

### 技术选型理由

| 技术 | 角色 | 理由 |
|------|------|------|
| **Spring Boot 3.2** | 后端框架 | 成熟的 Java 生态，JPA 自动建表，WebSocket 原生支持，Spring Security 企业级认证 |
| **Vue.js 3 + Element Plus** | 前端框架 | Composition API 更好的逻辑复用，Element Plus 专业的后台管理组件 |
| **Spigot API 1.20.4** | 游戏插件 | Minecraft 主流服务端平台，事件驱动架构天然适合作弊检测 |
| **DeepSeek** | AI 服务 | OpenAI 兼容 API，性价比高，支持流式输出（SSE） |
| **WebSocket** | 实时通信 | 延迟从秒级降到毫秒级，服务端可主动推送告警 |
| **JWT + API Key** | 双认证 | JWT 用于管理员无状态登录，API Key 用于插件→后端机器间认证 |
| **MySQL 8.0** | 数据存储 | 关系型数据库适合结构化查询和关联分析 |

---

## 核心功能

### 六种作弊检测器

每种检测器独立运行，通过 Bukkit 事件系统监听玩家行为，均可配置开关和阈值。所有检测器继承 `AbstractDetector`，支持权限绕过（`anticheat.bypass`）、创造/观察模式豁免、调试模式。

| 检测器 | 检测原理 | 核心指标 |
|--------|---------|---------|
| **飞行检测** | 监控空中停留 tick 数，排除水中/攀爬/药水效果 | 悬空时间 > 10 ticks、悬停次数 > 5 次 |
| **速度检测** | 计算水平移动速度，区分行走/冲刺/飞行模式 | 行走 5.0 m/s、冲刺 7.0 m/s、飞行 12.0 m/s |
| **自动点击** | 统计每秒左/右键点击次数 | > 15 CPS 触发告警 |
| **瞄准辅助** | 监控视角旋转角速度（yaw/pitch），检测非人类旋转 | yaw 变化 > 160°、pitch 变化 > 90° |
| **杀戮光环** | 短时间窗口内攻击多个不同目标 | 1 秒内攻击 3+ 不同实体 |
| **透视检测** | 统计挖掘方块中稀有矿石的比例 | 稀有矿石占比 > 15%，总挖掘 > 50 块 |

### 渐进式惩罚系统

```
第 1 次检测 ──→ 游戏内警告消息 + Title 提示
第 2 次检测 ──→ 临时封禁（可配置时长，默认 1 小时）
第 3 次检测 ──→ 永久封禁 + 踢出服务器
```

- 白名单玩家自动豁免检测
- 3 秒冷却期防止重复触发
- 封禁到期自动解封（`PunishmentScheduler` 每分钟检查）
- 后端不可用时三级降级策略：`allow`（放行）/ `deny`（拒绝）/ `whitelist_only`（仅白名单）

### AI 智能分析

集成 DeepSeek 大模型，提供四种分析模式和交互式对话：

| 分析模式 | 功能 | 输出 |
|---------|------|------|
| **作弊研判** | 综合分析玩家检测历史 | CLEAN / SUSPICIOUS / LIKELY / CONFIRMED + 置信度 |
| **举报分析** | 评估举报内容与证据 | CONFIRM / WARN / REJECT 建议 |
| **态势评估** | 服务器整体安全状况 | 洞察、威胁识别、优化建议 |
| **封禁复审** | 评估已有封禁决策的合理性 | 恰当 / 过严 / 过轻 |
| **交互对话** | SSE 流式响应的 AI 助手 | 多轮对话，会话记忆 |

技术实现：5 分钟响应缓存（相同查询复用结果）、30 分钟会话 TTL、可配置模型/温度/Tokens。

### 实时告警与通知

三层 WebSocket 架构实现端到端实时通信：

```
插件检测到作弊 ──WebSocket──→ 后端持久化 + 广播 ──WebSocket──→ 前端仪表盘实时刷新
```

- 15 秒心跳保活，断线自动重连（指数退避 5s → 60s）
- 通知规则引擎：自定义条件触发告警（如"检测到 KillAura 且严重度 > 5"）
- 最大 100 并发连接，64KB 消息上限
- 后端可通过 WebSocket 反向向插件发送指令（踢人、封禁、解封、刷新缓存、同步配置）

### 管理后台

| 页面 | 功能 |
|------|------|
| **仪表盘** | 动画统计卡片、ECharts 雷达图、实时作弊推送、安全态势概览 |
| **玩家管理** | 列表搜索、风险评分、踢出次数统计 |
| **作弊记录** | 分页查询、按类型/玩家筛选、时间线展示 |
| **举报管理** | 举报列表、证据查看、处理/驳回 |
| **封禁管理** | 手动封禁/解封、历史记录、自动过期 |
| **白名单** | 添加/移除白名单玩家 |
| **申诉处理** | 玩家申诉审核、通过/驳回 |
| **审计日志** | 管理员操作全程记录 |
| **系统设置** | 检测开关、阈值调整、插件配置同步 |
| **AI 助手** | DeepSeek 对话式分析交互，流式输出 |

---

## 快速开始

### 环境要求

| 环境 | 版本 |
|------|------|
| Java | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |
| Spigot/Paper | 1.20.4 |

### 1. 创建数据库

```sql
CREATE DATABASE anticheat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 构建项目

```bash
git clone https://github.com/wzbis666/Anti-cheat-system.git
cd Anti-cheat-system

# 构建后端
cd backend
mvn clean package -DskipTests

# 构建插件
cd ..
mvn clean package -DskipTests

# 安装前端依赖
cd frontend
npm install
```

### 3. 配置

**后端配置** — 编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/anticheat
    username: root
    password: ${DB_PASSWORD}          # 设置环境变量

jwt:
  secret: ${JWT_SECRET}               # 生产环境务必设置强随机值
  expiration: 86400000

api:
  key: ${API_KEY}                     # 与插件 config.yml 保持一致

ai:
  enabled: true
  provider: deepseek
  api-key: ${AI_API_KEY}              # DeepSeek API Key
```

**插件配置** — 编辑 `src/main/resources/config.yml`：

```yaml
api:
  key: ${API_KEY}                     # 与后端保持一致

websocket:
  host: localhost
  port: 8080
```

### 4. 启动

```bash
# 终端 1 — 启动后端
cd backend
java -jar target/anticheat-backend-1.0-SNAPSHOT.jar

# 终端 2 — 启动前端开发服务器
cd frontend
npm run dev                           # 访问 http://localhost:3030
```

### 5. 安装插件

将 `target/AntiCheatPlugin-1.0-SNAPSHOT.jar` 复制到 Spigot 服务器的 `plugins/` 目录，重启服务器即可。

### 一键启动（Docker Compose）

```bash
docker compose up -d
```

这会启动 MySQL + 后端 + 前端，访问 `http://localhost:3030` 即可打开管理面板。

### 演示与预览

| 仪表盘 | AI 分析 |
|--------|---------|
| 实时作弊推送、ECharts 雷达图、安全态势概览 | DeepSeek 流式对话、作弊行为智能研判 |

> 在线演示截图请参见项目文档或直接使用 Docker Compose 在本地启动体验。

---

## 项目结构

```
anticheatsystem/
├── src/                              # Minecraft 插件 (Spigot 1.20.4)
│   └── main/java/com/anticheat/
│       ├── AntiCheatPlugin.java           # 插件主类
│       ├── AntiCheatWebSocketClient.java  # WS 客户端（重连、心跳、消息路由）
│       ├── AbstractDetector.java          # 检测器抽象基类
│       ├── CacheManager.java              # 本地缓存（封禁/白名单，30s 刷新）
│       ├── ConfigManager.java             # 配置管理（支持从后端同步）
│       ├── PunishmentManager.java         # 渐进式惩罚（警告→临时→永久）
│       ├── PlayerJoinListener.java        # 入服校验（封禁检查 + 降级策略）
│       ├── FlyDetector.java               # 飞行检测
│       ├── SpeedDetector.java             # 速度检测
│       ├── AutoClickDetector.java         # 连点检测
│       ├── AimbotDetector.java            # 自瞄检测
│       ├── KillAuraDetector.java          # 杀戮光环检测
│       └── XrayDetector.java              # 透视检测
│
├── backend/                          # Spring Boot 后端 (Java 17)
│   └── src/main/java/com/anticheat/backend/
│       ├── controller/               # 12 个 REST 控制器
│       ├── service/                  # 业务逻辑层
│       ├── repository/               # JPA Repository
│       ├── model/                    # JPA 实体（Player, CheatRecord, Punishment...）
│       ├── dto/                      # 请求/响应 DTO
│       ├── security/                 # JWT + API Key 过滤器 + 限流
│       ├── handler/                  # WebSocket 处理器（实时广播 + 持久化）
│       ├── ai/                       # DeepSeek 集成（4 种分析 + 缓存 + SSE）
│       ├── scheduler/                # 定时任务（封禁自动解封）
│       └── config/                   # Security, Swagger, WebSocket, CORS 配置
│
├── frontend/                         # Vue.js 3 前端
│   └── src/
│       ├── views/                    # 11 个页面组件
│       ├── components/               # AI 助手等公共组件
│       ├── api/                      # Axios 封装 + 全部 API 接口
│       ├── router/                   # 路由守卫 + 11 条路由
│       └── locales/                  # 中英文国际化
│
├── docs/                             # 系统设计、数据库设计、部署指南、用户手册
├── scripts/                          # WebSocket 和 API 测试脚本
└── CLAUDE.md                         # AI 辅助开发指南
```

---

## 工程实践

- **配置外部化**：所有密钥、阈值、URL 均支持环境变量覆盖（`${ENV_VAR:default}` 模式）
- **降级设计**：后端不可用时插件使用本地缓存，不影响游戏正常运行，三级策略可配置
- **安全纵深**：JWT + API Key 双认证、BCrypt 密码哈希、常数时间 API Key 比较、IP 级别限流、CORS 白名单
- **可观测性**：审计日志记录所有管理员操作，检测事件全量持久化
- **国际化**：中英文双语支持，前后端均可切换

## 技术栈

| 层级 | 技术 |
|------|------|
| 游戏插件 | Spigot API 1.20.4, Java-WebSocket, Gson |
| 后端 | Spring Boot 3.2.4, Spring Security 6, JPA/Hibernate, Spring WebSocket |
| 数据库 | MySQL 8.0, HikariCP |
| 安全 | JWT (jjwt 0.12.3), BCrypt, API Key |
| AI | DeepSeek API (OpenAI 兼容) |
| 前端 | Vue.js 3, Element Plus 2.5, ECharts 5.4, Axios, vue-i18n |
| 构建 | Maven 3.6+, Vite 5.x |
| 文档 | SpringDoc OpenAPI 2.3 (Swagger) |

## 相关文档

- [系统设计文档](docs/system-design.md) — 完整架构设计、模块划分、核心流程
- [数据库设计](docs/database-design.md) — ER 图、表结构、索引策略
- [部署指南](docs/deployment-guide.md) — Docker Compose 生产部署
- [用户手册](docs/user-guide.md) — 管理面板使用说明
- [测试报告](TEST_REPORT.md) — 47 项审查结果与问题分级
- [项目展示](PROJECT_PORTFOLIO.md) — 面试用项目深度展示
- [面试准备](INTERVIEW_QA.md) — 常见面试问答

## 许可证

MIT License — 详见 [LICENSE](LICENSE)
