import { ref } from 'vue'

const listeners = new Map()

export const EventBus = {
  emit(event, data) {
    console.log('[EventBus] 发送事件:', event, '数据:', data, '监听器数量:', listeners.get(event)?.size || 0)
    const callbacks = listeners.get(event)
    if (callbacks) {
      callbacks.forEach(callback => callback(data))
    }
  },
  
  on(event, callback) {
    if (!listeners.has(event)) {
      listeners.set(event, new Set())
    }
    listeners.get(event).add(callback)
    console.log('[EventBus] 注册监听器:', event, '当前监听器数量:', listeners.get(event).size)
    
    return () => {
      listeners.get(event)?.delete(callback)
      console.log('[EventBus] 移除监听器:', event, '剩余监听器数量:', listeners.get(event)?.size || 0)
    }
  },
  
  off(event, callback) {
    listeners.get(event)?.delete(callback)
  },

  getListenerCount(event) {
    return listeners.get(event)?.size || 0
  }
}

export const Events = {
  STATS_CHANGED: 'stats:changed',
  PLAYER_BANNED: 'player:banned',
  PLAYER_UNBANNED: 'player:unbanned',
  REPORT_HANDLED: 'report:handled',
  CHEAT_DETECTED: 'cheat:detected'
}
