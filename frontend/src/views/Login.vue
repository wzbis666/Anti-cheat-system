<template>
  <div class="portal-page">
    <div class="portal-bg" :class="{ 'bg-fading': exiting }">
      <video ref="bgVideoRef" class="bg-video" autoplay loop muted playsinline disablePictureInPicture>
        <source src="/bg/nether-portal.mp4" type="video/mp4" />
      </video>
      <button class="sound-toggle" @click="toggleSound" :title="isMuted ? '开启声音' : '静音'">
        <svg v-if="isMuted" viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z"/></svg>
        <svg v-else viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/></svg>
      </button>
      <div class="bg-overlay"></div>
      <div class="bg-portal-aura"></div>

      <div class="ash-layer">
        <div
          v-for="i in 35"
          :key="'ash-' + i"
          class="ash-particle"
          :style="ashStyle(i)"
        ></div>
      </div>

      <div class="soul-layer">
        <div
          v-for="i in 6"
          :key="'soul-' + i"
          class="soul-particle"
          :style="soulStyle(i)"
        ></div>
      </div>
    </div>

    <div class="portal-container" :class="{ 'container-fading': exiting }">
      <div class="portal-frame">
        <div class="obsidian-border">
          <div class="obsidian-stripe" v-for="s in 8" :key="'stripe-' + s"></div>
        </div>
        <div class="portal-glow-pulse"></div>

        <div class="portal-card">
          <div class="portal-vortex">
            <div class="vortex-layer v1"></div>
            <div class="vortex-layer v2"></div>
            <div class="vortex-layer v3"></div>
          </div>

          <div class="portal-content">
            <div class="pearl-section">
              <div class="ender-eye">
                <svg viewBox="0 0 56 64" width="56" height="64" class="shield-icon">
                  <path d="M28 2 L52 12 L52 32 Q52 50 28 62 Q4 50 4 32 L4 12 Z" fill="#1a1a1a" stroke="#FFC800" stroke-width="2.5"/>
                  <path d="M28 6 L48 14 L48 32 Q48 47 28 58 Q8 47 8 32 L8 14 Z" fill="#0d0d0d"/>
                  <path d="M28 6 L48 14 L48 22 L28 14 Z" fill="#FFC800" opacity="0.9"/>
                  <path d="M28 6 L8 14 L8 22 L28 14 Z" fill="#CC9F00" opacity="0.9"/>
                  <path d="M28 14 L48 22 L48 32 Q48 47 28 58 L28 14 Z" fill="#1a1a1a"/>
                  <path d="M28 14 L8 22 L8 32 Q8 47 28 58 L28 14 Z" fill="#111111"/>
                  <path d="M20 32 L26 38 L38 26" fill="none" stroke="#FFC800" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M28 2 L52 12 L52 32 Q52 50 28 62 Q4 50 4 32 L4 12 Z" fill="none" stroke="#FFC800" stroke-width="2.5" opacity="0.6"/>
                </svg>
              </div>
            </div>

            <div class="title-section">
              <h1 class="portal-title">ANTICHEAT</h1>
              <p class="portal-subtitle">{{ t('nav.management') }}</p>
            </div>

            <div class="role-toggle">
              <button
                :class="['role-btn', { active: loginType === 'admin' }]"
                @click="loginType = 'admin'"
              >
                <span class="role-icon">
                  <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
                </span>
                OP
              </button>
              <button
                :class="['role-btn', { active: loginType === 'user' }]"
                @click="loginType = 'user'"
              >
                <span class="role-icon">
                  <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/></svg>
                </span>
                Player
              </button>
            </div>

            <form class="portal-form" @submit.prevent="handleSubmit">
              <div class="field-group f1">
                <div class="field-wrapper">
                  <span class="field-prefix">
                    <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/></svg>
                  </span>
                  <input
                    v-model="formData.username"
                    type="text"
                    class="field-input"
                    :placeholder="t('auth.usernamePlaceholder')"
                  />
                </div>
              </div>

              <div class="field-group f2">
                <div class="field-wrapper">
                  <span class="field-prefix">
                    <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg>
                  </span>
                  <input
                    v-model="formData.password"
                    :type="showPassword ? 'text' : 'password'"
                    class="field-input"
                    :placeholder="t('auth.passwordPlaceholder')"
                  />
                  <button type="button" class="eye-toggle" @click="showPassword = !showPassword">
                    <svg v-if="showPassword" viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                    <svg v-else viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M11.83 9L15 12.16V12a3 3 0 00-3-3h-.17m-4.3.84l1.55 1.55c-.05.2-.08.4-.08.61a3 3 0 003 3c.21 0 .41-.03.61-.08l1.55 1.55c-.66.33-1.39.53-2.16.53a5 5 0 01-5-5c0-.77.2-1.5.53-2.16M2 4.27l2.12 2.12C2.68 7.74 1.5 9.76 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l2.03 2.03L20 19.74 3.27 3 2 4.27z"/></svg>
                  </button>
                </div>
              </div>

              <button
                type="submit"
                class="portal-btn f3"
                :class="{ loading: loading }"
                :disabled="loading"
              >
                <span v-if="loading" class="btn-loading-content">
                  <span class="portal-spinner"></span>
                  {{ t('auth.signingIn') }}
                </span>
                <span v-else>
                  <span class="btn-label">{{ t('auth.login') }}</span>
                  <span class="btn-glow"></span>
                </span>
              </button>

              <button
                type="button"
                class="forgot-link f4"
                @click="showForgot = true"
              >
                {{ t('auth.forgotPassword') }}
              </button>

              <button
                v-if="loginType === 'user'"
                type="button"
                class="register-link f5"
                @click="showRegister = true"
              >
                {{ t('auth.noAccount') }}
              </button>
            </form>

            <div class="portal-lang">
              <svg viewBox="0 0 24 24" width="14" height="14" class="lang-icon"><path fill="currentColor" d="M12.87 15.07l-2.54-2.51.03-.03A17.52 17.52 0 0014.07 6H17V4h-7V2H8v2H1v2h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-.59v-.45zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z"/></svg>
              <button :class="['lang-btn', { active: currentLocale === 'zh' }]" @click="switchLanguage('zh')">中文</button>
              <span class="lang-sep"></span>
              <button :class="['lang-btn', { active: currentLocale === 'en' }]" @click="switchLanguage('en')">EN</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <transition name="travel-fade">
      <div v-if="exiting" class="portal-travel">
        <div class="travel-glow"></div>
        <div class="travel-grid"></div>
        <div class="travel-vignette"></div>
        <div class="travel-center-burst"></div>
        <div class="travel-label">{{ t('auth.entering') }}</div>
      </div>
    </transition>

    <transition name="dialog-fade">
      <div v-if="showRegister" class="register-overlay" @click.self="showRegister = false">
        <div class="register-modal">
          <div class="rm-header">
            <h3>{{ t('auth.register') }}</h3>
            <button class="rm-close" @click="showRegister = false">
              <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
            </button>
          </div>
          <form class="register-form" @submit.prevent="handleRegister">
            <div class="field-group">
              <label class="rm-label">{{ t('auth.username') }} *</label>
              <input v-model="registerForm.username" class="rm-input" :placeholder="t('auth.nicknamePlaceholder')" />
            </div>
            <div class="field-group">
              <label class="rm-label">{{ t('auth.password') }} *</label>
              <input v-model="registerForm.password" type="password" class="rm-input" :placeholder="t('auth.passwordMinPlaceholder')" />
            </div>
            <div class="field-group">
              <label class="rm-label">{{ t('auth.confirmPassword') }} *</label>
              <input v-model="registerForm.confirmPassword" type="password" class="rm-input" :placeholder="t('auth.confirmPasswordPlaceholder')" />
            </div>
            <div class="field-group">
              <label class="rm-label">{{ t('auth.nickname') }}</label>
              <input v-model="registerForm.nickname" class="rm-input" :placeholder="t('common.info')" />
            </div>
            <div class="field-group">
              <label class="rm-label">{{ t('auth.email') }}</label>
              <input v-model="registerForm.email" type="email" class="rm-input" :placeholder="t('common.info')" />
            </div>
            <div class="rm-actions">
              <button type="button" class="rm-btn cancel" @click="showRegister = false">{{ t('common.cancel') }}</button>
              <button type="submit" class="rm-btn confirm" :disabled="registerLoading">
                {{ registerLoading ? t('auth.registering') : t('auth.register') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>

    <transition name="dialog-fade">
      <div v-if="showForgot" class="register-overlay" @click.self="showForgot = false">
        <div class="forgot-modal">
          <div class="rm-header">
            <h3>{{ t('auth.forgotPasswordTitle') }}</h3>
            <button class="rm-close" @click="showForgot = false; resetResult = null">
              <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
            </button>
          </div>
          <div class="forgot-body">
            <p class="forgot-desc">{{ t('auth.forgotPasswordDesc') }}</p>

            <div class="field-group">
              <label class="rm-label">{{ t('auth.username') }}</label>
              <input v-model="forgotUsername" class="rm-input" :placeholder="t('auth.usernamePlaceholder')" :disabled="resetLoading" />
            </div>

            <transition name="dialog-fade">
              <div v-if="resetResult" class="reset-result">
                <div v-if="resetResult.success" class="reset-success">
                  <div class="success-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path fill="#4ade80" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
                  </div>
                  <p class="reset-msg">{{ t('auth.resetSuccess') }}</p>
                  <div class="new-pwd-box">
                    <span class="new-pwd-label">{{ t('auth.newPassword') }}:</span>
                    <code class="new-pwd-value">{{ resetResult.newPassword }}</code>
                    <button class="copy-btn" @click="copyNewPassword">{{ t('auth.copyAndLogin') }}</button>
                  </div>
                </div>
                <div v-else class="reset-error">
                  <p>{{ resetResult.message }}</p>
                </div>
              </div>
            </transition>

            <div class="rm-actions" v-if="!resetResult">
              <button type="button" class="rm-btn cancel" @click="showForgot = false">{{ t('common.cancel') }}</button>
              <button type="button" class="rm-btn confirm" @click="handleForgotPassword" :disabled="resetLoading">
                {{ resetLoading ? t('auth.resetting') : t('auth.resetPassword') }}
              </button>
            </div>
            <div class="rm-actions" v-else>
              <button type="button" class="rm-btn confirm" @click="showForgot = false; resetResult = null; forgotUsername = ''">{{ t('common.close') }}</button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'
import { setLocale } from '../i18n'

export default {
  name: 'Login',
  emits: ['login-success'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const loginType = ref('admin')
    const bgVideoRef = ref(null)
    const isMuted = ref(true)
    const showPassword = ref(false)
    const loading = ref(false)
    const exiting = ref(false)
    const showRegister = ref(false)
    const registerLoading = ref(false)
    const showForgot = ref(false)
    const resetLoading = ref(false)
    const forgotUsername = ref('')
    const resetResult = ref(null)
    const currentLocale = ref(localStorage.getItem('locale') || 'zh')

    const formData = reactive({
      username: '',
      password: ''
    })

    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      nickname: '',
      email: ''
    })

    const switchLanguage = (locale) => {
      setLocale(locale)
      currentLocale.value = locale
    }

    const toggleSound = () => {
      if (!bgVideoRef.value) return
      isMuted.value = !isMuted.value
      bgVideoRef.value.muted = isMuted.value
    }

    const handleSubmit = async () => {
      if (!formData.username || !formData.password) {
        ElMessage.warning(t('auth.usernameRequired'))
        return
      }

      loading.value = true
      try {
        const result = loginType.value === 'admin'
          ? await authApi.login(formData.username, formData.password)
          : await authApi.userLogin(formData.username, formData.password)

        if (result.success) {
          const userData = { ...result.data.user, userType: loginType.value }
          const storageKey = loginType.value === 'admin' ? 'admin' : 'user'
          localStorage.setItem(storageKey, JSON.stringify(userData))
          if (result.data.token) {
            localStorage.setItem('token', result.data.token)
          }

          exiting.value = true
          setTimeout(() => {
            ElMessage.success(t('auth.loginSuccess'))
            emit('login-success', userData)
          }, 2500)
        } else {
          ElMessage.error(result.message || 'Login failed')
          triggerShake()
        }
      } catch (error) {
        console.error('Login failed:', error)
        ElMessage.error(t('auth.loginError'))
        triggerShake()
      } finally {
        loading.value = false
      }
    }

    const triggerShake = () => {
      const frame = document.querySelector('.portal-frame')
      if (frame) {
        frame.classList.add('shake')
        setTimeout(() => frame.classList.remove('shake'), 500)
      }
    }

    const handleRegister = async () => {
      if (!registerForm.username || registerForm.username.length < 3) {
        ElMessage.warning(t('auth.usernameMinLength'))
        return
      }
      if (!registerForm.password || registerForm.password.length < 6) {
        ElMessage.warning(t('auth.passwordMinLength'))
        return
      }
      if (registerForm.password !== registerForm.confirmPassword) {
        ElMessage.warning(t('auth.passwordMismatch'))
        return
      }

      registerLoading.value = true
      try {
        const result = await authApi.register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname,
          email: registerForm.email
        })

        if (result.success) {
          if (result.data.token) {
            localStorage.setItem('token', result.data.token)
          }
          ElMessage.success(t('auth.registerSuccess'))
          showRegister.value = false
          loginType.value = 'user'
          formData.username = registerForm.username
          registerForm.username = ''
          registerForm.password = ''
          registerForm.confirmPassword = ''
          registerForm.nickname = ''
          registerForm.email = ''
        } else {
          ElMessage.error(result.message || 'Registration failed')
        }
      } catch (error) {
        console.error('Registration failed:', error)
        ElMessage.error('Registration failed. Please try again.')
      } finally {
        registerLoading.value = false
      }
    }

    const handleForgotPassword = async () => {
      if (!forgotUsername.value.trim()) {
        ElMessage.warning(t('auth.usernameRequired'))
        return
      }
      resetLoading.value = true
      resetResult.value = null
      try {
        const result = await authApi.forgotPassword(forgotUsername.value.trim(), loginType.value)
        resetResult.value = result
        if (result.success) {
          ElMessage.success(t('auth.resetSuccess'))
        }
      } catch (error) {
        console.error('Password reset failed:', error)
        resetResult.value = { success: false, message: t('auth.loginError') }
      } finally {
        resetLoading.value = false
      }
    }

    const copyNewPassword = async () => {
      if (resetResult.value?.newPassword) {
        try {
          await navigator.clipboard.writeText(resetResult.value.newPassword)
          formData.username = forgotUsername.value
          formData.password = ''
          showForgot.value = false
          resetResult.value = null
          forgotUsername.value = ''
          ElMessage.success(t('auth.resetSuccess'))
        } catch {
          formData.username = forgotUsername.value
          formData.password = resetResult.value.newPassword
          showForgot.value = false
          resetResult.value = null
          forgotUsername.value = ''
        }
      }
    }

    const ashStyle = (i) => ({
      '--ash-x': `${((i * 137 + 53) % 100)}%`,
      '--ash-delay': `${(i * 0.37) % 4}s`,
      '--ash-dur': `${3 + (i % 3)}s`,
      '--ash-size': `${2 + (i % 3)}px`,
      '--ash-drift': `${(i % 2 === 0 ? 1 : -1) * ((i * 7) % 20)}px`
    })

    const soulStyle = (i) => ({
      '--soul-x': `${((i * 173 + 29) % 90 + 5)}%`,
      '--soul-y': `${((i * 97 + 41) % 80 + 10)}%`,
      '--soul-delay': `${(i * 0.8) % 5}s`,
      '--soul-dur': `${4 + (i % 3)}s`
    })

    return {
      t,
      loginType,
      bgVideoRef,
      isMuted,
      showPassword,
      loading,
      exiting,
      showRegister,
      registerLoading,
      showForgot,
      resetLoading,
      forgotUsername,
      resetResult,
      currentLocale,
      formData,
      registerForm,
      switchLanguage,
      toggleSound,
      handleSubmit,
      handleRegister,
      handleForgotPassword,
      copyNewPassword,
      ashStyle,
      soulStyle
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap');

.portal-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  z-index: 10;
}

