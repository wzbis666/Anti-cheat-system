import { createI18n } from 'vue-i18n'
import zh from './locales/zh'
import en from './locales/en'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh',
  fallbackLocale: 'zh',
  messages: { zh, en }
})

export const setLocale = (locale) => {
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale)
  document.documentElement.setAttribute('lang', locale)
}

export const t = (key) => i18n.global.t(key)

export default i18n
