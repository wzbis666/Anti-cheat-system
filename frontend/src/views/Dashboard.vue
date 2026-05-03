<template>
  <div class="command-center">
    <canvas class="cc-datarain" ref="dataRainCanvas"></canvas>
    <canvas class="cc-particles" ref="particleCanvas"></canvas>

    <div class="cc-hero">
      <div class="threat-radar" ref="radarContainer">
        <svg viewBox="0 0 200 200" class="radar-svg">
          <g class="radar-rings-pixel" shape-rendering="crispEdges">
            <path d="M100 10h4l4 2 2 4v4l2 4 2 2h4l4-2 2-4v-4l2-4 2-2h4v-4h-4l-2-2-2-4v-4h-4l-4-2h-4l-4 2h-4v4l-2 4-2 2h-4l-2 4v4l-2 4-2 2h-4v80l2 4 2 2 2 4 2 2h4l2 4 6 2h8l4-2 4-2 2-4 2-2h4l2-4v-4l2-4 2-2h4v4l2 4 2 2 2 4 2 2h4l2 4 4 2h4l4-2 4-2 2-4 2-2h4l2-4v-80" fill="none" stroke="var(--border-color)" stroke-width="0.6" opacity="0.5"/>
            <path d="M100 35h3l3 1 1 3v3l1 3 1 1h3l3-1 1-3v-3l1-3 1-1h3v-3h-3l-1-1-1-3v-3h-3l-3-1h-3l-3 1h-3v3l-1 3-1 1h-3l-1 3v3l-1 3-1 1h-3v55l1 3 1 1 1 3 1 1h3l1 3 4 1h6l3-1 3-1 1-3 1-1h3l1-3v-3l1-3 1-1h3v3l1 3 1 1 1 3 1 1h3l1 3 3 1h3l3-1 3-1 1-3 1-1h3l1-3v-55" fill="none" stroke="var(--border-color)" stroke-width="0.4" opacity="0.4"/>
            <path d="M100 60h2l2 1 1 2v2l1 2 1 1h2l2-1 1-2v-2l1-2 1-1h2v-2h-2l-1-1-1-2v-2h-2l-2-1h-2l-2 1h-2v2l-1 2-1 1h-2l-1 2v2l-1 2-1 1h-2v30l1 2 1 1 1 2 1 1h2l1 2 3 1h5l2-1 2-1 1-2 1-1h2l1-2v-2l1-2 1-1h2v2l1 2 1 1 1 2 1 1h2l1 2 2 1h2l3-1 2-1 1-2 1-1h2l1-2v-30" fill="none" stroke="var(--border-color)" stroke-width="0.3" opacity="0.3"/>
          </g>
          <line x1="100" y1="10" x2="100" y2="190" stroke="var(--border-color)" stroke-width="0.4" opacity="0.4"/>
          <line x1="10" y1="100" x2="190" y2="100" stroke="var(--border-color)" stroke-width="0.4" opacity="0.4"/>
          <line x1="36" y1="36" x2="164" y2="164" stroke="var(--border-color)" stroke-width="0.3" opacity="0.3"/>
          <line x1="164" y1="36" x2="36" y2="164" stroke="var(--border-color)" stroke-width="0.3" opacity="0.3"/>

          <g class="radar-ring-outer" shape-rendering="crispEdges">
            <path d="M100 10h4l4 2 2 4v4l2 4 2 2h4l4-2 2-4v-4l2-4 2-2h4v-4h-4l-2-2-2-4v-4h-4l-4-2h-4l-4 2h-4v4l-2 4-2 2h-4l-2 4v4l-2 4-2 2h-4v80l2 4 2 2 2 4 2 2h4l2 4 6 2h8l4-2 4-2 2-4 2-2h4l2-4v-4l2-4 2-2h4v4l2 4 2 2 2 4 2 2h4l2 4 4 2h4l4-2 4-2 2-4 2-2h4l2-4v-80" fill="none" stroke="#a855f7" stroke-width="1" opacity="0.5" class="radar-ring-glow"/>
          </g>

          <line x1="100" y1="100" x2="100" y2="10" stroke="#a855f7" stroke-width="2" opacity="0.85" class="radar-sweep"/>

          <circle v-for="dot in radarDots" :key="dot.id" :cx="dot.x" :cy="dot.y" r="3" :fill="dot.color" class="radar-blip" shape-rendering="crispEdges">
            <animate attributeName="r" values="3;5;3" dur="1.2s" repeatCount="indefinite"/>
            <animate attributeName="opacity" values="1;0.3;1" dur="1.2s" repeatCount="indefinite"/>
          </circle>
        </svg>
        <div class="radar-center-info">
          <div class="threat-level-label">{{ t('dashboard.threatLevel') }}</div>
          <div :class="['threat-level-value', threatLevelClass]" :data-text="threatLevelText">{{ threatLevelText }}</div>
          <div class="threat-score">{{ stats.totalCheats || 0 }} {{ t('dashboard.events') }}</div>
        </div>
      </div>

      <div class="hero-stats">
        <div class="hero-stats-terminal-line">
          <span class="terminal-prompt">root@acs:~$</span>
          <span class="terminal-cmd">cat stats.dat</span>
        </div>
        <div v-for="(card, i) in statCards" :key="i" :class="['hero-stat-card', card.cls]">
          <div class="hsc-icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path fill="currentColor" :d="card.icon"/></svg>
          </div>
          <div class="hsc-data">
            <div class="hsc-value">{{ animatedStats[card.key] ?? stats[card.key] }}</div>
            <div class="hsc-label">{{ card.label }}</div>
          </div>
          <div class="hsc-bar">
            <div class="hsc-bar-fill" :style="{ width: getBarWidth(card.key) + '%', background: card.barColor }"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="cc-panels">
      <div class="cc-panel cc-panel-charts" ref="chartsPanel1">
        <div class="panel-header">
          <div class="panel-title-bar"></div>
          <span class="panel-title" :data-text="t('dashboard.cheatTypesChart')">{{ t('dashboard.cheatTypesChart') }}</span>
          <span class="panel-decorator">╺━╸</span>
        </div>
        <div ref="cheatTypeChart" class="panel-chart"></div>
      </div>

      <div class="cc-panel cc-panel-trend" ref="chartsPanel2">
        <div class="panel-header">
          <div class="panel-title-bar"></div>
          <span class="panel-title" :data-text="t('dashboard.trendChart')">{{ t('dashboard.trendChart') }}</span>
          <span class="panel-decorator">╺━╸</span>
        </div>
        <div ref="trendChart" class="panel-chart"></div>
      </div>

      <div class="cc-panel cc-panel-risk" ref="chartsPanel3">
        <div class="panel-header">
          <div class="panel-title-bar"></div>
          <span class="panel-title" :data-text="t('dashboard.riskChart')">{{ t('dashboard.riskChart') }}</span>
          <span class="panel-decorator">╺━╸</span>
        </div>
        <div ref="riskChart" class="panel-chart"></div>
      </div>
    </div>

    <div class="cc-console">
      <div class="console-header">
        <div class="console-dots">
          <span class="dot red"></span>
          <span class="dot yellow"></span>
          <span class="dot green"></span>
        </div>
        <span class="console-title">[{{ t('dashboard.realTimeMonitor') }}]</span>
        <div class="console-actions">
          <div :class="['console-status', wsConnected ? 'online' : 'offline']">
            <span class="console-status-dot"></span>
            {{ wsConnected ? t('dashboard.live') : t('dashboard.offline') }}
          </div>
          <button class="console-btn" @click="analyzeDashboard" :disabled="aiDashboardLoading">
            <svg viewBox="0 0 24 24" width="12" height="12"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/></svg>
            AI
          </button>
          <button class="console-btn" @click="refreshData" :disabled="refreshing">
            <svg viewBox="0 0 24 24" width="12" height="12" :class="{ spinning: refreshing }"><path fill="currentColor" d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
          </button>
        </div>
      </div>
      <div class="console-body" ref="consoleBody">
        <div v-if="alerts.length === 0" class="console-await">
          <span class="console-blink">_</span> {{ wsConnected ? t('dashboard.awaitingData') : t('dashboard.offline') }}
        </div>
        <div v-for="(alert, index) in alerts" :key="index" :class="['console-line', getAlertClass(alert)]">
          <span class="console-time">[{{ formatConsoleTime(alert.detectionTime) }}]</span>
          <span :class="['console-tag', getAlertTagClass(alert)]">{{ alert.cheatType }}</span>
          <span class="console-player">{{ alert.playerName }}</span>
          <span class="console-detail">{{ alert.details }}</span>
          <span :class="['console-severity', getSeverityClass(alert.severity)]">Lv.{{ alert.severity }}</span>
        </div>
      </div>
    </div>

    <div v-if="aiDashboardResult" class="cc-ai-panel">
      <div class="ai-panel-header">
        <svg viewBox="0 0 24 24" width="16" height="16" style="color:#a855f7"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/></svg>
        <span>[{{ t('dashboard.aiAnalysis') }}]</span>
        <button class="ai-close" @click="aiDashboardResult = null">×</button>
      </div>
      <div class="ai-panel-body">
        <div v-if="aiDashboardResult.analysis" class="ai-block">
          <div class="ai-block-title">> {{ t('dashboard.analysis') }}</div>
          <div class="ai-block-text" v-html="renderAiText(aiDashboardResult.analysis)"></div>
        </div>
        <div class="ai-block-row">
          <div v-if="aiDashboardResult.verdict" class="ai-block">
            <div class="ai-block-title">> {{ t('dashboard.verdict') }}</div>
            <span :class="['ai-risk-badge', getRiskLevelClass(aiDashboardResult.verdict)]">{{ aiDashboardResult.verdict }}</span>
          </div>
          <div v-if="aiDashboardResult.suggestedAction" class="ai-block">
            <div class="ai-block-title">> {{ t('dashboard.action') }}</div>
            <div class="ai-block-text" v-html="renderAiText(aiDashboardResult.suggestedAction)"></div>
          </div>
        </div>
        <div v-if="aiDashboardResult.reasoning" class="ai-block">
          <div class="ai-block-title">> {{ t('dashboard.reasoning') }}</div>
          <div class="ai-block-text" v-html="renderAiText(aiDashboardResult.reasoning)"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, reactive, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { statsApi, aiApi } from '../api'