.bg-fading {
  animation: bgFade 0.6s ease-out forwards;
}

@keyframes bgFade {
  from { opacity: 1; filter: blur(0); }
  to { opacity: 0; filter: blur(8px); }
}

.portal-container.container-fading {
  animation: containerFade 0.5s ease-out forwards;
}

@keyframes containerFade {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0.9); }
}

.portal-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(1.1) saturate(1);
}

.sound-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(10, 2, 20, 0.35);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(180, 140, 220, 0.12);
  border-radius: 8px;
  color: rgba(200, 180, 220, 0.5);
  cursor: pointer;
  transition: all 0.25s ease;
}

.sound-toggle:hover {
  background: rgba(10, 2, 20, 0.5);
  border-color: rgba(180, 140, 220, 0.25);
  color: rgba(200, 180, 220, 0.8);
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(170deg, rgba(8, 0, 15, 0.15) 0%, rgba(13, 0, 24, 0.08) 40%, rgba(10, 0, 20, 0.20) 100%);
}

.bg-portal-aura {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 700px;
  height: 700px;
  transform: translate(-50%, -50%);
  background: radial-gradient(ellipse, rgba(120, 40, 200, 0.12) 0%, rgba(80, 20, 150, 0.06) 40%, rgba(20, 5, 60, 0.02) 70%, transparent 100%);
  animation: auraPulse 4s ease-in-out infinite;
}

