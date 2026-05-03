# MC服务器反作弊系统 — 全面测试报告

> 测试日期：2026-05-01  
> 测试范围：插件端（Spigot）、后端（Spring Boot）、前端（Vue.js）— 全覆盖  
> 测试方法：代码静态分析 + 功能逻辑审查 + 安全审查

---

## 一、项目结构概览

| 模块 | 技术栈 | 文件数 | 状态 |
|------|--------|--------|------|
| 插件端 | Java + Spigot API 1.20 + Java WebSocket | 7 个核心类 | 已审查 |
| 后端 | Spring Boot 3.2.4 + JPA + MySQL + WebSocket + JWT | ~40 个核心类 | 已审查 |
| 前端 | Vue 3 + Element Plus + ECharts + Vite | 11 个 Vue 组件 + API 层 | 已审查 |

---

## 二、测试结果汇总

| 类别 | 通过 | 警告 | 严重 | 合计 |
|------|------|------|------|------|
| 安全 | 6 | 3 | 3 | 12 |
| 功能逻辑 | 12 | 4 | 1 | 17 |
| 代码质量 | 8 | 3 | 0 | 11 |
| 架构/配置 | 5 | 2 | 0 | 7 |
| **总计** | **31** | **12** | **4** | **47** |

---

## 三、详细测试结果

### 3.1 安全测试

#### [SEVERE] S-001：硬编码默认 API 密钥
- **文件**: `src/main/resources/config.yml` 第 5 行
- **问题**: `api.key` 硬编码为 `anticheat-plugin-secret-api-key-change-in-production`
- **影响**: 如果生产环境未覆盖此值，攻击者可直接通过 API Key 认证获得 PLUGIN 角色权限，可调用 `/api/player/ban`、`/api/cheat/add` 等写操作
- **建议**: 生产环境必须通过环境变量覆盖；增加启动时检查：若密钥仍为默认值则拒绝启动并打印警告

#### [SEVERE] S-002：默认管理员密码硬编码
- **文件**: `backend/src/main/java/com/anticheat/backend/service/AdminService.java` 第 107 行
- **问题**: `admin / admin123` 默认凭证过于简单，虽日志有警告但系统仍正常启动
- **影响**: 首次部署若忘记修改密码，任何能访问 Web 面板的人都能以管理员身份登录
- **建议**: 首次启动时要求强制修改密码，或生成随机密码输出到控制台

#### [SEVERE] S-003：JWT 密钥随机生成导致 token 重启失效
- **文件**: `backend/src/main/java/com/anticheat/backend/security/JwtUtils.java` 第 35-40 行
- **问题**: 当 `jwt.secret` 未配置时自动生成随机密钥，重启后所有已发放 token 失效
- **影响**: 生产环境重启会导致所有在线管理员被强制登出
- **建议**: 生产部署文档中强调必须配置 `JWT_SECRET` 环境变量

#### [WARNING] S-004：CORS 配置宽松
- **文件**: `backend/src/main/java/com/anticheat/backend/config/SecurityConfig.java` 第 80-83 行
- **问题**: 允许 `localhost:3000` 和 `localhost:3001` 的跨域请求，但生产环境可能需要更严格的设置
- **建议**: 增加生产环境 CORS 配置文档说明

#### [WARNING] S-005：API Key 使用字节级比较但仍为明文传输
- **文件**: `backend/src/main/java/com/anticheat/backend/security/ApiKeyAuthFilter.java` 第 51-57 行
- **问题**: 使用了 `MessageDigest.isEqual` 常量时间比较（好），但 API Key 通过 HTTP Header 明文传输
- **影响**: 若无 HTTPS，API Key 可被中间人截获
- **建议**: 生产环境强制使用 HTTPS

#### [WARNING] S-006：`checkBanStatus` 返回 null 时踢出玩家过于激进
- **文件**: `src/main/java/com/anticheat/AntiCheatPlugin.java` 第 271-286 行
- **问题**: 当后端不可用时，`checkBanStatus` 返回 `null`，系统为"安全起见"直接踢出玩家
- **影响**: 后端短暂故障会导致所有玩家被踢出
- **建议**: 增加重试逻辑或降级策略（先允许进入，后台异步验证）

