﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="app-container" :data-theme="theme">
    <canvas class="crt-overlay-canvas" ref="crtCanvas"></canvas>
    <canvas class="terminal-particle-canvas" ref="particleCanvas"></canvas>
    <Login v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <div v-else class="hud-layout">
      <header class="hud-topbar">
        <div class="topbar-left">
          <h2 class="topbar-title">{{ currentTitle }}</h2>
        </div>
        <div class="topbar-right">
          <span :class="['status-dot', wsConnected ? 'online' : 'offline']" :title="wsConnected ? t('header.connected') : t('header.disconnected')"></span>
          <button class="topbar-btn" @click="showGlobalSearch = true" title="Ctrl+K">
            <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          </button>
          <button class="topbar-btn" @click="showNotifications = !showNotifications" style="position:relative">
            <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg>
            <span v-if="unreadCount > 0" class="topbar-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
          </button>
          <button class="topbar-btn" @click="toggleTheme">
            <svg v-if="theme === 'dark'" viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79 1.8-1.41-1.41-1.79 1.8z"/></svg>
            <svg v-else viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M9 2c-1.05 0-2.05.16-3 .46 4.06 1.27 7 5.06 7 9.54 0 4.48-2.94 8.27-7 9.54.95.3 1.95.46 3 .46 5.52 0 10-4.48 10-10S14.52 2 9 2z"/></svg>
          </button>
          <div class="lang-switch">
            <button :class="{ active: currentLocale === 'zh' }" @click="switchLanguage('zh')">中</button>
            <button :class="{ active: currentLocale === 'en' }" @click="switchLanguage('en')">EN</button>
          </div>
          <button class="topbar-btn logout" @click="handleLogout" :title="t('common.logout')">
            <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M16 17v-3H9v-4h7V7l5 5-5 5M14 2a2 2 0 012 2v6h-2V4H5v16h9v-2h2v2a2 2 0 01-2 2H5a2 2 0 01-2-2V4a2 2 0 012-2h9z"/></svg>
          </button>
        </div>
      </header>

      <main class="hud-content">
        <transition name="hud-page" mode="out-in">
          <Dashboard v-if="currentView === 'dashboard'" key="dashboard" />
          <Players v-else-if="currentView === 'players'" key="players" />
          <Cheats v-else-if="currentView === 'cheats'" key="cheats" />
          <Reports v-else-if="currentView === 'reports'" key="reports" @update-badge="updateReportsBadge" />
          <Punishments v-else-if="currentView === 'punishments'" key="punishments" />
          <Whitelist v-else-if="currentView === 'whitelist'" key="whitelist" />
          <Settings v-else-if="currentView === 'settings'" key="settings" />
          <Profile v-else-if="currentView === 'profile'" key="profile" :admin="adminInfo" @update-admin="handleUpdateAdmin" />
        </transition>
      </main>

      <nav class="hud-hotbar">
        <div class="hotbar-track">
          <button
            v-for="item in navItems"
            :key="item.path"
            :class="['hotbar-slot', { active: currentView === item.path }]"
            @click="currentView = item.path"
            :title="item.label"
          >
            <span class="hs-icon" v-html="item.icon"></span>
            <span v-if="item.badge" class="hs-badge">{{ item.badge }}</span>
          </button>

          <button :class="['hotbar-slot', { active: currentView === 'settings' }]" @click="currentView = 'settings'" :title="t('nav.settings')">
            <span class="hs-icon"><svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg></span>
          </button>

          <button :class="['hotbar-slot', { active: currentView === 'profile' }]" @click="currentView = 'profile'" :title="adminInfo.nickname || adminInfo.username">
            <img :src="userAvatar" class="hs-head" />
          </button>
        </div>
        <div class="hotbar-info">
          <span class="hi-label">{{ currentTitle }}</span>
          <span class="hi-time">{{ currentTime }}</span>
        </div>
      </nav>
    </div>

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

    <transition name="fade">
      <div v-if="showGlobalSearch" class="search-overlay" @click.self="showGlobalSearch = false">
        <div class="search-box">
          <div class="search-input-row">
            <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
            <input v-model="searchQuery" :placeholder="t('dashboard.searchPlaceholder')" ref="searchInput" />
            <span class="kbd">ESC</span>
          </div>
          <div class="search-results">
            <template v-if="searchQuery">
              <div v-if="searchResults.players.length" class="search-group">{{ t('nav.players') }}</div>
              <div v-for="p in searchResults.players" :key="'p-'+p.id" class="search-item" @click="goToPlayer(p)">
                <img :src="`https://mc-heads.net/avatar/${p.playerName}/32`" style="width:24px;height:24px;border-radius:4px;image-rendering:pixelated" />
                <div><div class="si-label">{{ p.playerName }}</div><div class="si-sub">{{ p.uuid }}</div></div>
              </div>
              <div v-if="searchResults.cheats.length" class="search-group">{{ t('nav.cheats') }}</div>
              <div v-for="c in searchResults.cheats" :key="'c-'+c.id" class="search-item" @click="currentView='cheats';showGlobalSearch=false">
                <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z"/></svg>
                <div><div class="si-label">{{ c.cheatType }} - {{ c.player?.playerName }}</div><div class="si-sub">{{ c.details }}</div></div>
              </div>
            </template>
            <template v-else>
              <div class="search-group">{{ t('dashboard.quickNav') }}</div>
              <div v-for="item in navItems" :key="item.path" class="search-item" @click="currentView=item.path;showGlobalSearch=false">
                <span v-html="item.icon"></span>
                <div><div class="si-label">{{ item.label }}</div></div>
              </div>
            </template>
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
import Login from './views/Login.vue'
import Dashboard from './views/Dashboard.vue'
import Players from './views/Players.vue'
import Cheats from './views/Cheats.vue'
import Reports from './views/Reports.vue'
import Punishments from './views/Punishments.vue'
import Whitelist from './views/Whitelist.vue'
import Settings from './views/Settings.vue'
import Profile from './views/Profile.vue'
import AiAssistant from './components/AiAssistant.vue'
import { getWsUrl, authApi, reportApi, playerApi, cheatApi, onAuthError } from './api'
import { setLocale } from './i18n'
import { EventBus, Events } from './utils/eventBus'

