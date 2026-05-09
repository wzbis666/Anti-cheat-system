<template>
  <div class="settings-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ t('nav.settings') }}</h2>
        <span class="page-desc">{{ t('settings.subtitle') }}</span>
      </div>

      <div class="header-actions">
        <button class="action-btn reset-btn" @click="resetSettings">
          <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
          {{ t('settings.reset') }}
        </button>
        <button class="action-btn save-btn" @click="saveSettings">
          <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zM9 8h2v8H9zm4 2h2v6h-2z"/></svg>
          {{ t('settings.save') }}
        </button>
      </div>
    </div>

    <div class="settings-grid">
      <div v-for="group in settingGroups" :key="group.key" class="setting-card">
        <div class="card-header" @click="toggleGroup(group.key)">
          <div class="header-left">
            <div class="card-icon" :class="group.key">
              <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" :d="group.icon"/></svg>
            </div>
            <div class="header-info">
              <span class="card-title">{{ t(`settings.${group.key}Title`) }}</span>
              <span class="card-desc">{{ t(`settings.${group.key}Desc`) }}</span>
            </div>
          </div>
          <svg :class="['toggle-icon', { collapsed: collapsedGroups.has(group.key) }]" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></svg>
        </div>
        <div :class="['card-body', { collapsed: collapsedGroups.has(group.key) }]">
          <div v-for="item in group.items" :key="item.key" class="setting-item">
            <div class="item-info">
              <div class="item-label">{{ t(`settings.${item.key}`) }}</div>
              <div class="item-desc">{{ t(`settings.${item.key}Desc`) }}</div>
            </div>
            <div class="item-control">
              <div v-if="item.type === 'toggle'" :class="['toggle-switch', { active: settings[item.key] }]" @click="toggleSetting(item.key)">
                <div class="toggle-knob"></div>
              </div>
              <div v-else-if="item.type === 'number'" class="number-control">
                <button class="num-btn" @click="adjustNumber(item, -1)" :disabled="(settings[item.key] || 0) <= (item.min || 0)">−</button>
                <input type="number" v-model.number="settings[item.key]" :min="item.min" :max="item.max" class="num-input" />
                <span v-if="item.unit" class="num-unit">{{ item.unit }}</span>
                <button class="num-btn" @click="adjustNumber(item, 1)" :disabled="(settings[item.key] || 0) >= (item.max || 999)">+</button>
              </div>
              <select v-else-if="item.type === 'select'" v-model="settings[item.key]" class="select-control">
                <option v-for="opt in item.options" :key="opt.value" :value="opt.value">{{ t(`settings.${opt.label}`) || opt.label }}</option>
              </select>
              <input v-else-if="item.type === 'text'" v-model="settings[item.key]" class="text-input" :placeholder="item.placeholder" />
              <input v-else-if="item.type === 'password'" v-model="settings[item.key]" class="text-input" type="password" :placeholder="item.placeholder" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="sync-panel">
      <div class="panel-icon">
        <svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H3V5h18v14zM9 8h2v8H9zm4 2h2v6h-2z"/></svg>
      </div>
      <div class="panel-content">
        <span class="panel-title">{{ t('settings.syncTitle') }}</span>
        <span class="panel-desc">{{ t('settings.syncDesc') }}</span>
      </div>
      <button class="sync-btn" @click="syncToPlugin">
        {{ t('settings.syncToPlugin') }}
      </button>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { settingsApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'Settings',
  setup() {
    const { t } = useI18n()

    const settings = reactive({})
    const originalSettings = reactive({})
    const collapsedGroups = ref(new Set(['advanced']))

    const settingGroups = [
      {
        key: 'detection',
        icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z',
        items: [
          { key: 'enableFlyingDetection', type: 'toggle', default: true },
          { key: 'enableSpeedDetection', type: 'toggle', default: true },
          { key: 'enableKillAuraDetection', type: 'toggle', default: true },
          { key: 'enableAutoClickDetection', type: 'toggle', default: true },
          { key: 'detectionThreshold', type: 'number', default: 3, min: 1, max: 20 },
          { key: 'alertCooldown', type: 'number', default: 60, min: 10, max: 300, unit: 's' }
        ]
      },
      {
        key: 'punishment',
        icon: 'M12 2L2 22h20L12 2m0 4l7.5 14h-15L12 6z',
        items: [
          { key: 'autoBanEnabled', type: 'toggle', default: false },
          { key: 'banThreshold', type: 'number', default: 10, min: 3, max: 50 },
          { key: 'defaultBanDuration', type: 'select', default: '24h', options: [
            { value: '1h', label: '1小时' },
            { value: '6h', label: '6小时' },
            { value: '24h', label: '24小时' },
            { value: '7d', label: '7天' },
            { value: '30d', label: '30天' },
            { value: 'permanent', label: '永久' }
          ]},
          { key: 'enableTempBan', type: 'toggle', default: true }
        ]
      },
      {
        key: 'notification',
        icon: 'M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0',
        items: [
          { key: 'enableSoundAlert', type: 'toggle', default: false },
          { key: 'enableDesktopAlert', type: 'toggle', default: true },
          { key: 'highRiskAlert', type: 'toggle', default: true },
          { key: 'reportAlert', type: 'toggle', default: true }
        ]
      },
      {
        key: 'system',
        icon: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
        items: [
          { key: 'autoRefresh', type: 'toggle', default: true },
          { key: 'refreshInterval', type: 'number', default: 30, min: 5, max: 300, unit: 's' },
          { key: 'maxLogEntries', type: 'number', default: 1000, min: 100, max: 5000 },
          { key: 'language', type: 'select', default: 'zh', options: [
            { value: 'zh', label: '中文' },
            { value: 'en', label: 'English' }
          ]}
        ]
      },
      {
        key: 'advanced',
        icon: 'M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.39-1.08-.7-1.66-.94l-.38-2.65c-.03-.24-.24-.42-.48-.42h-4c-.24 0-.45.18-.48.42l-.38 2.65c-.58.24-1.14.55-1.66.94l-2.49-1c-.22-.08-.49 0-.61.22l2 3.46c.12.22.07.49.12.64l2.11 1.65c-.04.32-.07.64-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.39.3.61.22l2.49-1c.52.39 1.08.7 1.66.94l.38 2.65c.03.24.24.42.48.42h4c.24 0 .45-.18.48-.42l.38-2.65c.58-.24 1.14-.55 1.66-.94l2.49 1c.22.08.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z',
        items: [
          { key: 'enableDebug', type: 'toggle', default: false },
          { key: 'logLevel', type: 'select', default: 'info', options: [
            { value: 'debug', label: 'Debug' },
            { value: 'info', label: 'Info' },
            { value: 'warn', label: 'Warn' },
            { value: 'error', label: 'Error' }
          ]},
          { key: 'apiTimeout', type: 'number', default: 30, min: 5, max: 120, unit: 's' },
          { key: 'maxConnections', type: 'number', default: 100, min: 10, max: 500 }
        ]
      }
    ]

    const toggleGroup = (key) => {
      if (collapsedGroups.value.has(key)) {
        collapsedGroups.value.delete(key)
      } else {
        collapsedGroups.value.add(key)
      }
    }

    const toggleSetting = (key) => {
      settings[key] = !settings[key]
    }

    const adjustNumber = (item, delta) => {
      const current = settings[item.key] || item.default || 0
      const step = item.step || 1
      const newValue = current + delta * step
      if (newValue >= (item.min || 0) && newValue <= (item.max || 999)) {
        settings[item.key] = newValue
      }
    }

    const loadSettings = async () => {
      try {
        const data = await settingsApi.getAll()
        settingGroups.forEach(group => {
          group.items.forEach(item => {
            settings[item.key] = data[item.key] ?? item.default
            originalSettings[item.key] = settings[item.key]
          })
        })
      } catch (error) {
        settingGroups.forEach(group => {
          group.items.forEach(item => {
            settings[item.key] = item.default
            originalSettings[item.key] = settings[item.key]
          })
        })
      }
    }

    const saveSettings = async () => {
      try {
        await settingsApi.batchSave(settings)
        Object.assign(originalSettings, settings)
        ElMessage.success(t('common.success'))
      } catch (error) {
        ElMessage.error(t('common.error'))
      }
    }

    const resetSettings = () => {
      settingGroups.forEach(group => {
        group.items.forEach(item => {
          settings[item.key] = item.default
        })
      })
      ElMessage.info(t('settings.resetSuccess'))
    }

    const syncToPlugin = async () => {
      try {
        const pluginSettings = convertToPluginFormat(settings)
        await settingsApi.savePluginSettings(pluginSettings)
        ElMessage.success(t('settings.syncSuccess'))
      } catch (error) {
        ElMessage.error(t('common.error'))
      }
    }

    const convertToPluginFormat = (frontendSettings) => {
      const mapping = {
        'enableFlyingDetection': 'detect.fly',
        'enableSpeedDetection': 'detect.speed',
        'enableAutoClickDetection': 'detect.autoclick',
        'enableKillAuraDetection': 'detect.killaura',
        'detectionThreshold': 'threshold.violation',
        'autoClickThreshold': 'threshold.autoclick',
        'speedThreshold': 'threshold.speed',
        'banThreshold': 'cheat.fly.perm_ban_threshold',
        'autoKickThreshold': 'cheat.fly.kick_threshold'
      }
      
      const result = {}
      Object.keys(mapping).forEach(frontendKey => {
        if (frontendSettings[frontendKey] !== undefined) {
          result[mapping[frontendKey]] = frontendSettings[frontendKey]
        }
      })
      
      return result
    }

    onMounted(() => {
      loadSettings()
    })

    return {
      settings,
      collapsedGroups,
      settingGroups,
      toggleGroup,
      toggleSetting,
      adjustNumber,
      saveSettings,
      resetSettings,
      syncToPlugin,
      t
    }
  }
}
</script>

