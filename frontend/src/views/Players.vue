<template>
  <div class="players-terminal">
    <canvas class="pt-scanlines" ref="scanlineCanvas"></canvas>

    <div class="pt-header">
      <div class="pt-prompt">
        <span class="pt-prompt-sign">root@acs:~$</span>
        <span class="pt-prompt-cmd">./players.scan --target=all --sort=risk</span>
        <span class="pt-cursor">_</span>
      </div>
      <div class="pt-status">{{ filteredPlayers.length }} entities indexed</div>
    </div>

    <div class="pt-toolbar">
      <div class="pt-filters">
        <button :class="['pt-chip', { active: filter === 'all' }]" @click="filter = 'all'">
          <span class="pt-chip-glyph">◙</span> ALL
        </button>
        <button :class="['pt-chip', { active: filter === 'high' }]" @click="filter = 'high'">
          <span class="pt-chip-glyph" style="color:#ff3d5a">◆</span> HIGH
        </button>
        <button :class="['pt-chip', { active: filter === 'banned' }]" @click="filter = 'banned'">
          <span class="pt-chip-glyph" style="color:#a855f7">◈</span> BANNED
        </button>
      </div>
      <div class="pt-actions">
        <div class="pt-search">
          <span class="pt-search-prefix">$ grep</span>
          <input v-model="searchQuery" type="text" :placeholder="t('players.searchPlaceholder')" />
        </div>
        <button class="pt-btn" @click="exportCSV">
          <span class="pt-btn-icon">⇩</span> CSV
        </button>
      </div>
    </div>

    <div v-if="loading" class="pt-loading">
      <div class="pt-load-bar"><div class="pt-load-bar-fill"></div></div>
      <span class="pt-load-text">SCANNING ENTITIES <span class="pt-load-dots"><span>.</span><span>.</span><span>.</span></span></span>
    </div>

    <div v-else-if="filteredPlayers.length === 0" class="pt-empty">
      <div class="pt-empty-icon">⊡</div>
      <span>NO ENTITIES FOUND</span>
    </div>

    <div v-else class="pt-grid">
      <div
        v-for="(player, idx) in paginatedPlayers"
        :key="player.id"
        :class="['pt-card', getRiskTier(player.riskScore)]"
        @click="showPlayerDetail(player)"
        :style="{ animationDelay: (idx * 0.06) + 's' }"
        ref="playerCards"
      >
        <div class="pt-card-scanline"></div>
        <div class="pt-card-header">
          <div class="pt-avatar-box">
            <img :src="`https://mc-heads.net/avatar/${player.playerName}/48`" class="pt-avatar" />
            <div :class="['pt-avatar-border', getRiskTier(player.riskScore)]"></div>
          </div>
          <div class="pt-card-id">
            <div class="pt-card-name">{{ player.playerName }}</div>
            <div class="pt-card-uuid">{{ player.uuid ? player.uuid.substring(0, 8) : '-' }}</div>
          </div>
          <div :class="['pt-risk-chip', getRiskTier(player.riskScore)]">
            {{ getRiskText(player.riskScore) }}
          </div>
        </div>

        <div class="pt-card-stats">
          <div class="pt-stat">
            <span class="pt-stat-cmd">$ risk.query</span>
            <span class="pt-stat-val" :style="{ color: getRiskColor(player.riskScore) }">{{ player.riskScore }}</span>
          </div>
          <div class="pt-stat">
            <span class="pt-stat-cmd">$ kicks.count</span>
            <span class="pt-stat-val">{{ player.kickCount || 0 }}</span>
          </div>
          <div class="pt-stat">
            <span class="pt-stat-cmd">$ last.seen</span>
            <span class="pt-stat-val pt-stat-time">{{ formatTimeShort(player.lastSeen) }}</span>
          </div>
        </div>

        <div class="pt-risk-meter">
          <div class="pt-risk-track">
            <div class="pt-risk-fill" :style="{ width: Math.min(player.riskScore * 5, 100) + '%', background: getRiskColor(player.riskScore) }"></div>
          </div>
        </div>

        <div class="pt-card-actions">
          <button class="pt-act view" @click.stop="showPlayerDetail(player)">
            <svg viewBox="0 0 24 24" width="13" height="13"><path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
          </button>
          <button class="pt-act delete" @click.stop="handleDelete(player.id)">
            <svg viewBox="0 0 24 24" width="13" height="13"><path fill="currentColor" d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
          </button>
        </div>
      </div>
    </div>

    <div class="pt-pagination" v-if="filteredPlayers.length > 0">
      <div class="pt-pag-left">
        <span class="pt-pag-count">[{{ filteredPlayers.length }}] records</span>
        <button v-if="selectedIds.size > 0" class="pt-pag-batch" @click="handleBatchDelete">
          ▲ PURGE ({{ selectedIds.size }})
        </button>
      </div>
      <div class="pt-pag-center">
        <button class="pt-pag-btn" :disabled="currentPage === 1" @click="currentPage--">◄</button>
        <span class="pt-pag-num">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pt-pag-btn" :disabled="currentPage >= totalPages" @click="currentPage++">►</button>
      </div>
      <select v-model="pageSize" class="pt-pag-select">
        <option :value="12">12</option>
        <option :value="24">24</option>
        <option :value="48">48</option>
      </select>
    </div>

    <transition name="pt-modal-fade">
      <div v-if="detailDialogVisible" class="pt-overlay" @click.self="detailDialogVisible = false">
        <div class="pt-modal">
          <div class="pt-modal-topbar">
            <div class="pt-modal-player" v-if="selectedPlayer">
              <img :src="`https://mc-heads.net/avatar/${selectedPlayer.playerName}/64`" class="pt-modal-head" />
              <div>
                <div class="pt-modal-name">{{ selectedPlayer.playerName }}</div>
                <div class="pt-modal-uuid">&lt;{{ selectedPlayer.uuid }}&gt;</div>
              </div>
            </div>
            <button class="pt-modal-close" @click="detailDialogVisible = false">┼</button>
          </div>

          <div class="pt-modal-body" v-if="selectedPlayer">
            <div class="pt-modal-risk">
              <div class="pt-modal-gauge">
                <div class="pt-gauge-bar">
                  <div class="pt-gauge-fill" :style="{ width: Math.min(selectedPlayer.riskScore * 5, 100) + '%', background: getRiskColor(selectedPlayer.riskScore) }"></div>
                </div>
                <div class="pt-gauge-labels">
                  <span>0</span><span :style="{ color: getRiskColor(selectedPlayer.riskScore) }">{{ selectedPlayer.riskScore }}</span><span>20+</span>
                </div>
              </div>
              <div class="pt-modal-meta">
                <div class="pt-meta-item">
                  <span class="pt-meta-cmd">$ kicks.count</span>
                  <span class="pt-meta-val">{{ selectedPlayer.kickCount || 0 }}</span>
                </div>
                <div class="pt-meta-item">
                  <span class="pt-meta-cmd">$ ban.status</span>
                  <span :class="['pt-meta-val', banInfo.banned ? 'pt-meta-danger' : 'pt-meta-safe']">{{ banInfo.banned ? 'TRUE' : 'FALSE' }}</span>
                </div>
              </div>
            </div>

            <div class="pt-modal-tabs">
              <button :class="['pt-tab', { active: activeTab === 'basic' }]" @click="activeTab = 'basic'">info.dat</button>
              <button :class="['pt-tab', { active: activeTab === 'cheats' }]" @click="activeTab = 'cheats'">cheats.log</button>
              <button :class="['pt-tab', { active: activeTab === 'punishments' }]" @click="activeTab = 'punishments'">bans.log</button>
              <button :class="['pt-tab', { active: activeTab === 'ai' }]" @click="activeTab = 'ai'; analyzePlayerCheat()">ai.diag</button>
            </div>

            <div class="pt-tab-content">
              <div v-show="activeTab === 'basic'" class="pt-info-grid">
                <div class="pt-info-cell"><span class="pt-info-cmd">$ player.name</span><span class="pt-info-val">{{ selectedPlayer.playerName }}</span></div>
                <div class="pt-info-cell"><span class="pt-info-cmd">$ risk.score</span><span :class="['pt-info-val', getRiskClass(selectedPlayer.riskScore)]">{{ selectedPlayer.riskScore }} ({{ getRiskText(selectedPlayer.riskScore) }})</span></div>
                <div class="pt-info-cell"><span class="pt-info-cmd">$ kicks.total</span><span class="pt-info-val">{{ selectedPlayer.kickCount || 0 }}</span></div>
                <div class="pt-info-cell"><span class="pt-info-cmd">$ last.seen</span><span class="pt-info-val">{{ formatTime(selectedPlayer.lastSeen) }}</span></div>
              </div>

              <div v-show="activeTab === 'cheats'" class="pt-cheats-log">
                <div v-if="loadingRecords" class="pt-tab-status">$ tail -f cheats.log...</div>
                <div v-else-if="playerCheatRecords.length === 0" class="pt-tab-status">[EMPTY]</div>
                <div v-else :class="['pt-log-entry', record.severity >= 3 ? 'pt-log-danger' : '']" v-for="record in playerCheatRecords" :key="record.id">
                  <span class="pt-log-time">[{{ formatTimeShort(record.detectionTime) }}]</span>
                  <span :class="['pt-log-tag', getCheatClass(record.cheatType)]">{{ record.cheatType }}</span>
                  <span :class="['pt-log-sev', getSeverityClass(record.severity)]">Lv.{{ record.severity }}</span>
                  <span class="pt-log-msg">{{ record.details }}</span>
                </div>
              </div>

              <div v-show="activeTab === 'punishments'" class="pt-bans-log">
                <div v-if="loadingPunishments" class="pt-tab-status">$ tail -f bans.log...</div>
                <div v-else-if="playerPunishments.length === 0" class="pt-tab-status">[EMPTY]</div>
                <div v-else class="pt-ban-entry" v-for="punishment in playerPunishments" :key="punishment.id">
                  <span :class="['pt-ban-type', punishment.active ? 'pt-ban-active' : 'pt-ban-expired']">{{ punishment.punishmentType === 'PERMANENT' ? 'PERM' : 'TEMP' }}</span>
                  <span class="pt-ban-reason">{{ punishment.reason }}</span>
                </div>
              </div>

              <div v-show="activeTab === 'ai'" class="pt-ai-panel">
                <div v-if="aiPlayerLoading" class="pt-tab-status">
                  <div class="pt-ai-spinner"></div>
                  $ ai.diagnose --target={{ selectedPlayer?.playerName }}...
                </div>
                <div v-else-if="!aiPlayerResult" class="pt-tab-status">Press ai.diag to analyze</div>
                <div v-else class="pt-ai-result">
                  <div v-if="aiPlayerResult.analysis" class="pt-ai-block">
                    <div class="pt-ai-label">> DIAGNOSIS</div>
                    <div class="pt-ai-text" v-html="renderAiText(aiPlayerResult.analysis)"></div>
                  </div>
                  <div class="pt-ai-row">
                    <div v-if="aiPlayerResult.verdict" class="pt-ai-block">
                      <div class="pt-ai-label">> VERDICT</div>
                      <span :class="['pt-ai-chip', getVerdictClass(aiPlayerResult.verdict)]">{{ aiPlayerResult.verdict }}</span>
                    </div>
                    <div v-if="aiPlayerResult.confidence" class="pt-ai-block">
                      <div class="pt-ai-label">> CONFIDENCE</div>
                      <span class="pt-ai-conf">{{ (aiPlayerResult.confidence * 100).toFixed(0) }}%</span>
                    </div>
                  </div>
                  <div v-if="aiPlayerResult.suggestedAction" class="pt-ai-block">
                    <div class="pt-ai-label">> ACTION</div>
                    <span :class="['pt-ai-chip', getActionClass(aiPlayerResult.suggestedAction)]">{{ aiPlayerResult.suggestedAction }}</span>
                  </div>
                  <div v-if="aiPlayerResult.reasoning" class="pt-ai-block">
                    <div class="pt-ai-label">> REASONING</div>
                    <div class="pt-ai-text" v-html="renderAiText(aiPlayerResult.reasoning)"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="pt-modal-footer">
            <button class="pt-modal-btn sec" @click="detailDialogVisible = false">[ESC]</button>
            <button v-if="!banInfo.banned" class="pt-modal-btn danger" @click="handleBanPlayer">BAN</button>
            <button v-else class="pt-modal-btn safe" @click="handleUnbanPlayer">UNBAN</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="pt-modal-fade">
      <div v-if="banDialogVisible" class="pt-overlay" @click.self="banDialogVisible = false">
        <div class="pt-modal pt-modal-sm">
          <div class="pt-modal-topbar">
            <span class="pt-modal-title">> ban.execute --target={{ selectedPlayer?.playerName }}</span>
            <button class="pt-modal-close" @click="banDialogVisible = false">┼</button>
          </div>
          <div class="pt-modal-body">
            <div class="pt-field">
              <span class="pt-field-cmd">$ player.name</span>
              <input :value="selectedPlayer?.playerName" disabled class="pt-input" />
            </div>
            <div class="pt-field">
              <span class="pt-field-cmd">$ ban.type</span>
              <div class="pt-radio-row">
                <label class="pt-radio"><input type="radio" value="PERMANENT" v-model="banForm.type" /> <span>PERMANENT</span></label>
                <label class="pt-radio"><input type="radio" value="TEMPORARY" v-model="banForm.type" /> <span>TEMPORARY</span></label>
              </div>
            </div>
            <div class="pt-field" v-if="banForm.type === 'TEMPORARY'">
              <span class="pt-field-cmd">$ ban.duration (days)</span>
              <input type="number" v-model="banForm.duration" min="1" max="365" class="pt-input" />
            </div>
            <div class="pt-field">
              <span class="pt-field-cmd">$ ban.reason</span>
              <textarea v-model="banForm.reason" :placeholder="t('reports.banReason')" class="pt-textarea"></textarea>
            </div>
          </div>
          <div class="pt-modal-footer">
            <button class="pt-modal-btn sec" @click="banDialogVisible = false">CANCEL</button>
            <button class="pt-modal-btn danger" @click="confirmBan">EXECUTE</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue'
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
    const sortField = ref('riskScore')
    const sortOrder = ref('desc')
    const selectedIds = ref(new Set())
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
    const playerCards = ref([])
    const scanlineCanvas = ref(null)

    const fetchPlayers = async () => {
      loading.value = true
      try { players.value = await playerApi.getAll() } catch (e) { ElMessage.error(t('common.error')) } finally { loading.value = false }
    }

    const filteredPlayers = computed(() => {
      let list = players.value
      if (searchQuery.value) list = list.filter(p => p.playerName.toLowerCase().includes(searchQuery.value.toLowerCase()))
      if (filter.value === 'high') list = list.filter(p => p.riskScore >= 10)
      if (filter.value === 'banned') list = list.filter(p => p.banned === true || p.riskScore >= 100)
      if (sortField.value) {
        list = [...list].sort((a, b) => {
          const va = a[sortField.value] || 0, vb = b[sortField.value] || 0
          return sortOrder.value === 'asc' ? va - vb : vb - va
        })
      }
      return list
    })

    const totalPages = computed(() => Math.ceil(filteredPlayers.value.length / pageSize.value))
    const paginatedPlayers = computed(() => { const s = (currentPage.value - 1) * pageSize.value; return filteredPlayers.value.slice(s, s + pageSize.value) })

    const getRiskTier = (s) => { if (s >= 10) return 'tier-high'; if (s >= 5) return 'tier-medium'; return 'tier-low' }
    const getRiskClass = (s) => { if (s >= 10) return 'text-red'; if (s >= 5) return 'text-orange'; return 'text-green' }
    const getRiskText = (s) => { if (s >= 10) return t('players.high'); if (s >= 5) return t('players.medium'); return t('players.low') }
    const getRiskColor = (s) => { if (s >= 10) return '#ff3d5a'; if (s >= 5) return '#ff9100'; return '#00e676' }
    const getCheatClass = (ct) => { const m = { '飞行作弊': 'tag-fly', '速度作弊': 'tag-speed', '自动点击作弊': 'tag-auto', '杀戮光环': 'tag-kill' }; return m[ct] || 'tag-default' }
    const getSeverityClass = (s) => { if (s >= 4) return 'sev-critical'; if (s >= 3) return 'sev-high'; if (s >= 2) return 'sev-medium'; return 'sev-low' }

    const formatTime = (ts) => { if (!ts) return '-'; return new Date(ts).toLocaleString() }
    const formatTimeShort = (ts) => { if (!ts) return '-'; const d = new Date(ts); return `${d.getMonth()+1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}` }

    const exportCSV = () => {
      const headers = ['playerName', 'uuid', 'riskScore', 'kickCount', 'lastSeen']
      const rows = filteredPlayers.value.map(p => headers.map(h => p[h] || ''))
      const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
      const blob = new Blob([csv], { type: 'text/csv' })
      const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'players.csv'; a.click()
    }

    const showPlayerDetail = async (player) => {
      selectedPlayer.value = player; detailDialogVisible.value = true; activeTab.value = 'basic'; loadingRecords.value = true; loadingPunishments.value = true; aiPlayerResult.value = null
      try {
        const [records, punishments, banStatus] = await Promise.all([cheatApi.getByPlayerUuid(player.uuid), punishmentApi.getByUuid(player.uuid), punishmentApi.checkBanStatus(player.uuid)])
        playerCheatRecords.value = records; playerPunishments.value = punishments; banInfo.value = banStatus
      } catch (e) { console.error(e) } finally { loadingRecords.value = false; loadingPunishments.value = false }
    }

    const handleDelete = async (id) => {
      try { await ElMessageBox.confirm(t('players.confirmDelete'), t('common.confirm'), { confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel'), type: 'warning' }); await playerApi.delete(id); ElMessage.success(t('common.success')); fetchPlayers() } catch (e) { if (e !== 'cancel') { ElMessage.error(t('common.error')) } }
    }

    const handleBatchDelete = async () => {
      try { await ElMessageBox.confirm(`${t('common.delete')} ${selectedIds.value.size}?`, t('common.confirm'), { type: 'warning' }); for (const id of selectedIds.value) { await playerApi.delete(id) }; ElMessage.success(t('common.success')); selectedIds.value = new Set(); fetchPlayers() } catch (e) {}
    }

    const handleBanPlayer = () => { banForm.value = { type: 'PERMANENT', duration: 7, reason: '' }; banDialogVisible.value = true }
    const confirmBan = async () => {
      if (!banForm.value.reason) { ElMessage.warning(t('common.warning')); return }
      try { const dur = banForm.value.type === 'TEMPORARY' ? banForm.value.duration * 24 * 60 * 60 * 1000 : 0; await punishmentApi.ban({ uuid: selectedPlayer.value.uuid, playerName: selectedPlayer.value.playerName, punishmentType: banForm.value.type, reason: banForm.value.reason, duration: dur }); ElMessage.success(t('common.success')); banDialogVisible.value = false; showPlayerDetail(selectedPlayer.value); EventBus.emit(Events.STATS_CHANGED) } catch (e) { ElMessage.error(t('common.error')) }
    }
    const handleUnbanPlayer = async () => {
      try { await ElMessageBox.confirm(t('punishments.confirmUnban'), t('common.confirm'), { type: 'warning' }); const ap = playerPunishments.value.find(p => p.active); if (ap) { await punishmentApi.unban(ap.id); ElMessage.success(t('common.success')); showPlayerDetail(selectedPlayer.value); EventBus.emit(Events.STATS_CHANGED) } } catch (e) {}
    }

    const analyzePlayerCheat = async () => {
      if (!selectedPlayer.value || aiPlayerLoading.value) return
      aiPlayerLoading.value = true; aiPlayerResult.value = null
      try {
        const result = await aiApi.analyzeCheat(selectedPlayer.value.uuid, selectedPlayer.value.playerName)
        if (result.success) { aiPlayerResult.value = result } else { ElMessage.warning(result.error || t('ai.error')) }
      } catch (e) { ElMessage.error(t('ai.networkError')) } finally { aiPlayerLoading.value = false }
    }

    const getVerdictClass = (v) => { if (!v) return ''; const u = v.toUpperCase(); if (u.includes('CONFIRMED') || u.includes('CHEATING')) return 'verdict-danger'; if (u.includes('SUSPICIOUS') || u.includes('LIKELY')) return 'verdict-warning'; if (u.includes('CLEAN')) return 'verdict-safe'; return 'verdict-warning' }
    const getActionClass = (a) => { if (!a) return ''; const u = a.toUpperCase(); if (u.includes('PERM_BAN')) return 'action-danger'; if (u.includes('TEMP_BAN') || u.includes('KICK')) return 'action-warning'; if (u.includes('WARN')) return 'action-info'; if (u.includes('NONE')) return 'action-safe'; return 'action-info' }

    const initScanlines = () => {
      const canvas = scanlineCanvas.value
      if (!canvas) return
      const resize = () => {
        const parent = canvas.parentElement
        canvas.width = parent.offsetWidth
        canvas.height = parent.offsetHeight
      }
      resize()
      window.addEventListener('resize', resize)

      const ctx = canvas.getContext('2d')
      let offset = 0

      const draw = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        ctx.fillStyle = 'rgba(168, 85, 247, 0.04)'
        const gap = 3
        offset = (offset + 0.3) % gap
        for (let y = offset; y < canvas.height; y += gap) {
          ctx.fillRect(0, y, canvas.width, 1)
        }
        requestAnimationFrame(draw)
      }
      draw()
    }

    const setupCardTilt = () => {
      nextTick(() => {
        const cards = document.querySelectorAll('.pt-card')
        cards.forEach(card => {
          card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect()
            const x = (e.clientX - rect.left) / rect.width - 0.5
            const y = (e.clientY - rect.top) / rect.height - 0.5
            card.style.transform = `perspective(500px) rotateY(${x * 3}deg) rotateX(${-y * 3}deg)`
            const scanline = card.querySelector('.pt-card-scanline')
            if (scanline) scanline.style.opacity = '0.7'
          })
          card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(500px) rotateY(0deg) rotateX(0deg)'
            card.style.transition = 'transform 0.4s ease'
            const scanline = card.querySelector('.pt-card-scanline')
            if (scanline) scanline.style.opacity = '0.15'
          })
          card.addEventListener('mouseenter', () => {
            card.style.transition = 'transform 0.1s ease'
          })
        })
      })
    }

    onMounted(() => {
      fetchPlayers()
      nextTick(() => {
        initScanlines()
        setupCardTilt()
      })
    })

    return {
      players, searchQuery, currentPage, pageSize, filteredPlayers, paginatedPlayers, totalPages, loading, filter, selectedIds,
      detailDialogVisible, selectedPlayer, activeTab, playerCheatRecords, playerPunishments, loadingRecords, loadingPunishments,
      banInfo, banDialogVisible, banForm, aiPlayerLoading, aiPlayerResult,
      getRiskTier, getRiskClass, getRiskText, getRiskColor, getCheatClass, getSeverityClass,
      formatTime, formatTimeShort, exportCSV, showPlayerDetail, handleDelete, handleBatchDelete,
      handleBanPlayer, confirmBan, handleUnbanPlayer, analyzePlayerCheat, renderAiText, getVerdictClass, getActionClass,
      playerCards, scanlineCanvas, t
    }
  }
}
</script>

