<template>
  <div class="wl-terminal">
    <div class="wl-header">
      <div class="wl-prompt">
        <span class="wl-prompt-sign">root@acs:~$</span>
        <span class="wl-prompt-cmd">./whitelist.query --all</span>
        <span class="wl-cursor">_</span>
      </div>
      <button class="wl-add-btn" @click="showAddDialog = true">
        <span class="wl-add-icon">+</span> ADD TO WHITELIST
      </button>
    </div>

    <div class="wl-table-panel">
      <div v-if="loading" class="wl-loading">
        <div class="wl-load-bar"><div class="wl-load-bar-fill"></div></div>
        <span class="wl-load-text">QUERYING <span class="wl-load-dots"><span>.</span><span>.</span><span>.</span></span></span>
      </div>

      <div v-else-if="whitelist.length === 0" class="wl-empty">
        <div class="wl-empty-icon">⊡</div>
        <span>NO WHITELIST ENTRIES</span>
      </div>

      <div v-else class="wl-table-wrap">
        <table class="wl-table">
          <thead>
            <tr>
              <th>$ player</th>
              <th>$ uuid</th>
              <th>$ reason</th>
              <th>$ added_by</th>
              <th>$ added_at</th>
              <th>$ status</th>
              <th>$ actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in whitelist" :key="item.id" :class="{ 'wl-row-inactive': !item.active }">
              <td>
                <div class="wl-player-cell">
                  <img :src="`https://mc-heads.net/avatar/${item.playerName}/32`" class="wl-avatar" />
                  <span>{{ item.playerName }}</span>
                </div>
              </td>
              <td class="wl-uuid-cell">{{ item.uuid }}</td>
              <td class="wl-reason-cell">{{ item.reason }}</td>
              <td>{{ item.addedBy || '-' }}</td>
              <td class="wl-time-cell">{{ formatTime(item.addedTime) }}</td>
              <td>
                <span :class="['wl-tag', item.active ? 'wl-tag-green' : 'wl-tag-purple']">
                  {{ item.active ? t('players.active') : t('common.disable') }}
                </span>
              </td>
              <td>
                <div class="wl-actions">
                  <button v-if="item.active" class="wl-act disable" @click="handleRemove(item)">{{ t('common.disable') }}</button>
                  <button v-else class="wl-act enable" @click="handleEnable(item)">{{ t('common.enable') }}</button>
                  <button class="wl-act delete" @click="handleDelete(item.id)">DEL</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <transition name="wl-modal-fade">
      <div v-if="showAddDialog" class="wl-overlay" @click.self="showAddDialog = false">
        <div class="wl-modal">
          <div class="wl-modal-top">
            <span class="wl-modal-cmd">> whitelist.add</span>
            <button class="wl-modal-close" @click="showAddDialog = false">┼</button>
          </div>
          <div class="wl-modal-body">
            <div class="wl-field">
              <label class="wl-field-label">$ player.name</label>
              <input v-model="addForm.playerName" :placeholder="t('punishments.playerName')" class="wl-input" />
            </div>
            <div class="wl-field">
              <label class="wl-field-label">$ player.uuid</label>
              <input v-model="addForm.uuid" :placeholder="t('punishments.playerUUID')" class="wl-input" />
            </div>
            <div class="wl-field">
              <label class="wl-field-label">$ reason</label>
              <textarea v-model="addForm.reason" :placeholder="t('whitelist.whitelistReason')" class="wl-textarea"></textarea>
            </div>
          </div>
          <div class="wl-modal-actions">
            <button class="wl-btn-cancel" @click="showAddDialog = false">{{ t('common.cancel') }}</button>
            <button class="wl-btn-confirm" @click="handleAdd" :disabled="adding">{{ adding ? t('common.loading') : t('common.add') }}</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { whitelistApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EventBus, Events } from '../utils/eventBus'

