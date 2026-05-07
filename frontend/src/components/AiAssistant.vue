﻿﻿﻿<template>
  <div class="ai-assistant">
    <button v-if="!isOpen" class="ai-fab" @click="toggleOpen" :title="t('ai.assistant')">
      <svg viewBox="0 0 24 24" width="24" height="24"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2zm-1 9a2 2 0 100 4 2 2 0 000-4zm4 0a2 2 0 100 4 2 2 0 000-4zm-4 6.5a5.5 5.5 0 005.16-3.59L15.73 13H8.27l-.43 1.91A5.5 5.5 0 0011 17.5z"/></svg>
      <span v-if="!aiAvailable" class="ai-unavailable-dot"></span>
    </button>

    <div v-if="isOpen" class="ai-panel">
      <div class="ai-panel-header">
        <div class="ai-panel-title">
          <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2zm-1 9a2 2 0 100 4 2 2 0 000-4zm4 0a2 2 0 100 4 2 2 0 000-4z"/></svg>
          <span>{{ t('ai.assistant') }}</span>
          <span v-if="!aiAvailable" class="ai-status-badge offline">{{ t('ai.offline') }}</span>
          <span v-else class="ai-status-badge online">{{ t('ai.online') }}</span>
        </div>
        <div class="ai-panel-actions">
          <button class="ai-action-btn" @click="clearChat" :title="t('ai.clearChat')">
            <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
          <button class="ai-action-btn" @click="toggleOpen">
            <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M6 19h12v2H6z"/></svg>
          </button>
        </div>
      </div>

      <div class="ai-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="ai-welcome">
          <svg viewBox="0 0 24 24" width="40" height="40"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2zm-1 9a2 2 0 100 4 2 2 0 000-4zm4 0a2 2 0 100 4 2 2 0 000-4z"/></svg>
          <p>{{ t('ai.welcome') }}</p>
          <div class="ai-quick-actions">
            <button @click="sendQuickMessage(t('ai.quickQ1'))">{{ t('ai.quickQ1') }}</button>
            <button @click="sendQuickMessage(t('ai.quickQ2'))">{{ t('ai.quickQ2') }}</button>
            <button @click="sendQuickMessage(t('ai.quickQ3'))">{{ t('ai.quickQ3') }}</button>
          </div>
        </div>
        <div v-for="(msg, i) in messages" :key="i" :class="['ai-message', msg.role]">
          <div class="ai-message-avatar">
            <svg v-if="msg.role === 'user'" viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/></svg>
            <svg v-else viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/></svg>
          </div>
          <div class="ai-message-content">
            <div class="ai-message-text" v-html="renderAiText(msg.content)"></div>
          </div>
        </div>
        <div v-if="loading" class="ai-message assistant">
          <div class="ai-message-avatar">
            <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27a7 7 0 01-12.46 0H5a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73A2 2 0 0112 2z"/></svg>
          </div>
          <div class="ai-message-content">
            <div class="ai-typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="ai-input-area">
        <input
          v-model="inputMessage"
          @keydown.enter="sendMessage"
          :placeholder="aiAvailable ? t('ai.inputPlaceholder') : t('ai.disabledPlaceholder')"
          :disabled="!aiAvailable || loading"
          class="ai-input"
        />
        <button @click="sendMessage" :disabled="!aiAvailable || loading || !inputMessage.trim()" class="ai-send-btn">
          <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { aiApi } from '../api'
import { EventBus, Events } from '../utils/eventBus'
import { renderAiText } from '../utils/helpers'

export default {
  name: 'AiAssistant',
  setup() {
    const { t } = useI18n()
    const isOpen = ref(false)
    const aiAvailable = ref(false)
    const messages = ref([])
    const inputMessage = ref('')
    const loading = ref(false)
    const messagesContainer = ref(null)
    const sessionId = ref('sess_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8))

    const checkAiStatus = async () => {
      try {
        const data = await aiApi.getStatus()
        aiAvailable.value = data.available
      } catch (e) {
        aiAvailable.value = false
      }
    }

    const toggleOpen = () => {
      isOpen.value = !isOpen.value
      if (isOpen.value) checkAiStatus()
    }

    let statusInterval = null

    const startStatusPolling = () => {
      if (statusInterval) return
      checkAiStatus()
      statusInterval = setInterval(checkAiStatus, 60000)
    }

    const stopStatusPolling = () => {
      if (statusInterval) { clearInterval(statusInterval); statusInterval = null }
    }

    watch(isOpen, (v) => {
      if (v) startStatusPolling()
      else stopStatusPolling()
    })

    const scrollToBottom = async () => {
      await nextTick()
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    const sendMessage = async () => {
      const msg = inputMessage.value.trim()
      if (!msg || loading.value || !aiAvailable.value) return

      messages.value.push({ role: 'user', content: msg })
      inputMessage.value = ''
      loading.value = true
      const assistantIdx = messages.value.push({ role: 'assistant', content: '' }) - 1
      await scrollToBottom()

      try {
        const API_BASE = import.meta.env.VITE_API_URL || '/api'
        const response = await fetch(`${API_BASE}/ai/chat/stream`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(localStorage.getItem('token') ? { Authorization: `Bearer ${localStorage.getItem('token')}` } : {})
          },
          body: JSON.stringify({ message: msg, sessionId: sessionId.value })
        })

        if (!response.ok) throw new Error('HTTP error')

        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.startsWith('event:message')) {
              continue
            }
            if (line.startsWith('data:')) {
              const data = line.substring(5)
              if (data === '[DONE]') continue
              if (data.startsWith('⚠️') || data.startsWith('Error')) {
                messages.value[assistantIdx].content = data
              } else {
                messages.value[assistantIdx].content += data
              }
              await scrollToBottom()
            }
          }
        }

        if (!messages.value[assistantIdx].content) {
          messages.value[assistantIdx].content = t('ai.error')
        }
      } catch (e) {
        if (!messages.value[assistantIdx].content) {
          messages.value[assistantIdx].content = `⚠️ ${t('ai.networkError')}`
        }
      } finally {
        loading.value = false
        await scrollToBottom()
      }
    }

    const sendQuickMessage = (msg) => { inputMessage.value = msg; sendMessage() }

    const clearChat = async () => {
      try { await aiApi.clearSession(sessionId.value) } catch (e) {}
      messages.value = []
      sessionId.value = 'sess_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8)
    }

    onMounted(() => {
      const unsubscribe = EventBus.on(Events.WS_STATUS, (connected) => {
        if (!connected) aiAvailable.value = false
      })
      checkAiStatus()
    })

    onUnmounted(() => {
      stopStatusPolling()
    })

    return { isOpen, aiAvailable, messages, inputMessage, loading, messagesContainer, toggleOpen, sendMessage, sendQuickMessage, clearChat, renderAiText, t }
  }
}
</script>

