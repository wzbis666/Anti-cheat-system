<template>
  <div class="reports-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ t('nav.reports') }}</h2>
        <div v-if="pendingCount > 0" class="pending-badge">
          <span class="pending-count">{{ pendingCount }}</span>
          <span class="pending-label">{{ t('reports.pending') }}</span>
        </div>
      </div>

      <select v-model="statusFilter" class="status-filter">
        <option value="">{{ t('reports.allStatus') }}</option>
        <option value="PENDING">{{ t('reports.pending') }}</option>
        <option value="RESOLVED">{{ t('reports.resolved') }}</option>
        <option value="REJECTED">{{ t('reports.rejected') }}</option>
      </select>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>{{ t('common.loading') }}...</span>
      </div>

      <div v-else-if="filteredReports.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="48" height="48" class="empty-icon">
          <path fill="currentColor" d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6m-1 2l5 5h-5V4z"/>
        </svg>
        <p>{{ t('reports.noReports') }}</p>
      </div>

      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ t('reports.reporter') }}</th>
              <th>{{ t('reports.reported') }}</th>
              <th>{{ t('reports.type') }}</th>
              <th>{{ t('reports.reason') }}</th>
              <th>{{ t('reports.timestamp') }}</th>
              <th>{{ t('reports.status') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="report in filteredReports"
              :key="report.id"
              :class="['data-row', { pending: report.status === 'PENDING' }]"
            >
              <td>
                <div class="player-cell">
                  <img
                    :src="`https://mc-heads.net/avatar/${report.reporterName}/40`"
                    :alt="report.reporterName"
                    class="player-avatar"
                  />
                  <span class="player-name">{{ report.reporterName }}</span>
                </div>
              </td>
              <td>
                <div class="player-cell">
                  <img
                    :src="`https://mc-heads.net/avatar/${report.reportedName}/40`"
                    :alt="report.reportedName"
                    class="player-avatar"
                  />
                  <span class="player-name">{{ report.reportedName }}</span>
                </div>
              </td>
              <td>
                <span :class="['type-badge', getReportTypeClass(report.reportType)]">
                  {{ getReportTypeText(report.reportType) }}
                </span>
              </td>
              <td class="reason-cell">{{ report.reason }}</td>
              <td class="time-cell">{{ formatTime(report.reportTime) }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(report.status)]">
                  {{ getStatusText(report.status) }}
                </span>
              </td>
              <td>
                <div class="action-group">
                  <button
                    v-if="report.status === 'PENDING'"
                    class="action-btn resolve-btn"
                    @click="showHandleDialog(report)"
                  >
                    {{ t('reports.handle') }}
                  </button>
                  <button
                    class="action-btn ai-btn"
                    @click="analyzeWithAi(report)"
                    :disabled="aiLoading"
                  >
                    AI
                  </button>
                  <button
                    class="action-btn view-btn"
                    @click="showDetailDialog(report)"
                  >
                    <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                  </button>
                  <button
                    class="action-btn delete-btn"
                    @click="handleDelete(report.id)"
                  >
                    <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Handle Dialog -->
    <transition name="modal-fade">
      <div v-if="handleDialogVisible" class="modal-overlay" @click.self="handleDialogVisible = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ t('reports.handleReport') }} #{{ selectedReport?.id }}</h3>
            <button class="close-btn" @click="handleDialogVisible = false">×</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">{{ t('reports.reportedPlayer') }}</label>
              <input
                :value="selectedReport?.reportedName"
                disabled
                class="form-input disabled"
              />
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('reports.reason') }}</label>
              <input
                :value="selectedReport?.reason"
                disabled
                class="form-input disabled"
              />
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('reports.result') }}</label>
              <div class="radio-options">
                <label class="radio-option">
                  <input type="radio" value="RESOLVED" v-model="handleForm.status" />
                  <span>{{ t('reports.confirm') }}</span>
                </label>
                <label class="radio-option">
                  <input type="radio" value="REJECTED" v-model="handleForm.status" />
                  <span>{{ t('reports.reject') }}</span>
                </label>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('reports.notes') }}</label>
              <textarea
                v-model="handleForm.result"
                :placeholder="t('reports.notes')"
                class="form-textarea"
              ></textarea>
            </div>

            <div v-if="handleForm.status === 'RESOLVED'" class="form-group checkbox-group">
              <label class="checkbox-option">
                <input type="checkbox" v-model="handleForm.banPlayer" />
                <span>{{ t('reports.banPlayer') }}</span>
              </label>
            </div>

            <div v-if="handleForm.banPlayer && handleForm.status === 'RESOLVED'" class="form-group">
              <label class="form-label">{{ t('reports.banReason') }}</label>
              <input
                v-model="handleForm.banReason"
                :placeholder="t('reports.banReason')"
                class="form-input"
              />
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn secondary" @click="handleDialogVisible = false">
              {{ t('common.cancel') }}
            </button>
            <button class="btn primary" @click="confirmHandle" :disabled="handling">
              {{ handling ? '...' : t('reports.confirm') }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Detail Dialog -->
    <transition name="modal-fade">
      <div v-if="detailDialogVisible" class="modal-overlay" @click.self="detailDialogVisible = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ t('reports.reportDetail') }} #{{ selectedReport?.id }}</h3>
            <button class="close-btn" @click="detailDialogVisible = false">×</button>
          </div>

          <div class="modal-body" v-if="selectedReport">
            <div class="detail-grid">
              <div class="detail-cell">
                <span class="detail-label">{{ t('reports.reporter') }}</span>
                <span class="detail-value">{{ selectedReport.reporterName }}</span>
              </div>
              <div class="detail-cell">
                <span class="detail-label">{{ t('reports.reported') }}</span>
                <span class="detail-value">{{ selectedReport.reportedName }}</span>
              </div>
              <div class="detail-cell">
                <span class="detail-label">{{ t('reports.type') }}</span>
                <span :class="['type-badge', getReportTypeClass(selectedReport.reportType)]">
                  {{ getReportTypeText(selectedReport.reportType) }}
                </span>
              </div>
              <div class="detail-cell">
                <span class="detail-label">{{ t('reports.status') }}</span>
                <span :class="['status-badge', getStatusClass(selectedReport.status)]">
                  {{ getStatusText(selectedReport.status) }}
                </span>
              </div>
              <div class="detail-cell full">
                <span class="detail-label">{{ t('reports.timestamp') }}</span>
                <span class="detail-value">{{ formatTime(selectedReport.reportTime) }}</span>
              </div>
              <div class="detail-cell full">
                <span class="detail-label">{{ t('reports.reason') }}</span>
                <span class="detail-value">{{ selectedReport.reason }}</span>
              </div>
              <div v-if="selectedReport.handledBy" class="detail-cell">
                <span class="detail-label">{{ t('reports.handledBy') }}</span>
                <span class="detail-value">{{ selectedReport.handledBy }}</span>
              </div>
              <div v-if="selectedReport.handledTime" class="detail-cell">
                <span class="detail-label">{{ t('reports.handledAt') }}</span>
                <span class="detail-value">{{ formatTime(selectedReport.handledTime) }}</span>
              </div>
              <div v-if="selectedReport.result" class="detail-cell full">
                <span class="detail-label">{{ t('reports.result') }}</span>
                <span class="detail-value">{{ selectedReport.result }}</span>
              </div>
            </div>

            <div v-if="aiAnalysisResult" class="ai-panel">
              <div class="ai-header">
                <svg viewBox="0 0 24 24" width="16" height="16" style="color: var(--accent-gold)">
                  <path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/>
                </svg>
                <span>{{ t('ai.aiAnalysis') }}</span>
              </div>
              <div class="ai-body" v-html="renderAiAnalysis(aiAnalysisResult)"></div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn secondary" @click="detailDialogVisible = false">
              {{ t('common.close') }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { reportApi, punishmentApi, aiApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EventBus, Events } from '../utils/eventBus'

export default {
  name: 'Reports',
  emits: ['update-badge'],
  setup(props, { emit }) {
    const { t } = useI18n()

    const reports = ref([])
    const loading = ref(false)
    const pendingCount = ref(0)
    const statusFilter = ref('')
    const handleDialogVisible = ref(false)
    const detailDialogVisible = ref(false)
    const selectedReport = ref(null)
    const handling = ref(false)
    const handleForm = ref({
      status: 'RESOLVED',
      result: '',
      banPlayer: false,
      banReason: ''
    })
    const aiLoading = ref(false)
    const aiAnalysisResult = ref(null)

    let controller = null

    const fetchReports = async () => {
      loading.value = true
      try {
        controller = new AbortController()
        const { signal } = controller
        const [reportsData, countData] = await Promise.all([
          reportApi.getAll({ signal }),
          reportApi.getPendingCount({ signal })
        ])
        reports.value = reportsData
        pendingCount.value = countData.count
        emit('update-badge', countData.count)
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error('Failed to fetch reports:', error)
          ElMessage.error(t('common.error'))
        }
      } finally {
        loading.value = false
      }
    }

    const filteredReports = computed(() => {
      if (!statusFilter.value) return reports.value
      return reports.value.filter(r => r.status === statusFilter.value)
    })

    const formatTime = (timestamp) => {
      if (!timestamp) return '-'
      return new Date(timestamp).toLocaleString()
    }

    const getReportTypeClass = (type) => {
      const map = {
        'CHEATING': 'danger',
        'HACKING': 'danger',
        'GRIEFING': 'warning',
        'HARASSMENT': 'warning',
        'OTHER': 'info'
      }
      return map[type] || 'info'
    }

    const getReportTypeText = (type) => {
      const map = {
        'CHEATING': t('reports.cheating'),
        'HACKING': t('reports.hacking'),
        'GRIEFING': t('reports.griefing'),
        'HARASSMENT': t('reports.harassment'),
        'OTHER': t('reports.other')
      }
      return map[type] || type || t('reports.cheating')
    }

    const getStatusClass = (status) => {
      const map = {
        'PENDING': 'warning',
        'RESOLVED': 'success',
        'REJECTED': 'info'
      }
      return map[status] || 'info'
    }

    const getStatusText = (status) => {
      const map = {
        'PENDING': t('reports.pending'),
        'RESOLVED': t('reports.resolved'),
        'REJECTED': t('reports.rejected')
      }
      return map[status] || status
    }

    const showHandleDialog = (report) => {
      selectedReport.value = report
      handleForm.value = {
        status: 'RESOLVED',
        result: '',
        banPlayer: false,
        banReason: report.reason
      }
      handleDialogVisible.value = true
    }

    const showDetailDialog = (report) => {
      selectedReport.value = report
      aiAnalysisResult.value = null
      detailDialogVisible.value = true
    }

    const analyzeWithAi = async (report) => {
      aiLoading.value = true
      aiAnalysisResult.value = null

      try {
        const result = await aiApi.analyzeReport(report.id)
        if (result.success) {
          aiAnalysisResult.value = result
          selectedReport.value = report
          detailDialogVisible.value = true
        } else {
          ElMessage.warning(result.error || t('ai.error'))
        }
      } catch (e) {
        ElMessage.error(t('ai.networkError'))
      } finally {
        aiLoading.value = false
      }
    }

    const renderAiAnalysis = (result) => {
      if (!result) return ''

      let html = ''

      if (result.analysis) {
        html += `<p>${result.analysis.replace(/\n/g, '<br/>')}</p>`
      }

      if (result.verdict) {
        html += `<p><strong>${t('ai.verdict')}:</strong> ${result.verdict}</p>`
      }

      if (result.confidence) {
        html += `<p><strong>${t('ai.confidence')}:</strong> ${(result.confidence * 100).toFixed(0)}%</p>`
      }

      if (result.suggestedAction) {
        html += `<p><strong>${t('ai.suggestedAction')}:</strong> ${result.suggestedAction}</p>`
      }

      if (result.reasoning) {
        html += `<p><strong>${t('ai.reasoning')}:</strong> ${result.reasoning.replace(/\n/g, '<br/>')}</p>`
      }

      return html
    }

    const confirmHandle = async () => {
      handling.value = true

      try {
        await reportApi.handle(selectedReport.value.id, {
          status: handleForm.value.status,
          result: handleForm.value.result
        })

        if (handleForm.value.banPlayer && handleForm.value.status === 'RESOLVED') {
          await punishmentApi.ban({
            uuid: selectedReport.value.reportedUuid,
            playerName: selectedReport.value.reportedName,
            punishmentType: 'PERMANENT',
            reason: handleForm.value.banReason || 'Report confirmed'
          })
        }

        ElMessage.success(t('common.success'))
        handleDialogVisible.value = false
        fetchReports()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        console.error('Failed to handle report:', error)
        ElMessage.error(t('common.error'))
      } finally {
        handling.value = false
      }
    }

    const handleDelete = async (id) => {
      try {
        await ElMessageBox.confirm(
          t('common.confirm'),
          t('common.delete'),
          {
            confirmButtonText: t('common.delete'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
        )
        await reportApi.delete(id)
        ElMessage.success(t('common.success'))
        fetchReports()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to delete report:', error)
          ElMessage.error(t('common.error'))
        }
      }
    }

    onMounted(() => {
      fetchReports()
    })

    onUnmounted(() => {
      if (controller) {
        controller.abort()
      }
    })

    return {
      reports,
      loading,
      pendingCount,
      statusFilter,
      filteredReports,
      handleDialogVisible,
      detailDialogVisible,
      selectedReport,
      handling,
      handleForm,
      aiLoading,
      aiAnalysisResult,
      formatTime,
      getReportTypeClass,
      getReportTypeText,
      getStatusClass,
      getStatusText,
      showHandleDialog,
      showDetailDialog,
      analyzeWithAi,
      renderAiAnalysis,
      confirmHandle,
      handleDelete,
      t
    }
  }
}
</script>

<style scoped>
.reports-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== HEADER ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-family: var(--font-sans);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.pending-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(255, 140, 0, 0.12);
  border: 1px solid rgba(255, 140, 0, 0.25);
  border-radius: var(--radius-sm);
}

.pending-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  background: #FF8C00;
  color: #fff;
  border-radius: var(--radius-sm);
}

