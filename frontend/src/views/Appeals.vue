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
          <span :class="['ap-status', appeal.status.toLowerCase()]">{{ appeal.status }}</span>
        </div>

        <div class="ap-reason">
          <div class="ap-reason-label">> reason</div>
          <p class="ap-reason-text">{{ appeal.reason }}</p>
        </div>

        <div class="ap-meta">
          <span class="ap-time">{{ formatTime(appeal.createTime) }}</span>
        </div>

        <div v-if="appeal.status === 'PENDING'" class="ap-actions">
          <textarea v-model="responseText[appeal.id]" class="ap-response-input" placeholder="Admin response..."></textarea>
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
  font-family: var(--font-mono);
}
.ap-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.ap-prompt-sign { color: #a855f7; font-weight: 700; }
.ap-prompt-cmd { color: #c084fc; margin-left: 8px; }
.ap-cursor { color: #a855f7; animation: cursorBlink 1s step-end infinite; }
@keyframes cursorBlink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
.ap-stats { display: flex; gap: 8px; }
.ap-stat { font-size: 11px; padding: 2px 10px; border-radius: 10px; }
.ap-stat.pending { background: rgba(245,158,11,0.15); color: #f59e0b; }
.ap-filters { display: flex; gap: 6px; margin-bottom: 16px; }
.ap-filter-btn {
  padding: 6px 14px; font-family: var(--font-mono); font-size: 11px;
  background: var(--bg-secondary); border: 1px solid var(--border-color);
  border-radius: var(--radius-sm); color: var(--text-muted); cursor: pointer; transition: all 0.2s ease;
}
.ap-filter-btn:hover { border-color: #a855f7; color: #c084fc; }
.ap-filter-btn.active { background: rgba(168,85,247,0.12); border-color: #a855f7; color: #c084fc; }
.ap-skeleton { display: flex; flex-direction: column; gap: 8px; }
.ap-skel-row { display: flex; gap: 8px; }
.ap-skel-cell { height: 16px; background: var(--bg-hover); border-radius: 2px; animation: skelPulse 1.5s ease-in-out infinite; }
.ap-skel-cell.w-20 { width: 20%; } .ap-skel-cell.w-30 { width: 30%; }
.ap-skel-cell.w-10 { width: 10%; }
@keyframes skelPulse { 0%,100%{opacity:.3} 50%{opacity:.6} }
.ap-empty { text-align: center; padding: 48px; color: var(--text-muted); }
.ap-empty-icon { display: block; font-size: 32px; margin-bottom: 8px; }
.ap-list { display: flex; flex-direction: column; gap: 12px; }
.ap-card {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md);
  padding: 16px; transition: all 0.2s ease;
}
.ap-card:hover { border-color: rgba(168,85,247,0.3); box-shadow: 0 2px 16px rgba(147,51,234,0.06); }
.ap-card.approved { border-left: 3px solid #10b981; }
.ap-card.rejected { border-left: 3px solid #ff3d5a; }
.ap-card.pending { border-left: 3px solid #f59e0b; }
.ap-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.ap-player-info { display: flex; align-items: center; gap: 8px; }
.ap-avatar { border-radius: 2px; image-rendering: pixelated; border: 1px solid var(--border-color); }
.ap-player-name { color: var(--text-primary); font-weight: 600; font-size: 13px; }
.ap-punishment-ref { font-size: 10px; color: var(--text-muted); background: var(--bg-secondary); padding: 1px 6px; border-radius: 2px; }
.ap-status { padding: 2px 10px; border-radius: 2px; font-size: 10px; font-weight: 700; letter-spacing: 0.5px; }
.ap-status.pending { background: rgba(245,158,11,0.15); color: #f59e0b; }
.ap-status.approved { background: rgba(16,185,129,0.15); color: #10b981; }
.ap-status.rejected { background: rgba(255,61,90,0.15); color: #ff3d5a; }
.ap-reason { margin-bottom: 10px; }
.ap-reason-label { font-size: 10px; color: #a855f7; margin-bottom: 4px; }
.ap-reason-text { margin: 0; font-size: 12px; color: var(--text-secondary); line-height: 1.6; }
.ap-meta { display: flex; gap: 16px; }
.ap-time { font-size: 10px; color: var(--text-muted); }
.ap-actions { margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border-color); }
.ap-response-input {
  width: 100%; min-height: 60px; padding: 10px; font-family: var(--font-mono); font-size: 12px;
  background: var(--bg-primary); border: 1px solid var(--border-color);
  border-radius: var(--radius-sm); color: var(--text-primary); outline: none; resize: vertical;
  margin-bottom: 10px;
}
.ap-response-input:focus { border-color: rgba(168,85,247,0.5); }
.ap-action-btns { display: flex; gap: 8px; justify-content: flex-end; }
.ap-btn {
  padding: 7px 18px; font-family: var(--font-mono); font-size: 11px; font-weight: 600;
  border: none; border-radius: var(--radius-sm); cursor: pointer; transition: all 0.2s ease;
}
.ap-btn.approve { background: rgba(16,185,129,0.15); color: #10b981; }
.ap-btn.approve:hover { background: #10b981; color: #fff; }
.ap-btn.reject { background: rgba(255,61,90,0.15); color: #ff3d5a; }
.ap-btn.reject:hover { background: #ff3d5a; color: #fff; }
.ap-admin-response { margin-top: 12px; padding: 12px; background: var(--bg-secondary); border-radius: var(--radius-sm); }
.ap-response-label { font-size: 10px; color: #a855f7; margin-bottom: 4px; }
.ap-response-text { margin: 0; font-size: 12px; color: var(--text-secondary); line-height: 1.6; }
.ap-handled-by { font-size: 10px; color: var(--text-muted); margin-top: 6px; text-align: right; }
</style>
