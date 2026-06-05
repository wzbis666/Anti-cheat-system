# 贡献指南

感谢您对 AntiCheat System 的兴趣！我们欢迎任何形式的贡献。

## 如何贡献

### 报告问题

- 使用 GitHub Issues 报告 Bug
- 提交功能请求前先搜索是否已存在
- 描述问题时请包含：
  - 使用的版本号
  - 复现步骤
  - 预期行为 vs 实际行为
  - 服务器环境 (Spigot/Paper 版本)

### 代码贡献

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 开发环境

```bash
# 克隆你的 Fork
git clone https://github.com/YOUR_USERNAME/Anti-cheat-system.git
cd Anti-cheat-system

# 安装前端依赖
cd frontend && npm install && cd ..

# 使用 Docker Compose 启动开发环境
docker compose up -d
```

### 代码规范

- Java: 遵循 Google Java Style Guide
- Vue.js: 使用 Vue 3 Composition API
- 提交信息: 使用语义化提交 (Conventional Commits)

## 分支策略

- `main`: 稳定版本
- `develop`: 开发中的下一个版本
- `feature/*`: 新功能
- `fix/*`: Bug 修复
- `hotfix/*`: 紧急修复

## 许可证

贡献代码即表示您同意您的代码将基于 MIT 许可证开源。
