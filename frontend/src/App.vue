<template>
  <div class="app-container" :data-theme="theme">
    <canvas class="crt-overlay-canvas" ref="crtCanvas"></canvas>
    <canvas class="terminal-particle-canvas" ref="particleCanvas"></canvas>
    <Login v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <div v-else class="hud-layout">
      <!-- 左侧导航栏 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="logo-icon">⛨</div>
          <span class="logo-text">AntiCheat</span>
        </div>

        <nav class="sidebar-nav">
          <div class="nav-section-title">{{ t('nav.main') }}</div>
          <router-link v-for="item in mainNavItems" :key="item.path" :to="'/' + item.path"
            :class="['nav-item', { active: currentRoute === item.path }]">
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-label">{{ item.label }}</span>
            <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
          </router-link>

          <div class="nav-section-title">{{ t('nav.manage') }}</div>
          <router-link v-for="item in manageNavItems" :key="item.path" :to="'/' + item.path"
            :class="['nav-item', { active: currentRoute === item.path }]">
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-label">{{ item.label }}</span>
          </router-link>
        </nav>

        <div class="sidebar-footer">
          <div class="user-info">
            <img :src="userAvatar" class="user-avatar" />
            <div class="user-detail">
              <span class="user-name">{{ adminInfo.nickname || adminInfo.username || 'Admin' }}</span>
              <span class="user-role">{{ adminInfo.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <div class="main-area">
        <!-- 顶部工具栏 -->
        <header class="topbar">
          <h1 class="page-title">
            {{ currentTitle }}
          </h1>
          <div class="topbar-actions">
            <div class="lang-switch">
              <button :class="{ active: currentLocale === 'zh' }" @click="switchLanguage('zh')">中文</button>
              <button :class="{ active: currentLocale === 'en' }" @click="switchLanguage('en')">EN</button>
            </div>
            <div class="connection-status">
              <span :class="['status-dot', wsConnected ? 'online' : 'offline']"></span>
              <span>{{ wsConnected ? t('header.connected') : t('header.disconnected') }}</span>
            </div>
            <span class="current-time">{{ currentTime }}</span>
            <button class="topbar-btn" @click="showNotifications = !showNotifications" style="position:relative">
              <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg>
              <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
            </button>
            <button class="topbar-btn" @click="handleLogout" title="退出登录">
              <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M16 17v-3H9v-4h7V7l5 5-5 5M14 2a2 2 0 012 2v6h-2V4H5v16h9v-2h2v2a2 2 0 01-2 2H5a2 2 0 01-2-2V4a2 2 0 012-2h9z"/></svg>
            </button>
          </div>
        </header>

        <!-- 页面内容 -->
        <main class="content-area">
          <router-view v-slot="{ Component }">
            <transition name="hud-page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>
    </div>

    <!-- 通知面板 -->
    <transition name="slide-fade">
      <div v-if="showNotifications" class="notif-panel">
        <div class="notif-panel-header">
          <h4>{{ t('dashboard.notifications') }}</h4>
          <button class="notif-clear" @click="clearNotifications">{{ t('dashboard.clearAll') }}</button>
        </div>
        <div class="notif-list">
          <div v-if="notifications.length === 0" class="notif-empty">{{ t('dashboard.noNotifications') }}</div>
          <div v-for="(n, i) in notifications" :key="i" :class="['notif-item', { unread: !n.read }]" @click="handleNotificationClick(n)">
            <div :class="['notif-icon', n.type]">
              <svg v-if="n.type === 'cheat'" viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z"/></svg>
              <svg v-else-if="n.type === 'ban'" viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/></svg>
              <svg v-else viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
            </div>
            <div class="notif-body">
              <div class="notif-title">{{ n.title }}</div>
              <div class="notif-desc">{{ n.desc }}</div>
              <div class="notif-time">{{ n.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <AiAssistant />
  </div>
</template>
<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import Login from './views/Login.vue'
import AiAssistant from './components/AiAssistant.vue'
import { getWsUrl, authApi, reportApi, playerApi, cheatApi, onAuthError } from './api'
import { setLocale } from './i18n'
import { EventBus, Events } from './utils/eventBus'

export default {
  name: 'App',
  components: { Login, AiAssistant },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const route = useRoute()
    const isLoggedIn = ref(false)
    const adminInfo = ref({})
    const wsConnected = ref(false)
    const currentTime = ref('')
    const reportsBadge = ref(0)
    const currentLocale = ref(localStorage.getItem('locale') || 'zh')
    const theme = ref(localStorage.getItem('theme') || 'dark')
    const showNotifications = ref(false)
    const notifications = ref([])
    const crtCanvas = ref(null)
    const particleCanvas = ref(null)
    let timeInterval = null
    let crtAnimFrame = null
    let particleAnimFrame = null
    let ws = null
    let wsReconnectTimer = null
    let wsPingInterval = null

    const mainNavItems = computed(() => [
      { path: 'dashboard', label: t('nav.dashboard'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 13h8V3H3v10zm0 8h8v-4H3v4zm10 0h8V11h-8v10zm0-16v4h8V3h-8z"/></svg>' },
      { path: 'players', label: t('nav.players'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/></svg>' },
      { path: 'cheats', label: t('nav.cheats'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z"/></svg>' },
      { path: 'reports', label: t('nav.reports'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6m-1 2l5 5h-5V4z"/></svg>', badge: reportsBadge.value || null },
      { path: 'punishments', label: t('nav.punishments'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>' }
    ])

    const manageNavItems = computed(() => [
      { path: 'whitelist', label: t('nav.whitelist'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>' },
      { path: 'settings', label: t('nav.settings'), icon: '<svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>' }
    ])

    const allNavItems = computed(() => [...mainNavItems.value, ...manageNavItems.value])
    const currentRoute = computed(() => route.name || 'dashboard')
    const currentTitle = computed(() => {
      const item = allNavItems.value.find(i => i.path === currentRoute.value)
      return item ? item.label : ''
    })
    const userAvatar = computed(() => 'https://mc-heads.net/avatar/' + (adminInfo.value.username || 'Steve') + '/64')
    const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

    const switchLanguage = (locale) => { setLocale(locale); currentLocale.value = locale }
    const handleLoginSuccess = (admin) => { adminInfo.value = admin; isLoggedIn.value = true; fetchReportsCount(); initWebSocket() }
    const handleLogout = () => { isLoggedIn.value = false; adminInfo.value = {}; localStorage.removeItem('admin'); localStorage.removeItem('token'); localStorage.removeItem('user'); closeWebSocket(); router.push('/') }

    const fetchReportsCount = async () => {
      try { const result = await reportApi.getPendingCount(); reportsBadge.value = result.count || 0 } catch (e) { console.error(e) }
    }

    const updateTime = () => { currentTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }

    const addNotification = (type, title, desc) => {
      notifications.value.unshift({ type, title, desc, time: new Date().toLocaleTimeString(), read: false })
      if (notifications.value.length > 50) notifications.value.pop()
    }

    const clearNotifications = () => { notifications.value = []; showNotifications.value = false }
    const handleNotificationClick = (n) => { n.read = true; if (n.type === 'cheat') router.push('/cheats'); else if (n.type === 'ban') router.push('/punishments'); showNotifications.value = false }

    const initWebSocket = () => {
      try {
        ws = new WebSocket(getWsUrl())
        ws.onopen = () => { wsConnected.value = true; EventBus.emit(Events.WS_STATUS, true); if (wsPingInterval) clearInterval(wsPingInterval); wsPingInterval = setInterval(() => { if (ws && ws.readyState === WebSocket.OPEN) ws.send('ping') }, 25000) }
        ws.onmessage = (event) => {
          if (event.data === 'pong') return
          const data = JSON.parse(event.data)
          if (data.type === 'cheat_detected') {
            EventBus.emit(Events.CHEAT_DETECTED, data); EventBus.emit(Events.STATS_CHANGED); EventBus.emit(Events.WS_CHEAT_DATA, data)
            addNotification('cheat', data.playerName + ' - ' + data.cheatType, data.details)
          } else if (data.type === 'player_banned') {
            EventBus.emit(Events.PLAYER_BANNED, data); EventBus.emit(Events.STATS_CHANGED)
            addNotification('ban', data.playerName + ' ' + t('players.banned'), data.reason)
          } else if (data.type === 'player_unbanned') {
            EventBus.emit(Events.PLAYER_UNBANNED, data); EventBus.emit(Events.STATS_CHANGED)
            addNotification('ban', data.playerName + ' ' + t('players.unban'), '')
          } else if (data.type === 'rule_triggered') {
            addNotification('system', data.ruleName, data.message)
          }
        }
        ws.onclose = () => { wsConnected.value = false; EventBus.emit(Events.WS_STATUS, false); scheduleReconnect() }
        ws.onerror = () => { wsConnected.value = false; EventBus.emit(Events.WS_STATUS, false) }
      } catch (e) { scheduleReconnect() }
    }

    const scheduleReconnect = () => { if (wsReconnectTimer) clearTimeout(wsReconnectTimer); if (wsPingInterval) clearInterval(wsPingInterval); wsReconnectTimer = setTimeout(() => { if (ws) { ws.onclose = null; ws.close(); ws = null }; initWebSocket() }, 5000) }
    const closeWebSocket = () => { if (wsReconnectTimer) clearTimeout(wsReconnectTimer); if (wsPingInterval) clearInterval(wsPingInterval); if (ws) { ws.onclose = null; ws.close(); ws = null } }

    const initCrtScanlines = () => { const canvas = crtCanvas.value; if (!canvas) return; const ctx = canvas.getContext('2d'); const resize = () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight }; resize(); window.addEventListener('resize', resize); let offset = 0; const animate = () => { ctx.clearRect(0, 0, canvas.width, canvas.height); ctx.fillStyle = 'rgba(255,200,0,0.015)'; for (let y = offset % 4; y < canvas.height; y += 4) { ctx.fillRect(0, y, canvas.width, 1) }; offset += 0.3; crtAnimFrame = requestAnimationFrame(animate) }; animate() }

    const initTerminalParticles = () => { const canvas = particleCanvas.value; if (!canvas) return; const ctx = canvas.getContext('2d'); const resize = () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight }; resize(); window.addEventListener('resize', resize); const particles = []; for (let i = 0; i < 30; i++) { particles.push({ x: Math.random() * canvas.width, y: Math.random() * canvas.height, vx: (Math.random() - 0.5) * 0.25, vy: (Math.random() - 0.5) * 0.25, size: Math.random() * 1.5 + 0.5, color: 'rgba(255,200,0,' + (Math.random() * 0.12 + 0.04) + ')' }) }; const animate = () => { ctx.clearRect(0, 0, canvas.width, canvas.height); for (const p of particles) { p.x += p.vx; p.y += p.vy; if (p.x < 0) p.x = canvas.width; if (p.x > canvas.width) p.x = 0; if (p.y < 0) p.y = canvas.height; if (p.y > canvas.height) p.y = 0; ctx.fillStyle = p.color; ctx.fillRect(p.x, p.y, p.size, p.size) }; particleAnimFrame = requestAnimationFrame(animate) }; animate() }

    onMounted(async () => {
      onAuthError(() => { isLoggedIn.value = false; adminInfo.value = {}; closeWebSocket() })
      const savedAdmin = localStorage.getItem('admin') || localStorage.getItem('user')
      const savedToken = localStorage.getItem('token')
      if (savedAdmin && savedToken) {
        try {
          const result = await authApi.validateToken()
          if (result.data?.valid) { adminInfo.value = JSON.parse(savedAdmin); isLoggedIn.value = true; fetchReportsCount(); initWebSocket() }
          else { localStorage.removeItem('admin'); localStorage.removeItem('token') }
        } catch (e) { localStorage.removeItem('admin'); localStorage.removeItem('token') }
      }
      updateTime(); timeInterval = setInterval(updateTime, 1000)
      initCrtScanlines(); initTerminalParticles()
    })

    onUnmounted(() => { if (timeInterval) clearInterval(timeInterval); if (crtAnimFrame) cancelAnimationFrame(crtAnimFrame); if (particleAnimFrame) cancelAnimationFrame(particleAnimFrame); closeWebSocket() })

    return { t, router, isLoggedIn, adminInfo, wsConnected, currentTime, currentLocale, theme, showNotifications, notifications, mainNavItems, manageNavItems, currentRoute, currentTitle, userAvatar, unreadCount, crtCanvas, particleCanvas, switchLanguage, handleLoginSuccess, handleLogout, clearNotifications, handleNotificationClick }
  }
}
</script>
<style scoped>
.crt-overlay-canvas { position: fixed; inset: 0; width: 100vw; height: 100vh; pointer-events: none; z-index: 900; }
.terminal-particle-canvas { position: fixed; inset: 0; width: 100vw; height: 100vh; pointer-events: none; z-index: 0; }

.app-container {
  min-height: 100vh;
  background: var(--bg-primary);
  position: relative;
}

.hud-layout { display: flex; min-height: 100vh; position: relative; }

/* ========== SIDEBAR ========== */
.sidebar {
  width: var(--sidebar-width);
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(255,200,0,0.15), transparent);
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  font-size: 22px;
  line-height: 1;
}

.logo-text {
  font-family: var(--font-sans);
  font-size: 16px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px;
}

.nav-section-title {
  font-family: var(--font-sans);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  color: var(--text-muted);
  padding: 12px 12px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s ease;
  margin-bottom: 2px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 500;
  position: relative;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-primary);
}

.nav-item.active {
  background: rgba(255, 200, 0, 0.1);
  color: var(--accent-gold);
  font-weight: 600;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 3px;
  background: var(--accent-gold);
  border-radius: 0 2px 2px 0;
}

.nav-icon { display: flex; align-items: center; justify-content: center; flex-shrink: 0; opacity: 0.75; }
.nav-item.active .nav-icon { opacity: 1; }
.nav-icon :deep(svg) { display: block; }
.nav-label { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.nav-badge {
  padding: 2px 7px;
  font-size: 10px;
  font-weight: 700;
  background: #E74C3C;
  color: #fff;
  border-radius: 4px;
  min-width: 18px;
  text-align: center;
  line-height: 1.4;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--border-color);
  object-fit: cover;
}

.user-detail { display: flex; flex-direction: column; min-width: 0; }
.user-name { font-family: var(--font-sans); font-size: 13px; font-weight: 600; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-role { font-family: var(--font-sans); font-size: 11px; color: var(--text-muted); }

/* ========== MAIN AREA ========== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg-primary);
}

/* ========== TOPBAR ========== */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 56px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  position: relative;
}

.topbar::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,200,0,0.2), transparent);
}

.page-title {
  font-family: var(--font-sans);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lang-switch {
  display: flex;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.lang-switch button {
  padding: 5px 12px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-switch button:hover { color: var(--text-primary); }
.lang-switch button.active { background: var(--accent-gold); color: #0a0a0f; font-weight: 700; }

.connection-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-sans);
  font-size: 12px;
  color: var(--text-muted);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}
.status-dot.online { background: #2ECC71; box-shadow: 0 0 6px rgba(46,204,113,0.5); }
.status-dot.offline { background: #E74C3C; animation: dotPulse 2s ease-in-out infinite; }
@keyframes dotPulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.35; } }

.current-time { font-family: var(--font-sans); font-size: 12px; color: var(--text-muted); }

.topbar-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.topbar-btn:hover { background: var(--bg-hover); border-color: var(--accent-gold); color: var(--accent-gold); }

.notif-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: #E74C3C;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 4px;
  min-width: 15px;
  text-align: center;
  line-height: 1.3;
}

/* ========== CONTENT ========== */
.content-area {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

/* ========== NOTIFICATIONS ========== */
.notif-panel {
  position: fixed;
  top: 60px;
  right: 20px;
  width: 380px;
  max-height: 480px;
  background: var(--bg-card-solid);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg), 0 0 30px rgba(0,0,0,0.3);
  z-index: 200;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.notif-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.notif-panel-header h4 { font-family: var(--font-sans); font-size: 14px; font-weight: 700; margin: 0; color: var(--accent-gold); }

.notif-clear {
  background: var(--bg-hover);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 12px;
  font-family: var(--font-sans);
  padding: 5px 12px;
  transition: all 0.2s ease;
}
.notif-clear:hover { border-color: var(--accent-gold); color: var(--accent-gold); }

.notif-list { flex: 1; overflow-y: auto; }
.notif-empty { padding: 40px; text-align: center; color: var(--text-muted); font-size: 13px; font-family: var(--font-sans); }

.notif-item {
  display: flex;
  gap: 12px;
  padding: 12px 18px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
}
.notif-item:hover { background: var(--bg-hover); }
.notif-item.unread::before { content: ''; position: absolute; left: 0; top: 8px; bottom: 8px; width: 3px; background: var(--accent-gold); border-radius: 2px; }

.notif-icon { width: 34px; height: 34px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.notif-icon.cheat { background: rgba(231,76,60,0.12); color: #E74C3C; }
.notif-icon.ban { background: rgba(255,200,0,0.12); color: var(--accent-gold); }
.notif-icon.system { background: rgba(74,158,255,0.12); color: #4A9EFF; }

.notif-body { flex: 1; min-width: 0; }
.notif-title { font-size: 13px; font-weight: 600; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-family: var(--font-sans); }
.notif-desc { font-size: 12px; color: var(--text-muted); margin-top: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.notif-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; font-family: var(--font-sans); }

/* ========== TRANSITIONS ========== */
.hud-page-enter-active { animation: hudPageIn 0.3s ease-out; }
.hud-page-leave-active { animation: hudPageOut 0.2s ease-in; }
@keyframes hudPageIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
@keyframes hudPageOut { from { opacity: 1; } to { opacity: 0; transform: scale(0.99); } }

.slide-fade-enter-active { animation: slideFadeIn 0.2s ease-out; }
.slide-fade-leave-active { animation: slideFadeOut 0.15s ease-in; }
@keyframes slideFadeIn { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideFadeOut { from { opacity: 1; } to { opacity: 0; transform: translateY(-8px); } }

/* ========== RESPONSIVE ========== */
@media (max-width: 1024px) {
  :root { --sidebar-width: 72px; }
  .logo-text { display: none; }
  .nav-label { display: none; }
  .nav-item { justify-content: center; padding: 10px; }
  .nav-item.active::before { display: none; }
  .nav-section-title { display: none; }
  .user-detail { display: none; }
  .sidebar-footer { justify-content: center; }
}

@media (max-width: 768px) {
  .sidebar { position: fixed; left: 0; top: 0; bottom: 0; z-index: 100; transform: translateX(-100%); transition: transform 0.3s ease; }
  .sidebar.mobile-open { transform: translateX(0); }
  .content-area { padding: 16px; }
  .topbar { padding: 0 16px; }
  .lang-switch { display: none; }
  .current-time { display: none; }
  .notif-panel { width: calc(100vw - 32px); right: 16px; }
}
</style>