@keyframes auraPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; }
  50% { transform: translate(-50%, -50%) scale(1.08); opacity: 1; }
}

.ash-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.ash-particle {
  position: absolute;
  left: var(--ash-x);
  bottom: -10px;
  width: var(--ash-size);
  height: var(--ash-size);
  background: #444455;
  border-radius: 1px;
  opacity: 0;
  animation: ashRise var(--ash-dur) ease-in infinite;
  animation-delay: var(--ash-delay);
}

@keyframes ashRise {
  0% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 0.35;
  }
  80% {
    opacity: 0.2;
  }
  100% {
    transform: translateY(-105vh) translateX(var(--ash-drift));
    opacity: 0;
  }
}

.soul-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.soul-particle {
  position: absolute;
  left: var(--soul-x);
  top: var(--soul-y);
  width: 5px;
  height: 5px;
  background: #00bbbb;
  border-radius: 2px;
  box-shadow: 0 0 6px #00bbbb, 0 0 12px rgba(0, 170, 170, 0.5), 0 0 20px rgba(0, 170, 170, 0.25);
  animation: soulDrift var(--soul-dur) ease-in-out infinite;
  animation-delay: var(--soul-delay);
}

@keyframes soulDrift {
  0%, 100% {
    transform: translate(0, 0);
    opacity: 0.2;
  }
  25% {
    transform: translate(12px, -8px);
    opacity: 0.6;
  }
  50% {
    transform: translate(-6px, 4px);
    opacity: 0.3;
  }
  75% {
    transform: translate(-14px, -4px);
    opacity: 0.55;
  }
}

