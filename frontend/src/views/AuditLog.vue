<template>
  <div class="audit-terminal">
    <div class="at-header">
      <div class="at-prompt">
        <span class="at-prompt-sign">root@acs:~$</span>
        <span class="at-prompt-cmd">./audit.log --tail</span>
        <span class="at-cursor">_</span>
      </div>
      <div class="at-stats">
        <span class="at-stat">{{ totalRecords }} records</span>
      </div>
    </div>

    <div class="at-filters">
      <select v-model="filterType" class="at-select" @change="fetchLogs">
        <option value="">{{ t('common.all') }}</option>
        <option value="BAN">BAN</option>
        <option value="UNBAN">UNBAN</option>
        <option value="DELETE_PLAYER">DELETE_PLAYER</option>
        <option value="DELETE_CHEAT">DELETE_CHEAT</option>
        <option value="HANDLE_REPORT">HANDLE_REPORT</option>
        <option value="WHITELIST_ADD">WHITELIST_ADD</option>
        <option value="WHITELIST_REMOVE">WHITELIST_REMOVE</option>
        <option value="SETTINGS_UPDATE">SETTINGS_UPDATE</option>
      </select>
    </div>

    <div v-if="loading" class="at-skeleton">
      <div v-for="i in 6" :key="i" class="at-skel-row">
        <div class="at-skel-cell w-16"></div>
        <div class="at-skel-cell w-24"></div>
        <div class="at-skel-cell w-32"></div>
        <div class="at-skel-cell w-48"></div>
        <div class="at-skel-cell w-24"></div>
      </div>
    </div>

    <div v-else-if="logs.length === 0" class="at-empty">
      <span class="at-empty-icon">⊡</span>
      {{ t('common.noData') }}
    </div>

    <div v-else class="at-table-wrap">
      <table class="at-table">
        <thead>
          <tr>
            <th>$ time</th>
            <th>$ admin</th>
            <th>$ action</th>
            <th>$ target</th>
            <th>$ detail</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td class="at-time">{{ formatTime(log.createTime) }}</td>
            <td>
              <span class="at-admin">{{ log.adminName || 'SYSTEM' }}</span>
            </td>
            <td>
              <span :class="['at-tag', getActionClass(log.actionType)]">{{ log.actionType }}</span>
            </td>
            <td>
              <span class="at-target">{{ log.targetType }}{{ log.targetName ? ': ' + log.targetName : '' }}</span>
            </td>
            <td class="at-detail">{{ log.detail || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="at-pagination" v-if="totalPages > 1">
      <button class="at-page-btn" :disabled="page === 0" @click="page--; fetchLogs()">←</button>
      <span class="at-page-info">{{ page + 1 }} / {{ totalPages }}</span>
      <button class="at-page-btn" :disabled="page >= totalPages - 1" @click="page++; fetchLogs()">→</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import api from '../api'

export default {
  name: 'AuditLog',
  setup() {
    const { t } = useI18n()
    const logs = ref([])
    const loading = ref(true)
    const page = ref(0)
    const totalPages = ref(0)
    const totalRecords = ref(0)
    const filterType = ref('')

    const fetchLogs = async () => {
      loading.value = true
      try {
        const result = await api.get('/audit/list', {
          params: { page: page.value, size: 20 }
        })
        if (result.success) {
          logs.value = result.data.content
          totalPages.value = result.data.totalPages
          totalRecords.value = result.data.totalElements
        }
      } catch (e) {
        console.error(e)
      } finally {
        loading.value = false
      }
    }

    const formatTime = (ts) => {
      if (!ts) return '-'
      return new Date(ts).toLocaleString()
    }

    const getActionClass = (type) => {
      if (!type) return 'action-default'
      if (type.includes('BAN') || type.includes('DELETE')) return 'action-danger'
      if (type.includes('CREATE') || type.includes('ADD')) return 'action-success'
      if (type.includes('UPDATE') || type.includes('EDIT')) return 'action-info'
      return 'action-default'
    }

    onMounted(fetchLogs)

    return { t, logs, loading, page, totalPages, totalRecords, filterType, fetchLogs, formatTime, getActionClass }
  }
}
</script>

<style scoped>
.audit-terminal {
  font-family: var(--font-mono);
  color: var(--text-primary);
}
.at-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.at-prompt-sign { color: #a855f7; font-weight: 700; }
.at-prompt-cmd { color: #c084fc; margin-left: 8px; }
.at-cursor { color: #a855f7; animation: cursorBlink 1s step-end infinite; }
.at-stats { font-size: 11px; color: var(--text-muted); }
.at-filters { margin-bottom: 14px; }
.at-select {
  padding: 8px 12px; font-family: var(--font-mono); font-size: 12px;
  background: var(--bg-secondary); border: 1px solid var(--border-color);
  border-radius: var(--radius-sm); color: var(--text-primary); outline: none;
}
.at-skeleton { display: flex; flex-direction: column; gap: 6px; }
.at-skel-row { display: flex; gap: 8px; }
.at-skel-cell { height: 14px; background: var(--bg-hover); border-radius: 2px; animation: skelPulse 1.5s ease-in-out infinite; }
.at-skel-cell.w-16 { width: 16%; } .at-skel-cell.w-24 { width: 24%; }
.at-skel-cell.w-32 { width: 32%; } .at-skel-cell.w-48 { width: 48%; }
@keyframes skelPulse { 0%, 100% { opacity: 0.3; } 50% { opacity: 0.6; } }
.at-empty { text-align: center; padding: 48px; color: var(--text-muted); font-size: 13px; }
.at-empty-icon { display: block; font-size: 32px; margin-bottom: 8px; color: var(--text-muted); }
.at-table-wrap { overflow-x: auto; }
.at-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.at-table th { text-align: left; padding: 10px 12px; color: #a855f7; font-weight: 600; font-size: 10px; letter-spacing: 0.5px; border-bottom: 2px solid var(--border-color); white-space: nowrap; }
.at-table td { padding: 10px 12px; border-bottom: 1px solid rgba(147,51,234,0.06); white-space: nowrap; }
.at-table tbody tr:hover { background: rgba(147,51,234,0.04); }
.at-time { color: var(--text-muted); font-size: 11px; }
.at-admin { color: var(--text-secondary); }
.at-tag { padding: 2px 8px; border-radius: 2px; font-size: 10px; font-weight: 600; letter-spacing: 0.3px; }
.action-danger { background: rgba(255,61,90,0.12); color: #ff3d5a; }
.action-success { background: rgba(16,185,129,0.12); color: #10b981; }
.action-info { background: rgba(6,182,212,0.12); color: #06b6d4; }
.action-default { background: rgba(168,85,247,0.1); color: #c084fc; }
.at-target { color: var(--text-primary); }
.at-detail { color: var(--text-muted); font-size: 11px; max-width: 200px; overflow: hidden; text-overflow: ellipsis; }
.at-pagination { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: 16px; }
.at-page-btn {
  padding: 8px 14px; font-family: var(--font-mono); font-size: 12px;
  background: var(--bg-secondary); border: 1px solid var(--border-color);
  border-radius: var(--radius-sm); color: var(--text-primary); cursor: pointer;
  transition: all 0.2s ease;
}
.at-page-btn:hover:not(:disabled) { border-color: #a855f7; color: #c084fc; }
.at-page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.at-page-info { font-size: 12px; color: var(--text-muted); }
</style>
