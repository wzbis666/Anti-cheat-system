<template>
  <div class="punishments-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ t('nav.punishments') }}</h2>
        <span class="ban-count">{{ punishments.length }} {{ t('punishments.totalBans') }}</span>
      </div>

      <button class="add-ban-btn" @click="showBanDialog = true">
        <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
        {{ t('punishments.banPlayer') }}
      </button>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>{{ t('common.loading') }}...</span>
      </div>

      <div v-else-if="punishments.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="48" height="48" class="empty-icon">
          <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
        </svg>
        <p>{{ t('punishments.noBans') }}</p>
      </div>

      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ t('punishments.player') }}</th>
              <th>{{ t('punishments.type') }}</th>
              <th>{{ t('punishments.reason') }}</th>
              <th>{{ t('punishments.timestamp') }}</th>
              <th>{{ t('punishments.duration') }}</th>
              <th>{{ t('punishments.status') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="p in punishments"
              :key="p.id"
              :class="['data-row', { active: p.active }]"
            >
              <td>
                <div class="player-cell">
                  <img
                    v-if="p.player"
                    :src="`https://mc-heads.net/avatar/${p.player.playerName}/40`"
                    :alt="p.player.playerName"
                    class="player-avatar"
                  />
                  <span class="player-name">{{ p.player?.playerName || '-' }}</span>
                </div>
              </td>
              <td>
                <span :class="['type-badge', p.punishmentType === 'PERMANENT' ? 'permanent' : 'temporary']">
                  {{ p.punishmentType === 'PERMANENT' ? t('punishments.permanent') : t('punishments.temporary') }}
                </span>
              </td>
              <td class="reason-cell">{{ p.reason }}</td>
              <td class="time-cell">{{ formatTime(p.punishmentTime) }}</td>
              <td class="duration-cell">
                {{ p.punishmentType === 'PERMANENT' ? t('punishments.permanent') : formatDuration(p.duration) }}
              </td>
              <td>
                <span :class="['status-badge', p.active ? 'active' : 'expired']">
                  {{ p.active ? t('punishments.active') : t('punishments.expired') }}
                </span>
              </td>
              <td>
                <div class="action-group">
                  <button
                    v-if="p.active"
                    class="action-btn unban-btn"
                    @click="handleUnban(p)"
                  >
                    {{ t('punishments.unban') }}
                  </button>
                  <button
                    class="action-btn ai-btn"
                    @click="evaluateBan(p)"
                    :disabled="aiEvalLoading"
                  >
                    AI
                  </button>
                  <button
                    class="action-btn delete-btn"
                    @click="handleDelete(p.id)"
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

    <!-- Ban Dialog -->
    <transition name="modal-fade">
      <div v-if="showBanDialog" class="modal-overlay" @click.self="showBanDialog = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ t('punishments.banPlayer') }}</h3>
            <button class="close-btn" @click="showBanDialog = false">×</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">{{ t('punishments.playerName') }}</label>
              <input
                v-model="banForm.playerName"
                :placeholder="t('punishments.playerName')"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('punishments.playerUUID') }}</label>
              <input
                v-model="banForm.uuid"
                :placeholder="t('punishments.playerUUID')"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('punishments.banType') }}</label>
              <div class="radio-options">
                <label class="radio-option">
                  <input type="radio" value="PERMANENT" v-model="banForm.punishmentType" />
                  <span>{{ t('punishments.permanent') }}</span>
                </label>
                <label class="radio-option">
                  <input type="radio" value="TEMPORARY" v-model="banForm.punishmentType" />
                  <span>{{ t('punishments.temporary') }}</span>
                </label>
              </div>
            </div>

            <div v-if="banForm.punishmentType === 'TEMPORARY'" class="form-group">
              <label class="form-label">{{ t('punishments.duration') }}</label>
              <select v-model="banForm.duration" class="form-select">
                <option :value="3600000">1 {{ t('punishments.hour') }}</option>
                <option :value="21600000">6 {{ t('punishments.hours') }}</option>
                <option :value="86400000">1 {{ t('punishments.day') }}</option>
                <option :value="259200000">3 {{ t('punishments.days') }}</option>
                <option :value="604800000">7 {{ t('punishments.days') }}</option>
                <option :value="2592000000">30 {{ t('punishments.days') }}</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('reports.banReason') }}</label>
              <textarea
                v-model="banForm.reason"
                :placeholder="t('reports.banReason')"
                class="form-textarea"
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn secondary" @click="showBanDialog = false">
              {{ t('common.cancel') }}
            </button>
            <button class="btn danger" @click="handleBan" :disabled="banning">
              {{ banning ? '...' : t('punishments.execute') }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- AI Evaluation Dialog -->
    <transition name="modal-fade">
      <div v-if="aiEvalDialogVisible" class="modal-overlay" @click.self="aiEvalDialogVisible = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ t('punishments.aiEvaluation') }} - {{ aiEvalPlayer }}</h3>
            <button class="close-btn" @click="aiEvalDialogVisible = false">×</button>
          </div>

          <div class="modal-body">
            <div v-if="aiEvalLoading" class="eval-loading">
              <div class="loading-spinner"></div>
              <span>{{ t('punishments.evaluating') }}...</span>
            </div>

            <div v-else-if="aiEvalResult" class="eval-result">
              <div v-if="aiEvalResult.analysis" class="eval-block">
                <div class="eval-title">{{ t('dashboard.analysis') }}</div>
                <div class="eval-text" v-html="renderAiText(aiEvalResult.analysis)"></div>
              </div>

              <div class="eval-row">
                <div v-if="aiEvalResult.verdict" class="eval-block half">
                  <div class="eval-title">{{ t('dashboard.verdict') }}</div>
                  <span :class="['verdict-badge', getEvalVerdictClass(aiEvalResult.verdict)]">
                    {{ aiEvalResult.verdict }}
                  </span>
                </div>

                <div v-if="aiEvalResult.confidence" class="eval-block half">
                  <div class="eval-title">{{ t('players.confidence') }}</div>
                  <span class="confidence-value">{{ (aiEvalResult.confidence * 100).toFixed(0) }}%</span>
                </div>
              </div>

              <div v-if="aiEvalResult.suggestedAction" class="eval-block">
                <div class="eval-title">{{ t('dashboard.action') }}</div>
                <span :class="['verdict-badge', getActionClass(aiEvalResult.suggestedAction)]">
                  {{ aiEvalResult.suggestedAction }}
                </span>
              </div>

              <div v-if="aiEvalResult.reasoning" class="eval-block">
                <div class="eval-title">{{ t('dashboard.reasoning') }}</div>
                <div class="eval-text" v-html="renderAiText(aiEvalResult.reasoning)"></div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn secondary" @click="aiEvalDialogVisible = false">
              {{ t('common.close') }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { punishmentApi, aiApi } from '../api'
import { renderAiText } from '../utils/helpers'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EventBus, Events } from '../utils/eventBus'

export default {
  name: 'Punishments',
  setup() {
    const { t } = useI18n()

    const punishments = ref([])
    const loading = ref(false)
    const showBanDialog = ref(false)
    const banning = ref(false)
    const banForm = ref({
      playerName: '',
      uuid: '',
      punishmentType: 'PERMANENT',
      duration: 86400000,
      reason: 'Cheating'
    })
    const aiEvalLoading = ref(false)
    const aiEvalResult = ref(null)
    const aiEvalDialogVisible = ref(false)
    const aiEvalPlayer = ref('')

    let controller = null

    const fetchPunishments = async () => {
      loading.value = true
      try {
        controller = new AbortController()
        punishments.value = await punishmentApi.getAll({ signal: controller.signal })
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error('Failed to fetch punishments:', error)
          ElMessage.error(t('common.error'))
        }
      } finally {
        loading.value = false
      }
    }

    const formatTime = (timestamp) => {
      if (!timestamp) return '-'
      return new Date(timestamp).toLocaleString()
    }

    const formatDuration = (ms) => {
      if (!ms) return '-'
      const days = Math.floor(ms / 86400000)
      const hours = Math.floor((ms % 86400000) / 3600000)
      if (days > 0) return `${days}d ${hours}h`
      if (hours > 0) return `${hours}h`
      return `${Math.floor(ms / 60000)}m`
    }

    const handleBan = async () => {
      if (!banForm.value.playerName || !banForm.value.uuid) {
        ElMessage.warning(t('common.warning'))
        return
      }

      banning.value = true
      try {
        await punishmentApi.ban(banForm.value)
        ElMessage.success(t('common.success'))
        showBanDialog.value = false
        banForm.value = {
          playerName: '',
          uuid: '',
          punishmentType: 'PERMANENT',
          duration: 86400000,
          reason: 'Cheating'
        }
        fetchPunishments()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        console.error('Failed to ban player:', error)
        ElMessage.error(t('common.error'))
      } finally {
        banning.value = false
      }
    }

    const handleUnban = async (punishment) => {
      try {
        await ElMessageBox.confirm(
          `${t('punishments.confirmUnban')} ${punishment.player?.playerName || ''}?`,
          t('common.confirm'),
          {
            confirmButtonText: t('punishments.unban'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
        )
        await punishmentApi.unban(punishment.id)
        ElMessage.success(t('common.success'))
        fetchPunishments()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to unban:', error)
          ElMessage.error(t('common.error'))
        }
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
        await punishmentApi.delete(id)
        ElMessage.success(t('common.success'))
        fetchPunishments()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to delete:', error)
          ElMessage.error(t('common.error'))
        }
      }
    }

    const evaluateBan = async (punishment) => {
      const playerUuid = punishment.player?.uuid || punishment.playerUuid
      const playerName = punishment.player?.playerName || punishment.playerName || ''

      if (!playerUuid) {
        ElMessage.warning(t('ai.noPlayerData'))
        return
      }

      aiEvalLoading.value = true
      aiEvalResult.value = null
      aiEvalPlayer.value = playerName
      aiEvalDialogVisible.value = true

      try {
        const result = await aiApi.evaluateBan(playerUuid, playerName)
        if (result.success) {
          aiEvalResult.value = result
        } else {
          ElMessage.warning(result.error || t('ai.error'))
        }
      } catch (e) {
        ElMessage.error(t('ai.networkError'))
      } finally {
        aiEvalLoading.value = false
      }
    }

    const getEvalVerdictClass = (verdict) => {
      if (!verdict) return ''
      const v = verdict.toUpperCase()

      if (v.includes('APPROPRIATE')) return 'success'
      if (v.includes('TOO_HARSH')) return 'warning'
      if (v.includes('TOO_LENIENT')) return 'danger'
      if (v.includes('NEED_MORE')) return 'info'

      return 'info'
    }

    const getActionClass = (action) => {
      if (!action) return ''
      const a = action.toUpperCase()

      if (a.includes('UNBAN') || a.includes('REVOKE')) return 'success'
      if (a.includes('REDUCE')) return 'warning'
      if (a.includes('EXTEND') || a.includes('PERM')) return 'danger'

      return 'info'
    }

    onMounted(() => {
      fetchPunishments()
    })

    onUnmounted(() => {
      if (controller) {
        controller.abort()
      }
    })

    return {
      punishments,
      loading,
      showBanDialog,
      banning,
      banForm,
      aiEvalLoading,
      aiEvalResult,
      aiEvalDialogVisible,
      aiEvalPlayer,
      formatTime,
      formatDuration,
      handleBan,
      handleUnban,
      handleDelete,
      evaluateBan,
      renderAiText,
      getEvalVerdictClass,
      getActionClass,
      t
    }
  }
}
</script>

