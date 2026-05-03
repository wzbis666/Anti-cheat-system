<template>
  <div class="st-terminal">
    <div class="st-header">
      <div class="st-prompt">
        <span class="st-prompt-sign">root@acs:~$</span>
        <span class="st-prompt-cmd">./config.edit --all</span>
        <span class="st-cursor">_</span>
      </div>
    </div>

    <div v-for="group in settingGroups" :key="group.key" class="st-group">
      <div class="st-group-header" @click="toggleGroup(group.key)">
        <div class="st-group-header-left">
          <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" :d="group.icon"/></svg>
          <span class="st-group-title">{{ group.title }}</span>
        </div>
        <svg :class="['st-toggle-icon', { collapsed: collapsedGroups.has(group.key) }]" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></svg>
      </div>
      <div :class="['st-group-body', { collapsed: collapsedGroups.has(group.key) }]">
        <div v-for="item in group.items" :key="item.key" class="st-item">
          <div class="st-item-info">
            <div class="st-item-label">{{ item.label }}</div>
            <div class="st-item-desc">{{ item.desc }}</div>
          </div>
          <div class="st-item-control">
            <div v-if="item.type === 'toggle'" :class="['st-toggle', { active: settings[item.key] }]" @click="settings[item.key] = !settings[item.key]">
              <div class="st-toggle-knob"></div>
            </div>
            <div v-else-if="item.type === 'number'" class="st-number">
              <button class="st-num-btn" @click="settings[item.key] = Math.max(item.min || 0, (settings[item.key] || 0) - (item.step || 1))">−</button>
              <input type="number" v-model.number="settings[item.key]" :min="item.min" :max="item.max" class="st-num-input" />
              <span v-if="item.unit" class="st-num-unit">{{ item.unit }}</span>
              <button class="st-num-btn" @click="settings[item.key] = Math.min(item.max || 999, (settings[item.key] || 0) + (item.step || 1))">+</button>
            </div>
            <select v-else-if="item.type === 'select'" v-model="settings[item.key]" class="st-select">
              <option v-for="opt in item.options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
            <input v-else-if="item.type === 'text'" v-model="settings[item.key]" class="st-input" :placeholder="item.placeholder" />
          </div>
        </div>
      </div>
    </div>

    <div class="st-actions">
      <button class="st-act-btn save" @click="saveSettings">
        <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/></svg>
        {{ t('settings.save') }}
      </button>
      <button class="st-act-btn sync" @click="syncToPlugin">
        <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H3V5h18v14zM9 8h2v8H9zm4 2h2v6h-2z"/></svg>
        {{ t('settings.syncToPlugin') }}
      </button>
      <button class="st-act-btn reset" @click="resetSettings">
        <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
        {{ t('settings.reset') }}
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
    const collapsedGroups = ref(new Set())
    const settings = reactive({
      flyDetectionEnabled: true, flyThreshold: 5, flyAutoKick: true,
      speedDetectionEnabled: true, speedThreshold: 3, speedAutoKick: true,
      killAuraDetectionEnabled: true, killAuraThreshold: 5, killAuraAutoKick: false,
      autoClickDetectionEnabled: true, autoClickThreshold: 15, autoClickAutoKick: false,
      autoBanEnabled: true, autoBanThreshold: 10, autoBanType: 'PERMANENT',
      autoBanDuration: 7, autoKickEnabled: true, autoKickThreshold: 5,
      warningMessage: '§c[AntiCheat] 检测到异常行为，请停止作弊！',
      kickMessage: '§c[AntiCheat] 你因作弊行为被踢出服务器！',
      banMessage: '§c[AntiCheat] 你因多次作弊被永久封禁！',
      alertSound: true, alertOnlyHighSeverity: false
    })

    const settingGroups = [
      { key: 'fly', title: t('settings.flyDetection'), icon: 'M12 2L2 22h20L12 2m0 4l7.5 14h-15L12 6z', items: [
        { key: 'flyDetectionEnabled', label: t('settings.enableFlyDetection'), desc: t('settings.enableFlyDetectionDesc'), type: 'toggle' },
        { key: 'flyThreshold', label: t('settings.triggerThreshold'), desc: t('settings.triggerThresholdDesc'), type: 'number', min: 1, max: 20, step: 1 },
        { key: 'flyAutoKick', label: t('settings.autoKick'), desc: t('settings.autoKickDesc'), type: 'toggle' }
      ]},
      { key: 'speed', title: t('settings.speedDetection'), icon: 'M13 2.05v2.02c3.95.49 7 3.85 7 7.93 0 1.45-.39 2.81-1.07 3.98l1.75 1.02C21.53 15.49 22 13.81 22 12c0-5.18-3.95-9.45-9-9.95zM12 20c-4.42 0-8-3.58-8-8 0-3.44 2.19-6.37 5.24-7.49L8.19 2.77C3.79 4.25 1 8.76 1 12c0 6.07 4.93 11 11 11', items: [
        { key: 'speedDetectionEnabled', label: t('settings.enableSpeedDetection'), desc: t('settings.enableSpeedDetectionDesc'), type: 'toggle' },
        { key: 'speedThreshold', label: t('settings.triggerThreshold'), desc: t('settings.triggerThresholdDesc'), type: 'number', min: 1, max: 20, step: 1 },
        { key: 'speedAutoKick', label: t('settings.autoKick'), desc: t('settings.autoKickDesc'), type: 'toggle' }
      ]},
      { key: 'killaura', title: t('settings.killAuraDetection'), icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z', items: [
        { key: 'killAuraDetectionEnabled', label: t('settings.enableKillAuraDetection'), desc: t('settings.enableKillAuraDetectionDesc'), type: 'toggle' },
        { key: 'killAuraThreshold', label: t('settings.triggerThreshold'), desc: t('settings.triggerThresholdDesc'), type: 'number', min: 1, max: 20, step: 1 },
        { key: 'killAuraAutoKick', label: t('settings.autoKick'), desc: t('settings.autoKickDesc'), type: 'toggle' }
      ]},
      { key: 'autoclick', title: t('settings.autoClickDetection'), icon: 'M13 14h-2v-4h2m0 8h-2v-2h2M1 5h22l-2 18H3L1 5z', items: [
        { key: 'autoClickDetectionEnabled', label: t('settings.enableAutoClickDetection'), desc: t('settings.enableAutoClickDetectionDesc'), type: 'toggle' },
        { key: 'autoClickThreshold', label: t('settings.cpsThreshold'), desc: t('settings.cpsThresholdDesc'), type: 'number', min: 5, max: 30, step: 1, unit: 'CPS' },
        { key: 'autoClickAutoKick', label: t('settings.autoKick'), desc: t('settings.autoKickDesc'), type: 'toggle' }
      ]},
      { key: 'punishment', title: t('settings.punishmentSettings'), icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z', items: [
        { key: 'autoBanEnabled', label: t('settings.enableAutoBan'), desc: t('settings.enableAutoBanDesc'), type: 'toggle' },
        { key: 'autoBanThreshold', label: t('settings.autoBanThreshold'), desc: t('settings.autoBanThresholdDesc'), type: 'number', min: 5, max: 50, step: 1 },
        { key: 'autoBanType', label: t('settings.banType'), desc: t('settings.banTypeDesc'), type: 'select', options: [{ value: 'PERMANENT', label: t('settings.permanentBan') }, { value: 'TEMPORARY', label: t('settings.temporaryBan') }] },
        { key: 'autoBanDuration', label: t('settings.tempBanDuration'), desc: t('settings.tempBanDurationDesc'), type: 'number', min: 1, max: 365, step: 1, unit: t('settings.days') },
        { key: 'autoKickEnabled', label: t('settings.enableAutoKick'), desc: t('settings.enableAutoKickDesc'), type: 'toggle' },
        { key: 'autoKickThreshold', label: t('settings.autoKickThreshold'), desc: t('settings.autoKickThresholdDesc'), type: 'number', min: 1, max: 20, step: 1 }
      ]},
      { key: 'messages', title: t('settings.messageSettings'), icon: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z', items: [
        { key: 'warningMessage', label: t('settings.warningMessage'), desc: t('settings.warningMessageDesc'), type: 'text', placeholder: '§c[AntiCheat] Warning' },
        { key: 'kickMessage', label: t('settings.kickMessage'), desc: t('settings.kickMessageDesc'), type: 'text', placeholder: '§c[AntiCheat] Kick' },
        { key: 'banMessage', label: t('settings.banMessage'), desc: t('settings.banMessageDesc'), type: 'text', placeholder: '§c[AntiCheat] Ban' }
      ]},
      { key: 'notifications', title: t('settings.notificationSettings'), icon: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z', items: [
        { key: 'alertSound', label: t('settings.alertSound'), desc: t('settings.alertSoundDesc'), type: 'toggle' },
        { key: 'alertOnlyHighSeverity', label: t('settings.onlyHighSeverity'), desc: t('settings.onlyHighSeverityDesc'), type: 'toggle' }
      ]}
    ]

    const toggleGroup = (key) => { const s = new Set(collapsedGroups.value); if (s.has(key)) s.delete(key); else s.add(key); collapsedGroups.value = s }

    const loadSettings = async () => {
      try {
        const data = await settingsApi.getAll()
        Object.keys(data).forEach(key => {
          if (settings.hasOwnProperty(key)) {
            const v = data[key]
            settings[key] = v
          }
        })
      } catch (e) { console.error(e) }
    }

    const saveSettings = async () => {
      try {
        await settingsApi.batchSave({ ...settings })
        ElMessage.success(t('settings.saveSuccess'))
      } catch (e) { ElMessage.error(t('common.error')) }
    }

    const syncToPlugin = async () => {
      try { await saveSettings(); ElMessage.success(t('settings.syncSuccess')) } catch (e) { ElMessage.error(t('common.error')) }
    }

    const resetSettings = () => {
      Object.keys(settings).forEach(k => {
        const defaults = { flyDetectionEnabled: true, flyThreshold: 5, flyAutoKick: true, speedDetectionEnabled: true, speedThreshold: 3, speedAutoKick: true, killAuraDetectionEnabled: true, killAuraThreshold: 5, killAuraAutoKick: false, autoClickDetectionEnabled: true, autoClickThreshold: 15, autoClickAutoKick: false, autoBanEnabled: true, autoBanThreshold: 10, autoBanType: 'PERMANENT', autoBanDuration: 7, autoKickEnabled: true, autoKickThreshold: 5, warningMessage: '§c[AntiCheat] 检测到异常行为，请停止作弊！', kickMessage: '§c[AntiCheat] 你因作弊行为被踢出服务器！', banMessage: '§c[AntiCheat] 你因多次作弊被永久封禁！', alertSound: true, alertOnlyHighSeverity: false }
        settings[k] = defaults[k]
      })
      ElMessage.success(t('settings.resetSuccess'))
    }

    onMounted(() => loadSettings())

    return { settings, settingGroups, collapsedGroups, toggleGroup, saveSettings, syncToPlugin, resetSettings, t }
  }
}
</script>

