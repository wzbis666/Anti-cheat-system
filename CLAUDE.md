# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI-driven Minecraft anti-cheat system with three modules:
- **Minecraft Plugin** (`src/`) — Spigot 1.20.4 plugin with 6 cheat detectors, communicates with backend via WebSocket + HTTP
- **Spring Boot Backend** (`backend/`) — REST API, WebSocket relay, JWT/API-Key auth, MySQL persistence, DeepSeek AI integration
- **Vue.js 3 Frontend** (`frontend/`) — Admin dashboard with real-time monitoring, Element Plus, ECharts, i18n (zh/en)

## Build & Run

```bash
# Plugin (root pom.xml)
mvn clean package -DskipTests          # produces target/AntiCheatPlugin-1.0-SNAPSHOT.jar

# Backend
cd backend && mvn clean package -DskipTests   # produces backend/target/anticheat-backend-1.0-SNAPSHOT.jar
java -jar backend/target/anticheat-backend-1.0-SNAPSHOT.jar

# Frontend
cd frontend && npm install
npm run dev        # dev server on port 3030, proxies /api and /ws to localhost:8080
npm run build      # production build to frontend/dist/
```

Java 17+, Node 18+, MySQL 8.0+ required. Database `anticheat` is auto-created by JPA (`ddl-auto: update`).

## Architecture

### Plugin Detection Flow
```
Bukkit Event → Detector (extends AbstractDetector) → PunishmentManager (progressive: warn→temp ban→perm ban)
                                                    → WebSocket (CHEAT_DETECTED message to backend)
```

All 6 detectors (`FlyDetector`, `SpeedDetector`, `AimbotDetector`, `AutoClickDetector`, `KillAuraDetector`, `XrayDetector`) extend `AbstractDetector`, which provides `shouldSkipCheck()` (bypass permission), `shouldSkipGameMode()` (creative/spectator exempt), and `isEnabled()` (config toggle).

### Authentication
- **JWT** — human admins via `JwtAuthenticationFilter` (Bearer token, BCrypt-hashed passwords)
- **API Key** — plugin-to-backend via `ApiKeyAuthFilter` (constant-time comparison via `MessageDigest.isEqual`)
- **Rate limiting** — `RateLimitFilter` applied before both auth filters
- Security filter chain order: RateLimit → ApiKey → JWT → UsernamePasswordAuthenticationFilter

### WebSocket Architecture
Single endpoint `/ws/cheats` handled by `CheatWebSocketHandler`:
1. Plugin connects with API Key header, sends `CHEAT_DETECTED` messages → backend persists via `CheatRecordService` then broadcasts to all connected sessions
2. Frontend connects with JWT token (query param), receives broadcasts in real time
3. Backend can send commands to plugin: `KICK_PLAYER`, `BAN_PLAYER`, `UNBAN_PLAYER`, `WHITELIST_ADD/REMOVE`, `REFRESH_CACHE`, `CONFIG_UPDATE`, `CONFIG_SYNC`
4. Max 100 concurrent connections, 64KB message limit, 15s heartbeat (plugin side)

### Plugin Degradation Strategy
On player join, if backend is unreachable, `PlayerJoinListener` applies the configurable `fallback.strategy`:
- `allow` (default) — let everyone in
- `deny` — kick everyone
- `whitelist_only` — only allow cached-whitelist players

### Plugin Cache
`CacheManager` maintains `ConcurrentHashMap` of banned UUIDs and a `Set` of whitelisted UUIDs. Refreshes every 30 seconds via HTTP GET `/api/punishment/active` and `/api/whitelist/active`. On plugin enable, also refreshes immediately. Ban/whitelist changes from backend WebSocket commands update the cache instantly.

### AI Service
`AiService` calls DeepSeek API (OpenAI-compatible) with 4 analysis modes: cheat analysis, report analysis, dashboard assessment, ban evaluation. Features:
- 5-minute response cache (keyed by type+UUID+data hash)
- 30-minute session TTL for multi-turn chat, max 20 messages per session
- SSE streaming for chat via `streamChat()`
- Multi-layer JSON parsing: tries ```json block → ``` block → regex fallback

## Configuration

All sensitive values use `${ENV_VAR:default}` pattern in `backend/src/main/resources/application.yml`:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — MySQL connection
- `JWT_SECRET`, `API_KEY`, `AI_API_KEY` — must be set in production
- `AI_ENABLED`, `AI_PROVIDER`, `AI_MODEL`, `AI_TEMPERATURE` — AI tuning

Plugin config is at `src/main/resources/config.yml`. Detection thresholds, punishment tiers, and messages are all configurable. When `sync.enabled: true`, the plugin fetches config from `/api/settings/plugin` on startup.

## Key Dependencies

| Module | Key Deps |
|--------|----------|
| Plugin | Spigot API 1.20.4 (provided), Java-WebSocket 1.5.4, Gson 2.10.1 |
| Backend | Spring Boot 3.2.4, Spring Security 6, JPA/Hibernate, jjwt 0.12.3, SpringDoc OpenAPI 2.3 |
| Frontend | Vue 3.4, Element Plus 2.5, ECharts 5.4, vue-i18n 9.14, Axios 1.6, Vite 5.2 |

Plugin uses maven-shade-plugin to relocate `org.java_websocket` and `com.google.gson` into `com.anticheat.lib.*` to avoid conflicts with other plugins.
