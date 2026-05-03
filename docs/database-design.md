# 数据库设计文档

## 1. 概述

本文档描述 AntiCheat 反作弊系统的数据库设计，包括实体关系图(ER图)和详细表结构。

### 1.1 数据库选型

- **开发环境**: H2 嵌入式数据库
- **生产环境**: MySQL 8.0+

### 1.2 设计原则

- 采用 JPA (Jakarta Persistence API) 规范
- 使用自增主键
- 时间字段使用 Unix 时间戳 (毫秒)
- 外键关联采用逻辑外键

---

## 2. 实体关系图 (ER Diagram)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           AntiCheat 数据库 ER 图                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐       ┌──────────────┐
│    admins    │       │    users     │
├──────────────┤       ├──────────────┤
│ PK id        │       │ PK id        │
│    username  │       │    username  │
│    password  │       │    password  │
│    nickname  │       │    nickname  │
│    email     │       │    email     │
│    avatar    │       │    avatar    │
│    role      │       │    mc_username│
│    created_  │       │    mc_uuid   │
│    last_     │       │    created_  │
│    active    │       │    last_     │
└──────────────┘       │    active    │
                       └──────────────┘

┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   players    │──1:N──│cheat_records │       │ punishments  │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ PK id        │       │ PK id        │       │ PK id        │
│    player_   │       │ FK player_id │──┐    │ FK player_id │──┐
│    uuid      │       │    cheat_type│  │    │    punish_   │  │
│    risk_     │       │    detection_│  │    │    punish_   │  │
│    last_seen │       │    severity  │  │    │    duration  │  │
│    kick_     │       │    details   │  │    │    reason    │  │
└──────────────┘       └──────────────┘  │    │    active    │  │
       ▲                                 │    │    unbanned_ │  │
       │                                 │    │    unbanned_ │  │
       └─────────────────────────────────┘    └──────────────┘  │
              (player_id 外键关联)                                │
              ┌─────────────────────────────────────────────────┘
              │
┌──────────────┐       ┌──────────────┐
│   whitelist  │       │   reports    │
├──────────────┤       ├──────────────┤
│ PK id        │       │ PK id        │
│    player_   │       │    reporter_ │
│    uuid      │       │    reporter_ │
│    reason    │       │    reported_ │
│    added_by  │       │    reported_ │
│    added_    │       │    reason    │
│    active    │       │    report_   │
└──────────────┘       │    report_   │
                       │    status    │