export default {
  name: 'Whitelist',
  setup() {
    const { t } = useI18n()
    
    const whitelist = ref([])
    const loading = ref(false)
    const showAddDialog = ref(false)
    const adding = ref(false)
    const addForm = ref({ playerName: '', uuid: '', reason: '' })

    const fetchWhitelist = async () => {
      loading.value = true
      try {
        whitelist.value = await whitelistApi.getAll()
      } catch (error) {
        console.error('Failed to fetch whitelist:', error)
        ElMessage.error(t('common.error'))
      } finally {
        loading.value = false
      }
    }

    const formatTime = (timestamp) => {
      if (!timestamp) return '-'
      return new Date(timestamp).toLocaleString()
    }

    const handleAdd = async () => {
      if (!addForm.value.playerName || !addForm.value.uuid) {
        ElMessage.warning(t('common.warning'))
        return
      }
      adding.value = true
      try {
        await whitelistApi.add(addForm.value)
        ElMessage.success(t('common.success'))
        showAddDialog.value = false
        addForm.value = { playerName: '', uuid: '', reason: '' }
        fetchWhitelist()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        console.error('Failed to add:', error)
        ElMessage.error(t('common.error'))
      } finally {
        adding.value = false
      }
    }

    const handleRemove = async (row) => {
      try {
        await ElMessageBox.confirm(`${t('whitelist.confirmDisable')} ${row.playerName}?`, t('common.confirm'), { confirmButtonText: t('common.disable'), cancelButtonText: t('common.cancel'), type: 'warning' })
        await whitelistApi.remove(row.uuid)
        ElMessage.success(t('common.success'))
        fetchWhitelist()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to disable:', error)
          ElMessage.error(t('common.error'))
        }
      }
    }

    const handleEnable = async (row) => {
      try {
        await whitelistApi.add({ playerName: row.playerName, uuid: row.uuid, reason: row.reason || 'Re-enabled' })
        ElMessage.success(t('common.success'))
        fetchWhitelist()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        console.error('Failed to enable:', error)
        ElMessage.error(t('common.error'))
      }
    }

    const handleDelete = async (id) => {
      try {
        await ElMessageBox.confirm(t('common.confirm'), t('common.delete'), { confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel'), type: 'warning' })
        await whitelistApi.delete(id)
        ElMessage.success(t('common.success'))
        fetchWhitelist()
        EventBus.emit(Events.STATS_CHANGED)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to delete:', error)
          ElMessage.error(t('common.error'))
        }
      }
    }

    onMounted(() => { fetchWhitelist() })

    return {
      whitelist, loading, showAddDialog, adding, addForm,
      formatTime, handleAdd, handleRemove, handleEnable, handleDelete, t
    }
  }
}
</script>

<style scoped>
.wl-terminal { display: flex; flex-direction: column; gap: 20px; }