<style scoped>
.players-terminal {
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: relative;
}

.pt-scanlines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

/* ===== HEADER ===== */
.pt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.pt-prompt {
  font-family: var(--font-mono);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pt-prompt-sign {
  color: #06b6d4;
  font-weight: 700;
}

.pt-prompt-cmd {
  color: #a855f7;
}

.pt-cursor {
  color: #c084fc;
  animation: ptBlink 1s step-end infinite;
}

@keyframes ptBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.pt-status {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 1px;
}

/* ===== TOOLBAR ===== */
.pt-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.pt-filters {
  display: flex;
  gap: 4px;
  background: rgba(10, 4, 22, 0.6);
  border: 2px solid rgba(147, 51, 234, 0.12);
  border-radius: 2px;
  padding: 3px;
  image-rendering: pixelated;
}

.pt-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 1px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.pt-chip:hover {
  color: var(--text-primary);
  background: rgba(147, 51, 234, 0.08);
}

.pt-chip.active {
  background: rgba(147, 51, 234, 0.16);
  border-color: rgba(168, 85, 247, 0.45);
  color: #c084fc;
  box-shadow: 0 0 8px rgba(147, 51, 234, 0.1), inset 0 1px 0 rgba(255,255,255,0.03);
}

.pt-chip-glyph {
  font-size: 8px;
}

.pt-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pt-search {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  background: rgba(10, 4, 22, 0.6);
  border: 2px solid rgba(147, 51, 234, 0.1);
  border-radius: 2px;
  width: 240px;
  transition: all 0.2s ease;
}

.pt-search:focus-within {
  border-color: rgba(168, 85, 247, 0.4);
  box-shadow: 0 0 10px rgba(147, 51, 234, 0.1);
}

.pt-search-prefix {
  font-family: var(--font-mono);
  font-size: 10px;
  color: #06b6d4;
  font-weight: 600;
  flex-shrink: 0;
}

.pt-search input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-primary);
}