export default {
  name: 'App',
  components: { Login, Dashboard, Players, Cheats, Reports, Punishments, Whitelist, Settings, Profile, AiAssistant },
  setup() {
    const { t } = useI18n()
    const isLoggedIn = ref(false)
    const currentView = ref('dashboard')
    const adminInfo = ref({})
    const wsConnected = ref(false)
    const currentTime = ref('')
    const reportsBadge = ref(0)
    const currentLocale = ref(localStorage.getItem('locale') || 'zh')
    const theme = ref(localStorage.getItem('theme') || 'dark')
    const showNotifications = ref(false)
    const showGlobalSearch = ref(false)
    const searchQuery = ref('')
    const searchInput = ref(null)
    const notifications = ref([])
    const searchResults = ref({ players: [], cheats: [] })
    const crtCanvas = ref(null)
    const particleCanvas = ref(null)
    const uptimeStart = ref(Date.now())
    let timeInterval = null
    let crtAnimFrame = null
    let particleAnimFrame = null
    let ws = null
    let wsReconnectTimer = null
    let wsPingInterval = null
    let searchTimer = null

    const navItems = computed(() => [
      { path: 'dashboard', label: t('nav.dashboard'), icon: '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M13 3v6h8l-8-6M5 21V3h6v18H5m8-10v10h8V11h-8z"/></svg>' },
      { path: 'players', label: t('nav.players'), icon: '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/></svg>' },
      { path: 'cheats', label: t('nav.cheats'), icon: '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z"/></svg>' },
      { path: 'reports', label: t('nav.reports'), icon: '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6m-1 2l5 5h-5V4z"/></svg>', badge: reportsBadge.value || null },
      { path: 'punishments', label: t('nav.punishments'), icon: '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>' },
      { path: 'whitelist', label: t('nav.whitelist'), icon: '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>' }
    ])

    const currentTitle = computed(() => {
      const item = navItems.value.find(i => i.path === currentView.value)
      return item ? item.label : ''
    })

    const userAvatar = computed(() => `https://mc-heads.net/avatar/${adminInfo.value.username || 'Steve'}/32`)
    const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)
    const uptimeStr = computed(() => {
      const diff = Math.floor((Date.now() - uptimeStart.value) / 1000)
      const h = Math.floor(diff / 3600).toString().padStart(2, '0')
      const m = Math.floor((diff % 3600) / 60).toString().padStart(2, '0')
      const s = (diff % 60).toString().padStart(2, '0')
      return `${h}:${m}:${s}`
    })

    const toggleTheme = () => { theme.value = theme.value === 'dark' ? 'light' : 'dark'; localStorage.setItem('theme', theme.value) }
    const switchLanguage = (locale) => { setLocale(locale); currentLocale.value = locale }
    const handleLoginSuccess = (admin) => { adminInfo.value = admin; isLoggedIn.value = true; uptimeStart.value = Date.now(); fetchReportsCount(); initWebSocket() }
    const handleUpdateAdmin = (admin) => { adminInfo.value = admin }
    const handleLogout = () => { isLoggedIn.value = false; adminInfo.value = {}; localStorage.removeItem('admin'); localStorage.removeItem('token'); localStorage.removeItem('user'); closeWebSocket() }
    const updateReportsBadge = (count) => { reportsBadge.value = count }

    const fetchReportsCount = async () => {
      try { const result = await reportApi.getPendingCount(); reportsBadge.value = result.count || 0 } catch (e) { console.error(e) }
    }

    const updateTime = () => { currentTime.value = new Date().toLocaleTimeString() }

    const addNotification = (type, title, desc) => {
      notifications.value.unshift({ type, title, desc, time: new Date().toLocaleTimeString(), read: false })
      if (notifications.value.length > 50) notifications.value.pop()
    }

    const clearNotifications = () => { notifications.value = []; showNotifications.value = false }
    const handleNotificationClick = (n) => { n.read = true; if (n.type === 'cheat') currentView.value = 'cheats'; else if (n.type === 'ban') currentView.value = 'punishments'; showNotifications.value = false }
    const goToPlayer = (p) => { currentView.value = 'players'; showGlobalSearch.value = false }

    const performSearch = async (q) => {
      if (!q) { searchResults.value = { players: [], cheats: [] }; return }
      try {
        const [players, cheatData] = await Promise.all([playerApi.getAll(), cheatApi.getByPage(0, 5, 'detectionTime', 'desc')])
        searchResults.value = {
          players: players.filter(p => p.playerName.toLowerCase().includes(q.toLowerCase())).slice(0, 5),
          cheats: (cheatData.content || []).filter(c => (c.cheatType || '').toLowerCase().includes(q.toLowerCase()) || (c.player?.playerName || '').toLowerCase().includes(q.toLowerCase())).slice(0, 5)
        }
      } catch (e) { console.error(e) }
    }

    watch(searchQuery, (v) => { if (searchTimer) clearTimeout(searchTimer); searchTimer = setTimeout(() => performSearch(v), 300) })
    watch(showGlobalSearch, (v) => { if (v) { nextTick(() => { searchInput.value?.focus() }) } else { searchQuery.value = '' } })

    const initWebSocket = () => {
      try {
        ws = new WebSocket(getWsUrl())
        ws.onopen = () => { wsConnected.value = true; window.__wsConnected = true; EventBus.emit('ws:status', true); if (wsPingInterval) clearInterval(wsPingInterval); wsPingInterval = setInterval(() => { if (ws && ws.readyState === WebSocket.OPEN) ws.send('ping') }, 25000) }
        ws.onmessage = (event) => {
          if (event.data === 'pong') return
          const data = JSON.parse(event.data)
          if (data.type === 'cheat_detected') {
            EventBus.emit(Events.CHEAT_DETECTED, data); EventBus.emit(Events.STATS_CHANGED)
            window.__lastCheatData = data; window.__lastCheatTime = Date.now()
            addNotification('cheat', `${data.playerName} - ${data.cheatType}`, data.details)
          } else if (data.type === 'player_banned') {
            EventBus.emit(Events.PLAYER_BANNED, data); EventBus.emit(Events.STATS_CHANGED)
            addNotification('ban', `${data.playerName} ${t('players.banned')}`, data.reason)
          } else if (data.type === 'player_unbanned') {
            EventBus.emit(Events.PLAYER_UNBANNED, data); EventBus.emit(Events.STATS_CHANGED)
            addNotification('ban', `${data.playerName} ${t('players.unban')}`, '')
          }
        }
        ws.onclose = () => { wsConnected.value = false; window.__wsConnected = false; EventBus.emit('ws:status', false); scheduleReconnect() }
        ws.onerror = () => { wsConnected.value = false; window.__wsConnected = false; EventBus.emit('ws:status', false) }
      } catch (e) { scheduleReconnect() }
    }

    const scheduleReconnect = () => { if (wsReconnectTimer) clearTimeout(wsReconnectTimer); if (wsPingInterval) clearInterval(wsPingInterval); wsReconnectTimer = setTimeout(() => { if (ws) { ws.onclose = null; ws.close(); ws = null }; initWebSocket() }, 5000) }
    const closeWebSocket = () => { if (wsReconnectTimer) clearTimeout(wsReconnectTimer); if (wsPingInterval) clearInterval(wsPingInterval); if (ws) { ws.onclose = null; ws.close(); ws = null } }

    const handleKeydown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') { e.preventDefault(); showGlobalSearch.value = true }
      if (e.key === 'Escape') { showGlobalSearch.value = false; showNotifications.value = false }
    }

    const initCrtScanlines = () => {
      const canvas = crtCanvas.value
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const resize = () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight }
      resize()
      window.addEventListener('resize', resize)
      let offset = 0
      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        ctx.fillStyle = 'rgba(0, 0, 0, 0.025)'
        const lineH = 3
        for (let y = offset % lineH; y < canvas.height; y += lineH) {
          ctx.fillRect(0, y, canvas.width, 1)
        }
        if (Math.random() < 0.015) {
          const gy = Math.floor(Math.random() * canvas.height)
          ctx.fillStyle = 'rgba(168, 85, 247, 0.035)'
          ctx.fillRect(0, gy, canvas.width, Math.random() < 0.4 ? 4 : 1)
        }
        if (Math.random() < 0.005) {
          const sx = Math.random() * canvas.width * 0.7
          const sw = Math.random() * canvas.width * 0.25 + 60
          ctx.fillStyle = 'rgba(6, 182, 212, 0.03)'
          ctx.fillRect(sx, Math.random() * canvas.height, sw, 1)
        }
        offset += 0.4
        crtAnimFrame = requestAnimationFrame(animate)
      }
      animate()
    }

    const initTerminalParticles = () => {
      const canvas = particleCanvas.value
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const resize = () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight }
      resize()
      window.addEventListener('resize', resize)
      const count = 60
      const particles = []
      const colors = ['rgba(168,85,247,', 'rgba(6,182,212,', 'rgba(16,185,129,']
      for (let i = 0; i < count; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          vx: (Math.random() - 0.5) * 0.3,
          vy: (Math.random() - 0.5) * 0.3,
          size: Math.random() * 1.5 + 0.5,
          color: colors[Math.floor(Math.random() * colors.length)] + (Math.random() * 0.25 + 0.1) + ')'
        })
      }
      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        for (let i = 0; i < particles.length; i++) {
          const p = particles[i]
          p.x += p.vx
          p.y += p.vy
          if (p.x < 0) p.x = canvas.width
          if (p.x > canvas.width) p.x = 0
          if (p.y < 0) p.y = canvas.height
          if (p.y > canvas.height) p.y = 0
          ctx.fillStyle = p.color
          ctx.fillRect(p.x, p.y, p.size, p.size)
          for (let j = i + 1; j < particles.length; j++) {
            const p2 = particles[j]
            const dx = p.x - p2.x
            const dy = p.y - p2.y
            const dist = Math.sqrt(dx * dx + dy * dy)
            if (dist < 120) {
              ctx.strokeStyle = 'rgba(168,85,247,' + (0.06 * (1 - dist / 120)) + ')'
              ctx.lineWidth = 0.5
              ctx.beginPath()
              ctx.moveTo(p.x, p.y)
              ctx.lineTo(p2.x, p2.y)
              ctx.stroke()
            }
          }
        }
        particleAnimFrame = requestAnimationFrame(animate)
      }
      animate()
    }

    onMounted(async () => {
      onAuthError(() => {
        isLoggedIn.value = false
        adminInfo.value = {}
        closeWebSocket()
      })

      const savedAdmin = localStorage.getItem('admin') || localStorage.getItem('user')
      const savedToken = localStorage.getItem('token')
      if (savedAdmin && savedToken) {
        try {
          const result = await authApi.validateToken()
          if (result.valid) {
            adminInfo.value = JSON.parse(savedAdmin)
            isLoggedIn.value = true
            fetchReportsCount()
            initWebSocket()
          } else {
            localStorage.removeItem('admin')
            localStorage.removeItem('token')
          }
        } catch (e) {
          localStorage.removeItem('admin')
          localStorage.removeItem('token')
        }
      }
      updateTime(); timeInterval = setInterval(updateTime, 1000)
      initCrtScanlines()
      initTerminalParticles()
      document.addEventListener('keydown', handleKeydown)
    })

    onUnmounted(() => { if (timeInterval) clearInterval(timeInterval); if (crtAnimFrame) cancelAnimationFrame(crtAnimFrame); if (particleAnimFrame) cancelAnimationFrame(particleAnimFrame); closeWebSocket(); document.removeEventListener('keydown', handleKeydown) })

    return {
      t, isLoggedIn, currentView, adminInfo, wsConnected, currentTime, reportsBadge, currentLocale, theme,
      showNotifications, showGlobalSearch, searchQuery, searchInput, notifications, searchResults,
      navItems, currentTitle, userAvatar, unreadCount, uptimeStr, crtCanvas, particleCanvas,
      toggleTheme, switchLanguage, handleLoginSuccess, handleUpdateAdmin, handleLogout, updateReportsBadge,
      clearNotifications, handleNotificationClick, goToPlayer
    }
  }
}
</script>

