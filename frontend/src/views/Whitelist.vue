<template>
  <div class="whitelist-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ t('nav.whitelist') }}</h2>
        <span class="entry-count">{{ whitelist.length }} {{ t('whitelist.entries') }}</span>
      </div>

      <button class="add-whitelist-btn" @click="showAddDialog = true">
        <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
        {{ t('whitelist.addToWhitelist') }}
      </button>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>{{ t('common.loading') }}...</span>
      </div>

      <div v-else-if="whitelist.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="48" height="48" class="empty-icon">
          <path fill="currentColor" d="M12 17c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm6-9h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6z"/>
        </svg>
        <p>{{ t('whitelist.noEntries') }}</p>
      </div>

      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ t('common.player') }}</th>
              <th>{{ t('common.uuid') }}</th>
              <th>{{ t('common.reason') }}</th>
              <th>{{ t('whitelist.addedBy') }}</th>
              <th>{{ t('whitelist.addedAt') }}</th>
              <th>{{ t('whitelist.status') }}</th>
              <th>{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in whitelist"
              :key="item.id"
              :class="['data-row', { inactive: !item.active }]"
            >
              <td>
                <div class="player-cell">
                  <img
                    :src="`https://mc-heads.net/avatar/${item.playerName}/40`"
                    :alt="item.playerName"
                    class="player-avatar"
                  />
                  <span class="player-name">{{ item.playerName }}</span>
                </div>
              </td>
              <td class="uuid-cell">{{ item.uuid }}</td>
              <td class="reason-cell">{{ item.reason }}</td>
              <td>{{ item.addedBy || '-' }}</td>
              <td class="time-cell">{{ formatTime(item.addedTime) }}</td>
              <td>
                <span :class="['status-badge', item.active ? 'active' : 'inactive']">
                  {{ item.active ? t('players.active') : t('common.disable') }}
                </span>
              </td>
              <td>
                <div class="action-group">
                  <button
                    v-if="item.active"
                    class="action-btn disable-btn"
                    @click="handleRemove(item)"
                  >
                    {{ t('common.disable') }}
                  </button>
                  <button
                    v-else
                    class="action-btn enable-btn"
                    @click="handleEnable(item)"
                  >
                    {{ t('common.enable') }}
                  </button>
                  <button
                    class="action-btn delete-btn"
                    @click="handleDelete(item.id)"
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

    <!-- Add Dialog -->
    <transition name="modal-fade">
      <div v-if="showAddDialog" class="modal-overlay" @click.self="showAddDialog = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ t('whitelist.addToWhitelist') }}</h3>
            <button class="close-btn" @click="showAddDialog = false">×</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">{{ t('punishments.playerName') }}</label>
              <input
                v-model="addForm.playerName"
                :placeholder="t('punishments.playerName')"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('punishments.playerUUID') }}</label>
              <input
                v-model="addForm.uuid"
                :placeholder="t('punishments.playerUUID')"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label class="form-label">{{ t('whitelist.reason') }}</label>
              <textarea
                v-model="addForm.reason"
                :placeholder="t('whitelist.whitelistReason')"
                class="form-textarea"
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn secondary" @click="showAddDialog = false">
              {{ t('common.cancel') }}
            </button>
            <button class="btn primary" @click="handleAdd" :disabled="adding">
              {{ adding ? '...' : t('common.add') }}
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
    const addForm = ref({
      playerName: '',
      uuid: '',
      reason: ''
    })

    let controller = null

    const fetchWhitelist = async () => {
      loading.value = true
      try {
        controller = new AbortController()
        whitelist.value = await whitelistApi.getAll({ signal: controller.signal })
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error('Failed to fetch whitelist:', error)
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
        await ElMessageBox.confirm(
          `${t('whitelist.confirmDisable')} ${row.playerName}?`,
          t('common.confirm'),
          {
            confirmButtonText: t('common.disable'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
        )
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
        await whitelistApi.add({
          playerName: row.playerName,
          uuid: row.uuid,
          reason: row.reason || 'Re-enabled'
        })
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
        await ElMessageBox.confirm(
          t('common.confirm'),
          t('common.delete'),
          {
            confirmButtonText: t('common.delete'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
        )
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

    onMounted(() => {
      fetchWhitelist()
    })

    onUnmounted(() => {
      if (controller) {
        controller.abort()
      }
    })

    return {
      whitelist,
      loading,
      showAddDialog,
      adding,
      addForm,
      formatTime,
      handleAdd,
      handleRemove,
      handleEnable,
      handleDelete,
      t
    }
  }
}
</script>

<style scoped>
.whitelist-container {
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

.entry-count {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}

.add-whitelist-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  background: rgba(46, 204, 113, 0.12);
  border: 1px solid rgba(46, 204, 113, 0.3);
  border-radius: var(--radius-sm);
  color: #2ECC71;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-whitelist-btn:hover {
  background: rgba(46, 204, 113, 0.2);
  border-color: rgba(46, 204, 113, 0.5);
  box-shadow: 0 0 16px rgba(46, 204, 113, 0.15);
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

.data-table tbody tr.inactive {
  opacity: 0.55;
}

.data-table tbody tr.inactive:hover {
  opacity: 0.75;
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

.uuid-cell {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
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

.status-badge.active {
  background: rgba(46, 204, 113, 0.1);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.2);
}

.status-badge.inactive {
  background: rgba(155, 89, 182, 0.12);
  color: #9B59B6;
  border: 1px solid rgba(155, 89, 182, 0.25);
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

.action-btn.enable-btn {
  border-color: rgba(46, 204, 113, 0.3);
  color: #2ECC71;
}

.action-btn.enable-btn:hover {
  background: rgba(46, 204, 113, 0.12);
}

.action-btn.disable-btn {
  border-color: rgba(255, 140, 0, 0.3);
  color: #FF8C00;
}

.action-btn.disable-btn:hover {
  background: rgba(255, 140, 0, 0.12);
}

.action-btn.delete-btn {
  border-color: rgba(231, 76, 60, 0.3);
  color: #E74C3C;
  padding: 6px 10px;
}

.action-btn.delete-btn:hover {
  background: rgba(231, 76, 60, 0.12);
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
  max-width: 480px;
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
  background: rgba(46, 204, 113, 0.12);
  border-color: rgba(46, 204, 113, 0.3);
  color: #2ECC71;
}

.btn.primary:hover {
  background: rgba(46, 204, 113, 0.2);
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

.form-textarea {
  min-height: 80px;
  resize: vertical;
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