.pt-search input::placeholder {
  color: var(--text-muted);
}

.pt-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 12px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: rgba(10, 4, 22, 0.6);
  border: 2px solid rgba(147, 51, 234, 0.1);
  border-radius: 2px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.pt-btn:hover {
  border-color: rgba(168, 85, 247, 0.35);
  color: #c084fc;
}

.pt-btn-icon {
  font-size: 12px;
}

/* ===== LOADING / EMPTY ===== */
.pt-loading, .pt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 14px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 2px;
  position: relative;
  z-index: 1;
}

.pt-empty-icon {
  font-size: 48px;
  opacity: 0.15;
}

.pt-load-bar {
  width: 180px;
  height: 2px;
  background: rgba(147, 51, 234, 0.1);
  border-radius: 1px;
  overflow: hidden;
}

.pt-load-bar-fill {
  height: 100%;
  width: 40%;
  background: #a855f7;
  border-radius: 1px;
  animation: ptLoadScan 1.5s ease-in-out infinite;
}

@keyframes ptLoadScan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}

.pt-load-dots span {
  animation: ptDotFade 1.4s infinite;
  opacity: 0;
}

.pt-load-dots span:nth-child(1) { animation-delay: 0s; }
.pt-load-dots span:nth-child(2) { animation-delay: 0.2s; }
.pt-load-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes ptDotFade {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

/* ===== CARD GRID ===== */
.pt-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 14px;
  position: relative;
  z-index: 1;
}