<style scoped>
/* ========== CANVAS OVERLAYS ========== */
.crt-overlay-canvas {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 900;
}

.terminal-particle-canvas {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 0;
}

.app-container {
  min-height: 100vh;
  background: #050010;
  position: relative;
}

.hud-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
}

/* ========== TOPBAR ========== */
.hud-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  height: 54px;
  background: linear-gradient(180deg, rgba(14, 5, 28, 0.97) 0%, rgba(8, 2, 18, 0.95) 100%);
  border-bottom: 2px solid rgba(147, 51, 234, 0.25);
  flex-shrink: 0;
  backdrop-filter: blur(20px);
  z-index: 100;
  position: relative;
}

.hud-topbar::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(168, 85, 247, 0.025) 2px,
    rgba(168, 85, 247, 0.025) 4px
  );
  pointer-events: none;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-title {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #c084fc;
  text-transform: uppercase;
  margin: 0;
  position: relative;
}

.topbar-title::after {
  content: '█';
  color: #a855f7;
  margin-left: 5px;
  animation: cursorBlink 1s step-end infinite;
  font-size: 11px;
  vertical-align: middle;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 1px;
  margin-right: 8px;
  transition: all 0.3s ease;
}

.status-dot.online {
  background: #10b981;
  box-shadow: 0 0 8px #10b981, 0 0 18px rgba(16, 185, 129, 0.3);
}

