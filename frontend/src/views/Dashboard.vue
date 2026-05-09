<template>
  <div class="dashboard-container">
    <canvas class="dashboard-bg-canvas" ref="bgCanvas"></canvas>

    <div class="dashboard-grid">
      <!-- 统计卡片行 -->
      <div class="stats-row">
        <div v-for="(card, index) in statCards" :key="index" :class="['stat-card', card.type]">
          <div class="stat-card-glow"></div>
          <div class="stat-card-content">
            <div class="stat-icon-wrapper">
              <svg viewBox="0 0 24 24" width="24" height="24">
                <path fill="currentColor" :d="card.icon"/>
              </svg>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ animatedStats[card.key] ?? stats[card.key] }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
          <div class="stat-card-bar">
            <div class="stat-bar-fill" :style="{ width: getBarWidth(card.key) + '%' }"></div>
          </div>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 左侧：威胁雷达 + 实时监控 -->
        <div class="left-panel">
          <!-- 威胁雷达 -->
          <div class="panel threat-radar-panel">
            <div class="panel-header">
              <div class="panel-icon">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor" d="M12 2L2 22h20L12 2m0 4l7.5 14h-15L12 6z"/>
                </svg>
              </div>
              <span class="panel-title">{{ t('dashboard.threatLevel') }}</span>
              <div class="threat-badge" :class="threatLevelClass">
                {{ threatLevelText }}
              </div>
            </div>
            <div class="radar-container">
              <svg viewBox="0 0 200 200" class="radar-svg">
                <defs>
                  <radialGradient id="radarGlow">
                    <stop offset="0%" stop-color="var(--accent-gold)" stop-opacity="0.2"/>
                    <stop offset="100%" stop-color="transparent"/>
                  </radialGradient>
                </defs>
                <circle cx="100" cy="100" r="90" fill="url(#radarGlow)" opacity="0.3"/>

                <circle cx="100" cy="100" r="80" fill="none" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>
                <circle cx="100" cy="100" r="60" fill="none" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>
                <circle cx="100" cy="100" r="40" fill="none" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>
                <circle cx="100" cy="100" r="20" fill="none" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>

                <line x1="100" y1="20" x2="100" y2="180" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>
                <line x1="20" y1="100" x2="180" y2="100" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>
                <line x1="43" y1="43" x2="157" y2="157" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>
                <line x1="157" y1="43" x2="43" y2="157" stroke="var(--border-color)" stroke-width="0.5" opacity="0.3"/>

                <g class="radar-sweep-group">
                  <line x1="100" y1="100" x2="100" y2="20"
                        stroke="var(--accent-gold)"
                        stroke-width="2"
                        class="radar-sweep-line">
                    <animateTransform attributeName="transform"
                                      type="rotate"
                                      from="0 100 100"
                                      to="360 100 100"
                                      dur="3s"
                                      repeatCount="indefinite"/>
                  </line>
                  <polygon points="100,100 95,25 105,25"
                           fill="var(--accent-gold)"
                           opacity="0.15"
                           class="radar-sweep-area">
                    <animateTransform attributeName="transform"
                                      type="rotate"
                                      from="0 100 100"
                                      to="360 100 100"
                                      dur="3s"
                                      repeatCount="indefinite"/>
                  </polygon>
                </g>

                <circle v-for="dot in radarDots" :key="dot.id"
                        :cx="dot.x" :cy="dot.y" :r="dot.size"
                        :fill="dot.color" class="radar-dot">
                  <animate attributeName="r" :values="`${dot.size};${dot.size * 1.8};${dot.size}`" dur="1.5s" repeatCount="indefinite"/>
                  <animate attributeName="opacity" values="1;0.4;1" dur="1.5s" repeatCount="indefinite"/>
                </circle>
              </svg>
              <div class="radar-center">
                <div class="radar-value">{{ stats.totalCheats || 0 }}</div>
                <div class="radar-label">{{ t('dashboard.events') }}</div>
              </div>
            </div>
          </div>

          <!-- 实时监控控制台 -->
          <div class="panel console-panel">
            <div class="panel-header">
              <div class="console-dots">
                <span class="dot red"></span>
                <span class="dot yellow"></span>
                <span class="dot green"></span>
              </div>
              <span class="panel-title">[{{ t('dashboard.realTimeMonitor') }}]</span>
              <div class="console-actions">
                <div :class="['connection-status', wsConnected ? 'connected' : 'disconnected']">
                  <span class="status-dot"></span>
                  {{ wsConnected ? t('dashboard.live') : t('dashboard.offline') }}
                </div>
                <button class="action-btn ai-btn" @click="analyzeDashboard" :disabled="aiDashboardLoading">
                  <svg viewBox="0 0 24 24" width="14" height="14">
                    <path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/>
                  </svg>
                  AI
                </button>
                <button class="action-btn refresh-btn" @click="refreshData" :disabled="refreshing">
                  <svg viewBox="0 0 24 24" width="14" height="14" :class="{ spinning: refreshing }">
                    <path fill="currentColor" d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
                  </svg>
                </button>
              </div>
            </div>
            <div class="console-body" ref="consoleBody">
              <div v-if="alerts.length === 0" class="console-empty">
                <span class="cursor-blink">_</span> {{ wsConnected ? t('dashboard.awaitingData') : t('dashboard.offline') }}
              </div>
              <transition-group name="alert-list" tag="div" class="alert-list">
                <div v-for="(alert, index) in alerts" :key="alert.detectionTime || index"
                     :class="['alert-item', getAlertClass(alert)]">
                  <div class="alert-time">[{{ formatConsoleTime(alert.detectionTime) }}]</div>
                  <div :class="['alert-tag', getAlertTagClass(alert)]">{{ alert.cheatType }}</div>
                  <div class="alert-player">{{ alert.playerName }}</div>
                  <div class="alert-detail">{{ alert.details }}</div>
                  <div :class="['alert-severity', getSeverityClass(alert.severity)]">Lv.{{ alert.severity }}</div>
                </div>
              </transition-group>
            </div>
          </div>
        </div>

        <!-- 右侧：图表区域 -->
        <div class="right-panel">
          <!-- 作弊类型分布 -->
          <div class="panel chart-panel">
            <div class="panel-header">
              <div class="panel-icon">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor" d="M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z"/>
                </svg>
              </div>
              <span class="panel-title">{{ t('dashboard.cheatTypesChart') }}</span>
            </div>
            <div ref="cheatTypeChart" class="chart-container"></div>
          </div>

          <!-- 趋势图 -->
          <div class="panel chart-panel">
            <div class="panel-header">
              <div class="panel-icon">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor" d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
                </svg>
              </div>
              <span class="panel-title">{{ t('dashboard.trendChart') }}</span>
            </div>
            <div ref="trendChart" class="chart-container"></div>
          </div>

          <!-- 风险等级分布 -->
          <div class="panel chart-panel">
            <div class="panel-header">
              <div class="panel-icon">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
                </svg>
              </div>
              <span class="panel-title">{{ t('dashboard.riskChart') }}</span>
            </div>
            <div ref="riskChart" class="chart-container"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI分析面板 -->
    <transition name="slide-up">
      <div v-if="aiDashboardResult" class="ai-analysis-panel">
        <div class="ai-panel-header">
          <svg viewBox="0 0 24 24" width="18" height="18" style="color: var(--accent-gold)">
            <path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/>
          </svg>
          <span>[{{ t('dashboard.aiAnalysis') }}]</span>
          <button class="close-btn" @click="aiDashboardResult = null">×</button>
        </div>
        <div class="ai-panel-content">
          <div v-if="aiDashboardResult.analysis" class="ai-section">
            <div class="ai-section-title">{{ t('dashboard.analysis') }}</div>
            <div class="ai-section-text" v-html="renderAiText(aiDashboardResult.analysis)"></div>
          </div>
          <div class="ai-row">
            <div v-if="aiDashboardResult.verdict" class="ai-section half">
              <div class="ai-section-title">{{ t('dashboard.verdict') }}</div>
              <div :class="['risk-indicator', getRiskLevelClass(aiDashboardResult.verdict)]">
                {{ aiDashboardResult.verdict }}
              </div>
            </div>
            <div v-if="aiDashboardResult.suggestedAction" class="ai-section half">
              <div class="ai-section-title">{{ t('dashboard.action') }}</div>
              <div class="ai-section-text" v-html="renderAiText(aiDashboardResult.suggestedAction)"></div>
            </div>
          </div>
          <div v-if="aiDashboardResult.reasoning" class="ai-section">
            <div class="ai-section-title">{{ t('dashboard.reasoning') }}</div>
            <div class="ai-section-text" v-html="renderAiText(aiDashboardResult.reasoning)"></div>
          </div>
        </div>
      </div>
    </transition>
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
  setup() {
    const { t } = useI18n()

    const stats = ref({
      totalPlayers: 0,
      totalCheats: 0,
      highRiskPlayers: 0,
      mediumRiskPlayers: 0,
      lowRiskPlayers: 0,
      activeBans: 0,
      pendingReports: 0
    })

    const animatedStats = reactive({})
    const alerts = ref([])
    const cheatTypeChart = ref(null)
    const trendChart = ref(null)
    const riskChart = ref(null)
    const consoleBody = ref(null)
    const bgCanvas = ref(null)
    const wsConnected = ref(false)
    const refreshing = ref(false)
    const aiDashboardLoading = ref(false)
    const aiDashboardResult = ref(null)
    const radarDots = ref([])

    let cheatTypeChartInstance = null
    let trendChartInstance = null
    let riskChartInstance = null
    let unsubscribeStatsChanged = null
    let controller = null
    let unsubscribeCheatDetected = null
    let unsubscribeWsStatus = null
    let bgAnimFrame = null
    let dotIdCounter = 0

    const statCards = computed(() => [
      {
        key: 'totalPlayers',
        label: t('dashboard.totalPlayers'),
        type: 'players',
        icon: 'M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z'
      },
      {
        key: 'totalCheats',
        label: t('dashboard.cheatDetections'),
        type: 'cheats',
        icon: 'M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z'
      },
      {
        key: 'highRiskPlayers',
        label: t('dashboard.highRiskPlayers'),
        type: 'risk',
        icon: 'M12 2L2 22h20L12 2m0 4l7.5 14h-15L12 6z'
      },
      {
        key: 'activeBans',
        label: t('dashboard.activeBans'),
        type: 'bans',
        icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z'
      },
      {
        key: 'pendingReports',
        label: t('dashboard.pendingReports'),
        type: 'reports',
        icon: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6m-1 2l5 5h-5V4z'
      }
    ])

    const threatLevelClass = computed(() => {
      const count = stats.value.totalCheats || 0
      if (count >= 50) return 'critical'
      if (count >= 20) return 'high'
      if (count >= 5) return 'medium'
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
      const value = stats.value[key] || 0
      const keys = Object.keys(stats.value).filter(k => k !== 'mediumRiskPlayers' && k !== 'lowRiskPlayers')
      const maxValue = Math.max(...keys.map(k => stats.value[k] || 0), 1)
      return Math.min((value / maxValue) * 100, 100)
    }

    const animateValue = (key, target) => {
      const start = animatedStats[key] || 0
      const diff = target - start
      if (diff === 0) return

      const duration = 800
      const startTime = performance.now()

      const step = (currentTime) => {
        const elapsed = currentTime - startTime
        const progress = Math.min(elapsed / duration, 1)
        const eased = 1 - Math.pow(1 - progress, 3)
        animatedStats[key] = Math.round(start + diff * eased)

        if (progress < 1) {
          requestAnimationFrame(step)
        }
      }

      requestAnimationFrame(step)
    }

    const addRadarDot = (severity) => {
      const angle = Math.random() * Math.PI * 2
      const distance = 20 + Math.random() * 65
      const x = 100 + Math.cos(angle) * distance
      const y = 100 + Math.sin(angle) * distance
      const id = dotIdCounter++

      const colorMap = {
        1: '#2ECC71',
        2: '#FF8C00',
        3: '#E74C3C',
        4: '#9B59B6',
        5: '#FFC800'
      }

      radarDots.value.push({
        id,
        x,
        y,
        color: colorMap[severity] || '#FFC800',
        size: 3
      })

      setTimeout(() => {
        radarDots.value = radarDots.value.filter(d => d.id !== id)
      }, 4000)
    }

    const formatConsoleTime = (timestamp) => {
      if (!timestamp) return '--:--:--'
      const date = new Date(timestamp)
      return date.toLocaleTimeString('en-US', { hour12: false })
    }

    const getAlertClass = (alert) => {
      if (alert.severity >= 4) return 'critical'
      if (alert.severity >= 3) return 'high'
      if (alert.severity >= 2) return 'medium'
      return 'low'
    }

    const getAlertTagClass = (alert) => {
      if (!alert.cheatType) return 'default'

      const type = alert.cheatType.toLowerCase()
      if (type.includes('fly') || type.includes('飞行')) return 'fly'
      if (type.includes('speed') || type.includes('速度')) return 'speed'
      if (type.includes('auto') || type.includes('click') || type.includes('点击')) return 'auto'
      if (type.includes('kill') || type.includes('aura') || type.includes('杀戮')) return 'kill'
      if (type.includes('aim') || type.includes('瞄准')) return 'aim'
      return 'default'
    }

    const getSeverityClass = (severity) => {
      if (severity >= 4) return 'critical'
      if (severity >= 3) return 'high'
      if (severity >= 2) return 'medium'
      return 'low'
    }

    const fetchOverviewStats = async () => {
      try {
        controller = new AbortController()
        const result = await statsApi.getOverview({ signal: controller.signal })
        const data = result.data || result
        stats.value = { ...stats.value, ...data }

        Object.keys(data).forEach(key => {
          animateValue(key, data[key])
        })

        nextTick(() => updateRiskChart())
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error(error)
        }
      }
    }

    const fetchCheatTypeStats = async () => {
      try {
        controller = new AbortController()
        const result = await statsApi.getCheatTypes({ signal: controller.signal })
        const data = result.data || result
        nextTick(() => updateCheatTypeChart(data))
      } catch (error) {
        if (error.name !== 'AbortError') {
          nextTick(() => {
            updateCheatTypeChart({
              '飞行作弊': 0,
              '速度作弊': 0,
              '自动点击作弊': 0,
              '杀戮光环': 0
            })
          })
        }
      }
    }

    const fetchTrendData = async () => {
      try {
        controller = new AbortController()
        const result = await statsApi.getRecent(24, { signal: controller.signal })
        const data = result.data || result
        nextTick(() => updateTrendChart(data?.hourlyData || null))
      } catch (error) {
        if (error.name !== 'AbortError') {
          nextTick(() => updateTrendChart(null))
        }
      }
    }

    const refreshData = async () => {
      refreshing.value = true
      try {
        await Promise.all([
          fetchOverviewStats(),
          fetchCheatTypeStats(),
          fetchTrendData()
        ])
      } finally {
        refreshing.value = false
      }
    }

    const getRiskLabels = () => ({
      low: t('players.low'),
      medium: t('players.medium'),
      high: t('players.high')
    })

    const updateCheatTypeChart = (data) => {
      if (!cheatTypeChartInstance && cheatTypeChart.value) {
        cheatTypeChartInstance = echarts.init(cheatTypeChart.value)
      }
      if (!cheatTypeChartInstance) return

      const chartData = Object.entries(data).map(([name, value]) => ({ name, value }))

      cheatTypeChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          backgroundColor: 'rgba(10, 10, 15, 0.95)',
          borderColor: 'rgba(255, 200, 0, 0.3)',
          borderWidth: 1,
          textStyle: {
            color: '#E8E8ED',
            fontFamily: 'var(--font-sans)',
            fontSize: 12
          }
        },
        legend: {
          orient: 'vertical',
          right: '5%',
          top: 'center',
          textStyle: {
            color: '#9898A8',
            fontFamily: 'var(--font-sans)',
            fontSize: 11
          },
          itemWidth: 12,
          itemHeight: 12,
          itemGap: 12
        },
        series: [{
          name: 'Cheat Types',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['38%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
            borderColor: 'rgba(10, 10, 15, 0.8)',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              fontFamily: 'var(--font-sans)',
              color: '#E8E8ED'
            }
          },
          data: chartData,
          color: ['#E74C3C', '#FF8C00', '#FFC800', '#4A9EFF', '#2ECC71']
        }]
      })
    }

    const updateTrendChart = (data) => {
      if (!trendChartInstance && trendChart.value) {
        trendChartInstance = echarts.init(trendChart.value)
      }
      if (!trendChartInstance) return

      let hours = []
      let values = []

      if (data && Array.isArray(data)) {
        data.forEach(item => {
          hours.push(item.hour || item.time || '')
          values.push(item.count || 0)
        })
      } else {
        for (let i = 23; i >= 0; i--) {
          const hour = new Date()
          hour.setHours(hour.getHours() - i)
          hours.push(hour.getHours() + ':00')
          values.push(0)
        }
      }

      trendChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(10, 10, 15, 0.95)',
          borderColor: 'rgba(255, 200, 0, 0.3)',
          borderWidth: 1,
          textStyle: {
            color: '#E8E8ED',
            fontFamily: 'var(--font-sans)',
            fontSize: 12
          }
        },
        grid: {
          left: '10%',
          right: '4%',
          bottom: '14%',
          top: '8%'
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: hours,
          axisLabel: {
            color: '#5A5A68',
            fontFamily: 'var(--font-mono)',
            fontSize: 9
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(255, 255, 255, 0.06)'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#5A5A68',
            fontFamily: 'var(--font-mono)',
            fontSize: 9
          },
          axisLine: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(255, 255, 255, 0.04)'
            }
          }
        },
        series: [{
          name: 'Detections',
          type: 'line',
          smooth: true,
          symbol: 'none',
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255, 200, 0, 0.25)' },
              { offset: 1, color: 'rgba(255, 200, 0, 0)' }
            ])
          },
          lineStyle: {
            color: '#FFC800',
            width: 2.5,
            shadowColor: 'rgba(255, 200, 0, 0.4)',
            shadowBlur: 12
          },
          itemStyle: {
            color: '#FFC800'
          },
          data: values
        }]
      })
    }

    const updateRiskChart = () => {
      if (!riskChartInstance && riskChart.value) {
        riskChartInstance = echarts.init(riskChart.value)
      }
      if (!riskChartInstance) return

      const labels = getRiskLabels()

      riskChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(10, 10, 15, 0.95)',
          borderColor: 'rgba(255, 200, 0, 0.3)',
          borderWidth: 1,
          textStyle: {
            color: '#E8E8ED',
            fontFamily: 'var(--font-sans)',
            fontSize: 12
          }
        },
        grid: {
          left: '10%',
          right: '4%',
          bottom: '14%',
          top: '8%'
        },
        xAxis: {
          type: 'category',
          data: [labels.low, labels.medium, labels.high],
          axisLabel: {
            color: '#9898A8',
            fontFamily: 'var(--font-sans)',
            fontSize: 11
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(255, 255, 255, 0.06)'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#5A5A68',
            fontFamily: 'var(--font-mono)',
            fontSize: 9
          },
          axisLine: {
            show: false
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(255, 255, 255, 0.04)'
            }
          }
        },
        series: [{
          name: t('dashboard.player'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 2.5,
            shadowColor: 'rgba(255, 200, 0, 0.3)',
            shadowBlur: 8
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255, 200, 0, 0.2)' },
              { offset: 1, color: 'rgba(255, 200, 0, 0)' }
            ])
          },
          itemStyle: {
            color: (params) => {
              const colors = ['#2ECC71', '#FF8C00', '#E74C3C']
              return colors[params.dataIndex]
            },
            borderColor: '#fff',
            borderWidth: 2
          },
          data: [stats.value.lowRiskPlayers, stats.value.mediumRiskPlayers, stats.value.highRiskPlayers]
        }]
      })
    }

    const handleCheatDetected = (data) => {
      alerts.value.unshift({
        playerName: data.playerName,
        cheatType: data.cheatType,
        severity: data.severity,
        detectionTime: data.detectionTime,
        details: data.details
      })

      if (alerts.value.length > 30) {
        alerts.value.pop()
      }

      fetchOverviewStats()
      nextTick(() => fetchCheatTypeStats())

      addRadarDot(data.severity)

      nextTick(() => {
        if (consoleBody.value) {
          consoleBody.value.scrollTop = 0
        }
      })
    }

    const initBackgroundCanvas = () => {
      const canvas = bgCanvas.value
      if (!canvas) return

      const ctx = canvas.getContext('2d')

      const resize = () => {
        canvas.width = canvas.offsetWidth
        canvas.height = canvas.offsetHeight
      }

      resize()
      window.addEventListener('resize', resize)

      const particles = []
      for (let i = 0; i < 40; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          vx: (Math.random() - 0.5) * 0.3,
          vy: (Math.random() - 0.5) * 0.3,
          size: Math.random() * 1.5 + 0.5,
          opacity: Math.random() * 0.2 + 0.05
        })
      }

      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)

        particles.forEach(particle => {
          particle.x += particle.vx
          particle.y += particle.vy

          if (particle.x < 0) particle.x = canvas.width
          if (particle.x > canvas.width) particle.x = 0
          if (particle.y < 0) particle.y = canvas.height
          if (particle.y > canvas.height) particle.y = 0

          ctx.beginPath()
          ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2)
          ctx.fillStyle = `rgba(255, 200, 0, ${particle.opacity})`
          ctx.fill()
        })

        bgAnimFrame = requestAnimationFrame(animate)
      }

      animate()
    }

    const handleResize = () => {
      cheatTypeChartInstance?.resize()
      trendChartInstance?.resize()
      riskChartInstance?.resize()
    }

    const analyzeDashboard = async () => {
      aiDashboardLoading.value = true
      aiDashboardResult.value = null

      try {
        const result = await aiApi.analyzeDashboard()

        if (result.success) {
          aiDashboardResult.value = result
        } else {
          ElMessage.warning(result.error || t('ai.error'))
        }
      } catch (error) {
        ElMessage.error(t('ai.networkError'))
      } finally {
        aiDashboardLoading.value = false
      }
    }

    const getRiskLevelClass = (level) => {
      if (!level) return ''

      const upperLevel = level.toUpperCase()

      if (upperLevel.includes('HIGH')) return 'high'
      if (upperLevel.includes('MEDIUM')) return 'medium'
      if (upperLevel.includes('LOW')) return 'low'

      return 'medium'
    }

    onMounted(() => {
      fetchOverviewStats()
      fetchCheatTypeStats()
      fetchTrendData()
      nextTick(() => updateRiskChart())

      window.addEventListener('resize', handleResize)

      unsubscribeStatsChanged = EventBus.on(Events.STATS_CHANGED, () => {
        fetchOverviewStats()
        nextTick(() => fetchCheatTypeStats())
      })

      unsubscribeCheatDetected = EventBus.on(Events.CHEAT_DETECTED, handleCheatDetected)

      unsubscribeWsStatus = EventBus.on(Events.WS_STATUS, (connected) => {
        wsConnected.value = connected
      })

      initBackgroundCanvas()
    })

    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)

      if (bgAnimFrame) {
        cancelAnimationFrame(bgAnimFrame)
      }

      if (controller) {
        controller.abort()
      }

      unsubscribeStatsChanged?.()
      unsubscribeCheatDetected?.()
      unsubscribeWsStatus?.()

      cheatTypeChartInstance?.dispose()
      trendChartInstance?.dispose()
      riskChartInstance?.dispose()
    })

    return {
      stats,
      animatedStats,
      alerts,
      cheatTypeChart,
      trendChart,
      riskChart,
      consoleBody,
      bgCanvas,
      wsConnected,
      refreshing,
      statCards,
      aiDashboardLoading,
      aiDashboardResult,
      radarDots,
      threatLevelClass,
      threatLevelText,
      getBarWidth,
      refreshData,
      analyzeDashboard,
      renderAiText,
      getRiskLevelClass,
      formatConsoleTime,
      getAlertClass,
      getAlertTagClass,
      getSeverityClass,
      t
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
}

