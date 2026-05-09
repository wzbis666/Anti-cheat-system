<template>
  <div class="appeal-terminal">
    <div class="ap-header">
      <div class="ap-prompt">
        <span class="ap-prompt-sign">root@acs:~$</span>
        <span class="ap-prompt-cmd">./appeals.list --all</span>
        <span class="ap-cursor">_</span>
      </div>
      <div class="ap-stats">
        <span class="ap-stat pending" v-if="pendingCount > 0">{{ pendingCount }} {{ t('appeals.pending') }}</span>
      </div>
    </div>

    <div class="ap-filters">
      <button v-for="f in filters" :key="f.value" :class="['ap-filter-btn', { active: activeFilter === f.value }]" @click="activeFilter = f.value; fetchAppeals()">
        {{ f.label }}
      </button>
    </div>

    <div v-if="loading" class="ap-skeleton">
      <div v-for="i in 5" :key="i" class="ap-skel-row">
        <div class="ap-skel-cell w-20"></div>
        <div class="ap-skel-cell w-30"></div>
        <div class="ap-skel-cell w-30"></div>
        <div class="ap-skel-cell w-10"></div>
        <div class="ap-skel-cell w-10"></div>
      </div>
    </div>

    <div v-else-if="appeals.length === 0" class="ap-empty">
      <span class="ap-empty-icon">⊡</span>
      {{ t('common.noData') }}
    </div>

    <div v-else class="ap-list">
      <div v-for="appeal in appeals" :key="appeal.id" :class="['ap-card', appeal.status.toLowerCase()]">
        <div class="ap-card-header">
          <div class="ap-player-info">
            <img :src="`https://mc-heads.net/avatar/${appeal.playerName}/24`" class="ap-avatar" />
            <span class="ap-player-name">{{ appeal.playerName }}</span>
            <span class="ap-punishment-ref" v-if="appeal.punishmentId">Punish #{{ appeal.punishmentId }}</span>
          </div>
          <span :class="['ap-status', appeal.status.toLowerCase()]">{{ t('appeals.' + appeal.status) }}</span>
        </div>

        <div class="ap-reason">
          <div class="ap-reason-label">{{ t('appeals.reason') }}</div>
          <p class="ap-reason-text">{{ appeal.reason }}</p>
        </div>

        <div class="ap-meta">
          <span class="ap-time">{{ formatTime(appeal.createTime) }}</span>
        </div>

        <div v-if="appeal.status === 'PENDING'" class="ap-actions">
          <textarea v-model="responseText[appeal.id]" class="ap-response-input" :placeholder="t('appeals.adminResponse')"></textarea>
          <div class="ap-action-btns">
            <button class="ap-btn approve" @click="handleAppeal(appeal.id, 'APPROVED')">
              {{ t('appeals.approve') }}
            </button>
            <button class="ap-btn reject" @click="handleAppeal(appeal.id, 'REJECTED')">
              {{ t('appeals.reject') }}
            </button>
          </div>
        </div>

        <div v-if="appeal.adminResponse" class="ap-admin-response">
          <div class="ap-response-label">> admin response</div>
          <p class="ap-response-text">{{ appeal.adminResponse }}</p>
          <div class="ap-handled-by">— {{ appeal.handledBy }}, {{ formatTime(appeal.handleTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

export default {
  name: 'Appeals',
  setup() {
    const { t } = useI18n()
    const appeals = ref([])
    const loading = ref(true)
    const activeFilter = ref('all')
    const pendingCount = ref(0)
    const responseText = reactive({})

    const filters = [
      { value: 'all', label: t('common.all') },
      { value: 'pending', label: t('appeals.pending') },
      { value: 'approved', label: t('appeals.approved') },
      { value: 'rejected', label: t('appeals.rejected') }
    ]

    const fetchAppeals = async () => {
      loading.value = true
      try {
        let result
        if (activeFilter.value === 'all') {
          result = await api.get('/appeal/all')
        } else if (activeFilter.value === 'pending') {
          result = await api.get('/appeal/pending')
        } else {
          result = await api.get('/appeal/all')
        }
        if (result.success) {
          let data = result.data
          if (activeFilter.value !== 'all' && activeFilter.value !== 'pending') {
            data = data.filter(a => a.status === activeFilter.value.toUpperCase())
          }
          appeals.value = Array.isArray(data) ? data : (data.content || [])
        }

        const countRes = await api.get('/appeal/count/pending')
        if (countRes.success) {
          pendingCount.value = countRes.data.count
        }
      } catch (e) {
        console.error(e)
      } finally {
        loading.value = false
      }
    }

    const handleAppeal = async (id, status) => {
      try {
        await ElMessageBox.confirm(
          status === 'APPROVED' ? t('appeals.confirmApprove') : t('appeals.confirmReject'),
          t('common.confirm'),
          { type: 'warning' }
        )
      } catch (e) {
        if (e === 'cancel') return
      }

      try {
        const result = await api.post(`/appeal/handle/${id}`, {
          status,
          adminResponse: responseText[id] || ''
        })
        if (result.success) {
          ElMessage.success(t('common.success'))
          fetchAppeals()
        } else {
          ElMessage.error(result.message || t('common.error'))
        }
      } catch (e) {
        ElMessage.error(t('common.error'))
      }
    }

    const formatTime = (ts) => {
      if (!ts) return '-'
      return new Date(ts).toLocaleString()
    }

    onMounted(fetchAppeals)

    return { t, appeals, loading, activeFilter, pendingCount, responseText, filters, fetchAppeals, handleAppeal, formatTime }
  }
}
</script>

<style scoped>
.appeal-terminal {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.ap-prompt {
  font-family: var(--font-mono);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ap-prompt-sign { color: #06b6d4; font-weight: 700; }
.ap-prompt-cmd { color: #a855f7; }

.ap-cursor {
  color: #c084fc;
  animation: apBlink 1s step-end infinite;
}

@keyframes apBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.ap-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ap-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
}

.ap-stat.pending {
  background: rgba(245,158,11,0.1);
  border: 2px solid rgba(245,158,11,0.25);
  color: #f59e0b;
}

.ap-stat-count {
  background: #f59e0b;
  color: #000;
  padding: 1px 7px;
  font-size: 10px;
}

.ap-filters {
  display: flex;
  gap: 4px;
  background: rgba(10, 4, 22, 0.6);
  border: 2px solid rgba(147, 51, 234, 0.12);
  border-radius: 2px;
  padding: 3px;
}

.ap-filter-btn {
  padding: 6px 14px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.ap-filter-btn:hover {
  color: var(--text-primary);
  background: rgba(147, 51, 234, 0.08);
}

.ap-filter-btn.active {
  background: rgba(147, 51, 234, 0.16);
  border-color: rgba(168, 85, 247, 0.45);
  color: #c084fc;
  box-shadow: 0 0 8px rgba(147, 51, 234, 0.1);
}

.ap-skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.ap-skel-row {
  display: flex;
  gap: 10px;
  height: 40px;
}

.ap-skel-cell {
  background: rgba(147, 51, 234, 0.08);
  border-radius: 2px;
  animation: apSkelPulse 1.5s ease-in-out infinite;
}

.ap-skel-cell.w-20 { width: 20%; }
.ap-skel-cell.w-30 { width: 30%; }
.ap-skel-cell.w-10 { width: 10%; }

@keyframes apSkelPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

.ap-empty {
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

.ap-empty-icon {
  font-size: 48px;
  opacity: 0.15;
}

.ap-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ap-card {
  position: relative;
  background: rgba(8, 3, 18, 0.85);
  border: 2px solid rgba(147, 51, 234, 0.1);
  border-radius: 2px;
  padding: 16px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.ap-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(168,85,247,0.012) 2px, rgba(168,85,247,0.012) 4px);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.ap-card:hover::before {
  opacity: 1;
}

.ap-card:hover {
  border-color: rgba(168, 85, 247, 0.4);
  box-shadow: 0 1px 0 rgba(168,85,247,0.15), 0 4px 20px rgba(147,51,234,0.12);
}

.ap-card.approved { border-left: 4px solid #10b981; }
.ap-card.rejected { border-left: 4px solid #ff3d5a; }
.ap-card.pending { border-left: 4px solid #f59e0b; }

.ap-card.approved:hover { box-shadow: 0 1px 0 rgba(16,185,129,0.15), 0 4px 20px rgba(16,185,129,0.08); }
.ap-card.rejected:hover { box-shadow: 0 1px 0 rgba(255,61,90,0.15), 0 4px 20px rgba(255,61,90,0.1); }
.ap-card.pending:hover { box-shadow: 0 1px 0 rgba(245,158,11,0.15), 0 4px 20px rgba(245,158,11,0.08); }

.ap-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.ap-player-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ap-avatar {
  width: 32px;
  height: 32px;
  border-radius: 2px;
  image-rendering: pixelated;
  border: 1px solid rgba(147, 51, 234, 0.15);
}

.ap-player-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.ap-punishment-ref {
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
  background: rgba(147, 51, 234, 0.06);
  padding: 2px 6px;
  border-radius: 1px;
}

.ap-status {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 3px 8px;
  border: 1px solid;
}

.ap-status.pending { border-color: rgba(245,158,11,0.3); color: #f59e0b; background: rgba(245,158,11,0.08); }
.ap-status.approved { border-color: rgba(16,185,129,0.25); color: #10b981; background: rgba(16,185,129,0.06); }
.ap-status.rejected { border-color: rgba(255,61,90,0.3); color: #ff3d5a; background: rgba(255,61,90,0.08); }

.ap-reason {
  margin-bottom: 10px;
}

.ap-reason-label {
  font-family: var(--font-mono);
  font-size: 9px;
  color: #06b6d4;
  margin-bottom: 4px;
  opacity: 0.6;
}

.ap-reason-text {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.ap-meta {
  display: flex;
  gap: 16px;
}

.ap-time {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}

.ap-actions {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 2px solid rgba(147, 51, 234, 0.06);
}

.ap-response-input {
  width: 100%;
  min-height: 70px;
  padding: 10px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147, 51, 234, 0.03);
  border: 2px solid rgba(147, 51, 234, 0.1);
  color: var(--text-primary);
  outline: none;
  resize: vertical;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.ap-response-input:focus {
  border-color: rgba(168, 85, 247, 0.4);
  box-shadow: 0 0 8px rgba(147, 51, 234, 0.1);
}

.ap-action-btns {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.ap-btn {
  padding: 8px 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  border: 2px solid;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
}

.ap-btn.approve { border-color: rgba(16,185,129,0.25); color: #10b981; }
.ap-btn.approve:hover { background: rgba(16,185,129,0.14); }
.ap-btn.reject { border-color: rgba(255,61,90,0.25); color: #ff3d5a; }
.ap-btn.reject:hover { background: rgba(255,61,90,0.14); }

.ap-admin-response {
  margin-top: 14px;
  padding: 14px;
  background: rgba(147, 51, 234, 0.04);
  border: 1px solid rgba(147, 51, 234, 0.06);
}

.ap-response-label {
  font-family: var(--font-mono);
  font-size: 9px;
  color: #06b6d4;
  margin-bottom: 6px;
  opacity: 0.55;
}

.ap-response-text {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.ap-handled-by {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 6px;
  text-align: right;
  opacity: 0.55;
}
</style>
