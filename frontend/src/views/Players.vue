<template>
  <div class="players-container">
    <div class="players-header">
      <div class="header-info">
        <h2 class="page-heading">{{ t('nav.players') }}</h2>
        <span class="entity-count">{{ filteredPlayers.length }} {{ t('players.entitiesIndexed') }}</span>
      </div>

      <div class="header-actions">
        <button class="action-btn export-btn" @click="exportCSV">
          <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
          {{ t('common.csv') }}
        </button>
      </div>
    </div>

    <div class="toolbar">
      <div class="filter-group">
        <button :class="['filter-chip', { active: filter === 'all' }]" @click="filter = 'all'">
          {{ t('players.all') }}
        </button>
        <button :class="['filter-chip', { active: filter === 'high' }]" @click="filter = 'high'">
          <span class="chip-indicator high"></span>
          {{ t('players.highRisk') }}
        </button>
        <button :class="['filter-chip', { active: filter === 'banned' }]" @click="filter = 'banned'">
          <span class="chip-indicator banned"></span>
          {{ t('players.banned') }}
        </button>
      </div>

      <div class="search-box">
        <svg viewBox="0 0 24 24" width="16" height="16" class="search-icon">
          <path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="t('players.searchPlaceholder')"
          class="search-input"
        />
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>{{ t('common.loading') }}...</span>
    </div>

    <div v-else-if="filteredPlayers.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" width="48" height="48" class="empty-icon">
        <path fill="currentColor" d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/>
      </svg>
      <p>{{ t('players.noEntitiesFound') }}</p>
    </div>

    <div v-else class="player-grid">
      <transition-group name="card-list" tag="div" class="grid-wrapper">
        <div
          v-for="(player, idx) in paginatedPlayers"
          :key="player.id"
          :class="['player-card', getRiskTier(player.riskScore)]"
          @click="showPlayerDetail(player)"
          :style="{ animationDelay: (idx * 0.05) + 's' }"
        >
          <div class="card-glow"></div>

          <div class="card-header">
            <div class="avatar-section">
              <img
                :src="`https://mc-heads.net/avatar/${player.playerName}/64`"
                :alt="player.playerName"
                class="player-avatar"
              />
              <div :class="['avatar-ring', getRiskTier(player.riskScore)]"></div>
            </div>

            <div class="player-identity">
              <h3 class="player-name">{{ player.playerName }}</h3>
              <span class="player-uuid">{{ player.uuid ? player.uuid.substring(0, 12) + '...' : '-' }}</span>
            </div>

            <div :class="['risk-badge', getRiskTier(player.riskScore)]">
              {{ getRiskText(player.riskScore) }}
            </div>
          </div>

          <div class="card-stats">
            <div class="stat-item">
              <span class="stat-label">{{ t('players.riskScore') }}</span>
              <span class="stat-value" :style="{ color: getRiskColor(player.riskScore) }">{{ player.riskScore }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">{{ t('players.kickCount') }}</span>
              <span class="stat-value">{{ player.kickCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">{{ t('players.lastSeen') }}</span>
              <span class="stat-value stat-time">{{ formatTimeShort(player.lastSeen) }}</span>
            </div>
          </div>

          <div class="risk-bar-container">
            <div class="risk-bar-track">
              <div
                class="risk-bar-fill"
                :style="{ width: Math.min(player.riskScore * 5, 100) + '%', background: getRiskGradient(player.riskScore) }"
              ></div>
            </div>
          </div>

          <div class="card-footer">
            <button class="card-action view" @click.stop="showPlayerDetail(player)">
              <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
            </button>
            <button class="card-action delete" @click.stop="handleDelete(player.id)">
              <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
          </div>
        </div>
      </transition-group>
    </div>

    <div v-if="filteredPlayers.length > 0" class="pagination">
      <div class="pag-left">
        <span class="record-count">{{ filteredPlayers.length }} {{ t('players.records') }}</span>
      </div>

      <div class="pag-center">
        <button class="pag-btn" :disabled="currentPage === 1" @click="currentPage--">
          <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z"/></svg>
        </button>
        <span class="page-num">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pag-btn" :disabled="currentPage >= totalPages" @click="currentPage++">
          <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/></svg>
        </button>
      </div>

      <select v-model="pageSize" class="page-size-select">
        <option :value="12">12</option>
        <option :value="24">24</option>
        <option :value="48">48</option>
      </select>
    </div>

    <!-- Player Detail Modal -->
    <transition name="modal-fade">
      <div v-if="detailDialogVisible" class="modal-overlay" @click.self="detailDialogVisible = false">
        <div class="modal-content">
          <div class="modal-header">
            <div class="modal-player-info" v-if="selectedPlayer">
              <img :src="`https://mc-heads.net/avatar/${selectedPlayer.playerName}/80`" class="modal-avatar" />
              <div>
                <h3 class="modal-name">{{ selectedPlayer.playerName }}</h3>
                <span class="modal-uuid">{{ selectedPlayer.uuid }}</span>
              </div>
            </div>
            <button class="modal-close" @click="detailDialogVisible = false">×</button>
          </div>

          <div class="modal-body" v-if="selectedPlayer">
            <div class="risk-gauge-section">
              <div class="gauge-wrapper">
                <div class="gauge-bar">
                  <div
                    class="gauge-fill"
                    :style="{ width: Math.min(selectedPlayer.riskScore * 5, 100) + '%', background: getRiskGradient(selectedPlayer.riskScore) }"
                  ></div>
                </div>
                <div class="gauge-labels">
                  <span>0</span>
                  <span :style="{ color: getRiskColor(selectedPlayer.riskScore) }">{{ selectedPlayer.riskScore }}</span>
                  <span>20+</span>
                </div>
              </div>

              <div class="meta-info">
                <div class="meta-item">
                  <span class="meta-label">{{ t('players.kickCount') }}</span>
                  <span class="meta-value">{{ selectedPlayer.kickCount || 0 }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">{{ t('players.banStatus') }}</span>
                  <span :class="['meta-value', banInfo.banned ? 'danger' : 'safe']">
                    {{ banInfo.banned ? t('players.banned') : t('players.notBanned') }}
                  </span>
                </div>
              </div>
            </div>

            <div class="tab-navigation">
              <button :class="['tab-btn', { active: activeTab === 'basic' }]" @click="activeTab = 'basic'">
                {{ t('players.infoTab') }}
              </button>
              <button :class="['tab-btn', { active: activeTab === 'cheats' }]" @click="activeTab = 'cheats'; loadCheatRecords()">
                {{ t('players.cheatsLog') }}
              </button>
              <button :class="['tab-btn', { active: activeTab === 'punishments' }]" @click="activeTab = 'punishments'; loadPunishments()">
                {{ t('players.bansLog') }}
              </button>
              <button :class="['tab-btn', { active: activeTab === 'ai' }]" @click="activeTab = 'ai'; analyzePlayerCheat()">
                {{ t('ai.aiAnalysis') }}
              </button>
            </div>

            <div class="tab-content">
              <div v-show="activeTab === 'basic'" class="info-grid">
                <div class="info-cell">
                  <span class="info-label">{{ t('players.playerName') }}</span>
                  <span class="info-value">{{ selectedPlayer.playerName }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">{{ t('players.riskScore') }}</span>
                  <span :class="['info-value', getRiskClass(selectedPlayer.riskScore)]">
                    {{ selectedPlayer.riskScore }} ({{ getRiskText(selectedPlayer.riskScore) }})
                  </span>
                </div>
                <div class="info-cell">
                  <span class="info-label">{{ t('players.kickCount') }}</span>
                  <span class="info-value">{{ selectedPlayer.kickCount || 0 }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">{{ t('players.lastSeen') }}</span>
                  <span class="info-value">{{ formatTime(selectedPlayer.lastSeen) }}</span>
                </div>
              </div>

              <div v-show="activeTab === 'cheats'" class="cheats-log">
                <div v-if="loadingRecords" class="loading-text">{{ t('players.loadingCheats') }}...</div>
                <div v-else-if="playerCheatRecords.length === 0" class="empty-text">{{ t('players.noData') }}</div>
                <div v-else class="log-list">
                  <div
                    v-for="record in playerCheatRecords"
                    :key="record.id"
                    :class="['log-entry', record.severity >= 3 ? 'high-severity' : '']"
                  >
                    <span class="log-time">[{{ formatTimeShort(record.detectionTime) }}]</span>
                    <span :class="['log-tag', getCheatClass(record.cheatType)]">{{ record.cheatType }}</span>
                    <span :class="['log-level', getSeverityClass(record.severity)]">Lv.{{ record.severity }}</span>
                    <span class="log-message">{{ record.details }}</span>
                  </div>
                </div>
              </div>

              <div v-show="activeTab === 'punishments'" class="punishments-log">
                <div v-if="loadingPunishments" class="loading-text">{{ t('players.loadingBans') }}...</div>
                <div v-else-if="playerPunishments.length === 0" class="empty-text">{{ t('players.noData') }}</div>
                <div v-else class="ban-list">
                  <div v-for="punishment in playerPunishments" :key="punishment.id" class="ban-entry">
                    <span :class="['ban-type', punishment.active ? 'active' : 'expired']">
                      {{ punishment.punishmentType === 'PERMANENT' ? 'PERM' : 'TEMP' }}
                    </span>
                    <span class="ban-reason">{{ punishment.reason }}</span>
                  </div>
                </div>
              </div>

              <div v-show="activeTab === 'ai'" class="ai-panel">
                <div v-if="aiPlayerLoading" class="loading-text">
                  <div class="ai-spinner"></div>
                  Analyzing {{ selectedPlayer?.playerName }}...
                </div>
                <div v-else-if="!aiPlayerResult" class="empty-text">{{ t('players.pressAnalyze') }}</div>
                <div v-else class="ai-result">
                  <div v-if="aiPlayerResult.analysis" class="ai-block">
                    <div class="ai-block-title">{{ t('dashboard.analysis') }}</div>
                    <div class="ai-block-text" v-html="renderAiText(aiPlayerResult.analysis)"></div>
                  </div>
                  <div class="ai-row">
                    <div v-if="aiPlayerResult.verdict" class="ai-block half">
                      <div class="ai-block-title">{{ t('dashboard.verdict') }}</div>
                      <span :class="['verdict-badge', getVerdictClass(aiPlayerResult.verdict)]">
                        {{ aiPlayerResult.verdict }}
                      </span>
                    </div>
                    <div v-if="aiPlayerResult.confidence" class="ai-block half">
                      <div class="ai-block-title">{{ t('players.confidence') }}</div>
                      <span class="confidence-value">{{ (aiPlayerResult.confidence * 100).toFixed(0) }}%</span>
                    </div>
                  </div>
                  <div v-if="aiPlayerResult.suggestedAction" class="ai-block">
                    <div class="ai-block-title">{{ t('dashboard.action') }}</div>
                    <span :class="['action-badge', getActionClass(aiPlayerResult.suggestedAction)]">
                      {{ aiPlayerResult.suggestedAction }}
                    </span>
                  </div>
                  <div v-if="aiPlayerResult.reasoning" class="ai-block">
                    <div class="ai-block-title">{{ t('dashboard.reasoning') }}</div>
                    <div class="ai-block-text" v-html="renderAiText(aiPlayerResult.reasoning)"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="modal-btn secondary" @click="detailDialogVisible = false">{{ t('common.close') }}</button>
            <button v-if="!banInfo.banned" class="modal-btn danger" @click="handleBanPlayer">{{ t('players.ban') }}</button>
            <button v-else class="modal-btn success" @click="handleUnbanPlayer">{{ t('players.unban') }}</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Ban Modal -->
    <transition name="modal-fade">
      <div v-if="banDialogVisible" class="modal-overlay" @click.self="banDialogVisible = false">
        <div class="modal-content modal-small">
          <div class="modal-header">
            <h3 class="modal-title">{{ t('players.banPlayer') }} - {{ selectedPlayer?.playerName }}</h3>
            <button class="modal-close" @click="banDialogVisible = false">×</button>
          </div>

          <div class="modal-body">
            <div class="form-field">
              <label class="field-label">{{ t('players.playerName') }}</label>
              <input :value="selectedPlayer?.playerName" disabled class="field-input disabled" />
            </div>

            <div class="form-field">
              <label class="field-label">{{ t('players.banType') }}</label>
              <div class="radio-group">
                <label class="radio-option">
                  <input type="radio" value="PERMANENT" v-model="banForm.type" />
                  <span>{{ t('players.permanent') }}</span>
                </label>
                <label class="radio-option">
                  <input type="radio" value="TEMPORARY" v-model="banForm.type" />
                  <span>{{ t('players.temporary') }}</span>
                </label>
              </div>
            </div>

            <div v-if="banForm.type === 'TEMPORARY'" class="form-field">
              <label class="field-label">{{ t('players.durationDays') }}</label>
              <input type="number" v-model="banForm.duration" min="1" max="365" class="field-input" />
            </div>

            <div class="form-field">
              <label class="field-label">{{ t('reports.banReason') }}</label>
              <textarea v-model="banForm.reason" :placeholder="t('reports.banReason')" class="field-textarea"></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button class="modal-btn secondary" @click="banDialogVisible = false">{{ t('common.cancel') }}</button>
            <button class="modal-btn danger" @click="confirmBan">{{ t('players.execute') }}</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { playerApi, cheatApi, punishmentApi, aiApi } from '../api'
import { renderAiText } from '../utils/helpers'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EventBus, Events } from '../utils/eventBus'

export default {
  name: 'Players',
  setup() {
    const { t } = useI18n()

    const players = ref([])
    const searchQuery = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const loading = ref(false)
    const filter = ref('all')
    const detailDialogVisible = ref(false)
    const selectedPlayer = ref(null)
    const activeTab = ref('basic')
    const playerCheatRecords = ref([])
    const playerPunishments = ref([])
    const loadingRecords = ref(false)
    const loadingPunishments = ref(false)
    const banInfo = ref({ banned: false })
    const banDialogVisible = ref(false)
    const banForm = ref({ type: 'PERMANENT', duration: 7, reason: '' })
    const aiPlayerLoading = ref(false)
    const aiPlayerResult = ref(null)

    let controller = null

    const fetchPlayers = async () => {
      loading.value = true
      try {
        controller = new AbortController()
        players.value = await playerApi.getAll({ signal: controller.signal })
      } catch (e) {
        if (e.name !== 'AbortError') {
          ElMessage.error(t('common.error'))
        }
      } finally {
        loading.value = false
      }
    }

    const filteredPlayers = computed(() => {
      let list = players.value

      if (searchQuery.value) {
        list = list.filter(p =>
          p.playerName.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
      }

      if (filter.value === 'high') {
        list = list.filter(p => p.riskScore >= 10)
      }

      if (filter.value === 'banned') {
        list = list.filter(p => p.banned === true || p.riskScore >= 100)
      }

      return [...list].sort((a, b) => b.riskScore - a.riskScore)
    })

    const totalPages = computed(() =>
      Math.ceil(filteredPlayers.value.length / pageSize.value)
    )

    const paginatedPlayers = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      return filteredPlayers.value.slice(start, start + pageSize.value)
    })

    const getRiskTier = (score) => {
      if (score >= 10) return 'high'
      if (score >= 5) return 'medium'
      return 'low'
    }

    const getRiskClass = (score) => {
      if (score >= 10) return 'text-danger'
      if (score >= 5) return 'text-warning'
      return 'text-success'
    }

    const getRiskText = (score) => {
      if (score >= 10) return t('players.high')
      if (score >= 5) return t('players.medium')
      return t('players.low')
    }

    const getRiskColor = (score) => {
      if (score >= 10) return '#E74C3C'
      if (score >= 5) return '#FF8C00'
      return '#2ECC71'
    }

    const getRiskGradient = (score) => {
      const color = getRiskColor(score)
      return `linear-gradient(90deg, ${color}, ${color}88)`
    }

    const getCheatClass = (type) => {
      const map = {
        '飞行作弊': 'fly',
        '速度作弊': 'speed',
        '自动点击作弊': 'auto',
        '杀戮光环': 'kill'
      }
      return map[type] || 'default'
    }

    const getSeverityClass = (severity) => {
      if (severity >= 4) return 'critical'
      if (severity >= 3) return 'high'
      if (severity >= 2) return 'medium'
      return 'low'
    }

    const formatTime = (ts) => {
      if (!ts) return '-'
      return new Date(ts).toLocaleString()
    }

    const formatTimeShort = (ts) => {
      if (!ts) return '-'
      const d = new Date(ts)
      return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
    }

    const exportCSV = () => {
      const headers = ['playerName', 'uuid', 'riskScore', 'kickCount', 'lastSeen']
      const rows = filteredPlayers.value.map(p => headers.map(h => p[h] || ''))
      const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
      const blob = new Blob([csv], { type: 'text/csv' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = 'players.csv'
      a.click()
    }

    const showPlayerDetail = async (player) => {
      selectedPlayer.value = player
      detailDialogVisible.value = true
      activeTab.value = 'basic'
      loadingRecords.value = true
      loadingPunishments.value = true
      aiPlayerResult.value = null

      try {
        const [records, punishments, status] = await Promise.all([
          cheatApi.getByPlayerUuid(player.uuid),
          punishmentApi.getByUuid(player.uuid),
          punishmentApi.checkBanStatus(player.uuid)
        ])
        playerCheatRecords.value = records
        playerPunishments.value = punishments
        banInfo.value = status
      } catch (e) {
        console.error(e)
      } finally {
        loadingRecords.value = false
        loadingPunishments.value = false
      }
    }

    const loadCheatRecords = () => {
      if (selectedPlayer.value && playerCheatRecords.value.length === 0) {
        loadingRecords.value = true
        cheatApi.getByPlayerUuid(selectedPlayer.value.uuid)
          .then(records => {
            playerCheatRecords.value = records
          })
          .catch(console.error)
          .finally(() => {
            loadingRecords.value = false
          })
      }
    }

    const loadPunishments = () => {
      if (selectedPlayer.value && playerPunishments.value.length === 0) {
        loadingPunishments.value = true
        punishmentApi.getByUuid(selectedPlayer.value.uuid)
          .then(punishments => {
            playerPunishments.value = punishments
          })
          .catch(console.error)
          .finally(() => {
            loadingPunishments.value = false
          })
      }
    }

    const handleDelete = async (id) => {
      try {
        await ElMessageBox.confirm(
          t('players.confirmDelete'),
          t('common.confirm'),
          {
            confirmButtonText: t('common.delete'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
        )
        await playerApi.delete(id)
        ElMessage.success(t('common.success'))
        fetchPlayers()
      } catch (e) {
        if (e !== 'cancel') {
          ElMessage.error(t('common.error'))
        }
      }
    }

    const handleBanPlayer = () => {
      banForm.value = { type: 'PERMANENT', duration: 7, reason: '' }
      banDialogVisible.value = true
    }

    const confirmBan = async () => {
      if (!banForm.value.reason) {
        ElMessage.warning(t('common.warning'))
        return
      }

      try {
        const duration = banForm.value.type === 'TEMPORARY'
          ? banForm.value.duration * 24 * 60 * 60 * 1000
          : 0

        await punishmentApi.ban({
          uuid: selectedPlayer.value.uuid,
          playerName: selectedPlayer.value.playerName,
          punishmentType: banForm.value.type,
          reason: banForm.value.reason,
          duration
        })

        ElMessage.success(t('common.success'))
        banDialogVisible.value = false
        showPlayerDetail(selectedPlayer.value)
        EventBus.emit(Events.STATS_CHANGED)
      } catch (e) {
        ElMessage.error(t('common.error'))
      }
    }

    const handleUnbanPlayer = async () => {
      try {
        await ElMessageBox.confirm(
          t('punishments.confirmUnban'),
          t('common.confirm'),
          { type: 'warning' }
        )

        const activePunishment = playerPunishments.value.find(p => p.active)
        if (activePunishment) {
          await punishmentApi.unban(activePunishment.id)
          ElMessage.success(t('common.success'))
          showPlayerDetail(selectedPlayer.value)
          EventBus.emit(Events.STATS_CHANGED)
        }
      } catch (e) {}
    }

    const analyzePlayerCheat = async () => {
      if (!selectedPlayer.value || aiPlayerLoading.value) return

      aiPlayerLoading.value = true
      aiPlayerResult.value = null

      try {
        const result = await aiApi.analyzeCheat(
          selectedPlayer.value.uuid,
          selectedPlayer.value.playerName
        )

        if (result.success) {
          aiPlayerResult.value = result
        } else {
          ElMessage.warning(result.error || t('ai.error'))
        }
      } catch (e) {
        ElMessage.error(t('ai.networkError'))
      } finally {
        aiPlayerLoading.value = false
      }
    }

    const getVerdictClass = (verdict) => {
      if (!verdict) return ''
      const upper = verdict.toUpperCase()

      if (upper.includes('CONFIRMED') || upper.includes('CHEATING')) return 'danger'
      if (upper.includes('SUSPICIOUS') || upper.includes('LIKELY')) return 'warning'
      if (upper.includes('CLEAN')) return 'success'

      return 'warning'
    }

    const getActionClass = (action) => {
      if (!action) return ''
      const upper = action.toUpperCase()

      if (upper.includes('PERM_BAN')) return 'danger'
      if (upper.includes('TEMP_BAN') || upper.includes('KICK')) return 'warning'
      if (upper.includes('WARN')) return 'info'
      if (upper.includes('NONE')) return 'success'

      return 'info'
    }

    onMounted(() => {
      fetchPlayers()
    })

    onUnmounted(() => {
      if (controller) {
        controller.abort()
      }
    })

    return {
      players,
      searchQuery,
      currentPage,
      pageSize,
      filteredPlayers,
      paginatedPlayers,
      totalPages,
      loading,
      filter,
      detailDialogVisible,
      selectedPlayer,
      activeTab,
      playerCheatRecords,
      playerPunishments,
      loadingRecords,
      loadingPunishments,
      banInfo,
      banDialogVisible,
      banForm,
      aiPlayerLoading,
      aiPlayerResult,
      getRiskTier,
      getRiskClass,
      getRiskText,
      getRiskColor,
      getRiskGradient,
      getCheatClass,
      getSeverityClass,
      formatTime,
      formatTimeShort,
      exportCSV,
      showPlayerDetail,
      loadCheatRecords,
      loadPunishments,
      handleDelete,
      handleBanPlayer,
      confirmBan,
      handleUnbanPlayer,
      analyzePlayerCheat,
      renderAiText,
      getVerdictClass,
      getActionClass,
      t
    }
  }
}
</script>

<style scoped>
.players-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== HEADER ===== */
.players-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-heading {
  font-family: var(--font-sans);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.entity-count {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  background: rgba(255, 200, 0, 0.08);
  border: 1px solid rgba(255, 200, 0, 0.2);
  border-radius: var(--radius-sm);
  color: var(--accent-gold);
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(255, 200, 0, 0.15);
  border-color: rgba(255, 200, 0, 0.4);
  box-shadow: var(--shadow-gold);
}

/* ===== TOOLBAR ===== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 8px;
  background: var(--bg-tertiary);
  padding: 4px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.filter-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-chip:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.03);
}

.filter-chip.active {
  background: rgba(255, 200, 0, 0.12);
  color: var(--accent-gold);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.1);
}

.chip-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.chip-indicator.high {
  background: #E74C3C;
  box-shadow: 0 0 6px rgba(231, 76, 60, 0.5);
}

.chip-indicator.banned {
  background: #9B59B6;
  box-shadow: 0 0 6px rgba(155, 89, 182, 0.5);
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 280px;
  padding: 10px 12px 10px 38px;
  font-family: var(--font-sans);
  font-size: 13px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: rgba(255, 200, 0, 0.3);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.08);
}

.search-input::placeholder {
  color: var(--text-muted);
}

/* ===== LOADING / EMPTY STATES ===== */
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

.loading-spinner {
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

/* ===== PLAYER GRID ===== */
.player-grid {
  min-height: 300px;
}

.grid-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.player-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 20px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardIn 0.4s ease-out both;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 200, 0, 0.06) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.player-card:hover .card-glow {
  opacity: 1;
}

.player-card:hover {
  border-color: rgba(255, 200, 0, 0.25);
  transform: translateY(-4px);
  box-shadow: var(--shadow-gold-strong);
}

.player-card.high {
  border-left: 3px solid #E74C3C;
}

.player-card.medium {
  border-left: 3px solid #FF8C00;
}

.player-card.low {
  border-left: 3px solid #2ECC71;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}

.avatar-section {
  position: relative;
  flex-shrink: 0;
}

.player-avatar {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-sm);
  image-rendering: pixelated;
}

.avatar-ring {
  position: absolute;
  inset: -3px;
  border-radius: calc(var(--radius-sm) + 3px);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.player-card:hover .avatar-ring.high {
  border-color: rgba(231, 76, 60, 0.5);
  box-shadow: 0 0 12px rgba(231, 76, 60, 0.2);
}

.player-card:hover .avatar-ring.medium {
  border-color: rgba(255, 140, 0, 0.5);
  box-shadow: 0 0 12px rgba(255, 140, 0, 0.15);
}

.player-card:hover .avatar-ring.low {
  border-color: rgba(46, 204, 113, 0.4);
  box-shadow: 0 0 12px rgba(46, 204, 113, 0.12);
}

.player-identity {
  flex: 1;
  min-width: 0;
}

.player-name {
  font-family: var(--font-sans);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.player-uuid {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}

.risk-badge {
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  flex-shrink: 0;
}

.risk-badge.high {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.risk-badge.medium {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.25);
}

.risk-badge.low {
  background: rgba(46, 204, 113, 0.1);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.2);
}

/* ===== CARD STATS ===== */
.card-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 14px;
  position: relative;
  z-index: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-family: var(--font-sans);
  font-size: 10px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-time {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary);
}

/* ===== RISK BAR ===== */
.risk-bar-container {
  margin-bottom: 14px;
  position: relative;
  z-index: 1;
}

.risk-bar-track {
  width: 100%;
  height: 5px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 3px;
  overflow: hidden;
}

.risk-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 8px currentColor;
}

/* ===== CARD FOOTER ===== */
.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.card-action {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.3s ease;
}

.card-action.view:hover {
  background: rgba(74, 158, 255, 0.12);
  border-color: rgba(74, 158, 255, 0.3);
  color: #4A9EFF;
}

.card-action.delete:hover {
  background: rgba(231, 76, 60, 0.12);
  border-color: rgba(231, 76, 60, 0.3);
  color: #E74C3C;
}

/* ===== PAGINATION ===== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: 12px;
}

.pag-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-count {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}

.pag-center {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pag-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.pag-btn:hover:not(:disabled) {
  border-color: rgba(255, 200, 0, 0.3);
  color: var(--accent-gold);
  box-shadow: var(--shadow-gold);
}

.pag-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-num {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--text-secondary);
  min-width: 50px;
  text-align: center;
}

.page-size-select {
  padding: 8px 12px;
  font-family: var(--font-sans);
  font-size: 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  cursor: pointer;
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
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg), var(--shadow-gold-strong);
  animation: modalIn 0.3s ease-out;
}

.modal-small {
  max-width: 480px;
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

.modal-player-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.modal-avatar {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  image-rendering: pixelated;
  border: 2px solid rgba(255, 200, 0, 0.2);
}

.modal-name {
  font-family: var(--font-sans);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.modal-uuid {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}

.modal-title {
  font-family: var(--font-sans);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0;
}

.modal-close {
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

.modal-close:hover {
  background: #E74C3C;
  color: #fff;
}

.modal-body {
  padding: 22px;
}

/* ===== RISK GAUGE SECTION ===== */
.risk-gauge-section {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border-color);
}

.gauge-wrapper {
  flex: 1;
}

.gauge-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 4px;
  overflow: hidden;
}

.gauge-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 10px currentColor;
}

.gauge-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.meta-label {
  font-family: var(--font-sans);
  font-size: 10px;
  color: var(--text-muted);
  text-transform: uppercase;
}

.meta-value {
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.meta-value.danger {
  color: #E74C3C;
}

.meta-value.safe {
  color: #2ECC71;
}

/* ===== TABS ===== */
.tab-navigation {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  background: var(--bg-tertiary);
  padding: 4px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.tab-btn {
  flex: 1;
  padding: 10px 14px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  background: transparent;
  border: none;
  border-radius: calc(var(--radius-sm) - 2px);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  color: var(--text-primary);
}

.tab-btn.active {
  background: rgba(255, 200, 0, 0.12);
  color: var(--accent-gold);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.08);
}

.tab-content {
  min-height: 150px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.info-label {
  font-family: var(--font-sans);
  font-size: 10px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.info-value {
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.text-danger {
  color: #E74C3C;
}

.text-warning {
  color: #FF8C00;
}

.text-success {
  color: #2ECC71;
}

/* ===== LOGS ===== */
.cheats-log,
.punishments-log {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-list,
.ban-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.loading-text,
.empty-text {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-muted);
  font-family: var(--font-sans);
  font-size: 13px;
}

.ai-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border-color);
  border-top-color: var(--accent-gold);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

.log-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px 14px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--border-color);
  font-size: 12px;
  transition: all 0.2s ease;
}

.log-entry.high-severity {
  border-left-color: #E74C3C;
  background: rgba(231, 76, 60, 0.05);
}

.log-time {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.log-tag {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  text-transform: uppercase;
  flex-shrink: 0;
}

.log-tag.fly {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
}

.log-tag.speed {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
}

.log-tag.auto {
  background: rgba(74, 158, 255, 0.12);
  color: #4A9EFF;
}

.log-tag.kill {
  background: rgba(155, 89, 182, 0.12);
  color: #9B59B6;
}

.log-tag.default {
  background: rgba(255, 200, 0, 0.12);
  color: var(--accent-gold);
}

.log-level {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.log-level.critical {
  color: #9B59B6;
}

.log-level.high {
  color: #E74C3C;
}

.log-level.medium {
  color: #FF8C00;
}

.log-level.low {
  color: #2ECC71;
}

.log-message {
  color: var(--text-secondary);
  flex: 1;
  min-width: 0;
}

.ban-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--border-color);
}

.ban-type {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid;
  flex-shrink: 0;
}

.ban-type.active {
  border-color: rgba(231, 76, 60, 0.3);
  color: #E74C3C;
  background: rgba(231, 76, 60, 0.08);
}

.ban-type.expired {
  border-color: rgba(46, 204, 113, 0.25);
  color: #2ECC71;
  background: rgba(46, 204, 113, 0.06);
}

.ban-reason {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===== AI PANEL ===== */
.ai-panel,
.ai-result {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-row {
  display: flex;
  gap: 16px;
}

.ai-row .ai-block.half {
  flex: 1;
}

.ai-block-title {
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  color: var(--accent-gold);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.ai-block-text {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.ai-block-text :deep(strong) {
  color: var(--accent-gold);
}

.ai-block-text :deep(code) {
  background: rgba(255, 200, 0, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-family: var(--font-mono);
}

.verdict-badge,
.action-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 700;
}

.verdict-badge.danger,
.action-badge.danger {
  background: rgba(231, 76, 60, 0.12);
  color: #E74C3C;
  border: 1px solid rgba(231, 76, 60, 0.25);
}

.verdict-badge.warning,
.action-badge.warning {
  background: rgba(255, 140, 0, 0.12);
  color: #FF8C00;
  border: 1px solid rgba(255, 140, 0, 0.25);
}

.verdict-badge.success,
.action-badge.success {
  background: rgba(46, 204, 113, 0.1);
  color: #2ECC71;
  border: 1px solid rgba(46, 204, 113, 0.2);
}

.action-badge.info {
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

/* ===== MODAL FOOTER ===== */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}

.modal-btn {
  padding: 10px 20px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-btn.secondary {
  background: transparent;
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.modal-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--text-muted);
  color: var(--text-primary);
}

.modal-btn.danger {
  background: rgba(231, 76, 60, 0.12);
  border-color: rgba(231, 76, 60, 0.3);
  color: #E74C3C;
}

.modal-btn.danger:hover {
  background: rgba(231, 76, 60, 0.2);
}

.modal-btn.success {
  background: rgba(46, 204, 113, 0.1);
  border-color: rgba(46, 204, 113, 0.25);
  color: #2ECC71;
}

.modal-btn.success:hover {
  background: rgba(46, 204, 113, 0.18);
}

/* ===== FORM FIELDS ===== */
.form-field {
  margin-bottom: 16px;
}

.field-label {
  display: block;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.field-input,
.field-textarea {
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

.field-input:focus,
.field-textarea:focus {
  border-color: rgba(255, 200, 0, 0.3);
  box-shadow: 0 0 12px rgba(255, 200, 0, 0.08);
}

.field-input.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.field-textarea {
  min-height: 80px;
  resize: vertical;
}

.radio-group {
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

.card-list-enter-active {
  animation: cardIn 0.4s ease-out both;
}

.card-list-leave-active {
  animation: cardOut 0.2s ease-in both;
}

@keyframes cardOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.95);
  }
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1199px) {
  .grid-wrapper {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 767px) {
  .players-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .toolbar {
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .grid-wrapper {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .risk-gauge-section {
    flex-direction: column;
  }

  .tab-navigation {
    flex-wrap: wrap;
  }

  .tab-btn {
    font-size: 11px;
    padding: 8px 10px;
  }

  .pagination {
    flex-direction: column;
    align-items: stretch;
  }

  .pag-center {
    justify-content: center;
  }
}
</style>
