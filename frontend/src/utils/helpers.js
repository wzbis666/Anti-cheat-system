import DOMPurify from 'dompurify'
import { t } from '../i18n'

export function renderAiText(text) {
    if (!text) return ''
    let html = text
        .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br/>')
    return DOMPurify.sanitize(html, {
        ALLOWED_TAGS: ['strong', 'code', 'pre', 'br'],
        ALLOWED_ATTR: []
    })
}

export function formatTime(timestamp) {
    if (!timestamp) return '-'
    const date = new Date(typeof timestamp === 'number' ? timestamp : Number(timestamp))
    if (isNaN(date.getTime())) return '-'
    const locale = localStorage.getItem('locale') || 'zh'
    const lang = locale === 'en' ? 'en-US' : 'zh-CN'
    return date.toLocaleString(lang, {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
    })
}

export function getSeverityClass(severity) {
    if (!severity) return ''
    const s = String(severity).toUpperCase()
    if (s === 'CRITICAL' || s === '严重') return 'severity-critical'
    if (s === 'HIGH' || s === '高') return 'severity-high'
    if (s === 'MEDIUM' || s === '中') return 'severity-medium'
    if (s === 'LOW' || s === '低') return 'severity-low'
    return ''
}

export function getSeverityText(severity) {
    if (!severity) return ''
    const s = String(severity).toUpperCase()
    if (s === 'CRITICAL' || s === '严重') return t('dashboard.severityCritical')
    if (s === 'HIGH' || s === '高') return t('dashboard.severityHigh')
    if (s === 'MEDIUM' || s === '中') return t('dashboard.severityMedium')
    if (s === 'LOW' || s === '低') return t('dashboard.severityLow')
    return severity
}

export function getCheatClass(cheatType) {
    if (!cheatType) return ''
    const t = cheatType.toLowerCase()
    if (t.includes('fly') || t.includes('飞行')) return 'cheat-fly'
    if (t.includes('speed') || t.includes('速度')) return 'cheat-speed'
    if (t.includes('killaura') || t.includes('杀戮')) return 'cheat-killaura'
    if (t.includes('xray') || t.includes('透视')) return 'cheat-xray'
    if (t.includes('scaffold') || t.includes('搭桥')) return 'cheat-scaffold'
    if (t.includes('nuker') || t.includes('破坏')) return 'cheat-nuker'
    return 'cheat-other'
}