.status-dot.offline {
  background: #ff3d5a;
  box-shadow: 0 0 8px #ff3d5a;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.topbar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(20, 8, 40, 0.6);
  border: 2px solid rgba(147, 51, 234, 0.18);
  border-radius: 2px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.topbar-btn:hover {
  background: rgba(45, 16, 90, 0.7);
  border-color: rgba(168, 85, 247, 0.45);
  color: #c084fc;
  box-shadow: 0 0 14px rgba(147, 51, 234, 0.2);
}

.topbar-btn.logout:hover {
  color: #ff3d5a;
  border-color: rgba(255, 61, 90, 0.45);
  box-shadow: 0 0 14px rgba(255, 61, 90, 0.2);
}

.topbar-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: #ff3d5a;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 2px;
  min-width: 16px;
  text-align: center;
  box-shadow: 0 0 6px rgba(255, 61, 90, 0.55);
}

.lang-switch {
  display: flex;
  background: rgba(18, 8, 35, 0.6);
  border: 2px solid rgba(147, 51, 234, 0.2);
  border-radius: 2px;
  overflow: hidden;
}

.lang-switch button {
  padding: 5px 9px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 600;
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-switch button:hover {
  color: var(--text-primary);
  background: rgba(147, 51, 234, 0.12);
}

.lang-switch button.active {
  background: linear-gradient(180deg, #7c3aed, #6d28d9);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 0 12px rgba(124, 58, 237, 0.4);
}

/* ========== CONTENT ========== */
.hud-content {
  flex: 1;
  padding: 22px 26px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  background:
    repeating-linear-gradient(0deg, transparent, transparent 49px, rgba(168,85,247,0.022) 50px),
    repeating-linear-gradient(90deg, transparent, transparent 49px, rgba(168,85,247,0.022) 50px),
    radial-gradient(ellipse at 50% 25%, rgba(80,20,150,0.08) 0%, transparent 65%);
}

.hud-content::after {
  content: '';
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: linear-gradient(to top, rgba(5,0,10,0.94), transparent);
  pointer-events: none;
  display: block;
}

/* ========== HOTBAR ========== */
.hud-hotbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 58px;
  padding: 0 18px;
  background: linear-gradient(0deg, rgba(4, 1, 12, 0.98) 0%, rgba(8, 3, 20, 0.96) 100%);
  border-top: 2px solid rgba(147, 51, 234, 0.22);
  flex-shrink: 0;
  backdrop-filter: blur(20px);
  z-index: 100;
  position: relative;
}

.hud-hotbar::before {
  content: '';
  position: absolute;
  top: -2px;
  left: 8%;
  right: 8%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(168,85,247,0.4), #a855f7, rgba(168,85,247,0.4), transparent);
  animation: hotbarBeam 4s ease-in-out infinite;
}

.hud-hotbar::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(168,85,247,0.02) 2px, rgba(168,85,247,0.02) 4px);
  pointer-events: none;
}