┌──────────────┐       │    handled_  │
│system_settings│      │    handled_  │
├──────────────┤       │    result    │
│ PK id        │       └──────────────┘
│    setting_  │
│    setting_  │
│    description│
└──────────────┘
```

---

## 3. 表结构详细设计

### 3.1 管理员表 (admins)

存储系统管理员账户信息。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码(BCrypt加密) |
| nickname | VARCHAR(100) | | 昵称 |
| email | VARCHAR(100) | | 邮箱 |
| avatar | VARCHAR(255) | | 头像URL |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'ADMIN' | 角色 |
| created_time | BIGINT | NOT NULL | 创建时间戳 |
| last_login_time | BIGINT | | 最后登录时间戳 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否激活 |

**索引设计:**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_username (username)

---

### 3.2 用户表 (users)

存储普通用户账户信息。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码(BCrypt加密) |
| nickname | VARCHAR(100) | | 昵称 |
| email | VARCHAR(100) | | 邮箱 |
| avatar | VARCHAR(255) | | 头像URL |
| mc_username | VARCHAR(100) | | Minecraft用户名 |
| mc_uuid | VARCHAR(36) | | Minecraft UUID |
| created_time | BIGINT | NOT NULL | 创建时间戳 |
| last_login_time | BIGINT | | 最后登录时间戳 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否激活 |

**索引设计:**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_username (username)

---

### 3.3 玩家表 (players)

存储被监控的Minecraft玩家信息。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| player_name | VARCHAR(100) | UNIQUE, NOT NULL | 玩家名称 |
| uuid | VARCHAR(36) | UNIQUE, NOT NULL | Minecraft UUID |
| risk_score | INT | NOT NULL, DEFAULT 0 | 风险评分(0-100) |
| last_seen | BIGINT | | 最后在线时间戳 |
| kick_count | INT | NOT NULL, DEFAULT 0 | 被踢次数 |

**索引设计:**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_player_name (player_name)
- UNIQUE INDEX idx_uuid (uuid)
- INDEX idx_risk_score (risk_score)

---

### 3.4 作弊记录表 (cheat_records)

存储玩家作弊检测记录。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| player_id | BIGINT | FK, NOT NULL | 关联玩家ID |
| cheat_type | VARCHAR(50) | NOT NULL | 作弊类型 |
| detection_time | BIGINT | NOT NULL | 检测时间戳 |
| severity | INT | NOT NULL | 严重程度(1-10) |
| details | TEXT | | 详细信息 |

**作弊类型枚举:**
- KILLAURA - 杀戮光环
- FLY - 飞行作弊
- SPEED - 加速作弊
- XRAY - 透视作弊
- AUTOCLICKER - 自动点击
- REACH - 攻击距离作弊
- OTHER - 其他

**索引设计:**
- PRIMARY KEY (id)
- INDEX idx_player_id (player_id)
- INDEX idx_detection_time (detection_time)
- INDEX idx_cheat_type (cheat_type)

---

### 3.5 封禁记录表 (punishments)

存储玩家封禁记录。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| player_id | BIGINT | FK, NOT NULL | 关联玩家ID |
| punishment_type | VARCHAR(20) | NOT NULL | 封禁类型 |
| punishment_time | BIGINT | NOT NULL | 封禁时间戳 |
| duration | BIGINT | | 封禁时长(毫秒)，永久封禁为空 |
| reason | VARCHAR(255) | | 封禁原因 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否生效 |
| unbanned_time | BIGINT | | 解封时间戳 |
| unbanned_by | VARCHAR(50) | | 解封操作人 |

**封禁类型枚举:**
- PERMANENT - 永久封禁
- TEMPORARY - 临时封禁

**索引设计:**
- PRIMARY KEY (id)
- INDEX idx_player_id (player_id)
- INDEX idx_active (active)
- INDEX idx_punishment_time (punishment_time)

---

### 3.6 举报记录表 (reports)

存储玩家举报记录。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| reporter_name | VARCHAR(100) | NOT NULL | 举报人名称 |
| reporter_uuid | VARCHAR(36) | NOT NULL | 举报人UUID |
| reported_name | VARCHAR(100) | NOT NULL | 被举报人名称 |
| reported_uuid | VARCHAR(36) | NOT NULL | 被举报人UUID |
| reason | VARCHAR(255) | NOT NULL | 举报原因 |
| report_type | VARCHAR(50) | | 举报类型 |
| report_time | BIGINT | NOT NULL | 举报时间戳 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 处理状态 |
| handled_by | VARCHAR(50) | | 处理人 |
| handled_time | BIGINT | | 处理时间戳 |
| result | VARCHAR(255) | | 处理结果 |

**状态枚举:**
- PENDING - 待处理
- RESOLVED - 已解决
- REJECTED - 已驳回

**索引设计:**
- PRIMARY KEY (id)
- INDEX idx_status (status)
- INDEX idx_reported_uuid (reported_uuid)
- INDEX idx_report_time (report_time)

---

### 3.7 白名单表 (whitelist)

存储白名单玩家信息。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| player_name | VARCHAR(100) | NOT NULL | 玩家名称 |
| uuid | VARCHAR(36) | UNIQUE, NOT NULL | Minecraft UUID |
| reason | VARCHAR(255) | | 加入原因 |
| added_by | VARCHAR(50) | | 添加人 |
| added_time | BIGINT | NOT NULL | 添加时间戳 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否生效 |

**索引设计:**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_uuid (uuid)
- INDEX idx_active (active)

---

### 3.8 系统设置表 (system_settings)

存储系统配置参数。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| setting_key | VARCHAR(100) | UNIQUE, NOT NULL | 配置键 |
| setting_value | VARCHAR(500) | NOT NULL | 配置值 |
| description | VARCHAR(255) | | 配置说明 |

**预设配置项:**
| setting_key | setting_value | description |
|-------------|---------------|-------------|
| auto_ban_enabled | true | 自动封禁开关 |
| auto_ban_threshold | 100 | 自动封禁风险阈值 |
| max_risk_score | 100 | 最大风险评分 |
| alert_webhook | | 告警Webhook地址 |

---

## 4. 数据关系说明

### 4.1 核心关系

```
players (1) ──────< (N) cheat_records
    │                   关联字段: player_id
    │
    └──────< (N) punishments
                        关联字段: player_id