.pt-card {
  position: relative;
  background: rgba(8, 3, 18, 0.85);
  border: 2px solid rgba(147, 51, 234, 0.1);
  border-radius: 2px;
  padding: 16px;
  cursor: pointer;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  animation: ptCardIn 0.45s ease-out both;
  image-rendering: pixelated;
}

.pt-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(45deg, transparent 48%, rgba(168,85,247,0.02) 48%, rgba(168,85,247,0.02) 52%, transparent 52%),
    linear-gradient(-45deg, transparent 48%, rgba(168,85,247,0.02) 48%, rgba(168,85,247,0.02) 52%, transparent 52%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.pt-card:hover::before {
  opacity: 1;
}

@keyframes ptCardIn {
  from { opacity: 0; transform: translateY(12px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.pt-card:hover {
  border-color: rgba(168, 85, 247, 0.4);
  box-shadow: 0 1px 0 rgba(168,85,247,0.15), 0 4px 20px rgba(147,51,234,0.12);
}

.pt-card.tier-high { border-left: 4px solid #ff3d5a; }
.pt-card.tier-medium { border-left: 4px solid #f59e0b; }
.pt-card.tier-low { border-left: 4px solid #10b981; }

.pt-card.tier-high:hover { box-shadow: 0 1px 0 rgba(255,61,90,0.2), 0 4px 20px rgba(255,61,90,0.15); }
.pt-card.tier-medium:hover { box-shadow: 0 1px 0 rgba(245,158,11,0.2), 0 4px 20px rgba(245,158,11,0.12); }
.pt-card.tier-low:hover { box-shadow: 0 1px 0 rgba(16,185,129,0.15), 0 4px 20px rgba(16,185,129,0.08); }

/* -- scanline overlay -- */
.pt-card-scanline {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(168,85,247,0.03) 2px, rgba(168,85,247,0.03) 4px);
  pointer-events: none;
  opacity: 0.15;
  transition: opacity 0.3s ease;
}

/* ===== CARD HEADER ===== */
.pt-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  position: relative;
  z-index: 1;
}

.pt-avatar-box {
  position: relative;
  flex-shrink: 0;
}

.pt-avatar {
  width: 44px;
  height: 44px;
  image-rendering: pixelated;
  display: block;
}

.pt-avatar-border {
  position: absolute;
  inset: -2px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.pt-card:hover .pt-avatar-border.tier-high { border-color: rgba(255,61,90,0.5); box-shadow: 0 0 8px rgba(255,61,90,0.1); }
.pt-card:hover .pt-avatar-border.tier-medium { border-color: rgba(245,158,11,0.5); box-shadow: 0 0 8px rgba(245,158,11,0.08); }
.pt-card:hover .pt-avatar-border.tier-low { border-color: rgba(16,185,129,0.4); box-shadow: 0 0 8px rgba(16,185,129,0.06); }

.pt-card-id {
  flex: 1;
  min-width: 0;
}

.pt-card-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pt-card-uuid {
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
  margin-top: 2px;
}

.pt-risk-chip {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.5px;
  padding: 3px 8px;
  border: 1px solid;
  flex-shrink: 0;
}

.pt-risk-chip.tier-high { border-color: rgba(255,61,90,0.3); color: #ff3d5a; background: rgba(255,61,90,0.08); }
.pt-risk-chip.tier-medium { border-color: rgba(245,158,11,0.3); color: #f59e0b; background: rgba(245,158,11,0.08); }
.pt-risk-chip.tier-low { border-color: rgba(16,185,129,0.25); color: #10b981; background: rgba(16,185,129,0.06); }

/* ===== CARD STATS ===== */
.pt-card-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
  position: relative;
  z-index: 1;
}

.pt-stat {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.pt-stat-cmd {
  font-family: var(--font-mono);
  font-size: 8px;
  font-weight: 600;
  color: #06b6d4;
  opacity: 0.6;
}

.pt-stat-val {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.pt-stat-time {
  font-size: 10px;
  font-weight: 500;
  color: var(--text-secondary);
}

/* ===== RISK METER ===== */
.pt-risk-meter {
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.pt-risk-track {
  width: 100%;
  height: 4px;
  background: rgba(255,255,255,0.03);
  overflow: hidden;
}

.pt-risk-fill {
  height: 100%;
  transition: width 0.6s ease;
}

/* ===== ACTIONS ===== */
.pt-card-actions {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
  position: relative;
  z-index: 1;
}

.pt-act {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.02);
  border: 2px solid rgba(147,51,234,0.06);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.pt-act.view:hover { background: rgba(6,182,212,0.14); border-color: rgba(6,182,212,0.3); color: #06b6d4; }
.pt-act.delete:hover { background: rgba(255,61,90,0.14); border-color: rgba(255,61,90,0.3); color: #ff3d5a; }

/* ===== PAGINATION ===== */
.pt-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-top: 2px solid rgba(147,51,234,0.06);
  flex-wrap: wrap;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.pt-pag-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pt-pag-count {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}

.pt-pag-batch {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 4px 10px;
  background: rgba(255,61,90,0.1);
  border: 2px solid rgba(255,61,90,0.2);
  color: #ff3d5a;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pt-pag-batch:hover { background: rgba(255,61,90,0.2); }

.pt-pag-center {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pt-pag-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-secondary);
  font-family: var(--font-mono);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pt-pag-btn:hover:not(:disabled) { border-color: rgba(168,85,247,0.4); color: #c084fc; }
.pt-pag-btn:disabled { opacity: 0.2; cursor: not-allowed; }

.pt-pag-num {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-secondary);
}

.pt-pag-select {
  padding: 6px 10px;
  font-family: var(--font-mono);
  font-size: 10px;
  background: rgba(10,4,22,0.6);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
}

/* ===== OVERLAY ===== */
.pt-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(4,0,10,0.88);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

/* ===== MODAL ===== */
.pt-modal {
  background: rgba(8,3,18,0.98);
  border: 2px solid rgba(147,51,234,0.25);
  border-radius: 2px;
  width: 100%;
  max-width: 580px;
  max-height: 90vh;
  overflow-y: auto;
  animation: ptModalIn 0.28s ease-out;
  box-shadow: 0 10px 50px rgba(0,0,0,0.7), 0 0 40px rgba(147,51,234,0.12);
  image-rendering: pixelated;
}

.pt-modal-sm {
  max-width: 440px;
}

@keyframes ptModalIn {
  from { opacity: 0; transform: scale(0.94) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.pt-modal-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 2px solid rgba(147,51,234,0.1);
}

.pt-modal-player {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pt-modal-head {
  width: 48px;
  height: 48px;
  image-rendering: pixelated;
  border: 2px solid rgba(147,51,234,0.2);
}

.pt-modal-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.pt-modal-uuid {
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
  margin-top: 2px;
}

.pt-modal-title {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #a855f7;
  font-weight: 600;
}

.pt-modal-close {
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

.pt-modal-close:hover { background: rgba(255,61,90,0.2); color: #ff3d5a; }

.pt-modal-body {
  padding: 18px;
}

.pt-modal-risk {
  display: flex;
  gap: 20px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 2px solid rgba(147,51,234,0.06);
}

.pt-modal-gauge { flex: 1; }

.pt-gauge-bar {
  width: 100%;
  height: 8px;
  background: rgba(255,255,255,0.03);
  overflow: hidden;
}

.pt-gauge-fill {
  height: 100%;
  transition: width 0.6s ease;
}

.pt-gauge-labels {
  display: flex;
  justify-content: space-between;
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
  margin-top: 4px;
}

.pt-modal-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pt-meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pt-meta-cmd {
  font-family: var(--font-mono);
  font-size: 8px;
  color: #06b6d4;
  opacity: 0.6;
}

.pt-meta-val {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 700;
}

.pt-meta-danger { color: #ff3d5a; }
.pt-meta-safe { color: #10b981; }

.pt-modal-tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 16px;
  background: rgba(147,51,234,0.03);
  padding: 3px;
  border: 1px solid rgba(147,51,234,0.06);
}

.pt-tab {
  flex: 1;
  padding: 8px 10px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 600;
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.pt-tab:hover { color: var(--text-primary); }
.pt-tab.active { background: rgba(168,85,247,0.12); border-color: rgba(168,85,247,0.3); color: #c084fc; }

.pt-tab-content { min-height: 120px; }

.pt-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.pt-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: rgba(147,51,234,0.03);
  border: 1px solid rgba(147,51,234,0.05);
}

.pt-info-cmd {
  font-family: var(--font-mono);
  font-size: 8px;
  color: #06b6d4;
  opacity: 0.55;
}

.pt-info-val {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.text-red { color: #ff3d5a; }
.text-orange { color: #f59e0b; }
.text-green { color: #10b981; }

.pt-cheats-log, .pt-bans-log {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pt-log-entry {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 10px;
  background: rgba(147,51,234,0.025);
  border-left: 2px solid rgba(168,85,247,0.2);
  font-size: 11px;
}

.pt-log-entry.pt-log-danger {
  border-left-color: #ff3d5a;
  background: rgba(255,61,90,0.04);
}

.pt-log-time {
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.pt-log-tag {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 1px 6px;
  border: 1px solid;
}

.pt-log-tag.tag-fly { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.pt-log-tag.tag-speed { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.pt-log-tag.tag-auto { border-color: rgba(6,182,212,0.25); color: #06b6d4; background: rgba(6,182,212,0.06); }
.pt-log-tag.tag-kill { border-color: rgba(168,85,247,0.25); color: #a855f7; background: rgba(168,85,247,0.06); }
.pt-log-tag.tag-default { border-color: rgba(147,51,234,0.2); color: #c084fc; background: rgba(147,51,234,0.05); }

.pt-log-sev {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
}

.pt-log-sev.sev-critical { color: #a855f7; }
.pt-log-sev.sev-high { color: #ff3d5a; }
.pt-log-sev.sev-medium { color: #f59e0b; }
.pt-log-sev.sev-low { color: #10b981; }

.pt-log-msg { color: var(--text-secondary); }

.pt-ban-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(147,51,234,0.025);
  border-left: 2px solid rgba(147,51,234,0.1);
}

.pt-ban-type {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  padding: 2px 7px;
  border: 1px solid;
  flex-shrink: 0;
}

.pt-ban-active { border-color: rgba(255,61,90,0.25); color: #ff3d5a; }
.pt-ban-expired { border-color: rgba(16,185,129,0.2); color: #10b981; }

.pt-ban-reason { font-size: 12px; color: var(--text-secondary); }

.pt-tab-status {
  text-align: center;
  padding: 30px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.pt-ai-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(147,51,234,0.12);
  border-top-color: #a855f7;
  animation: ptSpin 0.7s linear infinite;
}

@keyframes ptSpin { to { transform: rotate(360deg); } }

.pt-ai-result { display: flex; flex-direction: column; gap: 14px; }
.pt-ai-row { display: flex; gap: 20px; }
.pt-ai-row .pt-ai-block { flex: 1; }

.pt-ai-label {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #06b6d4;
  margin-bottom: 6px;
}

.pt-ai-text {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.pt-ai-text :deep(strong) { color: #c084fc; }
.pt-ai-text :deep(code) { background: rgba(168,85,247,0.08); padding: 1px 5px; font-size: 10px; }

.pt-ai-chip {
  display: inline-block;
  padding: 4px 12px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  border: 1px solid;
}

.pt-ai-chip.verdict-danger { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.pt-ai-chip.verdict-warning { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.pt-ai-chip.verdict-safe { border-color: rgba(16,185,129,0.2); color: #10b981; background: rgba(16,185,129,0.05); }
.pt-ai-chip.action-danger { border-color: rgba(255,61,90,0.25); color: #ff3d5a; background: rgba(255,61,90,0.06); }
.pt-ai-chip.action-warning { border-color: rgba(245,158,11,0.25); color: #f59e0b; background: rgba(245,158,11,0.06); }
.pt-ai-chip.action-info { border-color: rgba(6,182,212,0.2); color: #06b6d4; background: rgba(6,182,212,0.05); }
.pt-ai-chip.action-safe { border-color: rgba(16,185,129,0.2); color: #10b981; background: rgba(16,185,129,0.05); }

.pt-ai-conf {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  color: #c084fc;
}

/* ===== MODAL FOOTER ===== */
.pt-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 18px;
  border-top: 2px solid rgba(147,51,234,0.06);
}

.pt-modal-btn {
  padding: 8px 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  border: 2px solid;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pt-modal-btn.sec {
  background: rgba(147,51,234,0.04);
  border-color: rgba(147,51,234,0.1);
  color: var(--text-muted);
}

.pt-modal-btn.sec:hover { background: rgba(147,51,234,0.1); color: var(--text-primary); }

.pt-modal-btn.danger {
  background: rgba(255,61,90,0.12);
  border-color: rgba(255,61,90,0.3);
  color: #ff3d5a;
}

.pt-modal-btn.danger:hover { background: rgba(255,61,90,0.22); }

.pt-modal-btn.safe {
  background: rgba(16,185,129,0.1);
  border-color: rgba(16,185,129,0.22);
  color: #10b981;
}

.pt-modal-btn.safe:hover { background: rgba(16,185,129,0.18); }

/* ===== FORM FIELDS ===== */
.pt-field {
  margin-bottom: 14px;
}

.pt-field-cmd {
  display: block;
  font-family: var(--font-mono);
  font-size: 9px;
  color: #06b6d4;
  margin-bottom: 6px;
  opacity: 0.6;
}

.pt-input {
  width: 100%;
  padding: 9px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147,51,234,0.03);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
  outline: none;
}

.pt-input:focus { border-color: rgba(168,85,247,0.4); box-shadow: 0 0 8px rgba(147,51,234,0.1); }

.pt-textarea {
  width: 100%;
  padding: 9px 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  background: rgba(147,51,234,0.03);
  border: 2px solid rgba(147,51,234,0.1);
  color: var(--text-primary);
  min-height: 70px;
  resize: vertical;
  outline: none;
}

.pt-textarea:focus { border-color: rgba(168,85,247,0.4); box-shadow: 0 0 8px rgba(147,51,234,0.1); }

.pt-radio-row {
  display: flex;
  gap: 16px;
}

.pt-radio {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-primary);
  cursor: pointer;
}

.pt-radio input { accent-color: #a855f7; }

/* ===== TRANSITIONS ===== */
.pt-modal-fade-enter-active { animation: ptFadeIn 0.22s ease-out; }
.pt-modal-fade-leave-active { animation: ptFadeIn 0.16s ease-in reverse; }

@keyframes ptFadeIn { from { opacity: 0; } to { opacity: 1; } }

@media (max-width: 767px) {
  .pt-search { width: 100%; }
  .pt-grid { grid-template-columns: 1fr; }
  .pt-info-grid { grid-template-columns: 1fr; }
  .pt-modal-risk { flex-direction: column; }
}
</style>