<style scoped>
.st-terminal { display: flex; flex-direction: column; gap: 16px; max-width: 900px; }

.st-header { margin-bottom: 4px; }
.st-prompt { font-family: var(--font-mono); font-size: 13px; }
.st-prompt-sign { color: #06b6d4; }
.st-prompt-cmd { color: #c084fc; margin-left: 8px; }
.st-cursor { color: #a855f7; animation: stBlink 1s step-end infinite; }
@keyframes stBlink { 0%,100%{opacity:1} 50%{opacity:0} }

.st-group {
  background: rgba(6,2,16,0.7);
  border: 2px solid rgba(147,51,234,0.18); border-radius: 2px;
  overflow: hidden;
}
.st-group-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 18px; cursor: pointer; user-select: none;
  transition: background 0.2s ease;
}
.st-group-header:hover { background: rgba(147,51,234,0.06); }
.st-group-header-left { display: flex; align-items: center; gap: 10px; }
.st-group-header-left svg { color: #a855f7; }
.st-group-title { font-family: var(--font-mono); font-size: 13px; font-weight: 600; color: #c084fc; text-transform: uppercase; letter-spacing: 1px; }
.st-toggle-icon { color: var(--text-muted); transition: transform 0.3s ease; }
.st-toggle-icon.collapsed { transform: rotate(-90deg); }

.st-group-body { padding: 0 18px 18px; display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.st-group-body.collapsed { display: none; }

.st-item { display: flex; flex-direction: column; gap: 6px; }
.st-item-info { display: flex; flex-direction: column; gap: 3px; }
.st-item-label { font-family: var(--font-mono); font-size: 12px; font-weight: 500; color: var(--text-secondary); }
.st-item-desc { font-family: var(--font-mono); font-size: 10px; color: var(--text-muted); opacity: 0.7; }
.st-item-control { display: flex; align-items: center; gap: 8px; margin-top: 2px; }

/* TOGGLE */
.st-toggle {
  width: 42px; height: 22px; border-radius: 2px;
  background: rgba(147,51,234,0.12); border: 2px solid rgba(147,51,234,0.2);
  cursor: pointer; position: relative; transition: all 0.2s ease;
}
.st-toggle.active {
  background: rgba(168,85,247,0.3); border-color: rgba(168,85,247,0.5);
  box-shadow: 0 0 10px rgba(147,51,234,0.25);
}
.st-toggle-knob {
  position: absolute; top: 2px; left: 2px;
  width: 14px; height: 14px; border-radius: 1px;
  background: var(--text-muted); transition: all 0.2s ease;
}
.st-toggle.active .st-toggle-knob {
  left: 22px; background: #c084fc;
  box-shadow: 0 0 6px rgba(168,85,247,0.5);
}

/* NUMBER */
.st-number { display: flex; align-items: center; gap: 4px; }
.st-num-btn {
  width: 28px; height: 28px; display: flex; align-items: center; justify-content: center;
  font-family: var(--font-mono); font-size: 14px; font-weight: 700;
  background: rgba(147,51,234,0.08); border: 2px solid rgba(147,51,234,0.18); border-radius: 2px;
  color: var(--text-muted); cursor: pointer; transition: all 0.15s ease;
}
.st-num-btn:hover { background: rgba(147,51,234,0.18); color: #c084fc; border-color: rgba(168,85,247,0.3); }
.st-num-input {
  width: 56px; height: 28px; text-align: center;
  font-family: var(--font-mono); font-size: 13px;
  background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.18); border-radius: 2px;
  color: var(--text-primary);
}
.st-num-input:focus { outline: none; border-color: rgba(168,85,247,0.35); }
.st-num-unit { font-family: var(--font-mono); font-size: 10px; color: var(--text-muted); text-transform: uppercase; }

/* SELECT & INPUT */
.st-select {
  padding: 7px 10px; font-family: var(--font-mono); font-size: 12px;
  background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.18); border-radius: 2px;
  color: var(--text-primary); width: 100%; transition: all 0.2s ease;
}
.st-select:focus { outline: none; border-color: rgba(168,85,247,0.35); }
.st-input {
  padding: 7px 10px; font-family: var(--font-mono); font-size: 12px;
  background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.18); border-radius: 2px;
  color: var(--text-primary); width: 100%; transition: all 0.2s ease;
}
.st-input:focus { outline: none; border-color: rgba(168,85,247,0.35); }

/* ACTIONS */
.st-actions { display: flex; gap: 10px; padding-top: 6px; }
.st-act-btn {
  display: flex; align-items: center; gap: 7px;
  padding: 10px 20px; font-family: var(--font-mono); font-size: 11px; font-weight: 700;
  border-radius: 2px; cursor: pointer; transition: all 0.2s ease;
  letter-spacing: 0.5px; text-transform: uppercase;
}
.st-act-btn.save { background: linear-gradient(180deg, #a855f7, #7c3aed); border: 2px solid rgba(147,51,234,0.4); color: #fff; }
.st-act-btn.save:hover { background: linear-gradient(180deg, #9333ea, #6d28d9); box-shadow: 0 0 16px rgba(147,51,234,0.3); }
.st-act-btn.sync { background: rgba(6,182,212,0.08); border: 2px solid rgba(6,182,212,0.25); color: #06b6d4; }
.st-act-btn.sync:hover { background: rgba(6,182,212,0.16); border-color: rgba(6,182,212,0.4); }
.st-act-btn.reset { background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.18); color: var(--text-muted); }
.st-act-btn.reset:hover { background: rgba(147,51,234,0.14); color: var(--text-primary); border-color: rgba(168,85,247,0.3); }

@media (max-width: 768px) { .st-group-body { grid-template-columns: 1fr; } }
</style>