.dashboard-bg-canvas {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  opacity: 0.4;
}

.dashboard-grid {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== STAT CARDS ========== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.stat-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 20px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--accent-gold), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  border-color: rgba(255, 200, 0, 0.3);
  transform: translateY(-4px);
  box-shadow: var(--shadow-gold-strong);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 200, 0, 0.08) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.stat-card:hover .stat-card-glow {
  opacity: 1;
}

.stat-card.players { --card-accent: #4A9EFF; }
.stat-card.cheats { --card-accent: #E74C3C; }
.stat-card.risk { --card-accent: #FF8C00; }
.stat-card.bans { --card-accent: #FFC800; }
.stat-card.reports { --card-accent: #4A9EFF; }

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 200, 0, 0.08);
  border: 1px solid rgba(255, 200, 0, 0.15);
  border-radius: var(--radius-sm);
  color: var(--accent-gold);
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: 1px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
  letter-spacing: 0.3px;
}

.stat-card-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(255, 255, 255, 0.03);
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent-gold), var(--accent-gold-light));
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 10px rgba(255, 200, 0, 0.4);
}

/* ========== MAIN CONTENT LAYOUT ========== */
.main-content {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== PANELS ========== */
.panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all 0.3s ease;
}

.panel:hover {
  border-color: rgba(255, 200, 0, 0.2);
  box-shadow: var(--shadow-gold);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.panel-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 200, 0, 0.1);
  border-radius: var(--radius-sm);
  color: var(--accent-gold);
}