<style scoped>
.settings-container { padding: 20px; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-family: var(--font-sans); font-size: 20px; font-weight: 700; color: var(--accent-gold); margin: 0; }
.page-desc { font-size: 13px; color: var(--text-muted); }

.header-actions { display: flex; gap: 10px; }
.action-btn { display: flex; align-items: center; gap: 6px; padding: 10px 16px; border-radius: var(--radius-sm); font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; }
.action-btn.reset-btn { background: rgba(231, 76, 60, 0.1); border: 1px solid rgba(231, 76, 60, 0.3); color: #E74C3C; }
.action-btn.reset-btn:hover { background: rgba(231, 76, 60, 0.2); border-color: #E74C3C; }
.action-btn.save-btn { background: rgba(46, 204, 113, 0.1); border: 1px solid rgba(46, 204, 113, 0.3); color: #2ECC71; }
.action-btn.save-btn:hover { background: rgba(46, 204, 113, 0.2); border-color: #2ECC71; }

.settings-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(480px, 1fr)); gap: 20px; margin-bottom: 20px; }

.setting-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: var(--bg-tertiary); cursor: pointer; transition: all 0.2s ease; }
.card-header:hover { background: var(--bg-hover); }
.card-header .header-left { display: flex; align-items: center; gap: 12px; }
.card-icon { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: var(--radius-sm); color: var(--accent-gold); }
.card-icon.detection { background: rgba(231, 76, 60, 0.1); }
.card-icon.punishment { background: rgba(255, 140, 0, 0.1); }
.card-icon.notification { background: rgba(74, 158, 255, 0.1); }
.card-icon.system { background: rgba(46, 204, 113, 0.1); }
.card-icon.advanced { background: rgba(155, 89, 182, 0.1); }
.header-info { display: flex; flex-direction: column; gap: 2px; }
.card-title { font-family: var(--font-sans); font-size: 14px; font-weight: 600; color: var(--text-primary); }
.card-desc { font-size: 11px; color: var(--text-muted); }
.toggle-icon { color: var(--text-muted); transition: transform 0.2s ease; }
.toggle-icon.collapsed { transform: rotate(-90deg); }

.card-body { padding: 16px; border-top: 1px solid var(--border-color); transition: all 0.2s ease; }
.card-body.collapsed { display: none; }

.setting-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--border-color); }
.setting-item:last-child { border-bottom: none; }
.item-info { display: flex; flex-direction: column; gap: 4px; }
.item-label { font-size: 13px; color: var(--text-primary); }
.item-desc { font-size: 11px; color: var(--text-muted); max-width: 280px; }