.pending-label {
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  color: #FF8C00;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-filter {
  padding: 10px 16px;
  font-family: var(--font-sans);
  font-size: 13px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-filter:focus {
  border-color: rgba(255, 200, 0, 0.3);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.08);
}

/* ===== TABLE CONTAINER ===== */
.table-container {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 16px;
  color: var(--text-muted);
  font-family: var(--font-sans);
  font-size: 14px;
}

.empty-icon {
  opacity: 0.2;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--border-color);
  border-top-color: var(--accent-gold);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead th {
  padding: 14px 18px;
  text-align: left;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
}

.data-table tbody td {
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-primary);
  transition: background 0.2s ease;
}

.data-table tbody tr:hover {
  background: rgba(255, 200, 0, 0.03);
}

.data-table tbody tr.pending {
  background: rgba(255, 140, 0, 0.04);
  border-left: 3px solid #FF8C00;
}

.data-table tbody tr.pending:hover {
  background: rgba(255, 140, 0, 0.07);
}

.player-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.player-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  image-rendering: pixelated;
  border: 2px solid var(--border-color);
}

.player-name {
  font-weight: 600;
}

.type-badge,
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.type-badge.danger {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.type-badge.warning {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.25);
}

.type-badge.info {
  background: rgba(74, 158, 255, 0.1);
  color: #4A9EFF;
  border: 1px solid rgba(74, 158, 255, 0.2);
}