import { EventBus, Events } from '../utils/eventBus'
import { ElMessage } from 'element-plus'
import { renderAiText } from '../utils/helpers'

export default {
  name: 'Dashboard',
  props: { fullscreen: Boolean },
  setup() {
    const { t, locale } = useI18n()

    const stats = ref({ totalPlayers: 0, totalCheats: 0, highRiskPlayers: 0, mediumRiskPlayers: 0, lowRiskPlayers: 0, activeBans: 0, pendingReports: 0 })
    const animatedStats = reactive({})
    const alerts = ref([])
    const cheatTypeChart = ref(null)
    const trendChart = ref(null)
    const riskChart = ref(null)
    const consoleBody = ref(null)
    const radarContainer = ref(null)
    const particleCanvas = ref(null)
    const dataRainCanvas = ref(null)
    const chartsPanel1 = ref(null)
    const chartsPanel2 = ref(null)
    const chartsPanel3 = ref(null)
    const wsConnected = ref(false)
    const refreshing = ref(false)
    const uptime = ref('00:00:00')
    const aiDashboardLoading = ref(false)
    const aiDashboardResult = ref(null)
    const radarDots = ref([])
    let cheatTypeChartInstance = null
    let trendChartInstance = null
    let riskChartInstance = null
    let unsubscribeStatsChanged = null
    let unsubscribeCheatDetected = null
    let unsubscribeWsStatus = null
    let uptimeInterval = null
    let particleAnimFrame = null
    let rainAnimFrame = null
    let startTime = Date.now()
    let dotIdCounter = 0

    const statCards = [
      { key: 'totalPlayers', label: t('dashboard.totalPlayers'), cls: 'players', icon: 'M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z', barColor: '#3d8bfd' },
      { key: 'totalCheats', label: t('dashboard.cheatDetections'), cls: 'cheats', icon: 'M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z', barColor: '#ff3d5a' },
      { key: 'highRiskPlayers', label: t('dashboard.highRiskPlayers'), cls: 'risk', icon: 'M12 2L2 22h20L12 2m0 4l7.5 14h-15L12 6z', barColor: '#ff9100' },
      { key: 'activeBans', label: t('dashboard.activeBans'), cls: 'bans', icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z', barColor: '#a855f7' },
      { key: 'pendingReports', label: t('dashboard.pendingReports'), cls: 'reports', icon: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6m-1 2l5 5h-5V4z', barColor: '#06b6d4' }
    ]

    const threatLevelClass = computed(() => {
      const c = stats.value.totalCheats || 0
      if (c >= 50) return 'critical'
      if (c >= 20) return 'high'
      if (c >= 5) return 'medium'
      return 'low'
    })

    const threatLevelText = computed(() => {
      const cls = threatLevelClass.value
      if (cls === 'critical') return t('dashboard.severityCritical')
      if (cls === 'high') return t('dashboard.severityHigh')
      if (cls === 'medium') return t('dashboard.severityMedium')
      return t('dashboard.severityLow')
    })

    const getBarWidth = (key) => {
      const v = stats.value[key] || 0
      const maxMap = { totalPlayers: 100, totalCheats: 100, highRiskPlayers: 50, activeBans: 50, pendingReports: 30 }
      return Math.min((v / (maxMap[key] || 100)) * 100, 100)
    }

    const animateValue = (key, target) => {
      const start = animatedStats[key] || 0
      const diff = target - start
      if (diff === 0) return
      const duration = 600
      const animStart = performance.now()
      const step = (now) => {
        const elapsed = now - animStart
        const progress = Math.min(elapsed / duration, 1)
        const eased = 1 - Math.pow(1 - progress, 3)
        animatedStats[key] = Math.round(start + diff * eased)
        if (progress < 1) requestAnimationFrame(step)
      }
      requestAnimationFrame(step)
    }

    const addRadarDot = (color) => {
      const angle = Math.random() * Math.PI * 2
      const dist = 20 + Math.random() * 65
      const x = 100 + Math.cos(angle) * dist
      const y = 100 + Math.sin(angle) * dist
      const id = dotIdCounter++
      radarDots.value.push({ id, x, y, color })
      setTimeout(() => { radarDots.value = radarDots.value.filter(d => d.id !== id) }, 4000)
    }

    const formatConsoleTime = (ts) => {
      if (!ts) return '--:--:--'
      const d = new Date(ts)
      return d.toLocaleTimeString('en-US', { hour12: false })
    }

    const getAlertClass = (alert) => {
      if (alert.severity >= 4) return 'alert-critical'
      if (alert.severity >= 3) return 'alert-high'
      if (alert.severity >= 2) return 'alert-medium'
      return 'alert-low'
    }

    const getAlertTagClass = (alert) => {
      const map = { '飞行作弊': 'tag-fly', '速度作弊': 'tag-speed', '自动点击作弊': 'tag-auto', '杀戮光环': 'tag-kill', '瞄准辅助': 'tag-aim' }
      return map[alert.cheatType] || 'tag-default'
    }

    const getSeverityClass = (s) => {
      if (s >= 4) return 'sev-critical'
      if (s >= 3) return 'sev-high'
      if (s >= 2) return 'sev-medium'
      return 'sev-low'
    }

    const fetchOverviewStats = async () => {
      try {
        const data = await statsApi.getOverview()
        stats.value = { ...stats.value, ...data }
        Object.keys(data).forEach(k => animateValue(k, data[k]))
        nextTick(() => updateRiskChart())
      } catch (e) { console.error(e) }
    }

    const fetchCheatTypeStats = async () => {
      try {
        const data = await statsApi.getCheatTypes()
        nextTick(() => updateCheatTypeChart(data))
      } catch (e) {
        nextTick(() => updateCheatTypeChart({ '飞行作弊': 0, '速度作弊': 0, '自动点击作弊': 0, '杀戮光环': 0 }))
      }
    }

    const fetchTrendData = async () => {
      try {
        const data = await statsApi.getRecent(24)
        nextTick(() => updateTrendChart(data))
      } catch (e) { nextTick(() => updateTrendChart(null)) }
    }

    const refreshData = async () => {
      refreshing.value = true
      try { await Promise.all([fetchOverviewStats(), fetchCheatTypeStats(), fetchTrendData()]) } finally { refreshing.value = false }
    }

    const getRiskLabels = () => ({ low: t('players.low'), medium: t('players.medium'), high: t('players.high') })

    const updateCheatTypeChart = (data) => {
      if (!cheatTypeChartInstance && cheatTypeChart.value) cheatTypeChartInstance = echarts.init(cheatTypeChart.value)
      if (!cheatTypeChartInstance) return
      const chartData = Object.entries(data).map(([name, value]) => ({ name, value }))
      cheatTypeChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)', backgroundColor: '#0a0416', borderColor: 'rgba(168,85,247,0.4)', borderWidth: 1, textStyle: { color: '#c084fc', fontFamily: 'JetBrains Mono', fontSize: 11 } },
        legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#8899b4', fontFamily: 'JetBrains Mono', fontSize: 10 } },
        series: [{ name: 'Cheat Types', type: 'pie', radius: ['50%', '75%'], center: ['35%', '50%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 2, borderColor: '#0a0416', borderWidth: 2 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold', fontFamily: 'JetBrains Mono', color: '#e8f0ff' } }, data: chartData, color: ['#ff3d5a', '#ff9100', '#a855f7', '#10b981', '#06b6d4'] }]
      })
    }

    const updateTrendChart = (data) => {
      if (!trendChartInstance && trendChart.value) trendChartInstance = echarts.init(trendChart.value)
      if (!trendChartInstance) return
      let hours = [], values = []
      if (data && Array.isArray(data)) {
        data.forEach(item => { hours.push(item.hour || item.time || ''); values.push(item.count || 0) })
      } else {
        for (let i = 23; i >= 0; i--) { const h = new Date(); h.setHours(h.getHours() - i); hours.push(h.getHours() + ':00'); values.push(0) }
      }
      trendChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', backgroundColor: '#0a0416', borderColor: 'rgba(168,85,247,0.4)', borderWidth: 1, textStyle: { color: '#c084fc', fontFamily: 'JetBrains Mono', fontSize: 11 } },
        grid: { left: '10%', right: '4%', bottom: '14%', top: '8%' },
        xAxis: { type: 'category', boundaryGap: false, data: hours, axisLabel: { color: '#5a6f90', fontFamily: 'JetBrains Mono', fontSize: 8 }, axisLine: { lineStyle: { color: 'rgba(168,85,247,0.15)' } } },
        yAxis: { type: 'value', axisLabel: { color: '#5a6f90', fontFamily: 'JetBrains Mono', fontSize: 8 }, axisLine: { show: false }, splitLine: { lineStyle: { color: 'rgba(168,85,247,0.06)' } } },
        series: [{ name: 'Detections', type: 'line', smooth: true, symbol: 'none', areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(168,85,247,0.3)' }, { offset: 1, color: 'rgba(10,4,22,0)' }]) }, lineStyle: { color: '#a855f7', width: 2, shadowColor: 'rgba(168,85,247,0.5)', shadowBlur: 10 }, itemStyle: { color: '#a855f7' }, data: values }]
      })
    }

    const updateRiskChart = () => {
      if (!riskChartInstance && riskChart.value) riskChartInstance = echarts.init(riskChart.value)
      if (!riskChartInstance) return
      const labels = getRiskLabels()
      riskChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', backgroundColor: '#0a0416', borderColor: 'rgba(168,85,247,0.4)', borderWidth: 1, textStyle: { color: '#c084fc', fontFamily: 'JetBrains Mono', fontSize: 11 } },
        grid: { left: '10%', right: '4%', bottom: '14%', top: '8%' },
        xAxis: { type: 'category', data: [labels.low, labels.medium, labels.high], axisLabel: { color: '#8899b4', fontFamily: 'JetBrains Mono', fontSize: 11 }, axisLine: { lineStyle: { color: 'rgba(168,85,247,0.15)' } } },
        yAxis: { type: 'value', axisLabel: { color: '#5a6f90', fontFamily: 'JetBrains Mono', fontSize: 8 }, axisLine: { show: false }, splitLine: { lineStyle: { color: 'rgba(168,85,247,0.06)' } } },
        series: [{ name: t('dashboard.player'), type: 'bar', barWidth: 36, itemStyle: { borderRadius: [3, 3, 0, 0], color: (params) => new echarts.graphic.LinearGradient(0, 0, 0, 1, [[0, ['#10b981', '#f59e0b', '#ff3d5a'][params.dataIndex]], [1, 'rgba(0,0,0,0.2)']]) }, data: [stats.value.lowRiskPlayers, stats.value.mediumRiskPlayers, stats.value.highRiskPlayers] }]
      })
    }

    const handleCheatDetected = (data) => {
      alerts.value.unshift({ playerName: data.playerName, cheatType: data.cheatType, severity: data.severity, detectionTime: data.detectionTime, details: data.details })
      if (alerts.value.length > 30) alerts.value.pop()
      fetchOverviewStats(); nextTick(() => fetchCheatTypeStats())
      const colorMap = { 1: '#10b981', 2: '#f59e0b', 3: '#ff3d5a', 4: '#a855f7', 5: '#a855f7' }
      addRadarDot(colorMap[data.severity] || '#ff3d5a')
      nextTick(() => { if (consoleBody.value) consoleBody.value.scrollTop = 0 })
    }

    const initDataRain = () => {
      const canvas = dataRainCanvas.value
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const resize = () => { canvas.width = canvas.offsetWidth; canvas.height = canvas.offsetHeight }
      resize()
      window.addEventListener('resize', resize)

      const chars = 'アイウエオカキクケコサシスセソタチツテトabcdefghijklmnopqrstuvwxyz0123456789{}[]<>/#@$%&'
      const fontSize = 12
      let columns = Math.floor(canvas.width / fontSize)
      let drops = []

      const init = () => {
        columns = Math.floor(canvas.width / fontSize)
        drops = Array(columns).fill(0).map(() => Math.random() * -canvas.height)
      }
      init()
      resize()
      init()

      const animate = () => {
        ctx.fillStyle = 'rgba(5, 0, 10, 0.04)'
        ctx.fillRect(0, 0, canvas.width, canvas.height)

        const purple = { r: 168, g: 85, b: 247 }
        const cyan = { r: 6, g: 182, b: 212 }

        for (let i = 0; i < drops.length; i++) {
          const char = chars[Math.floor(Math.random() * chars.length)]
          const x = i * fontSize
          const y = drops[i] * fontSize

          const t = Math.min(drops[i] / (canvas.height / fontSize), 1)
          const color = t < 0.5 ? purple : cyan
          const alpha = 0.12 + Math.random() * 0.15
          ctx.fillStyle = `rgba(${color.r},${color.g},${color.b},${alpha})`
          ctx.font = `${fontSize}px "JetBrains Mono"`
          ctx.fillText(char, x, y)

          if (y > canvas.height && Math.random() > 0.98) drops[i] = 0
          drops[i]++
        }
        rainAnimFrame = requestAnimationFrame(animate)
      }
      animate()
    }

    const initParticles = () => {
      const canvas = particleCanvas.value
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const resize = () => { canvas.width = canvas.offsetWidth; canvas.height = canvas.offsetHeight }
      resize()
      window.addEventListener('resize', resize)

      const particles = []
      for (let i = 0; i < 50; i++) {
        particles.push({ x: Math.random() * canvas.width, y: Math.random() * canvas.height, vx: (Math.random() - 0.5) * 0.25, vy: (Math.random() - 0.5) * 0.25, size: Math.random() * 1.5 + 0.5, opacity: Math.random() * 0.3 + 0.08 })
      }

      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        particles.forEach(p => {
          p.x += p.vx; p.y += p.vy
          if (p.x < 0) p.x = canvas.width; if (p.x > canvas.width) p.x = 0
          if (p.y < 0) p.y = canvas.height; if (p.y > canvas.height) p.y = 0
          ctx.beginPath()
          ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
          ctx.fillStyle = `rgba(168, 85, 247, ${p.opacity})`
          ctx.fill()
        })
        for (let i = 0; i < particles.length; i++) {
          for (let j = i + 1; j < particles.length; j++) {
            const dx = particles[i].x - particles[j].x; const dy = particles[i].y - particles[j].y
            const dist = Math.sqrt(dx * dx + dy * dy)
            if (dist < 100) {
              ctx.beginPath(); ctx.moveTo(particles[i].x, particles[i].y); ctx.lineTo(particles[j].x, particles[j].y)
              ctx.strokeStyle = `rgba(168, 85, 247, ${0.05 * (1 - dist / 100)})`
              ctx.lineWidth = 0.5; ctx.stroke()
            }
          }
        }
        particleAnimFrame = requestAnimationFrame(animate)
      }
      animate()
    }

    const setupHolographicTilt = () => {
      const panels = [chartsPanel1.value, chartsPanel2.value, chartsPanel3.value].filter(Boolean)
      panels.forEach(panel => {
        panel.addEventListener('mousemove', (e) => {
          const rect = panel.getBoundingClientRect()
          const x = (e.clientX - rect.left) / rect.width - 0.5
          const y = (e.clientY - rect.top) / rect.height - 0.5
          panel.style.transform = `perspective(600px) rotateY(${x * 4}deg) rotateX(${-y * 4}deg)`
          panel.style.boxShadow = `${-x * 10}px ${-y * 10}px 20px rgba(147, 51, 234, 0.08)`
        })
        panel.addEventListener('mouseleave', () => {
          panel.style.transform = 'perspective(600px) rotateY(0deg) rotateX(0deg)'
          panel.style.boxShadow = 'none'
          panel.style.transition = 'transform 0.5s ease, box-shadow 0.5s ease'
        })
        panel.addEventListener('mouseenter', () => {
          panel.style.transition = 'transform 0.1s ease, box-shadow 0.1s ease'
        })
      })
    }

    const handleResize = () => { cheatTypeChartInstance?.resize(); trendChartInstance?.resize(); riskChartInstance?.resize() }

    const updateUptime = () => {
      const diff = Math.floor((Date.now() - startTime) / 1000)
      uptime.value = `${String(Math.floor(diff / 3600)).padStart(2, '0')}:${String(Math.floor((diff % 3600) / 60)).padStart(2, '0')}:${String(diff % 60).padStart(2, '0')}`
    }

    const analyzeDashboard = async () => {
      aiDashboardLoading.value = true; aiDashboardResult.value = null
      try {
        const result = await aiApi.analyzeDashboard()
        if (result.success) aiDashboardResult.value = result
        else ElMessage.warning(result.error || t('ai.error'))
      } catch (e) { ElMessage.error(t('ai.networkError')) } finally { aiDashboardLoading.value = false }
    }

    const getRiskLevelClass = (level) => {
      if (!level) return ''
      const l = level.toUpperCase()
      if (l.includes('HIGH')) return 'risk-high'
      if (l.includes('MEDIUM')) return 'risk-medium'
      if (l.includes('LOW')) return 'risk-low'
      return 'risk-medium'
    }

    onMounted(() => {
      fetchOverviewStats(); fetchCheatTypeStats(); fetchTrendData(); nextTick(() => updateRiskChart())
      window.addEventListener('resize', handleResize)
      startTime = Date.now(); uptimeInterval = setInterval(updateUptime, 1000)
      unsubscribeStatsChanged = EventBus.on(Events.STATS_CHANGED, () => { fetchOverviewStats(); nextTick(() => fetchCheatTypeStats()) })
      unsubscribeCheatDetected = EventBus.on(Events.CHEAT_DETECTED, handleCheatDetected)
      unsubscribeWsStatus = EventBus.on('ws:status', (c) => { wsConnected.value = c })
      if (window.__wsConnected !== undefined) wsConnected.value = window.__wsConnected
      if (window.__lastCheatData && Date.now() - window.__lastCheatTime < 5000) handleCheatDetected(window.__lastCheatData)
      initDataRain(); initParticles(); setupHolographicTilt()
    })

    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
      if (uptimeInterval) clearInterval(uptimeInterval)
      if (particleAnimFrame) cancelAnimationFrame(particleAnimFrame)
      if (rainAnimFrame) cancelAnimationFrame(rainAnimFrame)
      unsubscribeStatsChanged?.(); unsubscribeCheatDetected?.(); unsubscribeWsStatus?.()
      cheatTypeChartInstance?.dispose(); trendChartInstance?.dispose(); riskChartInstance?.dispose()
    })

    return {
      stats, animatedStats, alerts, cheatTypeChart, trendChart, riskChart, consoleBody, radarContainer, particleCanvas, dataRainCanvas,
      wsConnected, refreshing, uptime, statCards, aiDashboardLoading, aiDashboardResult, radarDots,
      threatLevelClass, threatLevelText, getBarWidth, chartsPanel1, chartsPanel2, chartsPanel3,
      refreshData, analyzeDashboard, renderAiText, getRiskLevelClass, formatConsoleTime,
      getAlertClass, getAlertTagClass, getSeverityClass, t
    }
  }
}
</script>