.portal-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.portal-frame {
  position: relative;
  animation: portalAppear 0.8s cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}

@keyframes portalAppear {
  0% {
    transform: scale(0.85);
    opacity: 0;
    filter: blur(8px);
  }
  60% {
    transform: scale(1.02);
    opacity: 0.9;
    filter: blur(1px);
  }
  100% {
    transform: scale(1);
    opacity: 1;
    filter: blur(0);
  }
}

.portal-frame.shake {
  animation: portalShake 0.4s ease-in-out;
}

@keyframes portalShake {
  0%, 100% { transform: translateX(0); }
  10% { transform: translateX(-6px); }
  20% { transform: translateX(6px); }
  30% { transform: translateX(-5px); }
  40% { transform: translateX(5px); }
  50% { transform: translateX(-3px); }
  60% { transform: translateX(3px); }
  70% { transform: translateX(-2px); }
  80% { transform: translateX(2px); }
  90% { transform: translateX(-1px); }
}

.obsidian-border {
  position: absolute;
  inset: -2px;
  border-radius: 10px;
  border: 1px solid rgba(180, 140, 220, 0.15);
  z-index: 0;
  pointer-events: none;
}

.obsidian-stripe {
  display: none;
}

.portal-glow-pulse {
  position: absolute;
  inset: -6px;
  border-radius: 14px;
  box-shadow:
    0 0 40px rgba(120, 40, 200, 0.1),
    0 0 80px rgba(100, 30, 180, 0.05);
  animation: glowPulse 4s ease-in-out infinite;
  z-index: -1;
  pointer-events: none;
}