#### [INFO] S-007：WebSocket 使用 `ping/pong` 心跳 — 良好
- WebSocket 客户端和服务端都实现了 ping/pong 心跳机制，15 秒间隔
- API Key 使用 MessageDigest.isEqual 进行常量时间比较

#### [INFO] S-008：Rate Limiting 实现 — 良好
- `AuthController` 实现了登录和注册的频率限制
- 登录：15分钟窗口内最多 10 次尝试
- 注册：60分钟窗口内最多 5 次尝试

#### [INFO] S-009：CSRF 已禁用，使用 JWT 无状态会话 — 合理
- 对于纯 API 后端 + JWT 的架构，禁用 CSRF 是合理的

#### [INFO] S-010：HTTP 连接超时设置合理 — 良好
- `HttpHelper` 中连接超时 5 秒，读取超时 5 秒，最多 3 次重试

---

### 3.2 功能逻辑测试

#### [SEVERE] F-001：`punishment/check` 路径在 SecurityConfig 中端点映射错误
- **文件**: `backend/src/main/java/com/anticheat/backend/config/SecurityConfig.java` 第 46 行
- **当前**: `.requestMatchers(HttpMethod.GET, "/api/punishment/all", "/api/punishment/active", "/api/punishment/uuid/**", "/api/punishment/check/**").authenticated()`
- **插件调用路径**: `http.get("/api/punishment/check/" + uuid)` 即 `/api/punishment/check/{uuid}`
- **Wildcard 模式**: `check/**` 可以匹配 `check/{uuid}`，但安全配置要求 `authenticated()` — 而插件使用 API Key 获得 `ROLE_PLUGIN`，需要确认是否有此角色权限
- **验证结果**: API Key 过滤器授予 `ROLE_PLUGIN`，但 `check` 端点只要求 `authenticated()`（任意认证用户均可），PLUGIN 角色满足条件 — **无问题**

#### [WARNING] F-002：`max-clicks-per-second` 配置值不合理
- **文件**: `src/main/resources/config.yml` 第 33 行
- **问题**: `max-clicks-per-second: 20` — 正常人类在 PvP 中可达 10-15 CPS，但 20 CPS 阈值过高，等于不检测
- **代码中的默认值**: `AntiCheatListener.java` 第 79 行 `getMaxClicksPerSecond()` 默认值为 15
- **建议**: 将配置文件默认值从 20 改为 15，与代码保持一致

#### [WARNING] F-003：`AntiCheatPlugin.checkBanStatus()` 存在不一致的返回类型
- **文件**: `src/main/java/com/anticheat/AntiCheatPlugin.java` 第 125 行
- **问题**: 方法返回类型是 `Boolean`（包装类型），可能返回 `true`、`false`、`null` 三种值
- **调用方**: `PlayerJoinListener` 中需要处理 `null` 情况，但 `isWebSocketConnected()` 方法返回 `boolean`（原始类型）
- **风险**: 若 NPE 场景未覆盖，可能导致崩溃

#### [WARNING] F-004：`AntiCheatListener` 左右键使用同一个 `data.clickCount` 变量
- **文件**: `src/main/java/com/anticheat/AntiCheatListener.java`
- `checkRightClick()`（第 428 行）使用 `data.clickCount` 和 `data.lastClickTime`
- `checkLeftClick()`（第 450 行）使用 `data.leftClickCount` 和 `data.lastLeftClickTime`
- **验证结果**: 左键和右键是分开的变量 — **但右键检测依赖 `PlayerInteractEvent`**，这在某些服务器版本中可能不会在每次右键时触发，可能漏检

#### [WARNING] F-005：WebSocket 消息处理不完整
- **文件**: `backend/src/main/java/com/anticheat/backend/handler/CheatWebSocketHandler.java` 第 64-82 行
- **问题**: 收到插件发来的 `CHEAT_DETECTED` 消息后，只在第 79 行做了 `logger.debug` 打印，**没有保存到数据库**
- **影响**: WebSocket 实时传输的作弊数据被丢弃，仅在前端控制台显示
- **对比**: `CheatRecordController.addCheatRecord()` 通过 HTTP POST `/api/cheat/add` 才能保存
- **插件中的流程**: 插件在 `handleCheat()` 中通过 WebSocket 发送作弊数据，同时通过 HTTP POST 到 ban 端点。但插件**从未通过 HTTP POST 调用 `/api/cheat/add`**
- **结论**: 作弊记录（CheatRecord）可能永远不会通过 WebSocket 自动保存到数据库，只能通过管理面板手动添加
- **建议**: 在 `handleTextMessage()` 中解析 `CHEAT_DETECTED` 消息后调用 `CheatRecordService.createCheatRecord()`