.wl-header { display: flex; align-items: center; justify-content: space-between; }
.wl-prompt { font-family: var(--font-mono); font-size: 13px; }
.wl-prompt-sign { color: #06b6d4; }
.wl-prompt-cmd { color: #c084fc; margin-left: 8px; }
.wl-cursor { color: #a855f7; animation: wlBlink 1s step-end infinite; }
@keyframes wlBlink { 0%,100%{opacity:1} 50%{opacity:0} }

.wl-add-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 20px;
  font-family: var(--font-mono); font-size: 11px; font-weight: 700;
  background: linear-gradient(180deg, #a855f7, #7c3aed);
  border: 2px solid rgba(147,51,234,0.4); border-radius: 2px;
  color: #fff; cursor: pointer; transition: all 0.2s ease;
  letter-spacing: 1px;
}
.wl-add-btn:hover { background: linear-gradient(180deg, #9333ea, #6d28d9); border-color: rgba(168,85,247,0.6); box-shadow: 0 0 16px rgba(147,51,234,0.35); }
.wl-add-icon { font-size: 16px; font-weight: 700; }

/* TABLE */
.wl-table-panel {
  background: rgba(6,2,16,0.7);
  border: 2px solid rgba(147,51,234,0.18); border-radius: 2px;
  overflow: hidden;
}
.wl-table-wrap { overflow-x: auto; }
.wl-table { width: 100%; border-collapse: collapse; }
.wl-table thead th {
  font-family: var(--font-mono); font-size: 10px; font-weight: 600;
  color: #06b6d4; text-align: left; padding: 12px 14px;
  border-bottom: 2px solid rgba(147,51,234,0.18); text-transform: uppercase;
  letter-spacing: 0.5px; background: rgba(10,4,22,0.6);
}
.wl-table tbody td {
  padding: 12px 14px; font-size: 13px;
  border-bottom: 1px solid rgba(147,51,234,0.06); color: var(--text-primary);
  font-family: var(--font-mono);
}
.wl-table tbody tr { transition: background 0.15s ease; }
.wl-table tbody tr:hover { background: rgba(147,51,234,0.06); }
.wl-row-inactive { opacity: 0.55; }
.wl-player-cell { display: flex; align-items: center; gap: 10px; }
.wl-avatar { width: 32px; height: 32px; border-radius: 2px; image-rendering: pixelated; border: 2px solid rgba(147,51,234,0.25); }
.wl-uuid-cell { font-size: 11px; color: var(--text-muted); }
.wl-reason-cell { max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wl-time-cell { font-size: 12px; color: var(--text-muted); }

.wl-tag {
  display: inline-block; padding: 3px 10px; font-size: 10px; font-weight: 700;
  border-radius: 2px; text-transform: uppercase; letter-spacing: 0.5px;
  font-family: var(--font-mono);
}
.wl-tag-green { background: rgba(16,185,129,0.12); color: #10b981; border: 1px solid rgba(16,185,129,0.3); }
.wl-tag-purple { background: rgba(168,85,247,0.12); color: #a855f7; border: 1px solid rgba(168,85,247,0.3); }

.wl-actions { display: flex; gap: 6px; }
.wl-act {
  padding: 4px 10px; font-family: var(--font-mono); font-size: 10px; font-weight: 600;
  border-radius: 2px; cursor: pointer; transition: all 0.15s ease; border: 1px solid;
}
.wl-act.enable { background: rgba(16,185,129,0.1); color: #10b981; border-color: rgba(16,185,129,0.25); }
.wl-act.enable:hover { background: rgba(16,185,129,0.22); }
.wl-act.disable { background: rgba(245,158,11,0.1); color: #f59e0b; border-color: rgba(245,158,11,0.25); }
.wl-act.disable:hover { background: rgba(245,158,11,0.22); }
.wl-act.delete { background: rgba(255,61,90,0.1); color: #ff3d5a; border-color: rgba(255,61,90,0.25); }
.wl-act.delete:hover { background: rgba(255,61,90,0.22); }

/* LOADING & EMPTY */
.wl-loading { display: flex; flex-direction: column; align-items: center; gap: 14px; padding: 60px 20px; }
.wl-load-bar { width: 200px; height: 3px; background: rgba(147,51,234,0.1); border-radius: 2px; overflow: hidden; }
.wl-load-bar-fill { height: 100%; width: 40%; background: linear-gradient(90deg, transparent, #a855f7, #06b6d4, #a855f7, transparent); animation: wlLoadScan 1.6s ease-in-out infinite; border-radius: 2px; }
@keyframes wlLoadScan { 0%{transform:translateX(-100%)} 100%{transform:translateX(350%)} }
.wl-load-text { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); letter-spacing: 2px; }
.wl-load-dots span { animation: wlDotFade 1.2s infinite; }
.wl-load-dots span:nth-child(2) { animation-delay: 0.2s; }
.wl-load-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes wlDotFade { 0%,100%{opacity:0.2} 50%{opacity:1} }
.wl-empty { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 60px 20px; }
.wl-empty-icon { font-size: 40px; color: rgba(168,85,247,0.3); }
.wl-empty span { font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); letter-spacing: 2px; }

/* MODAL */
.wl-overlay {
  position: fixed; inset: 0; background: rgba(4,0,10,0.85); backdrop-filter: blur(10px);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.wl-modal {
  background: rgba(8,3,20,0.98); border: 2px solid rgba(147,51,234,0.3); border-radius: 2px;
  width: 100%; max-width: 420px; box-shadow: 0 14px 52px rgba(0,0,0,0.75), 0 0 40px rgba(147,51,234,0.12);
  overflow: hidden;
}
.wl-modal-top { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; border-bottom: 2px solid rgba(147,51,234,0.18); }
.wl-modal-cmd { font-family: var(--font-mono); font-size: 12px; color: #06b6d4; }
.wl-modal-close { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: rgba(255,61,90,0.08); border: 2px solid rgba(255,61,90,0.2); border-radius: 2px; color: var(--text-muted); font-size: 16px; cursor: pointer; transition: all 0.15s ease; }
.wl-modal-close:hover { background: rgba(255,61,90,0.2); color: #ff3d5a; }
.wl-modal-body { padding: 20px; }
.wl-field { margin-bottom: 18px; }
.wl-field-label { display: block; font-family: var(--font-mono); font-size: 10px; font-weight: 600; color: #06b6d4; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
.wl-input {
  width: 100%; padding: 10px 14px; font-family: var(--font-mono); font-size: 13px;
  background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.15); border-radius: 2px;
  color: var(--text-primary); transition: all 0.2s ease;
}
.wl-input:focus { outline: none; border-color: rgba(168,85,247,0.4); box-shadow: 0 0 10px rgba(147,51,234,0.1); }
.wl-textarea {
  width: 100%; padding: 10px 14px; font-family: var(--font-mono); font-size: 13px;
  background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.15); border-radius: 2px;
  color: var(--text-primary); min-height: 60px; resize: vertical; transition: all 0.2s ease;
}
.wl-textarea:focus { outline: none; border-color: rgba(168,85,247,0.4); box-shadow: 0 0 10px rgba(147,51,234,0.1); }
.wl-modal-actions { display: flex; justify-content: flex-end; gap: 10px; padding: 14px 18px; border-top: 2px solid rgba(147,51,234,0.18); }
.wl-btn-cancel { padding: 8px 18px; font-family: var(--font-mono); font-size: 11px; font-weight: 500; background: rgba(147,51,234,0.06); border: 2px solid rgba(147,51,234,0.18); border-radius: 2px; color: var(--text-muted); cursor: pointer; transition: all 0.2s ease; }
.wl-btn-cancel:hover { background: rgba(147,51,234,0.14); color: var(--text-primary); }
.wl-btn-confirm { padding: 8px 18px; font-family: var(--font-mono); font-size: 11px; font-weight: 700; background: linear-gradient(180deg, #a855f7, #7c3aed); border: 2px solid rgba(147,51,234,0.4); border-radius: 2px; color: #fff; cursor: pointer; transition: all 0.2s ease; letter-spacing: 0.5px; }
.wl-btn-confirm:hover:not(:disabled) { background: linear-gradient(180deg, #9333ea, #6d28d9); }
.wl-btn-confirm:disabled { opacity: 0.4; cursor: not-allowed; }

.wl-modal-fade-enter-active { animation: wlModalIn 0.25s ease-out; }
.wl-modal-fade-leave-active { animation: wlModalIn 0.18s ease-in reverse; }
@keyframes wlModalIn { from{opacity:0;transform:scale(0.95)} to{opacity:1;transform:scale(1)} }
</style>