@keyframes hotbarBeam {
  0%, 100% { opacity: 0.3; transform: scaleX(0.7); }
  50% { opacity: 1; transform: scaleX(1); }
}

.hotbar-track {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(6, 2, 16, 0.9);
  border: 2px solid rgba(147, 51, 234, 0.22);
  border-radius: 2px;
  padding: 4px 5px;
  position: relative;
}

.hotbar-track::before {
  content: '';
  position: absolute;
  inset: 2px;
  border-radius: 1px;
  border: 1px solid rgba(255, 255, 255, 0.03);
  pointer-events: none;
}

.hotbar-slot {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(180deg, rgba(35,14,60,0.7) 0%, rgba(18,6,32,0.8) 50%, rgba(10,3,20,0.8) 100%);
  border: 2px solid rgba(147, 51, 234, 0.15);
  border-radius: 2px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  image-rendering: pixelated;
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.05),
    inset 0 -1px 0 rgba(0,0,0,0.35),
    inset 1px 0 0 rgba(0,0,0,0.1),
    inset -1px 0 0 rgba(0,0,0,0.1);
}

.hotbar-slot:hover {
  background:
    linear-gradient(180deg, rgba(60,20,100,0.8) 0%, rgba(35,12,62,0.85) 50%, rgba(18,7,38,0.85) 100%);
  border-color: rgba(168, 85, 247, 0.55);
  color: #c084fc;
  transform: translateY(-4px);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.07),
    inset 0 -1px 0 rgba(0,0,0,0.3),
    0 2px 12px rgba(147,51,234,0.3);
}

