<template>
  <div class="punish-terminal">
    <div class="pn-header">
      <div class="pn-prompt">
        <span class="pn-prompt-sign">root@acs:~$</span>
        <span class="pn-prompt-cmd">./bans.list --all</span>
        <span class="pn-cursor">_</span>
      </div>
      <button class="pn-add-btn" @click="showBanDialog = true">
        <span class="pn-add-icon">+</span> BAN PLAYER
      </button>
    </div>

    <div class="pn-table-panel">
      <div v-if="loading" class="pn-loading">
        <div class="pn-load-bar"><div class="pn-load-bar-fill"></div></div>
        <span class="pn-load-text">LOADING <span class="pn-load-dots"><span>.</span><span>.</span><span>.</span></span></span>
      </div>

      <div v-else-if="punishments.length === 0" class="pn-empty">
        <div class="pn-empty-icon">⊡</div>
        <span>NO BANS</span>
      </div>

      <div v-else class="pn-table-wrap">
        <table class="pn-table">
          <thead>
            <tr>
              <th>$ player</th>
              <th>$ type</th>
              <th>$ reason</th>
              <th>$ timestamp</th>
              <th>$ duration</th>
              <th>$ status</th>
              <th>$ actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in punishments" :key="p.id" :class="{ 'pn-row-active': p.active }">
              <td>
                <div class="pn-player-cell">
                  <img v-if="p.player" :src="`https://mc-heads.net/avatar/${p.player.playerName}/32`" class="pn-avatar" />
                  <span>{{ p.player?.playerName || '-' }}</span>
                </div>
              </td>
              <td>
                <span :class="['pn-tag', p.punishmentType === 'PERMANENT' ? 'pn-tag-red' : 'pn-tag-orange']">
                  {{ p.punishmentType === 'PERMANENT' ? t('punishments.permanent') : t('punishments.temporary') }}
                </span>
              </td>
              <td class="pn-reason-cell">{{ p.reason }}</td>
              <td class="pn-time-cell">{{ formatTime(p.punishmentTime) }}</td>
              <td>{{ p.punishmentType === 'PERMANENT' ? t('punishments.permanent') : formatDuration(p.duration) }}</td>
              <td>
                <span :class="['pn-tag', p.active ? 'pn-tag-red' : 'pn-tag-green']">
                  {{ p.active ? 'ACTIVE' : 'EXPIRED' }}
                </span>
              </td>
              <td>
                <div class="pn-actions">
                  <button v-if="p.active" class="pn-act unban" @click="handleUnban(p)">UNBAN</button>
                  <button class="pn-act ai" @click="evaluateBan(p)" :disabled="aiEvalLoading">AI</button>
                  <button class="pn-act delete" @click="handleDelete(p.id)">DEL</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <transition name="pn-modal-fade">
      <div v-if="showBanDialog" class="pn-overlay" @click.self="showBanDialog = false">
        <div class="pn-modal">
          <div class="pn-modal-top">
            <span class="pn-modal-cmd">> ban.execute</span>
            <button class="pn-modal-close" @click="showBanDialog = false">┼</button>
          </div>
          <div class="pn-modal-body">
            <div class="pn-field">
              <span class="pn-field-cmd">$ player.name</span>
              <input v-model="banForm.playerName" :placeholder="t('punishments.playerName')" class="pn-input" />
            </div>
            <div class="pn-field">
              <span class="pn-field-cmd">$ player.uuid</span>
              <input v-model="banForm.uuid" :placeholder="t('punishments.playerUUID')" class="pn-input" />
            </div>
            <div class="pn-field">
              <span class="pn-field-cmd">$ ban.type</span>
              <div class="pn-radio-row">
                <label class="pn-radio"><input type="radio" value="PERMANENT" v-model="banForm.punishmentType" /> <span>PERMANENT</span></label>
                <label class="pn-radio"><input type="radio" value="TEMPORARY" v-model="banForm.punishmentType" /> <span>TEMPORARY</span></label>
              </div>
            </div>
            <div class="pn-field" v-if="banForm.punishmentType === 'TEMPORARY'">
              <span class="pn-field-cmd">$ ban.duration</span>
              <select v-model="banForm.duration" class="pn-select">
                <option :value="3600000">1h</option>
                <option :value="21600000">6h</option>
                <option :value="86400000">1d</option>
                <option :value="259200000">3d</option>
                <option :value="604800000">7d</option>
                <option :value="2592000000">30d</option>
              </select>
            </div>
            <div class="pn-field">
              <span class="pn-field-cmd">$ ban.reason</span>
              <textarea v-model="banForm.reason" :placeholder="t('reports.banReason')" class="pn-textarea"></textarea>
            </div>
          </div>
          <div class="pn-modal-footer">
            <button class="pn-modal-btn sec" @click="showBanDialog = false">CANCEL</button>
            <button class="pn-modal-btn danger" @click="handleBan" :disabled="banning">{{ banning ? '...' : 'EXECUTE' }}</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="pn-modal-fade">
      <div v-if="aiEvalDialogVisible" class="pn-overlay" @click.self="aiEvalDialogVisible = false">
        <div class="pn-modal">
          <div class="pn-modal-top">
            <span class="pn-modal-cmd">> ai.evaluate --target={{ aiEvalPlayer }}</span>
            <button class="pn-modal-close" @click="aiEvalDialogVisible = false">┼</button>
          </div>
          <div class="pn-modal-body">
            <div v-if="aiEvalLoading" class="pn-eval-loading">
              <div class="pn-eval-spinner"></div>
              <span>$ ai.evaluating...</span>
            </div>
            <div v-else-if="aiEvalResult" class="pn-eval-result">
              <div v-if="aiEvalResult.analysis" class="pn-eval-block">
                <div class="pn-eval-label">> ANALYSIS</div>
                <div class="pn-eval-text" v-html="renderAiText(aiEvalResult.analysis)"></div>
              </div>
              <div class="pn-eval-row">
                <div v-if="aiEvalResult.verdict" class="pn-eval-block">
                  <div class="pn-eval-label">> VERDICT</div>
                  <span :class="['pn-eval-chip', getEvalVerdictClass(aiEvalResult.verdict)]">{{ aiEvalResult.verdict }}</span>
                </div>
                <div v-if="aiEvalResult.confidence" class="pn-eval-block">
                  <div class="pn-eval-label">> CONFIDENCE</div>
                  <span class="pn-eval-conf">{{ (aiEvalResult.confidence * 100).toFixed(0) }}%</span>
                </div>
              </div>
              <div v-if="aiEvalResult.suggestedAction" class="pn-eval-block">
                <div class="pn-eval-label">> ACTION</div>
                <span class="pn-eval-chip pn-eval-chip-act">{{ aiEvalResult.suggestedAction }}</span>
              </div>
              <div v-if="aiEvalResult.reasoning" class="pn-eval-block">
                <div class="pn-eval-label">> REASONING</div>
                <div class="pn-eval-text" v-html="renderAiText(aiEvalResult.reasoning)"></div>
              </div>
            </div>
          </div>
          <div class="pn-modal-footer">
            <button class="pn-modal-btn sec" @click="aiEvalDialogVisible = false">[ESC]</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
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
    const banForm = ref({ playerName: '', uuid: '', punishmentType: 'PERMANENT', duration: 86400000, reason: 'Cheating' })
    const aiEvalLoading = ref(false)
    const aiEvalResult = ref(null)
    const aiEvalDialogVisible = ref(false)
    const aiEvalPlayer = ref('')

    const fetchPunishments = async () => {
      loading.value = true
      try {
        punishments.value = await punishmentApi.getAll()
      } catch (error) {
        console.error('Failed to fetch punishments:', error)
        ElMessage.error(t('common.error'))
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
        banForm.value = { playerName: '', uuid: '', punishmentType: 'PERMANENT', duration: 86400000, reason: 'Cheating' }
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
        await ElMessageBox.confirm(`${t('punishments.confirmUnban')} ${punishment.player?.playerName || ''}?`, t('common.confirm'), { confirmButtonText: t('punishments.unban'), cancelButtonText: t('common.cancel'), type: 'warning' })
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
        await ElMessageBox.confirm(t('common.confirm'), t('common.delete'), { confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel'), type: 'warning' })
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
      if (v.includes('APPROPRIATE')) return 'verdict-ok'
      if (v.includes('TOO_HARSH')) return 'verdict-warn'
      if (v.includes('TOO_LENIENT')) return 'verdict-danger'
      if (v.includes('NEED_MORE')) return 'verdict-info'
      return 'verdict-info'
    }

    onMounted(() => { fetchPunishments() })

    return {
      punishments, loading, showBanDialog, banning, banForm, aiEvalLoading, aiEvalResult, aiEvalDialogVisible, aiEvalPlayer,
      formatTime, formatDuration, handleBan, handleUnban, handleDelete, evaluateBan, renderAiText, getEvalVerdictClass, t
    }
  }
}
</script>

<style scoped>
.punish-terminal {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.pn-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.pn-prompt {
  font-family: var(--font-mono);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pn-prompt-sign { color: #06b6d4; font-weight: 700; }
.pn-prompt-cmd { color: #a855f7; }

.pn-cursor {
  color: #c084fc;
  animation: pnBlink 1s step-end infinite;
}

@keyframes pnBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.pn-add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: rgba(255,61,90,0.12);
  border: 2px solid rgba(255,61,90,0.35);
  color: #ff3d5a;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pn-add-btn:hover { background: rgba(255,61,90,0.22); border-color: rgba(255,61,90,0.55); }
.pn-add-icon { font-size: 14px; }

/* TABLE */
.pn-table-panel {
  background: rgba(8,3,18,0.85);
  border: 2px solid rgba(147,51,234,0.12);
  border-radius: 2px;
  overflow: hidden;
}

.pn-loading, .pn-empty {
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

.pn-empty-icon { font-size: 48px; opacity: 0.15; }

.pn-load-bar {
  width: 160px;
  height: 2px;
  background: rgba(147,51,234,0.1);
  overflow: hidden;
}

.pn-load-bar-fill {
  height: 100%;
  width: 40%;
  background: #a855f7;
  animation: pnLoadScan 1.5s ease-in-out infinite;
}

@keyframes pnLoadScan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}

.pn-load-dots span {
  animation: pnDotFade 1.4s infinite;
  opacity: 0;
}

.pn-load-dots span:nth-child(1) { animation-delay: 0s; }
.pn-load-dots span:nth-child(2) { animation-delay: 0.2s; }
.pn-load-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes pnDotFade {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

.pn-table-wrap { overflow-x: auto; }

.pn-table {
  width: 100%;
  border-collapse: collapse;
}

.pn-table thead th {
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

.pn-table tbody td {
  padding: 10px 14px;
  border-bottom: 1px solid rgba(147,51,234,0.05);
  font-size: 12px;
  color: var(--text-primary);
}

.pn-table tbody tr:hover { background: rgba(147,51,234,0.03); }
.pn-row-active { background: rgba(255,61,90,0.03); }
.pn-row-active:hover { background: rgba(255,61,90,0.07) !important; }

.pn-reason-cell { max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pn-time-cell { font-family: var(--font-mono); font-size: 10px !important; color: var(--text-muted) !important; }

.pn-player-cell { display: flex; align-items: center; gap: 10px; }

.pn-avatar {
  width: 28px;
  height: 28px;
  image-rendering: pixelated;
  border: 1px solid rgba(147,51,234,0.15);
}

.pn-tag {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 2px 8px;
  border: 1px solid;
}

.pn-tag.pn-tag-red { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.pn-tag.pn-tag-orange { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.pn-tag.pn-tag-green { border-color: rgba(16,185,129,0.2); color: #10b981; background: rgba(16,185,129,0.05); }

.pn-actions { display: flex; gap: 4px; }

.pn-act {
  padding: 4px 10px;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  border: 1px solid;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
}

.pn-act.unban { border-color: rgba(16,185,129,0.25); color: #10b981; }
.pn-act.unban:hover { background: rgba(16,185,129,0.1); }
.pn-act.ai { border-color: rgba(168,85,247,0.25); color: #a855f7; }
.pn-act.ai:hover { background: rgba(168,85,247,0.1); }
.pn-act.delete { border-color: rgba(255,61,90,0.25); color: #ff3d5a; }
.pn-act.delete:hover { background: rgba(255,61,90,0.1); }
.pn-act:disabled { opacity: 0.3; cursor: not-allowed; }

/* OVERLAY */
.pn-overlay {
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

.pn-modal {
  background: rgba(8,3,18,0.98);
  border: 2px solid rgba(147,51,234,0.25);
  border-radius: 2px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 50px rgba(0,0,0,0.7), 0 0 40px rgba(147,51,234,0.12);
}

@keyframes pnModalIn {
  from { opacity: 0; transform: scale(0.94) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.pn-modal-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 2px solid rgba(147,51,234,0.1);
}

.pn-modal-cmd {
  font-family: var(--font-mono);
  font-size: 11px;
  color: #06b6d4;
}

.pn-modal-close {
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

.pn-modal-close:hover { background: rgba(255,61,90,0.2); color: #ff3d5a; }

.pn-modal-body { padding: 18px; }

.pn-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 18px;
  border-top: 2px solid rgba(147,51,234,0.06);
}

.pn-modal-btn {
  padding: 8px 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pn-modal-btn.sec {
  background: rgba(147,51,234,0.04);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-muted);
}

.pn-modal-btn.sec:hover { background: rgba(147,51,234,0.1); color: var(--text-primary); }

.pn-modal-btn.danger {
  background: rgba(255,61,90,0.14);
  border: 2px solid rgba(255,61,90,0.3);
  color: #ff3d5a;
}

.pn-modal-btn.danger:hover { background: rgba(255,61,90,0.24); }
.pn-modal-btn.danger:disabled { opacity: 0.4; cursor: not-allowed; }

/* FIELDS */
.pn-field { margin-bottom: 14px; }

.pn-field-cmd {
  display: block;
  font-family: var(--font-mono);
  font-size: 9px;
  color: #06b6d4;
  opacity: 0.55;
  margin-bottom: 5px;
}

.pn-input {
  width: 100%;
  padding: 9px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147,51,234,0.03);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
  outline: none;
}

.pn-input:focus { border-color: rgba(168,85,247,0.4); }

.pn-select {
  width: 100%;
  padding: 9px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147,51,234,0.03);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
  outline: none;
}

.pn-select:focus { border-color: rgba(168,85,247,0.4); }

.pn-textarea {
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

.pn-textarea:focus { border-color: rgba(168,85,247,0.4); }

.pn-radio-row { display: flex; gap: 16px; }

.pn-radio {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-primary);
  cursor: pointer;
}

.pn-radio input { accent-color: #a855f7; }

/* AI EVALUATION */
.pn-eval-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 12px;
  gap: 10px;
}

.pn-eval-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(147,51,234,0.12);
  border-top-color: #a855f7;
  animation: pnSpin 0.7s linear infinite;
}

@keyframes pnSpin { to { transform: rotate(360deg); } }

.pn-eval-result { display: flex; flex-direction: column; gap: 14px; }
.pn-eval-row { display: flex; gap: 20px; }
.pn-eval-row .pn-eval-block { flex: 1; }

.pn-eval-label {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #06b6d4;
  margin-bottom: 6px;
}

.pn-eval-text {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.pn-eval-text :deep(strong) { color: #c084fc; }
.pn-eval-text :deep(code) { background: rgba(168,85,247,0.08); padding: 1px 5px; font-size: 10px; }

.pn-eval-chip {
  display: inline-block;
  padding: 4px 12px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  border: 1px solid;
}

.pn-eval-chip.verdict-ok { border-color: rgba(16,185,129,0.25); color: #10b981; background: rgba(16,185,129,0.06); }
.pn-eval-chip.verdict-warn { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.pn-eval-chip.verdict-danger { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.pn-eval-chip.verdict-info { border-color: rgba(6,182,212,0.2); color: #06b6d4; background: rgba(6,182,212,0.05); }
.pn-eval-chip.pn-eval-chip-act { border-color: rgba(168,85,247,0.25); color: #a855f7; background: rgba(168,85,247,0.06); }

.pn-eval-conf {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  color: #c084fc;
}

.pn-modal-fade-enter-active { animation: pnModalIn 0.22s ease-out; }
.pn-modal-fade-leave-active { animation: pnModalIn 0.16s ease-in reverse; }
</style>