.panel-title {
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  flex: 1;
}

/* ========== THREAT RADAR ========== */
.threat-radar-panel {
  min-height: 320px;
}

.threat-badge {
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.threat-badge.low {
  background: rgba(46, 204, 113, 0.15);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.3);
}

.threat-badge.medium {
  background: rgba(255, 140, 0, 0.15);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.3);
}

.threat-badge.high {
  background: rgba(231, 76, 60, 0.15);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.3);
  animation: pulse-danger 1.5s ease-in-out infinite;
}

.threat-badge.critical {
  background: rgba(255, 200, 0, 0.15);
  color: #FFC800;
  border: 1px solid rgba(255, 200, 0, 0.4);
  animation: pulse-critical 0.8s ease-in-out infinite;
}

@keyframes pulse-danger {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes pulse-critical {
  0%, 92%, 100% { opacity: 1; transform: scale(1); }
  93% { opacity: 0.7; transform: scale(1.05); }
  96% { opacity: 1; transform: scale(1); }
}

.radar-container {
  position: relative;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.radar-svg {
  width: 240px;
  height: 240px;
}

.radar-sweep-line {
  filter: drop-shadow(0 0 8px rgba(255, 200, 0, 0.6));
}

.radar-dot {
  filter: drop-shadow(0 0 4px currentColor);
}

.radar-center {
  position: absolute;
  z-index: 2;
  text-align: center;
  pointer-events: none;
}

.radar-value {
  font-family: var(--font-mono);
  font-size: 32px;
  font-weight: 800;
  color: var(--accent-gold);
  text-shadow: 0 0 20px rgba(255, 200, 0, 0.5);
  letter-spacing: 2px;
}

.radar-label {
  font-family: var(--font-sans);
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 4px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* ========== CONSOLE ========== */
.console-panel {
  flex: 1;
  min-height: 280px;
  display: flex;
  flex-direction: column;
}

.console-dots {
  display: flex;
  gap: 6px;
}

.console-dots .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.console-dots .dot.red { background: #E74C3C; }
.console-dots .dot.yellow { background: #FF8C00; }
.console-dots .dot.green { background: #2ECC71; }

.console-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: var(--radius-sm);
}

.connection-status.connected {
  color: #2ECC71;
  background: rgba(46, 204, 113, 0.1);
  border: 1px solid rgba(46, 204, 113, 0.25);
}

.connection-status.disconnected {
  color: #E74C3C;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: status-pulse 2s ease-in-out infinite;
}

@keyframes status-pulse {
  0%, 100% { box-shadow: 0 0 4px currentColor; }
  50% { box-shadow: 0 0 10px currentColor; }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  background: rgba(255, 200, 0, 0.05);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover:not(:disabled) {
  background: rgba(255, 200, 0, 0.1);
  border-color: rgba(255, 200, 0, 0.3);
  color: var(--accent-gold);
}

.action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.console-body {
  flex: 1;
  max-height: 220px;
  overflow-y: auto;
  padding: 8px 0;
  font-family: var(--font-sans);
  font-size: 12px;
}

.console-body::-webkit-scrollbar {
  width: 4px;
}

.console-body::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.console-empty {
  padding: 32px 18px;
  color: var(--text-muted);
  text-align: center;
  font-size: 13px;
}

.cursor-blink {
  color: var(--accent-gold);
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.alert-list {
  display: flex;
  flex-direction: column;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
  animation: slideInRight 0.3s ease-out;
}

.alert-item:hover {
  background: rgba(255, 200, 0, 0.03);
}

.alert-item.critical {
  border-left-color: #FFC800;
  background: rgba(255, 200, 0, 0.02);
}

.alert-item.high {
  border-left-color: #E74C3C;
}

.alert-item.medium {
  border-left-color: #FF8C00;
}

.alert-item.low {
  border-left-color: #2ECC71;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.alert-time {
  color: var(--text-muted);
  font-size: 10px;
  font-family: var(--font-mono);
  flex-shrink: 0;
}

.alert-tag {
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 10px;
  font-weight: 600;
  flex-shrink: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.alert-tag.fly {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
}

.alert-tag.speed {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
}

.alert-tag.auto {
  background: rgba(74, 158, 255, 0.12);
  color: #4A9EFF;
}

.alert-tag.kill {
  background: rgba(255, 200, 0, 0.12);
  color: #FFC800;
}

.alert-tag.aim {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
}

.alert-tag.default {
  background: rgba(255, 200, 0, 0.12);
  color: #FFC800;
}

.alert-player {
  color: var(--text-primary);
  font-weight: 600;
  min-width: 70px;
  flex-shrink: 0;
}

.alert-detail {
  color: var(--text-secondary);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alert-severity {
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 10px;
  font-weight: 600;
  flex-shrink: 0;
  font-family: var(--font-mono);
}

.alert-severity.critical {
  background: rgba(255, 200, 0, 0.15);
  color: #FFC800;
}

.alert-severity.high {
  background: rgba(231, 76, 60, 0.15);
  color: #E74C3C;
}

.alert-severity.medium {
  background: rgba(255, 140, 0, 0.15);
  color: #FF8C00;
}

.alert-severity.low {
  background: rgba(46, 204, 113, 0.15);
  color: #2ECC71;
}

/* ========== CHART PANELS ========== */
.chart-panel {
  flex: 1;
  min-height: 240px;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  min-height: 180px;
  padding: 8px;
}

/* ========== AI ANALYSIS PANEL ========== */
.ai-analysis-panel {
  background: var(--bg-card);
  border: 1px solid rgba(255, 200, 0, 0.3);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-gold-strong);
  animation: slideUpFade 0.4s ease-out;
}

@keyframes slideUpFade {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  color: var(--accent-gold);
}

.close-btn {
  margin-left: auto;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.25);
  border-radius: var(--radius-sm);
  color: #E74C3C;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #E74C3C;
  color: #fff;
}

.ai-panel-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-row {
  display: flex;
  gap: 18px;
}

.ai-row .ai-section.half {
  flex: 1;
}

.ai-section-title {
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  color: var(--accent-gold);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.ai-section-text {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.ai-section-text :deep(strong) {
  color: var(--accent-gold);
}

.ai-section-text :deep(code) {
  background: rgba(255, 200, 0, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-family: var(--font-mono);
}

.risk-indicator {
  display: inline-block;
  padding: 6px 18px;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 700;
}

.risk-indicator.high {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
}

.risk-indicator.medium {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
}

.risk-indicator.low {
  background: rgba(46, 204, 113, 0.12);
  color: #2ECC71;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(16px);
}

.alert-list-enter-active {
  transition: all 0.3s ease-out;
}

.alert-list-leave-active {
  transition: all 0.2s ease-in;
}

.alert-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.alert-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* ========== RESPONSIVE ========== */
@media (max-width: 1399px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1199px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .left-panel {
    flex-direction: row;
  }

  .threat-radar-panel {
    flex: 1;
  }

  .console-panel {
    flex: 1.5;
  }
}

@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .left-panel {
    flex-direction: column;
  }

  .alert-item {
    flex-wrap: wrap;
  }

  .alert-detail {
    width: 100%;
    order: 10;
  }

  .radar-container {
    height: 220px;
  }

  .radar-svg {
    width: 200px;
    height: 200px;
  }
}
</style>