@keyframes glowPulse {
  0%, 100% {
    box-shadow:
      0 0 40px rgba(120, 40, 200, 0.1),
      0 0 80px rgba(100, 30, 180, 0.05);
  }
  50% {
    box-shadow:
      0 0 60px rgba(140, 60, 220, 0.15),
      0 0 100px rgba(120, 40, 200, 0.08);
  }
}

.portal-card {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--border-light);
  z-index: 1;
}

.portal-vortex {
  display: none;
}

.portal-content {
  position: relative;
  z-index: 2;
  padding: 36px 32px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pearl-section {
  position: relative;
  width: 72px;
  height: 72px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ender-eye {
  width: 56px;
  height: 64px;
  position: relative;
  animation: eyeFloat 3s ease-in-out infinite;
}

.shield-icon {
  filter: drop-shadow(0 0 8px rgba(255, 200, 0, 0.4));
}

@keyframes eyeFloat {
  0%, 100% { margin-top: 0; }
  50% { margin-top: -6px; }
}

.title-section {
  text-align: center;
  margin-bottom: 20px;
}

.portal-title {
  font-family: var(--font-sans);
  font-size: 28px;
  font-weight: 800;
  color: var(--accent-gold);
  margin: 0 0 8px 0;
  text-shadow: 0 0 20px rgba(255, 200, 0, 0.4);
  letter-spacing: 3px;
  line-height: 1.4;
}

.portal-subtitle {
  font-family: var(--font-sans);
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
  letter-spacing: 1px;
}

.role-toggle {
  display: flex;
  gap: 8px;
  width: 100%;
  margin-bottom: 22px;
}

.role-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 14px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  background: var(--bg-tertiary);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: var(--transition);
  letter-spacing: 0.5px;
}

.role-btn:hover {
  border-color: var(--accent-gold);
  color: var(--accent-gold);
  background: var(--bg-hover);
}

.role-btn.active {
  background: rgba(255, 200, 0, 0.15);
  border-color: var(--accent-gold);
  color: var(--accent-gold);
  box-shadow: var(--shadow-gold);
}

.role-icon {
  display: flex;
  align-items: center;
  opacity: 0.7;
}

.role-btn.active .role-icon {
  opacity: 1;
}

.portal-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-group {
  width: 100%;
}

.field-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--bg-tertiary);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  transition: var(--transition);
}

