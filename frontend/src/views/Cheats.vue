﻿﻿﻿<template>
  <div class="cheats-terminal">
    <div class="ct-header">
      <div class="ct-prompt">
        <span class="ct-prompt-sign">root@acs:~$</span>
        <span class="ct-prompt-cmd">./cheats.query --sort=severity --limit=20</span>
        <span class="ct-cursor">_</span>
      </div>
      <div class="ct-status">{{ totalElements }} detections indexed</div>
    </div>

    <div class="ct-toolbar">
      <div class="ct-toolbar-left">
        <div class="ct-time-chips">
          <button :class="['ct-tchip', { active: timeRange === '1h' }]" @click="setTimeRange('1h')">1h</button>
          <button :class="['ct-tchip', { active: timeRange === '24h' }]" @click="setTimeRange('24h')">24h</button>
          <button :class="['ct-tchip', { active: timeRange === '7d' }]" @click="setTimeRange('7d')">7d</button>
          <button :class="['ct-tchip', { active: timeRange === 'all' }]" @click="setTimeRange('all')">{{ t('common.all') }}</button>
        </div>
        <select v-model="cheatTypeFilter" class="ct-select">
          <option value="">{{ t('cheats.allTypes') }}</option>
          <option v-for="ct in cheatTypes" :key="ct" :value="ct">{{ ct }}</option>
        </select>
        <select v-model="severityFilter" class="ct-select">
          <option value="">{{ t('cheats.allSeverity') }}</option>
          <option value="low">{{ t('cheats.severityLow') }}</option>
          <option value="medium">{{ t('cheats.severityMedium') }}</option>
          <option value="high">{{ t('cheats.severityHigh') }}</option>
          <option value="critical">{{ t('cheats.severityCritical') }}</option>
        </select>
      </div>
      <div class="ct-toolbar-right">
        <div class="ct-search">
          <span class="ct-search-prefix">$ grep</span>
          <input v-model="searchQuery" type="text" :placeholder="t('cheats.searchPlaceholder')" />
        </div>
        <button class="ct-btn" @click="exportCSV">
          <span class="ct-btn-icon">⇩</span> CSV
        </button>
      </div>
    </div>

    <div class="ct-chart-panel">
      <div class="ct-chart-header">
        <span class="ct-chart-cmd">> pie.chart --type=distribution</span>
        <span class="ct-panel-decor">╺━╸</span>
      </div>
      <div ref="distributionChart" class="ct-chart-body"></div>
    </div>

    <div class="ct-table-panel">
      <div v-if="loading" class="ct-loading">
        <div class="ct-load-bar"><div class="ct-load-bar-fill"></div></div>
        <span class="ct-load-text">SCANNING <span class="ct-load-dots"><span>.</span><span>.</span><span>.</span></span></span>
      </div>

      <div v-else-if="cheats.length === 0" class="ct-empty">
        <div class="ct-empty-icon">⊘</div>
        <span>NO DETECTIONS</span>
      </div>

      <div v-else class="ct-table-wrap">
        <table class="ct-table">
          <thead>
            <tr>
              <th @click="sortBy('playerName')">$ player <span class="ct-sort">↕</span></th>
              <th @click="sortBy('cheatType')">$ cheat.type <span class="ct-sort">↕</span></th>
              <th @click="sortBy('severity')">$ severity <span class="ct-sort">↕</span></th>
              <th @click="sortBy('detectionTime')">$ timestamp <span class="ct-sort">↕</span></th>
              <th>$ details</th>
              <th>$ actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cheat in paginatedCheats" :key="cheat.id">
              <td>
                <div class="ct-player-cell">
                  <img :src="`https://mc-heads.net/avatar/${cheat.player?.playerName || 'Steve'}/32`" class="ct-avatar" />
                  <span>{{ cheat.player?.playerName || '-' }}</span>
                </div>
              </td>
              <td><span :class="['ct-tag', getCheatClass(cheat.cheatType)]">{{ cheat.cheatType }}</span></td>
              <td><span :class="['ct-sev-badge', getSeverityClass(cheat.severity)]">{{ getSeverityText(cheat.severity) }}</span></td>
              <td class="ct-time-cell">{{ formatTime(cheat.detectionTime) }}</td>
              <td class="ct-detail-cell" :title="cheat.details">{{ cheat.details }}</td>
              <td>
                <div class="ct-actions">
                  <button class="ct-act view" @click="showCheatDetail(cheat)">VIEW</button>
                  <button class="ct-act delete" @click="handleDelete(cheat.id)">DEL</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="ct-pagination" v-if="totalPages > 0">
        <span class="ct-pag-count">[{{ totalElements }}] records</span>
        <div class="ct-pag-controls">
          <button class="ct-pag-btn" :disabled="currentPage === 0" @click="currentPage--">◄</button>
          <span class="ct-pag-num">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button class="ct-pag-btn" :disabled="currentPage >= totalPages - 1" @click="currentPage++">►</button>
        </div>
      </div>
    </div>

    <transition name="ct-modal-fade">
      <div v-if="detailVisible" class="ct-overlay" @click.self="detailVisible = false">
        <div class="ct-modal">
          <div class="ct-modal-top">
            <span class="ct-modal-cmd">> cheat.detail --id={{ selectedCheat?.id }}</span>
            <button class="ct-modal-close" @click="detailVisible = false">┼</button>
          </div>
          <div class="ct-modal-body" v-if="selectedCheat">
            <div class="ct-modal-player">
              <img :src="`https://mc-heads.net/avatar/${selectedCheat.player?.playerName || 'Steve'}/64`" class="ct-modal-head" />
              <div>
                <div class="ct-modal-name">{{ selectedCheat.player?.playerName }}</div>
                <div class="ct-modal-uuid">&lt;{{ selectedCheat.player?.uuid }}&gt;</div>
              </div>
            </div>
            <div class="ct-modal-grid">
              <div class="ct-modal-cell"><span class="ct-modal-label">$ cheat.type</span><span :class="['ct-tag', getCheatClass(selectedCheat.cheatType)]">{{ selectedCheat.cheatType }}</span></div>
              <div class="ct-modal-cell"><span class="ct-modal-label">$ severity</span><span :class="['ct-sev-badge', getSeverityClass(selectedCheat.severity)]">{{ getSeverityText(selectedCheat.severity) }}</span></div>
              <div class="ct-modal-cell full"><span class="ct-modal-label">$ timestamp</span><span>{{ formatTime(selectedCheat.detectionTime) }}</span></div>
              <div class="ct-modal-cell full"><span class="ct-modal-label">$ details</span><span>{{ selectedCheat.details }}</span></div>
            </div>
          </div>
          <div class="ct-modal-footer">
            <button class="ct-modal-btn sec" @click="detailVisible = false">[ESC]</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { cheatApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    const timeRange = ref('all')
    const sortField = ref('detectionTime')
    const sortOrder = ref('desc')
    const detailVisible = ref(false)
    const selectedCheat = ref(null)
    const distributionChart = ref(null)
    let chartInstance = null
    const cheatTypes = [t('cheats.flying'), t('cheats.speed'), t('cheats.autoClick'), t('cheats.killAura'), 'XRay', 'Scaffold']

    const fetchCheats = async () => {
      loading.value = true
      try {
        const data = await cheatApi.getByPage(currentPage.value, 20, sortField.value, sortOrder.value)
        cheats.value = data.content || []
        totalPages.value = data.totalPages || 0
        totalElements.value = data.totalElements || 0
        updateChart()
      } catch (e) { ElMessage.error(t('common.error')) } finally { loading.value = false }
    }

    const filteredCheats = computed(() => {
      let list = cheats.value
      if (searchQuery.value) list = list.filter(c => (c.player?.playerName || '').toLowerCase().includes(searchQuery.value.toLowerCase()) || (c.cheatType || '').toLowerCase().includes(searchQuery.value.toLowerCase()))
      if (cheatTypeFilter.value) list = list.filter(c => c.cheatType === cheatTypeFilter.value)
      if (severityFilter.value) {
        const map = { low: [0, 1], medium: [2], high: [3], critical: [4, 5] }
        const range = map[severityFilter.value] || [0]
        list = list.filter(c => range.includes(c.severity))
      }
      if (timeRange.value && timeRange.value !== 'all') {
        const now = Date.now()
        const rangeMs = { '1h': 3600000, '6h': 21600000, '24h': 86400000, '7d': 604800000 }
        const ms = rangeMs[timeRange.value]
        if (ms) {
          const cutoff = now - ms
          list = list.filter(c => c.detectionTime && Number(c.detectionTime) >= cutoff)
        }
      }
      return list
    })
    const paginatedCheats = computed(() => filteredCheats.value)

    const setTimeRange = (r) => { timeRange.value = r; fetchCheats() }
    const sortBy = (field) => { if (sortField.value === field) sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'; else { sortField.value = field; sortOrder.value = 'desc' } fetchCheats() }
    const formatTime = (ts) => { if (!ts) return '-'; return new Date(ts).toLocaleString() }
    const getCheatClass = (ct) => { const m = { [t('cheats.flying')]: 'ct-tag-red', [t('cheats.speed')]: 'ct-tag-orange', [t('cheats.autoClick')]: 'ct-tag-cyan', [t('cheats.killAura')]: 'ct-tag-red', 'XRay': 'ct-tag-purple', 'Scaffold': 'ct-tag-orange' }; return m[ct] || 'ct-tag-default' }
    const getSeverityClass = (s) => { if (s >= 4) return 'critical'; if (s >= 3) return 'high'; if (s >= 2) return 'medium'; return 'low' }
    const getSeverityText = (s) => { if (s >= 4) return t('cheats.severityCritical'); if (s >= 3) return t('cheats.severityHigh'); if (s >= 2) return t('cheats.severityMedium'); return t('cheats.severityLow') }

    const showCheatDetail = (cheat) => { selectedCheat.value = cheat; detailVisible.value = true }
    const handleDelete = async (id) => {
      try { await ElMessageBox.confirm(t('cheats.confirmDelete'), t('common.confirm'), { type: 'warning' }); await cheatApi.delete(id); ElMessage.success(t('common.success')); fetchCheats() } catch (e) {}
    }

    const exportCSV = () => {
      const headers = ['playerName', 'cheatType', 'severity', 'detectionTime', 'details']
      const rows = filteredCheats.value.map(c => [c.player?.playerName || '', c.cheatType, c.severity, c.detectionTime, c.details].map(v => `"${v || ''}"`))
      const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
      const blob = new Blob([csv], { type: 'text/csv' }); const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'cheats.csv'; a.click()
    }

    const updateChart = () => {
      if (!distributionChart.value) return
      if (!chartInstance) chartInstance = echarts.init(distributionChart.value)
      const counts = {}
      cheats.value.forEach(c => { counts[c.cheatType] = (counts[c.cheatType] || 0) + 1 })
      const data = Object.entries(counts).map(([name, value]) => ({ name, value }))
      chartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item', backgroundColor: '#0a0416', borderColor: 'rgba(147,51,234,0.3)', borderWidth: 2, textStyle: { color: '#c084fc', fontFamily: '"JetBrains Mono", monospace', fontSize: 11 } },
        series: [{ type: 'pie', radius: ['38%', '68%'], center: ['50%', '50%'], itemStyle: { borderColor: '#080312', borderWidth: 2, borderRadius: 0 }, label: { color: 'rgba(180,160,220,0.7)', fontFamily: '"JetBrains Mono", monospace', fontSize: 10 }, data, color: ['#ff3d5a', '#f59e0b', '#06b6d4', '#10b981', '#a855f7', '#c084fc'] }]
      })
    }

    const handleResize = () => chartInstance?.resize()

    watch(currentPage, () => fetchCheats())

    onMounted(() => { fetchCheats(); window.addEventListener('resize', handleResize) })
    onUnmounted(() => { window.removeEventListener('resize', handleResize); chartInstance?.dispose() })

    return { cheats, loading, currentPage, totalPages, totalElements, searchQuery, cheatTypeFilter, severityFilter, timeRange, filteredCheats, paginatedCheats, detailVisible, selectedCheat, distributionChart, cheatTypes, setTimeRange, sortBy, formatTime, getCheatClass, getSeverityClass, getSeverityText, showCheatDetail, handleDelete, exportCSV, t }
  }
}
</script>