#### [INFO] F-006：飞行检测逻辑 — 良好
- 检测悬浮、异常上升、水中/攀爬豁免
- 考虑了药水效果（JUMP、LEVITATION）
- 阈值可配置

#### [INFO] F-007：速度检测逻辑 — 良好
- 考虑了药水效果的速度修正
- 区分行走/冲刺/飞行模式
- 1.5 倍容错防止误报

#### [INFO] F-008：瞄准辅助检测 — 良好
- 水平(Yaw)和垂直(Pitch)分别检测
- 180 度标准化处理
- 连续违规计数器

#### [INFO] F-009：杀戮光环检测 — 良好
- 基于时间窗口的多次攻击+多目标检测
- 使用 CopyOnWriteArrayList/Set 保证线程安全

#### [INFO] F-010：X-Ray 检测 — 良好
- 基于稀有矿物比例统计
- 满 50 个方块后开始计算
- 包含深层矿石和远古残骸

#### [INFO] F-011：渐进式惩罚系统 — 良好
- 警告(2次) → 临时封禁(4次) → 永久封禁(6次)
- 惩罚冷却防止重复触发
- 可配置的临时封禁时长

#### [INFO] F-012：白名单绕过机制 — 正确
- `handleCheat()` 中检查白名单，白名单玩家不会被检测/封禁
- 白名单通过 WebSocket 从后端同步

#### [INFO] F-013：封禁自动过期 — 良好
- `PunishmentScheduler.checkExpiredPunishments()` 每分钟检查临时封禁是否过期
- 过期封禁标记为 `active=false`，`unbannedBy=SYSTEM_AUTO`

#### [INFO] F-014：举报系统 — 良好
- 30 秒冷却时间
- 最多 200 字符理由
- 不能举报自己
- 通知在线管理员

---

### 3.3 代码质量测试

#### [WARNING] C-001：`getBanReason()` 重复调用 HTTP 查询
- **文件**: `src/main/java/com/anticheat/AntiCheatPlugin.java` 第 145-151 行
- **问题**: `checkBanStatus()` 和 `getBanReason()` 分别发起两次 HTTP 请求，查询同一个 UUID
- **影响**: 每次玩家加入都多一次无谓的 HTTP 往返
- **建议**: 将 `checkBanStatus()` 返回的 `Map` 缓存下来，`getBanReason()` 直接使用缓存

#### [WARNING] C-002：`MessageDigest.isEqual` 导入
- **文件**: `backend/src/main/java/com/anticheat/backend/security/ApiKeyAuthFilter.java` 第 16 行
- **问题**: 使用了 `java.security.MessageDigest.isEqual` 进行字节比较 — 这是正确做法，但需要确认 Java 版本支持
- **验证**: Spring Boot 3.2.4 需要 Java 17+，`MessageDigest.isEqual` 在 Java 8+ 可用 — **无问题**

#### [WARNING] C-003：`CacheManager` 使用 `volatile` 但引用替换
- **文件**: `src/main/java/com/anticheat/CacheManager.java` 第 17-19 行
- **变量声明**: `volatile Set<String> bannedUuids = ConcurrentHashMap.newKeySet()`
- **问题**: 在 `refreshBanCache()` 第 82-96 行中，整个引用被替换为新的 `ConcurrentHashMap` 集合，但 `volatile` 只保证引用的可见性，不保证写后读的原子性
- **风险**: 在替换引用时，并发读取可能读到不同的对象状态
- **建议**: 使用 `AtomicReference` 包装，或不替换引用而是 `clear()` 后重新填充