<style scoped>
.ai-assistant { position: fixed; bottom: 24px; right: 24px; z-index: 300; }
.ai-fab { width: 56px; height: 56px; border-radius: 50%; background: linear-gradient(135deg, #a855f7, #7c3aed); border: none; color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 16px rgba(168, 85, 247, 0.4); transition: all 0.2s ease; position: relative; }
.ai-fab:hover { transform: scale(1.1); box-shadow: 0 6px 24px rgba(168, 85, 247, 0.55); }
.ai-unavailable-dot { position: absolute; top: 4px; right: 4px; width: 12px; height: 12px; border-radius: 50%; background: var(--accent-red); border: 2px solid var(--bg-primary); }
.ai-panel { width: 400px; height: 520px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); display: flex; flex-direction: column; overflow: hidden; }
.ai-panel-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; background: var(--bg-secondary); border-bottom: 1px solid var(--border-color); }
.ai-panel-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--accent-cyan); }
.ai-status-badge { font-size: 10px; padding: 2px 8px; border-radius: 10px; font-weight: 500; }
.ai-status-badge.online { background: rgba(46, 204, 113, 0.15); color: var(--accent-green); }
.ai-status-badge.offline { background: rgba(231, 76, 60, 0.15); color: var(--accent-red); }
.ai-panel-actions { display: flex; gap: 4px; }
.ai-action-btn { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: var(--bg-tertiary); border: none; border-radius: var(--radius-sm); color: var(--text-muted); cursor: pointer; }
.ai-action-btn:hover { color: var(--text-primary); background: var(--bg-hover); }
.ai-messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.ai-welcome { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--text-muted); text-align: center; gap: 12px; }
.ai-welcome p { font-size: 14px; }
.ai-quick-actions { display: flex; flex-direction: column; gap: 8px; width: 100%; padding: 0 20px; }
.ai-quick-actions button { padding: 10px 14px; font-family: inherit; font-size: 13px; background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-secondary); cursor: pointer; text-align: left; transition: var(--transition); }
.ai-quick-actions button:hover { border-color: var(--accent-cyan); color: var(--accent-cyan); }
.ai-message { display: flex; gap: 10px; }
.ai-message.user { flex-direction: row-reverse; }
.ai-message-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.ai-message.assistant .ai-message-avatar { background: rgba(168, 85, 247, 0.15); color: #c084fc; }
.ai-message.user .ai-message-avatar { background: rgba(52, 152, 219, 0.15); color: var(--accent-blue); }
.ai-message-content { max-width: 80%; }
.ai-message-text { padding: 10px 14px; border-radius: var(--radius-md); font-size: 13px; line-height: 1.6; }
.ai-message.assistant .ai-message-text { background: var(--bg-secondary); color: var(--text-primary); border-bottom-left-radius: 4px; }
.ai-message.user .ai-message-text { background: var(--accent-cyan); color: #fff; border-bottom-right-radius: 4px; }
.ai-message-text :deep(code) { background: rgba(255,255,255,0.1); padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.ai-message-text :deep(pre) { background: rgba(0,0,0,0.3); padding: 10px; border-radius: var(--radius-sm); overflow-x: auto; margin: 8px 0; }
.ai-message-text :deep(pre code) { background: none; padding: 0; }
.ai-typing { display: flex; gap: 4px; padding: 12px 16px; background: var(--bg-secondary); border-radius: var(--radius-md); }
.ai-typing span { width: 8px; height: 8px; border-radius: 50%; background: var(--accent-cyan); animation: typing 1.4s infinite; }
.ai-typing span:nth-child(2) { animation-delay: 0.2s; }
.ai-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%, 60%, 100% { opacity: 0.3; transform: translateY(0); } 30% { opacity: 1; transform: translateY(-4px); } }
.ai-input-area { display: flex; gap: 8px; padding: 12px 16px; border-top: 1px solid var(--border-color); background: var(--bg-secondary); }
.ai-input { flex: 1; padding: 10px 14px; font-family: inherit; font-size: 13px; background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-primary); outline: none; }
.ai-input:focus { border-color: var(--accent-cyan); }
.ai-input::placeholder { color: var(--text-muted); }
.ai-input:disabled { opacity: 0.5; }
.ai-send-btn { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; background: var(--accent-cyan); border: none; border-radius: var(--radius-sm); color: #fff; cursor: pointer; transition: var(--transition); }
.ai-send-btn:hover:not(:disabled) { background: var(--accent-cyan-light); }
.ai-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
@media (max-width: 768px) { .ai-panel { width: calc(100vw - 32px); height: 60vh; bottom: 0; right: 0; } }
</style>
