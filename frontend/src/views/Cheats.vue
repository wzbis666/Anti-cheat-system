﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="cheats-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ t('nav.cheats') }}</h2>
        <span class="entity-count">{{ totalElements }} {{ t('cheats.records') }}</span>
      </div>

      <div class="header-actions">
        <button class="action-btn export-btn" @click="exportCSV">
          <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
          {{ t('common.csv') }}
        </button>
      </div>
    </div>

    <div class="toolbar">
      <div class="filter-group">
        <div class="time-chips">
          <button :class="['filter-chip', { active: timeRange === '1h' }]" @click="setTimeRange('1h')">1h</button>
          <button :class="['filter-chip', { active: timeRange === '24h' }]" @click="setTimeRange('24h')">24h</button>
          <button :class="['filter-chip', { active: timeRange === '7d' }]" @click="setTimeRange('7d')">7d</button>
          <button :class="['filter-chip', { active: timeRange === 'all' }]" @click="setTimeRange('all')">{{ t('common.all') }}</button>
        </div>
        <select v-model="cheatTypeFilter" class="filter-select" @change="currentPage = 0">
          <option value="">{{ t('cheats.allTypes') }}</option>
          <option v-for="ct in cheatTypes" :key="ct" :value="ct">{{ ct }}</option>
        </select>
        <select v-model="severityFilter" class="filter-select" @change="currentPage = 0">
          <option value="">{{ t('cheats.allSeverity') }}</option>
          <option value="low">{{ t('cheats.severityLow') }}</option>
          <option value="medium">{{ t('cheats.severityMedium') }}</option>
          <option value="high">{{ t('cheats.severityHigh') }}</option>
          <option value="critical">{{ t('cheats.severityCritical') }}</option>
        </select>
      </div>
      <div class="search-box">
        <svg viewBox="0 0 24 24" width="14" height="14" class="search-icon"><path fill="currentColor" d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.77l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
        <input v-model="searchQuery" type="text" :placeholder="t('cheats.searchPlaceholder')" @keyup.enter="currentPage = 0" />
      </div>
    </div>

    <div class="chart-panel">
      <div class="panel-header">
        <div class="panel-icon">
          <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/></svg>
        </div>
        <span class="panel-title">{{ t('cheats.distributionChart') }}</span>
      </div>
      <div ref="distributionChart" class="chart-container"></div>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>{{ t('common.loading') }}...</span>
      </div>

      <div v-else-if="cheats.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="48" height="48" class="empty-icon">
          <path fill="currentColor" d="M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z"/>
        </svg>
        <p>{{ t('cheats.noRecords') }}</p>
      </div>

      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th @click="sortBy('playerName')">{{ t('cheats.player') }} <span class="sort-icon">↕</span></th>
              <th @click="sortBy('cheatType')">{{ t('cheats.cheatType') }} <span class="sort-icon">↕</span></th>
              <th @click="sortBy('severity')">{{ t('cheats.severity') }} <span class="sort-icon">↕</span></th>
              <th @click="sortBy('detectionTime')">{{ t('common.time') }} <span class="sort-icon">↕</span></th>
              <th>{{ t('common.details') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cheat in paginatedCheats" :key="cheat.id" class="data-row">
              <td>
                <div class="player-cell">
                  <img :src="`https://mc-heads.net/avatar/${cheat.player?.playerName || 'Steve'}/32`" class="player-avatar" />
                  <span class="player-name">{{ cheat.player?.playerName || '-' }}</span>
                </div>
              </td>
              <td>
                <span :class="['type-badge', getCheatClass(cheat.cheatType)]">{{ cheat.cheatType }}</span>
              </td>
              <td>
                <span :class="['severity-badge', getSeverityClass(cheat.severity)]">{{ getSeverityText(cheat.severity) }}</span>
              </td>
              <td class="time-cell">{{ formatTime(cheat.detectionTime) }}</td>
              <td class="details-cell">{{ cheat.details }}</td>
              <td>
                <div class="action-group">
                  <button class="action-btn view-btn" @click="showCheatDetail(cheat)">
                    {{ t('common.view') }}
                  </button>
                  <button class="action-btn delete-btn" @click="handleDelete(cheat.id)">
                    {{ t('common.delete') }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination" v-if="totalPages > 0">
        <span class="pagination-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <div class="pagination-controls">
          <button class="pagination-btn" :disabled="currentPage === 0" @click="currentPage--">
            <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12l4.58-4.59z"/></svg>
          </button>
          <button class="pagination-btn" :disabled="currentPage >= totalPages - 1" @click="currentPage++">
            <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6-6-6z"/></svg>
          </button>
        </div>
      </div>
    </div>

    <transition name="modal-fade">
      <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
        <div class="modal-dialog">
          <div class="modal-header">
            <span class="modal-title">{{ t('cheats.detailTitle') }}</span>
            <button class="modal-close" @click="detailVisible = false">×</button>
          </div>
          <div class="modal-body" v-if="selectedCheat">
            <div class="detail-player">
              <img :src="`https://mc-heads.net/avatar/${selectedCheat.player?.playerName || 'Steve'}/64`" class="detail-avatar" />
              <div class="detail-player-info">
                <div class="detail-player-name">{{ selectedCheat.player?.playerName }}</div>
                <div class="detail-player-uuid">{{ selectedCheat.player?.uuid }}</div>
              </div>
            </div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">{{ t('cheats.cheatType') }}</span>
                <span :class="['detail-value', getCheatClass(selectedCheat.cheatType)]">{{ selectedCheat.cheatType }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">{{ t('cheats.severity') }}</span>
                <span :class="['detail-value', getSeverityClass(selectedCheat.severity)]">{{ getSeverityText(selectedCheat.severity) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">{{ t('cheats.detectedAt') }}</span>
                <span class="detail-value">{{ formatTime(selectedCheat.detectionTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">{{ t('common.details') }}</span>
                <span class="detail-value">{{ selectedCheat.details }}</span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn secondary" @click="detailVisible = false">{{ t('common.close') }}</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { cheatApi } from '../api'
import { ElMessage } from 'element-plus'
import { EventBus, Events } from '../utils/eventBus'

export default {
  name: 'Cheats',
  setup() {
    const { t } = useI18n()

    const cheats = ref([])
    const loading = ref(false)
    const currentPage = ref(0)
    const totalPages = ref(0)
    const totalElements = ref(0)
    const searchQuery = ref('')
    const cheatTypeFilter = ref('')
    const severityFilter = ref('')
    const timeRange = ref('24h')
    const sortByField = ref('detectionTime')
    const sortOrder = ref('desc')
    const detailVisible = ref(false)
    const selectedCheat = ref(null)
    const distributionChart = ref(null)
    const cheatTypes = ['飞行作弊', '速度作弊', '自动点击作弊', '杀戮光环']

    let chartInstance = null
    let unsubscribeCheatData = null
    let controller = null

    const filteredCheats = computed(() => {
      let result = [...cheats.value]
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(c => 
          (c.player?.playerName?.toLowerCase().includes(query) || '') ||
          (c.cheatType?.toLowerCase().includes(query) || '')
        )
      }
      if (cheatTypeFilter.value) {
        result = result.filter(c => c.cheatType === cheatTypeFilter.value)
      }
      if (severityFilter.value) {
        result = result.filter(c => getSeverityClass(c.severity) === severityFilter.value)
      }
      result.sort((a, b) => {
        const aVal = a[sortByField.value]
        const bVal = b[sortByField.value]
        if (sortOrder.value === 'asc') {
          return aVal > bVal ? 1 : -1
        }
        return aVal < bVal ? 1 : -1
      })
      return result
    })

    const paginatedCheats = computed(() => {
      const pageSize = 20
      const start = currentPage.value * pageSize
      return filteredCheats.value.slice(start, start + pageSize)
    })

    const sortBy = (field) => {
      if (sortByField.value === field) {
        sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortByField.value = field
        sortOrder.value = 'desc'
      }
    }

    const formatTime = (timestamp) => {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const getCheatClass = (type) => {
      if (!type) return 'default'
      if (type.includes('飞行')) return 'flying'
      if (type.includes('速度')) return 'speed'
      if (type.includes('auto') || type.includes('click') || type.includes('自动')) return 'auto'
      if (type.includes('kill') || type.includes('aura') || type.includes('杀戮')) return 'kill'
      return 'default'
    }

    const getSeverityClass = (severity) => {
      if (severity >= 4) return 'critical'
      if (severity >= 3) return 'high'
      if (severity >= 2) return 'medium'
      return 'low'
    }

    const getSeverityText = (severity) => {
      if (severity >= 4) return t('cheats.severityCritical')
      if (severity >= 3) return t('cheats.severityHigh')
      if (severity >= 2) return t('cheats.severityMedium')
      return t('cheats.severityLow')
    }

    const fetchCheats = async () => {
      loading.value = true
      try {
        controller = new AbortController()
        const data = await cheatApi.getByPage(currentPage.value, 20, 'detectionTime', 'desc', { signal: controller.signal })
        cheats.value = data.content || []
        totalPages.value = data.totalPages || 0
        totalElements.value = data.totalElements || 0
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error(error)
        }
      } finally {
        loading.value = false
        nextTick(() => updateChart())
      }
    }

    const updateChart = () => {
      if (!chartInstance && distributionChart.value) {
        chartInstance = echarts.init(distributionChart.value)
      }
      if (!chartInstance) return

      const typeCount = {}
      cheats.value.forEach(c => {
        typeCount[c.cheatType] = (typeCount[c.cheatType] || 0) + 1
      })

      const chartData = Object.entries(typeCount).map(([name, value]) => ({ name, value }))

      const option = {
        color: ['#E74C3C', '#FF8C00', '#4A9EFF', '#9B59B6'],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          backgroundColor: 'rgba(18, 18, 24, 0.95)',
          borderColor: 'rgba(255, 200, 0, 0.2)',
          textStyle: { color: '#E8E8ED' }
        },
        series: [{
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#1a1a22',
            borderWidth: 2
          },
          label: {
            show: true,
            color: '#9898A8',
            fontSize: 12
          },
          labelLine: {
            lineStyle: { color: '#5A5A68' }
          },
          data: chartData
        }]
      }

      chartInstance.setOption(option)
    }

    const handleResize = () => {
      chartInstance?.resize()
    }

    const setTimeRange = (range) => {
      timeRange.value = range
      currentPage.value = 0
      fetchCheats()
    }

    const showCheatDetail = (cheat) => {
      selectedCheat.value = cheat
      detailVisible.value = true
    }

    const handleDelete = async (id) => {
      try {
        await cheatApi.delete(id)
        ElMessage.success(t('common.success'))
        fetchCheats()
      } catch (error) {
        ElMessage.error(t('common.error'))
      }
    }

    const exportCSV = async () => {
      try {
        const data = await cheatApi.exportAll()
        const blob = new Blob([data], { type: 'text/csv' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `cheats_${new Date().toISOString().split('T')[0]}.csv`
        a.click()
        URL.revokeObjectURL(url)
      } catch (error) {
        ElMessage.error(t('common.error'))
      }
    }

    onMounted(() => {
      fetchCheats()
      window.addEventListener('resize', handleResize)
      
      unsubscribeCheatData = EventBus.on(Events.WS_CHEAT_DATA, (data) => {
        if (data.player && data.cheatType) {
          cheats.value.unshift({
            id: Date.now(),
            player: { playerName: data.playerName, uuid: data.playerUuid },
            cheatType: data.cheatType,
            severity: data.severity || 3,
            detectionTime: new Date().getTime(),
            details: data.details || '检测到作弊行为'
          })
          totalElements.value++
          nextTick(() => updateChart())
        }
      })
    })

    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
      chartInstance?.dispose()
      unsubscribeCheatData?.()
      
      if (controller) {
        controller.abort()
      }
    })

    watch([searchQuery, cheatTypeFilter, severityFilter], () => {
      currentPage.value = 0
    })

    return {
      cheats,
      loading,
      currentPage,
      totalPages,
      totalElements,
      searchQuery,
      cheatTypeFilter,
      severityFilter,
      timeRange,
      detailVisible,
      selectedCheat,
      distributionChart,
      cheatTypes,
      filteredCheats,
      paginatedCheats,
      setTimeRange,
      sortBy,
      formatTime,
      getCheatClass,
      getSeverityClass,
      getSeverityText,
      showCheatDetail,
      handleDelete,
      exportCSV,
      t
    }
  }
}
</script>

<style scoped>
.cheats-container { padding: 20px; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.page-title { font-family: var(--font-sans); font-size: 20px; font-weight: 700; color: var(--accent-gold); margin: 0; }
.entity-count { font-size: 13px; color: var(--text-muted); }

.header-actions { display: flex; gap: 10px; }
.action-btn { display: flex; align-items: center; gap: 6px; padding: 10px 16px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-secondary); cursor: pointer; font-size: 13px; transition: all 0.2s ease; }
.action-btn:hover { border-color: var(--accent-gold); color: var(--accent-gold); }

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding: 16px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); }
.filter-group { display: flex; align-items: center; gap: 12px; }
.time-chips { display: flex; gap: 6px; }
.filter-chip { padding: 6px 14px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: 20px; color: var(--text-secondary); font-size: 12px; cursor: pointer; transition: all 0.2s ease; }
.filter-chip:hover { border-color: var(--accent-gold); }
.filter-chip.active { background: var(--accent-gold-dim); border-color: var(--accent-gold); color: var(--accent-gold); }
.filter-select { padding: 8px 12px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-primary); font-size: 12px; cursor: pointer; }

.search-box { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); }
.search-icon { color: var(--text-muted); }
.search-box input { background: transparent; border: none; outline: none; color: var(--text-primary); font-size: 12px; width: 200px; }
.search-box input::placeholder { color: var(--text-muted); }

.chart-panel { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; margin-bottom: 20px; }
.panel-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.panel-icon { color: var(--accent-gold); }
.panel-title { font-family: var(--font-sans); font-size: 14px; font-weight: 600; color: var(--text-primary); }
.chart-container { height: 220px; }

.table-container { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); overflow: hidden; }
.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px; gap: 12px; }
.spinner { width: 32px; height: 32px; border: 2px solid var(--border-color); border-top-color: var(--accent-gold); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px; gap: 12px; }
.empty-icon { color: var(--text-muted); }
.empty-state p { color: var(--text-muted); font-size: 14px; }