.status-badge.warning {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.25);
}

.status-badge.success {
  background: rgba(46, 204, 113, 0.1);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.2);
}

.status-badge.info {
  background: rgba(74, 158, 255, 0.1);
  color: #4A9EFF;
  border: 1px solid rgba(74, 158, 255, 0.2);
}

.reason-cell {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-secondary);
}

.action-group {
  display: flex;
  gap: 6px;
}

.action-btn {
  padding: 6px 12px;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  border: 1px solid;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
}

.action-btn.resolve-btn {
  border-color: rgba(46, 204, 113, 0.3);
  color: #2ECC71;
}

.action-btn.resolve-btn:hover {
  background: rgba(46, 204, 113, 0.12);
}

.action-btn.ai-btn {
  border-color: rgba(155, 89, 182, 0.3);
  color: #9B59B6;
}

.action-btn.ai-btn:hover {
  background: rgba(155, 89, 182, 0.12);
}

.action-btn.view-btn {
  border-color: rgba(74, 158, 255, 0.3);
  color: #4A9EFF;
  padding: 6px 10px;
}

.action-btn.view-btn:hover {
  background: rgba(74, 158, 255, 0.12);
}

.action-btn.delete-btn {
  border-color: rgba(231, 76, 60, 0.3);
  color: #E74C3C;
  padding: 6px 10px;
}