<style scoped>
.punishments-container {
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
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-family: var(--font-sans);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.ban-count {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}

.add-ban-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  background: rgba(231, 76, 60, 0.12);
  border: 1px solid rgba(231, 76, 60, 0.3);
  border-radius: var(--radius-sm);
  color: #E74C3C;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-ban-btn:hover {
  background: rgba(231, 76, 60, 0.2);
  border-color: rgba(231, 76, 60, 0.5);
  box-shadow: 0 0 16px rgba(231, 76, 60, 0.15);
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

.data-table tbody tr.active {
  background: rgba(231, 76, 60, 0.04);
  border-left: 3px solid #E74C3C;
}

.data-table tbody tr.active:hover {
  background: rgba(231, 76, 60, 0.07);
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

.type-badge.permanent {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.type-badge.temporary {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.25);
}

.status-badge.active {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.status-badge.expired {
  background: rgba(46, 204, 113, 0.1);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.2);
}

.reason-cell {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell,
.duration-cell {
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

.action-btn.unban-btn {
  border-color: rgba(46, 204, 113, 0.3);
  color: #2ECC71;
}

.action-btn.unban-btn:hover {
  background: rgba(46, 204, 113, 0.12);
}

.action-btn.ai-btn {
  border-color: rgba(155, 89, 182, 0.3);
  color: #9B59B6;
}

.action-btn.ai-btn:hover {
  background: rgba(155, 89, 182, 0.12);
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
  max-width: 520px;
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

.btn.danger {
  background: rgba(231, 76, 60, 0.12);
  border-color: rgba(231, 76, 60, 0.3);
  color: #E74C3C;
}

.btn.danger:hover {
  background: rgba(231, 76, 60, 0.2);
}

.btn.danger:disabled {
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
.form-textarea,
.form-select {
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
.form-textarea:focus,
.form-select:focus {
  border-color: rgba(255, 200, 0, 0.3);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.08);
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

/* ===== AI EVALUATION ===== */
.eval-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px 20px;
  color: var(--text-muted);
  font-family: var(--font-sans);
  font-size: 13px;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-color);
  border-top-color: var(--accent-gold);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}

.eval-result {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.eval-row {
  display: flex;
  gap: 16px;
}

.eval-row .eval-block.half {
  flex: 1;
}

.eval-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.eval-title {
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  color: var(--accent-gold);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.eval-text {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.eval-text :deep(strong) {
  color: var(--accent-gold);
}

.eval-text :deep(code) {
  background: rgba(255, 200, 0, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-family: var(--font-mono);
}

.verdict-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 700;
}

.verdict-badge.success {
  background: rgba(46, 204, 113, 0.1);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.2);
}

.verdict-badge.warning {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.25);
}

.verdict-badge.danger {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.verdict-badge.info {
  background: rgba(74, 158, 255, 0.1);
  color: #4A9EFF;
  border: 1px solid rgba(74, 158, 255, 0.2);
}

.confidence-value {
  font-family: var(--font-mono);
  font-size: 26px;
  font-weight: 800;
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
}
</style>