.table-wrapper { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 12px 16px; color: var(--text-secondary); font-weight: 600; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid var(--border-color); cursor: pointer; }
.sort-icon { font-size: 10px; margin-left: 4px; opacity: 0.5; }
.data-table td { padding: 12px 16px; border-bottom: 1px solid var(--border-color); }
.data-row:hover td { background: var(--bg-hover); }

.player-cell { display: flex; align-items: center; gap: 10px; }
.player-avatar { width: 32px; height: 32px; border-radius: 50%; }
.player-name { font-size: 13px; color: var(--text-primary); }

.type-badge { padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.type-badge.flying { background: rgba(231, 76, 60, 0.15); color: #E74C3C; }
.type-badge.speed { background: rgba(255, 140, 0, 0.15); color: #FF8C00; }
.type-badge.auto { background: rgba(74, 158, 255, 0.15); color: #4A9EFF; }
.type-badge.kill { background: rgba(155, 89, 182, 0.15); color: #9B59B6; }
.type-badge.default { background: var(--bg-secondary); color: var(--text-secondary); }

.severity-badge { padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.severity-badge.critical { background: rgba(231, 76, 60, 0.15); color: #E74C3C; }
.severity-badge.high { background: rgba(255, 140, 0, 0.15); color: #FF8C00; }
.severity-badge.medium { background: rgba(241, 196, 15, 0.15); color: #F1C40F; }
.severity-badge.low { background: rgba(46, 204, 113, 0.15); color: #2ECC71; }

.time-cell { color: var(--text-muted); font-size: 12px; }
.details-cell { color: var(--text-secondary); font-size: 12px; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.action-group { display: flex; gap: 6px; }
.action-btn { padding: 6px 12px; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 12px; cursor: pointer; transition: all 0.2s ease; }
.action-btn.view-btn { background: var(--bg-secondary); color: var(--text-secondary); }
.action-btn.view-btn:hover { border-color: var(--accent-blue); color: var(--accent-blue); }
.action-btn.delete-btn { background: rgba(231, 76, 60, 0.1); border-color: rgba(231, 76, 60, 0.3); color: #E74C3C; }
.action-btn.delete-btn:hover { background: #E74C3C; color: #fff; border-color: #E74C3C; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; padding: 16px; border-top: 1px solid var(--border-color); }
.pagination-info { font-size: 13px; color: var(--text-muted); }
.pagination-controls { display: flex; gap: 4px; }
.pagination-btn { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-secondary); cursor: pointer; transition: all 0.2s ease; }
.pagination-btn:hover:not(:disabled) { border-color: var(--accent-gold); color: var(--accent-gold); }
.pagination-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; }
.modal-dialog { background: var(--bg-card-solid); border: 1px solid var(--border-light); border-radius: var(--radius-lg); width: 100%; max-width: 480px; max-height: 85vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--border-color); background: var(--bg-tertiary); border-radius: var(--radius-lg) var(--radius-lg) 0 0; }
.modal-title { font-family: var(--font-sans); font-size: 15px; font-weight: 700; color: var(--accent-gold); }
.modal-close { width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; background: rgba(231, 76, 60, 0.1); border: 1px solid rgba(231, 76, 60, 0.2); border-radius: var(--radius-sm); color: #E74C3C; cursor: pointer; font-size: 20px; transition: all 0.2s ease; }
.modal-close:hover { background: #E74C3C; color: #fff; }
.modal-body { padding: 20px; }

.detail-player { display: flex; align-items: center; gap: 16px; padding-bottom: 20px; border-bottom: 1px solid var(--border-color); margin-bottom: 20px; }
.detail-avatar { width: 64px; height: 64px; border-radius: 50%; }
.detail-player-info { display: flex; flex-direction: column; gap: 4px; }
.detail-player-name { font-family: var(--font-sans); font-size: 18px; font-weight: 700; color: var(--text-primary); }
.detail-player-uuid { font-size: 12px; color: var(--text-muted); font-family: var(--font-mono); }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-label { font-size: 11px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
.detail-value { font-size: 13px; color: var(--text-primary); }

.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 14px 20px; border-top: 1px solid var(--border-color); background: var(--bg-tertiary); border-radius: 0 0 var(--radius-lg) var(--radius-lg); }
.btn { padding: 10px 20px; border-radius: var(--radius-sm); font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; }
.btn.secondary { background: var(--bg-secondary); border: 1px solid var(--border-color); color: var(--text-secondary); }
.btn.secondary:hover { border-color: var(--accent-gold); color: var(--accent-gold); }
</style>