.field-wrapper:focus-within {
  border-color: var(--accent-gold);
  background: var(--bg-secondary);
  box-shadow: var(--shadow-gold);
}

.field-prefix {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  flex-shrink: 0;
  color: var(--text-muted);
  transition: color 0.3s ease;
}

.field-wrapper:focus-within .field-prefix {
  color: var(--accent-gold);
}

.field-input {
  flex: 1;
  padding: 12px 12px 12px 0;
  font-family: var(--font-sans);
  font-size: 14px;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
}

.field-input::placeholder {
  color: var(--text-muted);
}

.eye-toggle {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  color: rgba(200, 180, 220, 0.35);
  cursor: pointer;
  transition: color 0.2s;
  padding: 0;
}

.eye-toggle:hover {
  color: rgba(200, 180, 220, 0.7);
}

.portal-btn {
  position: relative;
  width: 100%;
  padding: 14px 20px;
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--accent-gold) 0%, var(--accent-gold-dark) 100%);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: none;
  border-radius: var(--radius-sm);
  color: #1a1a2e;
  cursor: pointer;
  transition: var(--transition);
  margin-top: 6px;
  overflow: hidden;
  letter-spacing: 1px;
}

.portal-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

.portal-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--accent-gold-light) 0%, var(--accent-gold) 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(255, 200, 0, 0.4);
}

