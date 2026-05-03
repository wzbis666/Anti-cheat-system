﻿<template>
  <div class="pf-terminal">
    <div class="pf-header">
      <div class="pf-prompt">
        <span class="pf-prompt-sign">root@acs:~$</span>
        <span class="pf-prompt-cmd">./admin.profile --view</span>
        <span class="pf-cursor">_</span>
      </div>
    </div>

    <div class="pf-card">
      <div class="pf-card-header">
        <div class="pf-avatar-box">
          <img :src="adminInfo.avatar || defaultAvatar" class="pf-avatar" />
          <div class="pf-avatar-border"></div>
        </div>
        <div class="pf-user-info">
          <div class="pf-user-name">{{ adminInfo.nickname || adminInfo.username }}</div>
          <span :class="['pf-role', adminInfo.role === 'SUPER_ADMIN' ? 'super' : 'admin']">
            {{ adminInfo.role === 'SUPER_ADMIN' ? t('profile.superAdmin') : t('profile.admin') }}
          </span>
        </div>
      </div>

      <div class="pf-tabs">
        <button :class="['pf-tab', { active: activeTab === 'info' }]" @click="activeTab = 'info'">
          > info.dat
        </button>
        <button :class="['pf-tab', { active: activeTab === 'password' }]" @click="activeTab = 'password'">
          > passwd
        </button>
      </div>

      <div class="pf-content">
        <div v-show="activeTab === 'info'" class="pf-form">
          <div class="pf-field">
            <label class="pf-field-label">$ username</label>
            <input :value="adminInfo.username" disabled class="pf-input disabled" />
          </div>
          <div class="pf-field">
            <label class="pf-field-label">$ nickname</label>
            <input v-model="profileForm.nickname" :placeholder="t('auth.nickname')" class="pf-input" />
          </div>
          <div class="pf-field">
            <label class="pf-field-label">$ email</label>
            <input v-model="profileForm.email" :placeholder="t('auth.email')" class="pf-input" />
          </div>
          <div class="pf-field">
            <label class="pf-field-label">$ created_at</label>
            <input :value="formatTime(adminInfo.createdTime)" disabled class="pf-input disabled" />
          </div>
          <div class="pf-field">
            <label class="pf-field-label">$ last_login</label>
            <input :value="formatTime(adminInfo.lastLoginTime)" disabled class="pf-input disabled" />
          </div>
          <button class="pf-btn save" @click="saveProfile" :disabled="saving">
            {{ saving ? t('profile.saving') : t('profile.saveChanges') }}
          </button>
        </div>

        <div v-show="activeTab === 'password'" class="pf-form">
          <div class="pf-field">
            <label class="pf-field-label">$ current_password</label>
            <input v-model="passwordForm.oldPassword" type="password" :placeholder="t('profile.currentPassword')" class="pf-input" />
          </div>
          <div class="pf-field">
            <label class="pf-field-label">$ new_password</label>
            <input v-model="passwordForm.newPassword" type="password" :placeholder="t('profile.newPassword')" class="pf-input" />
          </div>
          <div class="pf-field">
            <label class="pf-field-label">$ confirm_password</label>
            <input v-model="passwordForm.confirmPassword" type="password" :placeholder="t('profile.confirmNewPassword')" class="pf-input" />
          </div>
          <button class="pf-btn passwd" @click="changePassword" :disabled="changing">
            {{ changing ? t('profile.changing') : t('profile.changePassword') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'Profile',
  props: {
    admin: { type: Object, required: true }
  },
  emits: ['update-admin'],
  setup(props, { emit }) {
    const { t } = useI18n()
    
    const activeTab = ref('info')
    const saving = ref(false)
    const changing = ref(false)
    const adminInfo = ref(props.admin || {})
    const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    
    const profileForm = reactive({
      nickname: props.admin?.nickname || '',
      email: props.admin?.email || ''
    })
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const formatTime = (timestamp) => {
      if (!timestamp) return '-'
      return new Date(timestamp).toLocaleString()
    }
    
    const saveProfile = async () => {
      saving.value = true
      try {
        const result = await authApi.updateProfile(adminInfo.value.id, profileForm)
        if (result.success) {
          adminInfo.value = { ...adminInfo.value, ...result.admin }
          emit('update-admin', adminInfo.value)
          localStorage.setItem('admin', JSON.stringify(adminInfo.value))
          ElMessage.success(t('common.success'))
        } else {
          ElMessage.error(result.message || t('common.error'))
        }
      } catch (error) {
        console.error('Failed to save:', error)
        ElMessage.error(t('common.error'))
      } finally {
        saving.value = false
      }
    }
    
    const changePassword = async () => {
      if (!passwordForm.oldPassword || !passwordForm.newPassword) {
        ElMessage.warning(t('common.warning'))
        return
      }
      if (passwordForm.newPassword.length < 6) {
        ElMessage.warning(t('common.warning'))
        return
      }
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        ElMessage.warning(t('common.warning'))
        return
      }
      
      changing.value = true
      try {
        const result = await authApi.changePassword(adminInfo.value.id, passwordForm.oldPassword, passwordForm.newPassword)
        if (result.success) {
          ElMessage.success(t('common.success'))
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          ElMessage.error(result.message || t('common.error'))
        }
      } catch (error) {
        console.error('Failed to change password:', error)
        ElMessage.error(t('common.error'))
      } finally {
        changing.value = false
      }
    }
    
    onMounted(() => {
      profileForm.nickname = adminInfo.value.nickname || ''
      profileForm.email = adminInfo.value.email || ''
    })
    
    return {
      activeTab, adminInfo, defaultAvatar, profileForm, passwordForm, saving, changing,
      formatTime, saveProfile, changePassword, t
    }
  }
}
</script>

