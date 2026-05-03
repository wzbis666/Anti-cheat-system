<template>
  <div class="reports-terminal">
    <div class="rp-header">
      <div class="rp-prompt">
        <span class="rp-prompt-sign">root@acs:~$</span>
        <span class="rp-prompt-cmd">./reports.list --status=all</span>
        <span class="rp-cursor">_</span>
      </div>
      <div class="rp-header-right">
        <div class="rp-pending" v-if="pendingCount > 0">
          <span class="rp-pending-count">{{ pendingCount }}</span>
          <span>PENDING</span>
        </div>
        <select v-model="statusFilter" class="rp-select">
          <option value="">{{ t('reports.allStatus') }}</option>
          <option value="PENDING">{{ t('reports.pending') }}</option>
          <option value="RESOLVED">{{ t('reports.resolved') }}</option>
          <option value="REJECTED">{{ t('reports.rejected') }}</option>
        </select>
      </div>
    </div>

    <div class="rp-table-panel">
      <div v-if="loading" class="rp-loading">
        <div class="rp-load-bar"><div class="rp-load-bar-fill"></div></div>
        <span class="rp-load-text">LOADING <span class="rp-load-dots"><span>.</span><span>.</span><span>.</span></span></span>
      </div>

      <div v-else-if="filteredReports.length === 0" class="rp-empty">
        <div class="rp-empty-icon">⊡</div>
        <span>NO REPORTS</span>
      </div>

      <div v-else class="rp-table-wrap">
        <table class="rp-table">
          <thead>
            <tr>
              <th>$ reporter</th>
              <th>$ reported</th>
              <th>$ type</th>
              <th>$ reason</th>
              <th>$ timestamp</th>
              <th>$ status</th>
              <th>$ actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="report in filteredReports" :key="report.id" :class="{ 'rp-row-pending': report.status === 'PENDING' }">
              <td>
                <div class="rp-player-cell">
                  <img :src="`https://mc-heads.net/avatar/${report.reporterName}/32`" class="rp-avatar" />
                  <span>{{ report.reporterName }}</span>
                </div>
              </td>
              <td>
                <div class="rp-player-cell">
                  <img :src="`https://mc-heads.net/avatar/${report.reportedName}/32`" class="rp-avatar" />
                  <span>{{ report.reportedName }}</span>
                </div>
              </td>
              <td><span :class="['rp-tag', getReportTypeClass(report.reportType)]">{{ getReportTypeText(report.reportType) }}</span></td>
              <td class="rp-reason-cell">{{ report.reason }}</td>
              <td class="rp-time-cell">{{ formatTime(report.reportTime) }}</td>
              <td><span :class="['rp-tag', getStatusClass(report.status)]">{{ getStatusText(report.status) }}</span></td>
              <td>
                <div class="rp-actions">
                  <button v-if="report.status === 'PENDING'" class="rp-act resolve" @click="showHandleDialog(report)">HANDLE</button>
                  <button class="rp-act ai" @click="analyzeWithAi(report)" :disabled="aiLoading">AI</button>
                  <button class="rp-act view" @click="showDetailDialog(report)">VIEW</button>
                  <button class="rp-act delete" @click="handleDelete(report.id)">DEL</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <transition name="rp-modal-fade">
      <div v-if="handleDialogVisible" class="rp-overlay" @click.self="handleDialogVisible = false">
        <div class="rp-modal">
          <div class="rp-modal-top">
            <span class="rp-modal-cmd">> report.handle --id={{ selectedReport?.id }}</span>
            <button class="rp-modal-close" @click="handleDialogVisible = false">┼</button>
          </div>
          <div class="rp-modal-body">
            <div class="rp-field">
              <span class="rp-field-cmd">$ reported</span>
              <input :value="selectedReport?.reportedName" disabled class="rp-input" />
            </div>
            <div class="rp-field">
              <span class="rp-field-cmd">$ reason</span>
              <input :value="selectedReport?.reason" disabled class="rp-input" />
            </div>
            <div class="rp-field">
              <span class="rp-field-cmd">$ result</span>
              <div class="rp-radio-row">
                <label class="rp-radio"><input type="radio" value="RESOLVED" v-model="handleForm.status" /> <span>CONFIRM</span></label>
                <label class="rp-radio"><input type="radio" value="REJECTED" v-model="handleForm.status" /> <span>REJECT</span></label>
              </div>
            </div>
            <div class="rp-field">
              <span class="rp-field-cmd">$ notes</span>
              <textarea v-model="handleForm.result" :placeholder="t('reports.notes')" class="rp-textarea"></textarea>
            </div>
            <div class="rp-field" v-if="handleForm.status === 'RESOLVED'">
              <label class="rp-checkbox">
                <input type="checkbox" v-model="handleForm.banPlayer" />
                <span>$ ban.player</span>
              </label>
            </div>
            <div class="rp-field" v-if="handleForm.banPlayer">
              <span class="rp-field-cmd">$ ban.reason</span>
              <input v-model="handleForm.banReason" :placeholder="t('reports.banReason')" class="rp-input" />
            </div>
          </div>
          <div class="rp-modal-footer">
            <button class="rp-modal-btn sec" @click="handleDialogVisible = false">CANCEL</button>
            <button class="rp-modal-btn primary" @click="confirmHandle" :disabled="handling">{{ handling ? '...' : 'CONFIRM' }}</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="rp-modal-fade">
      <div v-if="detailDialogVisible" class="rp-overlay" @click.self="detailDialogVisible = false">
        <div class="rp-modal">
          <div class="rp-modal-top">
            <span class="rp-modal-cmd">> report.detail --id={{ selectedReport?.id }}</span>
            <button class="rp-modal-close" @click="detailDialogVisible = false">┼</button>
          </div>
          <div class="rp-modal-body" v-if="selectedReport">
            <div class="rp-detail-grid">
              <div class="rp-detail-cell"><span class="rp-detail-label">$ reporter</span><span class="rp-detail-val">{{ selectedReport.reporterName }}</span></div>
              <div class="rp-detail-cell"><span class="rp-detail-label">$ reported</span><span class="rp-detail-val">{{ selectedReport.reportedName }}</span></div>
              <div class="rp-detail-cell"><span class="rp-detail-label">$ type</span><span :class="['rp-tag', getReportTypeClass(selectedReport.reportType)]">{{ getReportTypeText(selectedReport.reportType) }}</span></div>
              <div class="rp-detail-cell"><span class="rp-detail-label">$ status</span><span :class="['rp-tag', getStatusClass(selectedReport.status)]">{{ getStatusText(selectedReport.status) }}</span></div>
              <div class="rp-detail-cell full"><span class="rp-detail-label">$ timestamp</span><span class="rp-detail-val">{{ formatTime(selectedReport.reportTime) }}</span></div>
              <div class="rp-detail-cell full"><span class="rp-detail-label">$ reason</span><span class="rp-detail-val">{{ selectedReport.reason }}</span></div>
              <div class="rp-detail-cell" v-if="selectedReport.handledBy"><span class="rp-detail-label">$ handled.by</span><span class="rp-detail-val">{{ selectedReport.handledBy }}</span></div>
              <div class="rp-detail-cell" v-if="selectedReport.handledTime"><span class="rp-detail-label">$ handled.at</span><span class="rp-detail-val">{{ formatTime(selectedReport.handledTime) }}</span></div>
              <div class="rp-detail-cell full" v-if="selectedReport.result"><span class="rp-detail-label">$ result</span><span class="rp-detail-val">{{ selectedReport.result }}</span></div>
            </div>
            <div v-if="aiAnalysisResult" class="rp-ai-panel">
              <div class="rp-ai-top">
                <span class="rp-ai-icon">◈</span> AI DIAGNOSIS
              </div>
              <div class="rp-ai-body" v-html="renderAiAnalysis(aiAnalysisResult)"></div>
            </div>
          </div>
          <div class="rp-modal-footer">
            <button class="rp-modal-btn sec" @click="detailDialogVisible = false">[ESC]</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
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
    const handleForm = ref({ status: 'RESOLVED', result: '', banPlayer: false, banReason: '' })
    const aiLoading = ref(false)
    const aiAnalysisResult = ref(null)

    const fetchReports = async () => {
      loading.value = true
      try {
        const [reportsData, countData] = await Promise.all([reportApi.getAll(), reportApi.getPendingCount()])
        reports.value = reportsData
        pendingCount.value = countData.count
        emit('update-badge', countData.count)
      } catch (error) {
        console.error('Failed to fetch reports:', error)
        ElMessage.error(t('common.error'))
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
      const map = { 'CHEATING': 'rp-tag-red', 'HACKING': 'rp-tag-red', 'GRIEFING': 'rp-tag-orange', 'HARASSMENT': 'rp-tag-orange', 'OTHER': 'rp-tag-cyan' }
      return map[type] || 'rp-tag-cyan'
    }

    const getReportTypeText = (type) => {
      const map = { 'CHEATING': t('reports.cheating'), 'HACKING': t('reports.hacking'), 'GRIEFING': t('reports.griefing'), 'HARASSMENT': t('reports.harassment'), 'OTHER': t('reports.other') }
      return map[type] || type || t('reports.cheating')
    }

    const getStatusClass = (status) => {
      const map = { 'PENDING': 'rp-tag-orange', 'RESOLVED': 'rp-tag-green', 'REJECTED': 'rp-tag-cyan' }
      return map[status] || 'rp-tag-cyan'
    }

    const getStatusText = (status) => {
      const map = { 'PENDING': t('reports.pending'), 'RESOLVED': t('reports.resolved'), 'REJECTED': t('reports.rejected') }
      return map[status] || status
    }

    const showHandleDialog = (report) => {
      selectedReport.value = report
      handleForm.value = { status: 'RESOLVED', result: '', banPlayer: false, banReason: report.reason }
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
      if (result.analysis) html += `<p>${result.analysis.replace(/\n/g, '<br/>')}</p>`
      if (result.verdict) html += `<p><strong>${t('ai.verdict')}:</strong> ${result.verdict}</p>`
      if (result.confidence) html += `<p><strong>${t('ai.confidence')}:</strong> ${(result.confidence * 100).toFixed(0)}%</p>`
      if (result.suggestedAction) html += `<p><strong>${t('ai.suggestedAction')}:</strong> ${result.suggestedAction}</p>`
      if (result.reasoning) html += `<p><strong>${t('ai.reasoning')}:</strong> ${result.reasoning.replace(/\n/g, '<br/>')}</p>`
      return html
    }

    const confirmHandle = async () => {
      handling.value = true
      try {
        await reportApi.handle(selectedReport.value.id, { status: handleForm.value.status, result: handleForm.value.result })
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
        await ElMessageBox.confirm(t('common.confirm'), t('common.delete'), { confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel'), type: 'warning' })
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

    onMounted(() => { fetchReports() })

    return {
      reports, loading, pendingCount, statusFilter, filteredReports,
      handleDialogVisible, detailDialogVisible, selectedReport, handling, handleForm, aiLoading, aiAnalysisResult,
      formatTime, getReportTypeClass, getReportTypeText, getStatusClass, getStatusText,
      showHandleDialog, showDetailDialog, analyzeWithAi, renderAiAnalysis, confirmHandle, handleDelete, t
    }
  }
}
</script>

<style scoped>
.reports-terminal {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.rp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.rp-prompt {
  font-family: var(--font-mono);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.rp-prompt-sign { color: #06b6d4; font-weight: 700; }
.rp-prompt-cmd { color: #a855f7; }

.rp-cursor {
  color: #c084fc;
  animation: rpBlink 1s step-end infinite;
}

@keyframes rpBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.rp-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rp-pending {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(245,158,11,0.1);
  border: 2px solid rgba(245,158,11,0.25);
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  color: #f59e0b;
}

.rp-pending-count {
  background: #f59e0b;
  color: #000;
  padding: 1px 7px;
  font-size: 10px;
}

.rp-select {
  padding: 7px 12px;
  font-family: var(--font-mono);
  font-size: 10px;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
}

/* TABLE */
.rp-table-panel {
  background: rgba(8,3,18,0.85);
  border: 2px solid rgba(147,51,234,0.12);
  border-radius: 2px;
  overflow: hidden;
}

.rp-loading, .rp-empty {
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

.rp-empty-icon { font-size: 48px; opacity: 0.15; }

.rp-load-bar {
  width: 160px;
  height: 2px;
  background: rgba(147,51,234,0.1);
  overflow: hidden;
}

.rp-load-bar-fill {
  height: 100%;
  width: 40%;
  background: #a855f7;
  animation: rpLoadScan 1.5s ease-in-out infinite;
}

@keyframes rpLoadScan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}

.rp-load-dots span {
  animation: rpDotFade 1.4s infinite;
  opacity: 0;
}

.rp-load-dots span:nth-child(1) { animation-delay: 0s; }
.rp-load-dots span:nth-child(2) { animation-delay: 0.2s; }
.rp-load-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes rpDotFade {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

.rp-table-wrap { overflow-x: auto; }

.rp-table {
  width: 100%;
  border-collapse: collapse;
}

.rp-table thead th {
  padding: 10px 14px;
  text-align: left;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  color: #06b6d4;
  background: rgba(147,51,234,0.04);
  border-bottom: 2px solid rgba(147,51,234,0.1);
  letter-spacing: 0.5px;
}

.rp-table tbody td {
  padding: 10px 14px;
  border-bottom: 1px solid rgba(147,51,234,0.05);
  font-size: 12px;
  color: var(--text-primary);
}

.rp-table tbody tr:hover { background: rgba(147,51,234,0.03); }
.rp-row-pending { background: rgba(245,158,11,0.03); }
.rp-row-pending:hover { background: rgba(245,158,11,0.06) !important; }

.rp-reason-cell { max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rp-time-cell { font-family: var(--font-mono); font-size: 10px !important; color: var(--text-muted) !important; }

.rp-player-cell { display: flex; align-items: center; gap: 10px; }

.rp-avatar {
  width: 28px;
  height: 28px;
  image-rendering: pixelated;
  border: 1px solid rgba(147,51,234,0.15);
}

.rp-tag {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 2px 8px;
  border: 1px solid;
}

.rp-tag.rp-tag-red { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.rp-tag.rp-tag-orange { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.rp-tag.rp-tag-green { border-color: rgba(16,185,129,0.2); color: #10b981; background: rgba(16,185,129,0.05); }
.rp-tag.rp-tag-cyan { border-color: rgba(6,182,212,0.2); color: #06b6d4; background: rgba(6,182,212,0.05); }

.rp-actions { display: flex; gap: 4px; }

.rp-act {
  padding: 4px 10px;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  border: 1px solid;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
}

.rp-act.resolve { border-color: rgba(16,185,129,0.25); color: #10b981; }
.rp-act.resolve:hover { background: rgba(16,185,129,0.1); }
.rp-act.ai { border-color: rgba(168,85,247,0.25); color: #a855f7; }
.rp-act.ai:hover { background: rgba(168,85,247,0.1); }
.rp-act.view { border-color: rgba(6,182,212,0.25); color: #06b6d4; }
.rp-act.view:hover { background: rgba(6,182,212,0.1); }
.rp-act.delete { border-color: rgba(255,61,90,0.25); color: #ff3d5a; }
.rp-act.delete:hover { background: rgba(255,61,90,0.1); }
.rp-act:disabled { opacity: 0.3; cursor: not-allowed; }

/* OVERLAY */
.rp-overlay {
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

.rp-modal {
  background: rgba(8,3,18,0.98);
  border: 2px solid rgba(147,51,234,0.25);
  border-radius: 2px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 50px rgba(0,0,0,0.7), 0 0 40px rgba(147,51,234,0.12);
}

@keyframes rpModalIn {
  from { opacity: 0; transform: scale(0.94) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.rp-modal-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 2px solid rgba(147,51,234,0.1);
}

.rp-modal-cmd {
  font-family: var(--font-mono);
  font-size: 11px;
  color: #06b6d4;
}

.rp-modal-close {
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

.rp-modal-close:hover { background: rgba(255,61,90,0.2); color: #ff3d5a; }

.rp-modal-body { padding: 18px; }

.rp-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 18px;
  border-top: 2px solid rgba(147,51,234,0.06);
}

.rp-modal-btn {
  padding: 8px 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.rp-modal-btn.sec {
  background: rgba(147,51,234,0.04);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-muted);
}

.rp-modal-btn.sec:hover { background: rgba(147,51,234,0.1); color: var(--text-primary); }

.rp-modal-btn.primary {
  background: #a855f7;
  border: 2px solid #a855f7;
  color: #fff;
}

.rp-modal-btn.primary:hover { background: #9333ea; border-color: #9333ea; }
.rp-modal-btn.primary:disabled { opacity: 0.4; cursor: not-allowed; }

/* FIELDS */
.rp-field { margin-bottom: 14px; }

.rp-field-cmd {
  display: block;
  font-family: var(--font-mono);
  font-size: 9px;
  color: #06b6d4;
  opacity: 0.55;
  margin-bottom: 5px;
}

.rp-input {
  width: 100%;
  padding: 9px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147,51,234,0.03);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
  outline: none;
}

.rp-input:focus { border-color: rgba(168,85,247,0.4); }

.rp-textarea {
  width: 100%;
  padding: 9px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147,51,234,0.03);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
  min-height: 60px;
  resize: vertical;
  outline: none;
}

.rp-textarea:focus { border-color: rgba(168,85,247,0.4); }

.rp-radio-row { display: flex; gap: 16px; }

.rp-radio {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-primary);
  cursor: pointer;
}

.rp-radio input { accent-color: #a855f7; }

.rp-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-primary);
  cursor: pointer;
}

.rp-checkbox input { accent-color: #a855f7; }

/* DETAIL */
.rp-detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }

.rp-detail-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: rgba(147,51,234,0.025);
  border: 1px solid rgba(147,51,234,0.05);
}

.rp-detail-cell.full { grid-column: 1 / -1; }

.rp-detail-label {
  font-family: var(--font-mono);
  font-size: 8px;
  color: #06b6d4;
  opacity: 0.55;
}

.rp-detail-val { font-size: 13px; font-weight: 500; }

/* AI */
.rp-ai-panel {
  margin-top: 16px;
  border: 2px solid rgba(168,85,247,0.2);
  overflow: hidden;
}

.rp-ai-top {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(168,85,247,0.08);
  color: #c084fc;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.rp-ai-icon { color: #a855f7; }

.rp-ai-body {
  padding: 14px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.rp-ai-body :deep(strong) { color: #c084fc; }

.rp-modal-fade-enter-active { animation: rpModalIn 0.22s ease-out; }
.rp-modal-fade-leave-active { animation: rpModalIn 0.16s ease-in reverse; }

@media (max-width: 767px) {
  .rp-detail-grid { grid-template-columns: 1fr; }
}
</style>