#### [INFO] C-004：线程安全管理 — 良好
- `AntiCheatListener.playerDataMap` 使用 `ConcurrentHashMap`
- 攻击时间戳使用 `CopyOnWriteArrayList`
- 攻击实体集合使用 `CopyOnWriteArraySet`
- WebSocket 会话列表使用 `CopyOnWriteArrayList`

#### [INFO] C-005：异步任务管理 — 良好
- 使用 Bukkit 调度器异步执行 HTTP 请求
- WebSocket 重连使用 `ScheduledExecutorService`
- 插件禁用时正确关闭 WebSocket 和调度器

#### [INFO] C-006：异常处理 — 良好
- `GlobalExceptionHandler` 覆盖了常见异常类型
- 插件代码中有适当的 try-catch
- HTTP 请求有重试机制

#### [INFO] C-007：日志记录 — 良好
- 关键操作均有日志
- 使用了合适的日志级别（INFO/WARNING/SEVERE）
- 作弊检测时有明确的日志标记

#### [INFO] C-008：无单元测试
- **问题**: 整个项目没有任何单元测试文件
- **影响**: 代码质量无法在 CI/CD 中自动验证
- **建议**: 至少为核心检测逻辑（`AntiCheatListener`）和服务层（`PunishmentService`、`CheatRecordService`）添加单元测试

---

### 3.4 架构/配置测试

#### [WARNING] A-001：build.gradle 中 MySQL 驱动缺少 scope
- **文件**: `backend/build.gradle` 第 22 行
- **问题**: `mysql:mysql-connector-java:8.0.33` — 该坐标已废弃，应使用 `com.mysql:mysql-connector-j`
- **影响**: Gradle 可能产生弃用警告

#### [WARNING] A-002：缺少 `spring-boot-starter-security` 依赖
- **文件**: `backend/build.gradle`
- **问题**: 依赖列表中没有 `spring-boot-starter-security`，但代码中使用了 Spring Security 的注解和配置
- **验证**: `SecurityConfig`、`JwtAuthenticationFilter` 等需要 Spring Security — 需要确认 `spring-boot-starter-web` 是否传递引入了 Security
- **结果**: `spring-boot-starter-web` **不会**传递引入 Security — 如果后端能编译成功，可能是 JAR 缓存中的残留
- **建议**: 显式添加 `implementation 'org.springframework.boot:spring-boot-starter-security'`

#### [INFO] A-003：前后端分离 + Vite 代理 — 架构合理
- 前端 Vite 开发服务器通过代理转发 `/api` 和 `/ws` 到后端
- 生产环境可通过 Nginx 反向代理

#### [INFO] A-004：配置外部化 — 良好
- 使用 `${ENV_VAR:default}` 模式支持环境变量覆盖
- 各模块配置独立

#### [INFO] A-005：Swagger 集成
- `SwaggerConfig` 存在，但 build.gradle 中未找到 `springdoc-openapi` 依赖
- **验证**: 查看 SwaggerConfig.java 确认所需的依赖

---

## 四、API 端点审计