.portal-btn:active:not(:disabled) {
  transform: translateY(0);
}

.portal-btn:disabled {
  opacity: 0.75;
  cursor: wait;
}

.portal-btn .btn-glow {
  position: absolute;
  inset: -2px;
  border-radius: var(--radius-sm);
  background: transparent;
  box-shadow: var(--shadow-gold-strong);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.portal-btn:hover:not(:disabled) .btn-glow {
  opacity: 1;
}

.btn-loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.portal-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(26, 26, 46, 0.3);
  border-top-color: #1a1a2e;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.register-link {
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--font-sans);
  font-size: 13px;
  cursor: pointer;
  text-align: center;
  padding: 6px;
  transition: var(--transition);
  width: 100%;
  letter-spacing: 0.5px;
}

.register-link:hover {
  color: var(--accent-gold);
}

.portal-footer {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid rgba(180, 140, 220, 0.08);
  width: 100%;
  text-align: center;
}

.portal-footer p {
  font-size: 11px;
  color: rgba(200, 180, 220, 0.35);
  margin: 0;
  font-family: var(--font-mono);
}

.portal-lang {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 14px;
  padding: 8px;
  background: var(--bg-tertiary);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.lang-icon {
  color: var(--text-muted);
  margin-right: 2px;
}

.lang-btn {
  padding: 6px 14px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: var(--transition);
}

.lang-btn:hover {
  color: var(--accent-gold);
}

.lang-btn.active {
  background: var(--accent-gold);
  color: #1a1a2e;
}

.lang-sep {
  width: 1px;
  height: 14px;
  background: var(--border-color);
}

.f1 { animation: fadeSlideIn 0.5s ease-out 0.6s both; }
.f2 { animation: fadeSlideIn 0.5s ease-out 0.75s both; }
.f3 { animation: fadeSlideIn 0.5s ease-out 0.9s both; }
.f4 { animation: fadeSlideIn 0.5s ease-out 1.05s both; }
.f5 { animation: fadeSlideIn 0.5s ease-out 1.2s both; }

@keyframes fadeSlideIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.portal-travel {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: #050010;
  overflow: hidden;
}

.travel-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 40%, rgba(139, 92, 246, 0.35) 0%, rgba(88, 28, 135, 0.15) 35%, transparent 65%);
  animation: portalBreathe 2s ease-in-out infinite alternate;
}

@keyframes portalBreathe {
  from { opacity: 0.6; transform: scale(1); }
  to { opacity: 1; transform: scale(1.06); }
}

.travel-grid {
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 7.8%, rgba(147, 51, 234, 0.08) 8%),
    repeating-linear-gradient(90deg, transparent, transparent 7.8%, rgba(147, 51, 234, 0.08) 8%);
  animation: gridPulse 3s ease-in-out infinite alternate;
}

@keyframes gridPulse {
  from { opacity: 0.3; }
  to { opacity: 0.7; }
}

.travel-vignette {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background: radial-gradient(ellipse at 50% 45%, transparent 20%, rgba(5, 0, 15, 0.5) 60%, rgba(2, 0, 8, 0.9) 100%);
}

.travel-center-burst {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(196, 167, 255, 0.5) 0%, rgba(139, 92, 246, 0.15) 30%, transparent 60%);
  animation: centerBurst 2.5s ease-out forwards;
}

@keyframes centerBurst {
  0% { opacity: 0; transform: translate(-50%, -50%) scale(0.3); }
  30% { opacity: 1; transform: translate(-50%, -50%) scale(1.5); }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(10); }
}

.travel-label {
  position: absolute;
  bottom: 15%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  font-family: 'Press Start 2P', 'JetBrains Mono', monospace;
  font-size: 11px;
  color: rgba(192, 132, 252, 0.7);
  text-shadow: 0 0 10px rgba(147, 51, 234, 0.5), 0 0 20px rgba(120, 40, 180, 0.3);
  animation: labelBlink 0.7s ease-in-out infinite alternate;
  letter-spacing: 2px;
  text-align: center;
}