<style scoped>
.command-center {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 22px;
  min-height: 100%;
}

/* ---- Canvas layers ---- */
.cc-datarain {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;
  z-index: 0;
  opacity: 0.35;
}

.cc-particles {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;
  z-index: 0;
}

/* ========== HERO ROW ========== */
.cc-hero {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 22px;
  position: relative;
  z-index: 1;
}

/* ---- MC Pixel Radar ---- */
.threat-radar {
  position: relative;
  width: 320px;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle, rgba(168,85,247,0.06) 0%, transparent 70%);
  border: 2px solid rgba(147,51,234,0.2);
  border-radius: 0;
  image-rendering: pixelated;
  animation: radarPulse 4s ease-in-out infinite;
  box-shadow:
    inset 0 0 40px rgba(168,85,247,0.04),
    0 0 30px rgba(147,51,234,0.05);
}

.radar-svg {
  width: 280px;
  height: 280px;
  position: absolute;
  image-rendering: pixelated;
}

.radar-sweep {
  transform-origin: 100px 100px;
  animation: radarSweep 3s linear infinite;
}

@keyframes radarSweep {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.radar-blip {
  filter: drop-shadow(0 0 6px currentColor);
}

.radar-ring-glow {
  animation: ringGlow 3s ease-in-out infinite;
}

@keyframes ringGlow {
  0%, 100% { opacity: 0.35; }
  50% { opacity: 0.65; }
}

@keyframes radarPulse {
  0%, 100% { box-shadow: inset 0 0 20px rgba(168,85,247,0.03), 0 0 20px rgba(147,51,234,0.04); }
  50% { box-shadow: inset 0 0 50px rgba(168,85,247,0.07), 0 0 40px rgba(147,51,234,0.08); }
}

.radar-center-info {
  position: relative;
  z-index: 2;
  text-align: center;
  pointer-events: none;
}

.threat-level-label {
  font-family: var(--font-mono);
  font-size: 9px;
  letter-spacing: 3px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.threat-level-value {
  font-family: var(--font-mono);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 4px;
  position: relative;
}

.threat-level-value.low { color: #10b981; text-shadow: 0 0 20px rgba(16,185,129,0.4); }
.threat-level-value.medium { color: #f59e0b; text-shadow: 0 0 20px rgba(245,158,11,0.4); }
.threat-level-value.high { color: #ff3d5a; text-shadow: 0 0 20px rgba(255,61,90,0.4); animation: threatFlash 1.8s ease-in-out infinite; }
.threat-level-value.critical { color: #a855f7; text-shadow: 0 0 28px rgba(168,85,247,0.7); animation: threatGlitch 0.3s ease-in-out infinite; }

@keyframes threatGlitch {
  0%, 92%, 100% { transform: translate(0); opacity: 1; }
  93% { transform: translate(-3px, 1px); opacity: 0.8; }
  94% { transform: translate(4px, -1px); opacity: 0.6; }
  95% { transform: translate(-2px, 2px); opacity: 0.9; }
  96% { transform: translate(0); opacity: 1; }
}

@keyframes threatFlash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.threat-score {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 1px;
  margin-top: 4px;
}

/* ---- Terminal Hero Stats ---- */
.hero-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero-stats-terminal-line {
  font-family: var(--font-mono);
  font-size: 10px;
  margin-bottom: 2px;
  padding: 2px 0;
}

.terminal-prompt { color: #a855f7; font-weight: 700; }
.terminal-cmd { color: #c084fc; margin-left: 8px; }

.hero-stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 13px 18px;
  background: linear-gradient(135deg, rgba(10,4,22,0.8) 0%, rgba(14,6,28,0.7) 100%);
  border: 2px solid rgba(147,51,234,0.08);
  border-radius: 2px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  image-rendering: pixelated;
}

.hero-stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  transition: all 0.3s ease;
}

.hero-stat-card.players::before { background: #3d8bfd; }
.hero-stat-card.cheats::before { background: #ff3d5a; }
.hero-stat-card.risk::before { background: #f59e0b; }
.hero-stat-card.bans::before { background: #a855f7; }
.hero-stat-card.reports::before { background: #06b6d4; }

.hero-stat-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(168,85,247,0.012) 2px, rgba(168,85,247,0.012) 4px);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.hero-stat-card:hover {
  border-color: rgba(168,85,247,0.45);
  background: linear-gradient(135deg, rgba(18,8,38,0.9) 0%, rgba(22,10,42,0.85) 100%);
  transform: translateX(6px);
  box-shadow:
    0 2px 20px rgba(147,51,234,0.15),
    inset 0 0 20px rgba(168,85,247,0.03);
}

.hero-stat-card:hover::after { opacity: 1; }

.hsc-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
  flex-shrink: 0;
  border: 1px solid rgba(255,255,255,0.04);
}

.hero-stat-card.players .hsc-icon { background: rgba(61,139,253,0.12); color: #3d8bfd; }
.hero-stat-card.cheats .hsc-icon { background: rgba(255,61,90,0.12); color: #ff3d5a; }
.hero-stat-card.risk .hsc-icon { background: rgba(245,158,11,0.12); color: #f59e0b; }
.hero-stat-card.bans .hsc-icon { background: rgba(168,85,247,0.12); color: #a855f7; }
.hero-stat-card.reports .hsc-icon { background: rgba(6,182,212,0.1); color: #06b6d4; }

.hsc-data { flex: 1; min-width: 0; }

.hsc-value {
  font-family: var(--font-mono);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: 2px;
}

.hsc-label {
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 3px;
  letter-spacing: 0.5px;
}

.hsc-bar {
  width: 60px;
  height: 5px;
  background: rgba(255,255,255,0.03);
  border-radius: 1px;
  overflow: hidden;
  flex-shrink: 0;
}

.hsc-bar-fill {
  height: 100%;
  border-radius: 1px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ========== PANELS ========== */
.cc-panels {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 18px;
  position: relative;
  z-index: 1;
}

.cc-panel {
  background: rgba(10,4,22,0.75);
  border: 1px solid rgba(147,51,234,0.1);
  border-radius: 2px;
  overflow: hidden;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  cursor: default;
}

.cc-panel:hover {
  border-color: rgba(168,85,247,0.35);
  box-shadow: 0 4px 24px rgba(147,51,234,0.08);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(147,51,234,0.08);
}

.panel-title-bar {
  width: 4px;
  height: 14px;
  background: linear-gradient(180deg, #a855f7, #7c3aed);
  border-radius: 0;
}

.panel-title {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #c084fc;
  text-transform: uppercase;
}

.panel-decorator {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 10px;
  color: rgba(168,85,247,0.25);
  letter-spacing: 2px;
}

.panel-chart {
  height: 220px;
  padding: 8px;
}

/* ========== CONSOLE ========== */
.cc-console {
  background: rgba(4,1,14,0.94);
  border: 1px solid rgba(147,51,234,0.15);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.cc-console::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(168,85,247,0.01) 2px,
    rgba(168,85,247,0.01) 4px
  );
  pointer-events: none;
  z-index: 1;
}

.console-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(147,51,234,0.12);
  background: rgba(6,2,16,0.5);
  position: relative;
  z-index: 2;
}

.console-dots { display: flex; gap: 6px; }

.console-dots .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.console-dots .dot.red { background: #ff5f57; }
.console-dots .dot.yellow { background: #febc2e; }
.console-dots .dot.green { background: #28c840; }

.console-title {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #c084fc;
  flex: 1;
}

.console-actions { display: flex; align-items: center; gap: 8px; }

.console-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 3px 10px;
  border-radius: 10px;
}

.console-status.online {
  color: #10b981;
  background: rgba(16,185,129,0.1);
  border: 1px solid rgba(16,185,129,0.2);
}

.console-status.offline {
  color: #ff3d5a;
  background: rgba(255,61,90,0.1);
  border: 1px solid rgba(255,61,90,0.2);
}

.console-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { box-shadow: 0 0 4px currentColor; }
  50% { box-shadow: 0 0 10px currentColor; }
}

.console-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: rgba(147,51,234,0.1);
  border: 1px solid rgba(147,51,234,0.2);
  border-radius: 2px;
  color: #c084fc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.console-btn:hover:not(:disabled) {
  background: rgba(168,85,247,0.22);
  border-color: rgba(168,85,247,0.45);
  box-shadow: 0 0 10px rgba(147,51,234,0.18);
}

.console-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.console-body {
  max-height: 240px;
  overflow-y: auto;
  padding: 8px 0;
  font-family: var(--font-mono);
  font-size: 11px;
  position: relative;
  z-index: 2;
  scrollbar-width: thin;
  scrollbar-color: rgba(168,85,247,0.15) transparent;
}

.console-body::-webkit-scrollbar { width: 4px; }
.console-body::-webkit-scrollbar-thumb { background: rgba(168,85,247,0.15); border-radius: 2px; }

.console-await {
  padding: 24px 16px;
  color: var(--text-muted);
  font-size: 11px;
  text-align: center;
}

.console-blink {
  color: #a855f7;
  animation: cursorBlink 1s step-end infinite;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.console-line {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 16px;
  border-left: 2px solid transparent;
  transition: all 0.15s ease;
}

.console-line:hover { background: rgba(147,51,234,0.05); }

.console-line.alert-critical { border-left-color: #a855f7; box-shadow: inset 0 0 12px rgba(168,85,247,0.06); }
.console-line.alert-high { border-left-color: #ff3d5a; }
.console-line.alert-medium { border-left-color: #f59e0b; }
.console-line.alert-low { border-left-color: #10b981; }

.console-time { color: var(--text-muted); font-size: 10px; flex-shrink: 0; }

.console-tag {
  padding: 1px 6px;
  border-radius: 2px;
  font-size: 9px;
  font-weight: 600;
  flex-shrink: 0;
}

.console-tag.tag-fly { background: rgba(255,61,90,0.15); color: #ff3d5a; }
.console-tag.tag-speed { background: rgba(245,158,11,0.15); color: #f59e0b; }
.console-tag.tag-auto { background: rgba(61,139,253,0.15); color: #3d8bfd; }
.console-tag.tag-kill { background: rgba(168,85,247,0.15); color: #a855f7; }
.console-tag.tag-aim { background: rgba(245,158,11,0.15); color: #f59e0b; }
.console-tag.tag-default { background: rgba(168,85,247,0.12); color: #c084fc; }

.console-player { color: var(--text-primary); font-weight: 700; min-width: 75px; }
.console-detail { color: var(--text-secondary); flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.console-severity {
  padding: 1px 6px;
  border-radius: 2px;
  font-size: 9px;
  font-weight: 700;
  flex-shrink: 0;
}

.console-severity.sev-critical { background: rgba(168,85,247,0.18); color: #a855f7; }
.console-severity.sev-high { background: rgba(255,61,90,0.18); color: #ff3d5a; }
.console-severity.sev-medium { background: rgba(245,158,11,0.18); color: #f59e0b; }
.console-severity.sev-low { background: rgba(16,185,129,0.18); color: #10b981; }

/* ========== AI PANEL ========== */
.cc-ai-panel {
  background: rgba(12,5,28,0.65);
  border: 1px solid rgba(147,51,234,0.25);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.4s ease-out;
  box-shadow: 0 0 24px rgba(147,51,234,0.1);
}

.ai-panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(147,51,234,0.12);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #c084fc;
}

.ai-close {
  margin-left: auto;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,61,90,0.12);
  border: 1px solid rgba(255,61,90,0.25);
  border-radius: 2px;
  color: #ff3d5a;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.ai-close:hover { background: #ff3d5a; color: #fff; }

.ai-panel-body { padding: 16px; display: flex; flex-direction: column; gap: 14px; }
.ai-block-row { display: flex; gap: 16px; }
.ai-block-row .ai-block { flex: 1; }

.ai-block-title {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #a855f7;
  margin-bottom: 6px;
}

.ai-block-text { font-size: 12px; line-height: 1.7; color: var(--text-secondary); }
.ai-block-text :deep(strong) { color: #c084fc; }
.ai-block-text :deep(code) { background: rgba(168,85,247,0.12); padding: 1px 6px; border-radius: 2px; font-size: 10px; }

.ai-risk-badge {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 2px;
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
}

.ai-risk-badge.risk-high { background: rgba(255,61,90,0.14); color: #ff3d5a; }
.ai-risk-badge.risk-medium { background: rgba(245,158,11,0.14); color: #f59e0b; }
.ai-risk-badge.risk-low { background: rgba(16,185,129,0.14); color: #10b981; }

.spinning { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== RESPONSIVE ========== */
@media (max-width: 1199px) {
  .cc-panels { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 1024px) {
  .cc-hero { grid-template-columns: 1fr; justify-items: center; }
  .hero-stats { width: 100%; }
  .cc-panels { grid-template-columns: 1fr; }
}

@media (max-width: 767px) {
  .threat-radar { width: 240px; height: 240px; }
  .radar-svg { width: 210px; height: 210px; }
  .console-line { flex-wrap: wrap; }
  .console-detail { width: 100%; order: 10; }
}
</style>