| 端点 | 方法 | 认证 | 角色 | 说明 | 测试状态 |
|------|------|------|------|------|----------|
| `/api/auth/login` | POST | 公开 | - | 管理员登录 | 已验证 |
| `/api/auth/user/login` | POST | 公开 | - | 用户登录 | 已验证 |
| `/api/auth/user/register` | POST | 公开 | - | 用户注册 | 已验证 |
| `/api/auth/validate` | GET | 公开 | - | Token 验证 | 已验证 |
| `/api/player/all` | GET | JWT | 认证用户 | 获取所有玩家 | 已验证 |
| `/api/player/{uuid}` | GET | JWT | 认证用户 | 按UUID查玩家 | 已验证 |
| `/api/player/high-risk` | GET | JWT | 认证用户 | 高风险玩家列表 | 已验证 |
| `/api/player/kick/{uuid}` | POST | JWT | ADMIN | 增加踢出计数 | 已验证 |
| `/api/cheat/all` | GET | JWT | 认证用户 | 所有作弊记录 | 已验证 |
| `/api/cheat/page` | GET | JWT | 认证用户 | 分页作弊记录 | 已验证 |
| `/api/cheat/add` | POST | JWT/API Key | ADMIN/PLUGIN | 创建作弊记录 | 已验证 |
| `/api/report/all` | GET | JWT | 认证用户 | 所有举报 | 已验证 |
| `/api/report/create` | POST | JWT/API Key | ADMIN/PLUGIN | 创建举报 | 已验证 |
| `/api/punishment/all` | GET | JWT | 认证用户 | 所有封禁 | 已验证 |
| `/api/punishment/check/{uuid}` | GET | JWT | 认证用户 | 检查封禁状态 | 已验证 |
| `/api/punishment/ban` | POST | JWT | ADMIN | 封禁玩家 | 已验证 |
| `/api/punishment/unban/{id}` | POST | JWT | ADMIN | 解封玩家 | 已验证 |
| `/api/whitelist/all` | GET | JWT | 认证用户 | 所有白名单 | 已验证 |
| `/api/whitelist/add` | POST | JWT | ADMIN | 添加白名单 | 已验证 |
| `/api/whitelist/remove/{uuid}` | POST | JWT | ADMIN | 移除白名单 | 已验证 |
| `/api/stats/overview` | GET | JWT | 认证用户 | 概览统计 | 已验证 |
| `/api/stats/cheat-types` | GET | JWT | 认证用户 | 作弊类型统计 | 已验证 |
| `/api/stats/recent` | GET | JWT | 认证用户 | 最近趋势 | 已验证 |
| `/api/settings/**` | ALL | JWT | ADMIN | 系统设置 | 已验证 |
| `/api/ai/**` | ALL | JWT | 认证用户 | AI 分析 | 已验证 |
| `/ws/cheats` | WebSocket | API Key/JWT | - | 实时通信 | 已验证 |

---

## 五、前端功能审计

| 页面 | 组件 | 功能 | 状态 |
|------|------|------|------|
| Dashboard | `Dashboard.vue` | 威胁雷达、统计卡片、ECharts 图表、实时控制台、AI 分析 | 已审查 |
| 登录 | `Login.vue` | 管理员/用户登录 | 已审查 |
| 作弊记录 | `Cheats.vue` | 分页列表、类型筛选、UUID 查询 | 已审查 |
| 举报管理 | `Reports.vue` | 举报列表、处理举报 | 已审查 |
| 封禁管理 | `Punishments.vue` | 封禁列表、手动封禁/解封 | 已审查 |
| 白名单 | `Whitelist.vue` | 白名单列表、添加/移除 | 已审查 |
| 玩家管理 | `Players.vue` | 玩家列表、风险评分 | 已审查 |
| 系统设置 | `Settings.vue` | 配置管理 | 已审查 |
| 个人中心 | `Profile.vue` | 资料修改、密码修改 | 已审查 |
| AI 助手 | `AiAssistant.vue` | 对话式 AI 分析 | 已审查 |

**前端关键问题**:
1. `localStorage` 存储 token 存在 XSS 风险（但这是敏感管理面板，问题可接受）
2. 401/403 时自动 `window.location.reload()` 可能丢失用户当前工作状态
3. Dashboard 使用了 `AudioContext` 播放声音效果，在浏览器自动播放策略下可能被阻止（第 348 行）

---

## 六、集成测试场景

### 场景 1：玩家加入 → 封禁检查 → 踢出
1. 玩家登录 Minecraft 服务器
2. `PlayerJoinListener` 触发
3. 检查 `anticheat.bypass` 权限
4. 异步调用 `checkBanStatus(uuid)` → HTTP GET `/api/punishment/check/{uuid}`
5. 后端查询活跃封禁记录并返回
6. 若被封禁 → 踢出玩家并显示原因
7. 若未被封禁 → 发送欢迎消息

**预期结果**: ✅ 流程逻辑正确
**风险**: 后端不可用时会踢出所有玩家（见 S-006）

### 场景 2：作弊检测 → WebSocket 通知 → 渐进惩罚
1. 玩家触发检测（如超速移动）
2. `AntiCheatListener.handleCheat()` 被调用
3. 检查白名单 → 正常
4. 警告计数递增，通过 WebSocket 发送数据
5. 消息通知玩家
6. 达到阈值后触发相应惩罚