.action-btn.delete-btn:hover {
  background: rgba(231, 76, 60, 0.12);
}

.action-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

/* ===== MODAL ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-card-solid);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg), var(--shadow-gold-strong);
  animation: modalIn 0.3s ease-out;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(12px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.modal-title {
  font-family: var(--font-sans);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.2);
  border-radius: var(--radius-sm);
  color: #E74C3C;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #E74C3C;
  color: #fff;
}

.modal-body {
  padding: 22px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}

.btn {
  padding: 10px 20px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn.secondary {
  background: transparent;
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.btn.secondary:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--text-muted);
  color: var(--text-primary);
}

.btn.primary {
  background: rgba(255, 200, 0, 0.12);
  border-color: rgba(255, 200, 0, 0.3);
  color: var(--accent-gold);
}

.btn.primary:hover {
  background: rgba(255, 200, 0, 0.2);
}

.btn.primary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ===== FORM ===== */
.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  font-family: var(--font-sans);
  font-size: 13px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  transition: all 0.3s ease;
}

.form-input:focus,
.form-textarea:focus {
  border-color: rgba(255, 200, 0, 0.3);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.08);
}

.form-input.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-textarea {
  min-height: 80px;
  resize: vertical;
}

.radio-options {
  display: flex;
  gap: 16px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
}