.hotbar-slot.active {
  background:
    linear-gradient(180deg, rgba(105,32,185,0.85) 0%, rgba(65,20,115,0.88) 50%, rgba(35,10,72,0.8) 100%);
  border-color: rgba(168, 85, 247, 0.8);
  color: #ede9fe;
  transform: translateY(-7px);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.12),
    inset 0 -1px 0 rgba(0,0,0,0.3),
    0 3px 22px rgba(147,51,234,0.5),
    0 0 28px rgba(147,51,234,0.22);
}

.hotbar-slot.active::after {
  content: '';
  position: absolute;
  bottom: -10px;
  width: 10px;
  height: 4px;
  background: #a855f7;
  box-shadow:
    0 0 14px #a855f7,
    0 0 28px rgba(168,85,247,0.7),
    0 0 40px rgba(168,85,247,0.35);
  border-radius: 1px;
  animation: neonBreath 2s ease-in-out infinite;
}

@keyframes neonBreath {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

.hs-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.hs-icon :deep(svg) {
  width: 20px;
  height: 20px;
}

.hs-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ff3d5a;
  color: #fff;
  font-size: 8px;
  font-weight: 700;
  padding: 1px 4px;
  border-radius: 2px;
  min-width: 14px;
  text-align: center;
  line-height: 1;
  box-shadow: 0 0 6px rgba(255,61,90,0.55);
  z-index: 2;
}

