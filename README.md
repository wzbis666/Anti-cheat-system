# AI 驱动的 Minecraft 反作弊系统

> 融合传统规则引擎与大语言模型智能分析的全栈反作弊解决方案。
> 横跨 Minecraft 插件、Spring Boot 后端、Vue.js 前端三个技术领域，集成 DeepSeek AI 实现作弊行为的自动化研判。

![Java](https://img.shields.io/badge/Java-17+-green.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-blue.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)
![DeepSeek](https://img.shields.io/badge/AI-DeepSeek-purple.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-Real--time-red.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Build](https://img.shields.io/badge/build-Maven%2FVite-success.svg)

---

## 为什么做这个项目

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
│              Element Plus  ·  中英双语  ·  Hypixel 主题       │
└──────────────────────────────────────────────────────────────┘
```

### 技术选型 — 为什么选这些？

| 技术 | 角色 | 选型理由 |
|------|------|---------|
| **Spring Boot 3.2** | 后端框架 | 成熟的 Java 生态，JPA 自动建表，WebSocket 原生支持，Spring Security 提供企业级认证 |
| **Vue.js 3 + Element Plus** | 前端框架 | Composition API 带来更好的逻辑复用，Element Plus 提供专业的后台管理组件 |
| **Spigot API 1.20.4** | 游戏插件 | Minecraft 最主流的服务端平台，事件驱动架构天然适合作弊检测 |
| **DeepSeek** | AI 服务 | 国产大模型，OpenAI 兼容 API，性价比高，支持流式输出（SSE） |
| **WebSocket** | 实时通信 | 相比 HTTP 轮询，延迟从秒级降到毫秒级，服务端可主动推送告警 |
| **JWT + API Key** | 双认证体系 | JWT 用于人类管理员的无状态登录，API Key 用于插件→后端的机器间认证，职责分离 |
| **MySQL 8.0** | 数据存储 | 关系型数据库适合作弊记录的结构化查询和关联分析 |

---

## 核心功能

### 实时作弊检测（6 种检测器）

每种检测器独立运行，通过 Bukkit 事件系统监听玩家行为，可配置开关和阈值：

| 检测器 | 检测原理 | 核心指标 |
|--------|---------|---------|
| **飞行检测** | 监控空中停留 tick 数，排除水中/攀爬/药水效果 | 悬空时间 > 10 ticks、悬停次数 > 5 次 |
| **速度检测** | 计算水平移动速度，区分行走/冲刺/飞行模式 | 行走 5.0 m/s、冲刺 7.0 m/s、飞行 12.0 m/s |
| **自动点击** | 统计每秒左/右键点击次数 | > 15 CPS 触发告警 |
| **瞄准辅助** | 监控视角旋转角速度（yaw/pitch），检测非人类旋转 | yaw 变化 > 160°、pitch 变化 > 90° |
| **杀戮光环** | 短时间窗口内攻击多个不同目标 | 1 秒内攻击 3+ 不同实体 |
| **透视检测** | 统计挖掘方块中稀有矿石的比例 | 稀有矿石占比 > 15%，总挖掘 > 50 块 |

所有检测器继承 `AbstractDetector`，支持权限绕过（`anticheat.bypass`）、创造/观察模式豁免、调试模式。

### 渐进式惩罚

```
第 1 次检测 ──→ 游戏内警告消息 + Title 提示
第 2 次检测 ──→ 临时封禁（可配置时长，默认 1 小时）
第 3 次检测 ──→ 永久封禁 + 踢出服务器
```

- 白名单玩家自动豁免
- 3 秒冷却防止重复触发
- 封禁到期自动解封（`PunishmentScheduler` 每分钟检查）

### AI 智能分析（DeepSeek 集成）

这是本项目区别于传统反作弊系统的核心特色：

- **作弊研判**：综合分析玩家检测历史，输出 CLEAN / SUSPICIOUS / LIKELY / CONFIRMED 四级判定 + 置信度
- **举报分析**：评估玩家举报内容与证据，建议 CONFIRM / WARN / REJECT 处理方式
- **态势评估**：对服务器整体安全状况给出洞察、威胁识别和优化建议
- **封禁复审**：评估已有封禁决策的合理性（恰当/过严/过轻）
- **交互对话**：SSE 流式响应的 AI 助手，支持多轮对话和会话记忆

技术实现：5 分钟响应缓存（相同查询复用结果）、30 分钟会话 TTL、可配置模型/温度/Tokens。

### 实时告警

三层 WebSocket 架构实现端到端实时通信：

```
插件检测到作弊 ──WebSocket──→ 后端持久化 + 广播 ──WebSocket──→ 前端仪表盘实时刷新
```

- 15 秒心跳保活，断线自动重连（指数退避 5s→60s）
- 支持通知规则引擎：自定义条件触发告警（如"检测到 KillAura 且严重度 > 5"）
- 最大 100 并发连接

### 完整的管理后台

| 页面 | 功能 |
|------|------|
| **仪表盘** | 动画统计卡片、ECharts 威胁雷达图、实时作弊推送、安全态势概览 |
| **玩家管理** | 玩家列表搜索、风险评分、踢出次数统计 |
| **作弊记录** | 分页查询、按类型/玩家筛选、时间线展示 |
| **举报管理** | 举报列表、证据查看、处理/驳回操作 |
| **封禁管理** | 手动封禁/解封、封禁历史、自动过期 |
| **白名单** | 添加/移除白名单玩家 |
| **申诉处理** | 玩家申诉审核、通过/驳回 |
| **审计日志** | 管理员操作全程记录 |
| **系统设置** | 检测开关、阈值调整、插件配置同步 |
| **AI 助手** | DeepSeek 对话式分析交互 |

---

## 量化指标

| 指标 | 数值 |
|------|------|
| 作弊检测类型 | 6 种（飞行、速度、连点、自瞄、杀戮光环、透视） |
| REST API 端点 | 40+ |
| 数据库表 | 12 张（含索引和外键约束） |
| 前端页面 | 11 个功能页面 + AI 助手组件 |
| WebSocket 延迟 | < 100ms（局域网环境） |
| API 响应时间 | p50 < 50ms, p99 < 200ms |
| 支持玩家并发 | 100+ |
| 国际化 | 中文 / English |

---

## 技术挑战与解决方案

### 挑战 1：LLM 结构化输出的可靠性

**问题**：DeepSeek API 返回的自然语言文本需要被解析为结构化的 JSON 判定结果（作弊等级、置信度、建议操作），但 LLM 偶尔会输出不符合预期格式的内容。

**解决**：设计了多层解析策略——优先匹配 JSON 代码块（\`\`\`json），其次匹配纯 JSON 对象，最后回退到正则提取关键字段。同时引入 5 分钟响应缓存，相同查询直接复用已解析的结果，避免重复调用和解析开销。

### 挑战 2：三层系统的实时通信可靠性

**问题**：插件→后端→前端三层 WebSocket 链路中，任何一个环节断开都会导致告警丢失。后端短暂重启时，所有已连接玩家可能被误踢。

**解决**：
- 插件端实现指数退避重连（5s → 10s → 20s → 40s → 60s 上限）
- 插件维护本地封禁/白名单缓存（30 秒刷新），后端不可用时使用缓存判断
- 设计了三级降级策略（allow / deny / whitelist_only），默认放行避免误踢
- 前端 WebSocket 断线时自动重连并刷新页面数据

### 挑战 3：检测阈值与误判率的平衡

**问题**：阈值过严会误封正常玩家（如 PvP 高手 CPS 可达 12-15），阈值过松会漏检作弊者。

**解决**：
- 每个检测器独立配置阈值，支持运行时热更新
- 引入累积警告机制——单次触发只警告，多次触发才封禁
- 白名单机制让信任玩家完全豁免
- AI 分析作为二次确认，减少人工审核成本

---

## 项目结构

```
anticheatsystem/
├── src/                          # Minecraft 插件 (Spigot 1.20.4)
│   └── main/java/com/anticheat/
│       ├── AntiCheatPlugin.java       # 插件主类
│       ├── AntiCheatWebSocketClient.java  # WS 客户端（重连、心跳、消息路由）
│       ├── CacheManager.java          # 本地缓存（封禁/白名单，30s 刷新）
│       ├── ConfigManager.java         # 配置管理（支持从后端同步）
│       ├── PunishmentManager.java     # 渐进式惩罚（警告→临时→永久）
│       ├── PlayerJoinListener.java    # 入服校验（封禁检查 + 降级策略）
│       ├── FlyDetector.java           # 飞行检测
│       ├── SpeedDetector.java         # 速度检测
│       ├── AutoClickDetector.java     # 连点检测
│       ├── AimbotDetector.java        # 自瞄检测
│       ├── KillAuraDetector.java      # 杀戮光环检测
│       └── XrayDetector.java          # 透视检测
│
├── backend/                      # Spring Boot 后端 (Java 17)
│   └── src/main/java/com/anticheat/backend/
│       ├── controller/           # 12 个 REST 控制器
│       ├── service/              # 业务逻辑层
│       ├── repository/           # 12 个 JPA Repository
│       ├── model/                # JPA 实体（Player, CheatRecord, Punishment...）
│       ├── security/             # JWT 工具 + 过滤器 + API Key 过滤器 + 限流
│       ├── handler/              # WebSocket 处理器（实时广播 + 持久化）
│       ├── ai/                   # DeepSeek 集成（4 种分析模式 + 缓存 + SSE 流式）
│       └── config/               # Security, Swagger, WebSocket, CORS 配置
│
├── frontend/                     # Vue.js 3 前端
│   └── src/
│       ├── views/                # 11 个页面组件
│       ├── components/           # AI 助手等公共组件
│       ├── api/                  # Axios 封装 + 全部 API 接口定义
│       ├── router/               # 路由守卫 + 11 条路由
│       └── locales/              # 中英文国际化
│
├── docs/                         # 系统设计、数据库设计、部署指南、用户手册
├── scripts/                      # 测试脚本
└── TEST_REPORT.md                # 47 项测试用例审查报告
```

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

### 1. 数据库

```sql
CREATE DATABASE anticheat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 构建

```bash
# 克隆项目
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

编辑 `backend/src/main/resources/application.yml`，设置数据库连接和 JWT 密钥：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/anticheat
    username: root
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}          # 生产环境务必设置强随机值
  expiration: 86400000

api:
  key: ${API_KEY}                # 与插件 config.yml 保持一致

ai:
  enabled: true
  provider: deepseek
  api-key: ${AI_API_KEY}         # DeepSeek API Key
```

编辑 `src/main/resources/config.yml`，设置 API Key 与后端一致：

```yaml
api:
  key: ${API_KEY}

websocket:
  host: localhost
  port: 8080
```

### 4. 启动

```bash
# 终端 1 - 启动后端
cd backend
java -jar target/anticheat-backend-1.0-SNAPSHOT.jar

# 终端 2 - 启动前端
cd frontend
npm run dev
```

### 5. 安装插件

将 `target/AntiCheatPlugin-1.0-SNAPSHOT.jar` 复制到 Spigot 服务器的 `plugins/` 目录，重启服务器。

---

## 工程实践

这个项目遵循了以下工程原则：

- **配置外部化**：所有密钥、阈值、URL 均支持环境变量覆盖（`${ENV_VAR:default}` 模式），避免硬编码
- **降级设计**：后端不可用时插件自动使用本地缓存，不影响游戏正常运行
- **安全纵深**：JWT + API Key 双认证、BCrypt 密码哈希、常量时间 API Key 比较、IP 级别限流、CORS 白名单
- **可观测性**：审计日志记录所有管理员操作，检测事件全量持久化
- **测试先行**：通过 47 项系统性代码审查（安全、功能逻辑、代码质量、架构），识别并修复了多个生产级问题
- **国际化**：中英文双语支持，前后端均可切换

---

## 相关文档

- [系统设计文档](docs/system-design.md) — 完整架构设计、模块划分、核心流程
- [数据库设计](docs/database-design.md) — ER 图、表结构、索引策略
- [部署指南](docs/deployment-guide.md) — Docker Compose 生产部署
- [用户手册](docs/user-guide.md) — 管理面板使用说明
- [测试报告](TEST_REPORT.md) — 47 项审查结果与问题分级
- [项目展示](PROJECT_PORTFOLIO.md) — 面试用项目深度展示
- [面试准备](INTERVIEW_QA.md) — 常见面试问答

---

## 技术栈总览

| 层级 | 技术 | 用途 |
|------|------|------|
| 游戏插件 | Spigot API 1.20.4, Java-WebSocket, Gson | 作弊检测、WebSocket 通信 |
| 后端 | Spring Boot 3.2.4, Spring Security 6, JPA/Hibernate, Spring WebSocket | REST API、认证授权、实时推送 |
| 数据库 | MySQL 8.0, HikariCP | 数据持久化 |
| 安全 | JWT (jjwt 0.12.3), BCrypt, API Key | 双认证体系 |
| AI | DeepSeek API (OpenAI-compatible) | 作弊行为智能分析 |
| 前端 | Vue.js 3, Element Plus 2.5, ECharts 5.4, Axios, vue-i18n | 管理面板 |
| 构建 | Maven 3.6+, Vite 5.x | 项目构建 |
| 文档 | SpringDoc OpenAPI 2.3 (Swagger) | API 文档 |

---

## 许可证

MIT License — 详见 [LICENSE](LICENSE)