<style scoped>
.pf-terminal { display: flex; flex-direction: column; gap: 20px; max-width: 500px; }

.pf-header { margin-bottom: 4px; }
.pf-prompt { font-family: var(--font-mono); font-size: 13px; }
.pf-prompt-sign { color: #06b6d4; }
.pf-prompt-cmd { color: #c084fc; margin-left: 8px; }
.pf-cursor { color: #a855f7; animation: pfBlink 1s step-end infinite; }
@keyframes pfBlink { 0%,100%{opacity:1} 50%{opacity:0} }

.pf-card {
  background: rgba(6,2,16,0.8);
  border: 2px solid rgba(147,51,234,0.22); border-radius: 2px;
  overflow: hidden;
}

.pf-card-header {
  display: flex; align-items: center; gap: 20px;
  padding: 24px; border-bottom: 2px solid rgba(147,51,234,0.16);
}
.pf-avatar-box { position: relative; }
.pf-avatar { width: 72px; height: 72px; border-radius: 2px; image-rendering: pixelated; display: block; }
.pf-avatar-border { position: absolute; inset: 0; border: 2px solid rgba(168,85,247,0.35); border-radius: 2px; pointer-events: none; }
.pf-user-name { font-family: var(--font-mono); font-size: 18px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
.pf-role { display: inline-block; padding: 3px 12px; font-family: var(--font-mono); font-size: 10px; font-weight: 700; border-radius: 2px; text-transform: uppercase; letter-spacing: 1px; }
.pf-role.super { background: linear-gradient(135deg, #a855f7, #7c3aed); color: #fff; }
.pf-role.admin { background: rgba(6,182,212,0.15); color: #06b6d4; border: 1px solid rgba(6,182,212,0.3); }

.pf-tabs { display: flex; border-bottom: 2px solid rgba(147,51,234,0.16); }
.pf-tab {
  flex: 1; padding: 14px; font-family: var(--font-mono); font-size: 12px; font-weight: 500;
  background: transparent; border: none; color: var(--text-muted); cursor: pointer;
  transition: all 0.2s ease; letter-spacing: 0.5px;
}
.pf-tab:hover { color: var(--text-primary); background: rgba(147,51,234,0.05); }
.pf-tab.active { color: #c084fc; border-bottom: 2px solid #a855f7; background: rgba(147,51,234,0.04); }

.pf-content { padding: 24px; }
.pf-form { display: flex; flex-direction: column; gap: 16px; }
.pf-field { display: flex; flex-direction: column; gap: 5px; }
.pf-field-label { font-family: var(--font-mono); font-size: 10px; font-weight: 600; color: #06b6d4; text-transform: uppercase; letter-spacing: 0.5px; }
.pf-input {
  width: 100%; padding: 11px 14px; font-family: var(--font-mono); font-size: 13px;
  background: rgba(147,51,234,0.05); border: 2px solid rgba(147,51,234,0.15); border-radius: 2px;
  color: var(--text-primary); transition: all 0.2s ease;
}
.pf-input:focus { outline: none; border-color: rgba(168,85,247,0.4); box-shadow: 0 0 10px rgba(147,51,234,0.1); }
.pf-input.disabled { opacity: 0.55; cursor: not-allowed; }
.pf-input::placeholder { color: var(--text-muted); }

.pf-btn {
  width: 100%; padding: 12px; font-family: var(--font-mono); font-size: 12px; font-weight: 700;
  border-radius: 2px; cursor: pointer; transition: all 0.2s ease;
  letter-spacing: 0.5px; text-transform: uppercase; margin-top: 4px;
}
.pf-btn.save { background: linear-gradient(180deg, #a855f7, #7c3aed); border: 2px solid rgba(147,51,234,0.4); color: #fff; }
.pf-btn.save:hover:not(:disabled) { background: linear-gradient(180deg, #9333ea, #6d28d9); box-shadow: 0 0 16px rgba(147,51,234,0.3); }
.pf-btn.save:disabled { opacity: 0.4; cursor: not-allowed; }
.pf-btn.passwd { background: rgba(245,158,11,0.08); border: 2px solid rgba(245,158,11,0.3); color: #f59e0b; }
.pf-btn.passwd:hover:not(:disabled) { background: rgba(245,158,11,0.18); border-color: rgba(245,158,11,0.5); }
.pf-btn.passwd:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