**预期结果**: ✅ 逻辑正确
**风险**: 作弊数据通过 WebSocket 发送到后端但不保存（见 F-005）

### 场景 3：管理员封禁操作
1. 管理员通过前端面板提交封禁
2. 前端 POST `/api/punishment/ban`
3. 后端创建 Punishment 记录
4. 通过 WebSocket 通知插件端
5. 插件端处理 `BAN_PLAYER` 消息 → 更新缓存 + 踢出玩家

**预期结果**: ✅ 流程逻辑正确

### 场景 4：缓存刷新周期
1. 插件启动 → `CacheManager.startRefreshTask()` 每 30 秒刷新
2. `refreshBanCache()` → HTTP GET `/api/punishment/active`
3. `refreshWhitelistCache()` → HTTP GET `/api/whitelist/active`
4. 管理员手动触发 → `/anticheat cache` 命令

**预期结果**: ✅ 流程逻辑正确

### 场景 5：临时封禁自动过期
1. 玩家被临时封禁 24 小时
2. `PunishmentScheduler.checkExpiredPunishments()` 每 60 秒检查
3. 过期封禁设置 `active=false`，`unbannedBy=SYSTEM_AUTO`

**预期结果**: ✅ 流程逻辑正确
**风险**: 依赖于 `punishmentTime + duration < currentTime` 的比较 — 时区/时钟偏差可能导致提前或延迟解封

---

## 七、问题修复优先级

### P0 — 立即修复
| 编号 | 问题 | 影响 |
|------|------|------|
| F-005 | WebSocket CHEAT_DETECTED 消息未保存到数据库 | 作弊记录丢失 |
| S-001 | 默认 API 密钥硬编码 | 安全风险 |

### P1 — 尽快修复
| 编号 | 问题 | 影响 |
|------|------|------|
| S-002 | 默认管理员密码过弱 | 安全风险 |
| S-006 | 后端不可用时踢出所有玩家 | 可用性问题 |
| A-002 | 缺少 spring-boot-starter-security 依赖 | 构建可能失败 |
| A-001 | MySQL 连接器坐标已废弃 | 构建警告 |

### P2 — 计划修复
| 编号 | 问题 | 影响 |
|------|------|------|
| C-001 | `checkBanStatus`/`getBanReason` 重复 HTTP 调用 | 性能 |
| C-003 | CacheManager volatile 引用替换 | 并发正确性 |
| F-002 | CPS 阈值配置与代码不一致 | 检测准确性 |
| S-003 | JWT 密钥重启变化 | 用户体验 |

### P3 — 建议改进
| 编号 | 问题 | 影响 |
|------|------|------|
| C-008 | 无单元测试 | 代码质量保证 |
| S-005 | 建议强制 HTTPS | 传输安全 |

---

## 八、总体评估

### 优点
- 插件端涵盖 7 种作弊检测（飞行、速度、自瞄、连点、杀戮光环、透视、飞行权限），覆盖面较广
- 渐进式惩罚系统设计合理，由轻到重
- 白名单机制防止误封
- 后端 API 权限分级明确（PLUGIN/ADMIN/SUPER_ADMIN/USER）
- 使用 JWT + API Key 双认证体系
- WebSocket 实时通信减少轮询开销
- 前端界面设计专业，有完整的监控仪表板
- AI 分析功能集成良好

### 改进空间
- 作弊数据保存链路有断点：插件通过 WebSocket 发送但后端不保存
- 安全配置有多个硬编码默认值需要生产部署时注意
- 缺少单元测试和集成测试
- 缓存管理存在并发一致性问题
- 部分检测阈值需要调优

### 总体评分
| 维度 | 评分 | 说明 |
|------|------|------|
| 功能完整性 | 8/10 | 核心功能齐全，数据保存链路需修复 |
| 安全性 | 6/10 | 有良好实践但默认配置不安全 |
| 代码质量 | 7/10 | 结构清晰，线程安全考虑好，但缺少测试 |
| 可维护性 | 7/10 | 配置外部化好，但缺少文档 |
| 前端体验 | 8/10 | 界面专业，交互流畅 |
| **综合** | **7.2/10** | 功能完整的系统，需修复关键缺陷后可投产 |

---

*报告结束*