.hs-head {
  width: 28px;
  height: 28px;
  border-radius: 2px;
  image-rendering: pixelated;
  border: 2px solid rgba(147, 51, 234, 0.3);
}

.hotbar-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hi-label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: rgba(192, 132, 252, 0.65);
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.hi-label::before {
  content: '// ';
  color: rgba(168, 85, 247, 0.4);
}

.hi-time {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  opacity: 0.55;
}

/* ========== NOTIFICATIONS ========== */
.notif-panel {
  position: fixed;
  top: 60px;
  right: 18px;
  width: 350px;
  max-height: 460px;
  background: rgba(8, 2, 18, 0.98);
  border: 2px solid rgba(147, 51, 234, 0.28);
  border-radius: 2px;
  box-shadow:
    0 12px 48px rgba(0, 0, 0, 0.7),
    0 0 35px rgba(147, 51, 234, 0.18);
  z-index: 200;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(24px);
}

.notif-panel::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(168,85,247,0.022) 2px,
    rgba(168,85,247,0.022) 4px
  );
  pointer-events: none;
  z-index: 1;
}

.notif-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 2px solid rgba(147, 51, 234, 0.2);
  position: relative;
  z-index: 2;
}

.notif-panel-header h4 {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  margin: 0;
  color: #c084fc;
  text-transform: uppercase;
}

.notif-clear {
  background: rgba(147,51,234,0.08);
  border: 2px solid rgba(147,51,234,0.22);
  border-radius: 2px;
  color: #a855f7;
  cursor: pointer;
  font-size: 10px;
  font-family: var(--font-mono);
  padding: 3px 9px;
  transition: all 0.2s ease;
}

.notif-clear:hover {
  background: rgba(147,51,234,0.16);
  border-color: rgba(168,85,247,0.45);
}

.notif-list {
  flex: 1;
  overflow-y: auto;
  max-height: 390px;
  position: relative;
  z-index: 2;
}

.notif-empty {
  padding: 36px;
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  font-family: var(--font-mono);
}

.notif-item {
  display: flex;
  gap: 10px;
  padding: 11px 14px;
  border-bottom: 1px solid rgba(147, 51, 234, 0.06);
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
}