<style scoped>
.cheats-terminal {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ct-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ct-prompt {
  font-family: var(--font-mono);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ct-prompt-sign { color: #06b6d4; font-weight: 700; }
.ct-prompt-cmd { color: #a855f7; }

.ct-cursor {
  color: #c084fc;
  animation: ctBlink 1s step-end infinite;
}

@keyframes ctBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.ct-status {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 1px;
}

/* TOOLBAR */
.ct-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.ct-toolbar-left, .ct-toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.ct-time-chips {
  display: flex;
  gap: 2px;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  padding: 2px;
}

.ct-tchip {
  padding: 5px 12px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.ct-tchip:hover { color: var(--text-primary); background: rgba(147,51,234,0.06); }
.ct-tchip.active { background: rgba(147,51,234,0.14); border-color: rgba(168,85,247,0.4); color: #c084fc; }

.ct-select {
  padding: 7px 12px;
  font-family: var(--font-mono);
  font-size: 10px;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
}

.ct-search {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  width: 220px;
  transition: all 0.2s ease;
}

.ct-search:focus-within { border-color: rgba(168,85,247,0.4); }

.ct-search-prefix {
  font-family: var(--font-mono);
  font-size: 10px;
  color: #06b6d4;
  font-weight: 600;
  flex-shrink: 0;
}

.ct-search input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-primary);
}

.ct-search input::placeholder { color: var(--text-muted); }

.ct-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 12px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.ct-btn:hover { border-color: rgba(168,85,247,0.35); color: #c084fc; }
.ct-btn-icon { font-size: 12px; }

/* CHART */
.ct-chart-panel {
  background: rgba(8,3,18,0.85);
  border: 2px solid rgba(147,51,234,0.12);
  border-radius: 2px;
  overflow: hidden;
}

.ct-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 2px solid rgba(147,51,234,0.08);
}

.ct-chart-cmd {
  font-family: var(--font-mono);
  font-size: 10px;
  color: #06b6d4;
}

.ct-panel-decor {
  font-family: var(--font-mono);
  font-size: 12px;
  color: rgba(168,85,247,0.3);
}

.ct-chart-body { height: 220px; padding: 8px; }

/* TABLE */
.ct-table-panel {
  background: rgba(8,3,18,0.85);
  border: 2px solid rgba(147,51,234,0.12);
  border-radius: 2px;
  overflow: hidden;
}

.ct-loading, .ct-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 2px;
  gap: 14px;
}

.ct-empty-icon { font-size: 48px; opacity: 0.15; }

.ct-load-bar {
  width: 160px;
  height: 2px;
  background: rgba(147,51,234,0.1);
  overflow: hidden;
}

.ct-load-bar-fill {
  height: 100%;
  width: 40%;
  background: #a855f7;
  animation: ctLoadScan 1.5s ease-in-out infinite;
}

@keyframes ctLoadScan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}

.ct-load-dots span {
  animation: ctDotFade 1.4s infinite;
  opacity: 0;
}

.ct-load-dots span:nth-child(1) { animation-delay: 0s; }
.ct-load-dots span:nth-child(2) { animation-delay: 0.2s; }
.ct-load-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes ctDotFade {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

.ct-table-wrap { overflow-x: auto; }

.ct-table {
  width: 100%;
  border-collapse: collapse;
}

.ct-table thead th {
  padding: 10px 14px;
  text-align: left;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  color: #06b6d4;
  background: rgba(147,51,234,0.04);
  border-bottom: 2px solid rgba(147,51,234,0.1);
  letter-spacing: 0.5px;
  cursor: pointer;
  user-select: none;
}

.ct-table thead th:hover { color: #06b6d4; }

.ct-table tbody td {
  padding: 10px 14px;
  border-bottom: 1px solid rgba(147,51,234,0.05);
  font-size: 12px;
  color: var(--text-primary);
}

.ct-table tbody tr:hover { background: rgba(147,51,234,0.03); }

.ct-time-cell { font-family: var(--font-mono); font-size: 10px !important; color: var(--text-muted) !important; }
.ct-detail-cell { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.ct-player-cell { display: flex; align-items: center; gap: 10px; }

.ct-avatar {
  width: 28px;
  height: 28px;
  image-rendering: pixelated;
  border: 1px solid rgba(147,51,234,0.15);
}

.ct-tag {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 2px 8px;
  border: 1px solid;
}

.ct-tag.ct-tag-red { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.ct-tag.ct-tag-orange { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.ct-tag.ct-tag-cyan { border-color: rgba(6,182,212,0.2); color: #06b6d4; background: rgba(6,182,212,0.05); }
.ct-tag.ct-tag-purple { border-color: rgba(168,85,247,0.25); color: #a855f7; background: rgba(168,85,247,0.06); }
.ct-tag.ct-tag-default { border-color: rgba(147,51,234,0.2); color: #c084fc; background: rgba(147,51,234,0.05); }

.ct-sev-badge {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 2px 8px;
  border: 1px solid;
}

.ct-sev-badge.critical { border-color: rgba(168,85,247,0.25); color: #a855f7; background: rgba(168,85,247,0.06); }
.ct-sev-badge.high { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.ct-sev-badge.medium { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.ct-sev-badge.low { border-color: rgba(16,185,129,0.2); color: #10b981; background: rgba(16,185,129,0.05); }

.ct-sort { font-size: 10px; margin-left: 4px; opacity: 0.4; }

.ct-actions { display: flex; gap: 4px; }

.ct-act {
  padding: 4px 10px;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  border: 1px solid;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ct-act.view { border-color: rgba(6,182,212,0.25); color: #06b6d4; background: transparent; }
.ct-act.view:hover { background: rgba(6,182,212,0.1); }
.ct-act.delete { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: transparent; }
.ct-act.delete:hover { background: rgba(255,61,90,0.1); }

/* PAGINATION */
.ct-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 2px solid rgba(147,51,234,0.06);
}

.ct-pag-count {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}

.ct-pag-controls { display: flex; align-items: center; gap: 10px; }

.ct-pag-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-secondary);
  font-family: var(--font-mono);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ct-pag-btn:hover:not(:disabled) { border-color: rgba(168,85,247,0.4); color: #c084fc; }
.ct-pag-btn:disabled { opacity: 0.2; cursor: not-allowed; }

.ct-pag-num {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-secondary);
}

/* OVERLAY */
.ct-overlay {
  position: fixed;
  inset: 0;
  background: rgba(4,0,10,0.88);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.ct-modal {
  background: rgba(8,3,18,0.98);
  border: 2px solid rgba(147,51,234,0.25);
  border-radius: 2px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 50px rgba(0,0,0,0.7), 0 0 40px rgba(147,51,234,0.12);
}

@keyframes ctModalIn {
  from { opacity: 0; transform: scale(0.94) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.ct-modal-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 2px solid rgba(147,51,234,0.1);
}

.ct-modal-cmd {
  font-family: var(--font-mono);
  font-size: 11px;
  color: #06b6d4;
}

.ct-modal-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,61,90,0.08);
  border: 2px solid rgba(255,61,90,0.15);
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ct-modal-close:hover { background: rgba(255,61,90,0.2); color: #ff3d5a; }

.ct-modal-body { padding: 18px; }

.ct-modal-player {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 2px solid rgba(147,51,234,0.06);
}

.ct-modal-head {
  width: 56px;
  height: 56px;
  image-rendering: pixelated;
  border: 2px solid rgba(147,51,234,0.2);
}

.ct-modal-name { font-size: 16px; font-weight: 700; color: var(--text-primary); }
.ct-modal-uuid { font-family: var(--font-mono); font-size: 9px; color: var(--text-muted); margin-top: 2px; }

.ct-modal-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }

.ct-modal-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: rgba(147,51,234,0.025);
  border: 1px solid rgba(147,51,234,0.05);
}

.ct-modal-cell.full { grid-column: 1 / -1; }

.ct-modal-label {
  font-family: var(--font-mono);
  font-size: 8px;
  color: #06b6d4;
  opacity: 0.55;
}

.ct-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 18px;
  border-top: 2px solid rgba(147,51,234,0.06);
}

.ct-modal-btn.sec {
  padding: 8px 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  background: rgba(147,51,234,0.04);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.ct-modal-btn.sec:hover { background: rgba(147,51,234,0.1); color: var(--text-primary); }

.ct-modal-fade-enter-active { animation: ctModalIn 0.22s ease-out; }
.ct-modal-fade-leave-active { animation: ctModalIn 0.16s ease-in reverse; }

@media (max-width: 767px) {
  .ct-search { width: 100%; }
  .ct-modal-grid { grid-template-columns: 1fr; }
}
</style>