.radio-option input {
  accent-color: var(--accent-gold);
}

.checkbox-group {
  margin-top: 4px;
}

.checkbox-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
}

.checkbox-option input {
  accent-color: var(--accent-gold);
}

/* ===== DETAIL GRID ===== */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.detail-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.detail-cell.full {
  grid-column: 1 / -1;
}

.detail-label {
  font-family: var(--font-sans);
  font-size: 10px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* ===== AI PANEL ===== */
.ai-panel {
  margin-top: 16px;
  border: 1px solid rgba(255, 200, 0, 0.2);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255, 200, 0, 0.08);
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 700;
  color: var(--accent-gold);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.ai-body {
  padding: 16px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.ai-body :deep(strong) {
  color: var(--accent-gold);
}

/* ===== TRANSITIONS ===== */
.modal-fade-enter-active {
  animation: fadeIn 0.25s ease-out;
}

.modal-fade-leave-active {
  animation: fadeIn 0.2s ease-in reverse;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1024px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .reason-cell {
    max-width: 120px;
  }
}

@media (max-width: 767px) {
  .data-table thead th,
  .data-table tbody td {
    padding: 12px 14px;
  }

  .action-group {
    flex-wrap: wrap;
  }

  .reason-cell {
    max-width: none;
    white-space: normal;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