@keyframes labelBlink {
  from { opacity: 0.4; }
  to { opacity: 1; }
}

.travel-fade-enter-active {
  animation: travelIn 0.3s ease-out;
}
.travel-fade-leave-active {
  animation: travelOut 0.2s ease-in forwards;
}

@keyframes travelIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes travelOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

.register-overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 0, 8, 0.5);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.register-modal {
  width: 400px;
  background: var(--bg-card-solid);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg), var(--shadow-gold-strong);
  overflow: hidden;
}

.rm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
}

.rm-header h3 {
  font-family: var(--font-sans);
  font-size: 15px;
  font-weight: 700;
  color: var(--accent-gold);
  margin: 0;
  letter-spacing: 0.5px;
}

.rm-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: var(--transition);
}

.rm-close:hover {
  background: rgba(231, 76, 60, 0.15);
  border-color: #E74C3C;
  color: #E74C3C;
}

.register-form {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rm-label {
  display: block;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.rm-input {
  width: 100%;
  padding: 12px 14px;
  font-family: var(--font-sans);
  font-size: 14px;
  background: var(--bg-tertiary);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  transition: var(--transition);
}

.rm-input:focus {
  border-color: var(--accent-gold);
  box-shadow: var(--shadow-gold);
}

.rm-input::placeholder {
  color: var(--text-muted);
}

.rm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
}

.rm-btn {
  padding: 10px 20px;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
}

.rm-btn.cancel {
  background: var(--bg-tertiary);
  backdrop-filter: blur(6px);
  border: 1px solid var(--border-color);
  color: var(--text-muted);
}

.rm-btn.cancel:hover {
  border-color: var(--accent-gold);
  color: var(--accent-gold);
}

.rm-btn.confirm {
  background: linear-gradient(135deg, var(--accent-gold) 0%, var(--accent-gold-dark) 100%);
  border: none;
  color: #1a1a2e;
}

.rm-btn.confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--accent-gold-light) 0%, var(--accent-gold) 100%);
  box-shadow: 0 4px 15px rgba(255, 200, 0, 0.4);
}

.rm-btn.confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.dialog-fade-enter-active { animation: dialogIn 0.25s ease-out; }
.dialog-fade-leave-active { animation: dialogIn 0.15s ease-in reverse; }

@keyframes dialogIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.forgot-link {
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--font-sans);
  font-size: 13px;
  cursor: pointer;
  text-align: center;
  padding: 0;
  transition: var(--transition);
  width: 100%;
}

.forgot-link:hover {
  color: var(--accent-gold);
}

.forgot-modal {
  width: 400px;
  background: var(--bg-card-solid);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg), var(--shadow-gold-strong);
  overflow: hidden;
}

.forgot-body {
  padding: 20px;
}

.forgot-desc {
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.reset-result {
  margin-top: 12px;
}

.reset-success {
  text-align: center;
}

.success-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.reset-msg {
  font-family: var(--font-sans);
  font-size: 13px;
  color: #2ECC71;
  margin: 0 0 14px 0;
}

.new-pwd-box {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
  padding: 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.new-pwd-label {
  font-family: var(--font-sans);
  font-size: 12px;
  color: var(--text-muted);
}

.new-pwd-value {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-gold);
  background: rgba(255, 200, 0, 0.1);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  letter-spacing: 1px;
}

.copy-btn {
  padding: 6px 16px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent-gold) 0%, var(--accent-gold-dark) 100%);
  border: none;
  border-radius: var(--radius-sm);
  color: #1a1a2e;
  cursor: pointer;
  transition: var(--transition);
}

.copy-btn:hover {
  background: linear-gradient(135deg, var(--accent-gold-light) 0%, var(--accent-gold) 100%);
  box-shadow: 0 4px 15px rgba(255, 200, 0, 0.4);
}

.reset-error p {
  font-family: var(--font-sans);
  font-size: 13px;
  color: #E74C3C;
  margin: 0;
  text-align: center;
}
</style>