.notif-item:hover {
  background: rgba(147, 51, 234, 0.08);
}

.notif-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: #a855f7;
  box-shadow: 0 0 10px rgba(168, 85, 247, 0.6);
  border-radius: 1px;
}

.notif-icon {
  width: 30px;
  height: 30px;
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-icon.cheat { background: rgba(255, 61, 90, 0.16); color: #ff3d5a; }
.notif-icon.ban { background: rgba(168, 85, 247, 0.16); color: #a855f7; }
.notif-icon.system { background: rgba(6, 182, 212, 0.12); color: #06b6d4; }

.notif-body { flex: 1; min-width: 0; }
.notif-title { font-size: 13px; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.notif-desc { font-size: 11px; color: var(--text-muted); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.notif-time { font-size: 10px; color: var(--text-muted); margin-top: 3px; font-family: var(--font-mono); opacity: 0.55; }

/* ========== SEARCH OVERLAY ========== */
.search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 0, 10, 0.85);
  backdrop-filter: blur(10px);
  z-index: 500;
  display: flex;
  justify-content: center;
  padding-top: 110px;
}

.search-box {
  width: 540px;
  background: rgba(8, 3, 20, 0.98);
  border: 2px solid rgba(147, 51, 234, 0.32);
  border-radius: 2px;
  box-shadow:
    0 16px 56px rgba(0, 0, 0, 0.8),
    0 0 50px rgba(147, 51, 234, 0.14);
  overflow: hidden;
  max-height: 440px;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(24px);
}

.search-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 2px solid rgba(147, 51, 234, 0.22);
  color: #a855f7;
  position: relative;
  z-index: 2;
}

.search-input-row input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  font-family: var(--font-mono);
  font-size: 15px;
  color: var(--text-primary);
}

.search-input-row input::placeholder {
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.kbd {
  font-size: 10px;
  padding: 3px 7px;
  background: rgba(147, 51, 234, 0.14);
  border: 2px solid rgba(147, 51, 234, 0.25);
  border-radius: 2px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
  position: relative;
  z-index: 2;
}

.search-group {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: #a855f7;
  padding: 8px 12px 4px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.search-item:hover {
  background: rgba(147, 51, 234, 0.1);
  border-left: 2px solid #a855f7;
}

.si-label { font-size: 13px; color: var(--text-primary); }
.si-sub { font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); }

/* ========== TRANSITIONS ========== */
.hud-page-enter-active {
  animation: hudPageIn 0.32s cubic-bezier(0.16, 1, 0.3, 1);
}

.hud-page-leave-active {
  animation: hudPageOut 0.2s cubic-bezier(0.4, 0, 1, 1);
}

@keyframes hudPageIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.97);
    filter: blur(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

@keyframes hudPageOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.97);
  }
}

.slide-fade-enter-active { animation: slideFadeIn 0.22s ease-out; }
.slide-fade-leave-active { animation: slideFadeOut 0.16s ease-in; }

@keyframes slideFadeIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideFadeOut {
  from { opacity: 1; }
  to { opacity: 0; transform: translateY(-8px); }
}

.fade-enter-active { animation: fadeIn 0.22s ease-out; }
.fade-leave-active { animation: fadeIn 0.16s ease-in reverse; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* ========== RESPONSIVE ========== */
@media (max-width: 767px) {
  .hud-content { padding: 12px; }
  .hud-topbar { padding: 0 10px; height: 44px; }
  .hud-hotbar { padding: 0 6px; height: 50px; }
  .hotbar-slot { width: 32px; height: 32px; }
  .hotbar-slot.active { transform: translateY(-4px); }
  .topbar-title { font-size: 9px; letter-spacing: 1px; }
  .lang-switch { display: none; }
  .notif-panel { width: calc(100vw - 16px); right: 8px; top: 50px; }
  .search-box { width: calc(100vw - 32px); }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .hud-content { padding: 16px; }
  .hotbar-slot { width: 36px; height: 36px; }
}
</style>