.item-control { display: flex; align-items: center; gap: 8px; }

.toggle-switch { width: 48px; height: 26px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: 13px; cursor: pointer; position: relative; transition: all 0.2s ease; }
.toggle-switch.active { background: var(--accent-gold-dim); border-color: var(--accent-gold); }
.toggle-knob { position: absolute; top: 3px; left: 3px; width: 18px; height: 18px; background: var(--text-secondary); border-radius: 50%; transition: all 0.2s ease; }
.toggle-switch.active .toggle-knob { left: 27px; background: var(--accent-gold); }

.number-control { display: flex; align-items: center; gap: 4px; }
.num-btn { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-secondary); cursor: pointer; font-size: 14px; font-weight: 600; transition: all 0.2s ease; }
.num-btn:hover:not(:disabled) { border-color: var(--accent-gold); color: var(--accent-gold); }
.num-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.num-input { width: 60px; height: 28px; padding: 0 8px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-primary); font-size: 13px; text-align: center; outline: none; }
.num-input:focus { border-color: var(--accent-gold); }
.num-unit { font-size: 12px; color: var(--text-muted); }

.select-control { padding: 6px 12px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-primary); font-size: 12px; cursor: pointer; outline: none; }
.select-control:focus { border-color: var(--accent-gold); }

.text-input { padding: 6px 12px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-primary); font-size: 12px; outline: none; width: 180px; }
.text-input:focus { border-color: var(--accent-gold); }
.text-input::placeholder { color: var(--text-muted); }

.sync-panel { display: flex; align-items: center; gap: 16px; padding: 20px; background: linear-gradient(135deg, var(--accent-gold-dim) 0%, var(--bg-card) 100%); border: 1px solid var(--border-gold); border-radius: var(--radius-md); }
.panel-icon { color: var(--accent-gold); }
.panel-content { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.sync-panel .panel-title { font-family: var(--font-sans); font-size: 14px; font-weight: 600; color: var(--text-primary); }
.sync-panel .panel-desc { font-size: 12px; color: var(--text-muted); }
.sync-btn { padding: 10px 24px; background: var(--accent-gold); border: none; border-radius: var(--radius-sm); color: #0a0a0f; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s ease; }
.sync-btn:hover { background: var(--accent-gold-light); box-shadow: var(--shadow-gold-strong); }
</style>