```

### 4.2 独立实体

以下表为独立实体，不与其他表建立外键关系：
- `admins` - 管理员账户
- `users` - 用户账户
- `reports` - 举报记录
- `whitelist` - 白名单
- `system_settings` - 系统设置

---

## 5. 数据库初始化脚本

### 5.1 MySQL 建表脚本

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS anticheat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE anticheat;

-- 管理员表
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    email VARCHAR(100),
    avatar VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    created_time BIGINT NOT NULL,
    last_login_time BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    email VARCHAR(100),
    avatar VARCHAR(255),
    mc_username VARCHAR(100),
    mc_uuid VARCHAR(36),
    created_time BIGINT NOT NULL,
    last_login_time BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 玩家表
CREATE TABLE players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL UNIQUE,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    risk_score INT NOT NULL DEFAULT 0,
    last_seen BIGINT,
    kick_count INT NOT NULL DEFAULT 0,
    INDEX idx_player_name (player_name),
    INDEX idx_uuid (uuid),
    INDEX idx_risk_score (risk_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 作弊记录表
CREATE TABLE cheat_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    cheat_type VARCHAR(50) NOT NULL,
    detection_time BIGINT NOT NULL,
    severity INT NOT NULL,
    details TEXT,
    INDEX idx_player_id (player_id),
    INDEX idx_detection_time (detection_time),
    INDEX idx_cheat_type (cheat_type),
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 封禁记录表
CREATE TABLE punishments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    punishment_type VARCHAR(20) NOT NULL,
    punishment_time BIGINT NOT NULL,
    duration BIGINT,
    reason VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    unbanned_time BIGINT,
    unbanned_by VARCHAR(50),
    INDEX idx_player_id (player_id),
    INDEX idx_active (active),
    INDEX idx_punishment_time (punishment_time),
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 举报记录表
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_name VARCHAR(100) NOT NULL,
    reporter_uuid VARCHAR(36) NOT NULL,
    reported_name VARCHAR(100) NOT NULL,
    reported_uuid VARCHAR(36) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    report_type VARCHAR(50),
    report_time BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    handled_by VARCHAR(50),
    handled_time BIGINT,
    result VARCHAR(255),
    INDEX idx_status (status),
    INDEX idx_reported_uuid (reported_uuid),
    INDEX idx_report_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 白名单表
CREATE TABLE whitelist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    reason VARCHAR(255),
    added_by VARCHAR(50),
    added_time BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_uuid (uuid),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统设置表
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO admins (username, password, nickname, email, role, created_time, active)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@localhost', 'ADMIN', UNIX_TIMESTAMP()*1000, TRUE);

-- 初始化系统设置
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('auto_ban_enabled', 'true', '自动封禁开关'),
('auto_ban_threshold', '100', '自动封禁风险阈值'),
('max_risk_score', '100', '最大风险评分');
```

---

## 6. 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| 1.0 | 2024-04-11 | 初始版本 | AntiCheat Team |
