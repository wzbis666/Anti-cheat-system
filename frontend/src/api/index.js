import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'
const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
const WS_BASE_URL = import.meta.env.VITE_WS_URL || `${wsProtocol}//${window.location.host}/ws/cheats`

export function getWsUrl() {
  const token = localStorage.getItem('token')
  const separator = WS_BASE_URL.includes('?') ? '&' : '?'
  return token ? `${WS_BASE_URL}${separator}token=${token}` : WS_BASE_URL
}

export const WS_URL = WS_BASE_URL

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

const authErrorCallbacks = []

export function onAuthError(callback) {
  authErrorCallbacks.push(callback)
}

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      localStorage.removeItem('token')
      localStorage.removeItem('admin')
      localStorage.removeItem('user')
      authErrorCallbacks.forEach(cb => {
        try { cb() } catch (e) { console.error('Auth error callback failed:', e) }
      })
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (username, password) => api.post('/auth/login', { username, password }),
  userLogin: (username, password) => api.post('/auth/user/login', { username, password }),
  register: (data) => api.post('/auth/user/register', data),
  validateToken: () => api.get('/auth/validate'),
  getProfile: (id) => api.get(`/auth/profile/${id}`),
  updateProfile: (id, data) => api.post(`/auth/profile/${id}`, data),
  changePassword: (id, oldPassword, newPassword) => api.post(`/auth/password/${id}`, { oldPassword, newPassword }),
  forgotPassword: (username, userType) => api.post('/auth/forgot-password', { username, userType })
}

export const playerApi = {
  getAll: () => api.get('/player/all'),
  getByUuid: (uuid) => api.get(`/player/${uuid}`),
  delete: (id) => api.delete(`/player/${id}`),
  updateRiskScore: (id, score) => api.put(`/player/${id}/risk`, { score })
}

export const cheatApi = {
  getAll: () => api.get('/cheat/all'),
  getByPage: (page, size, sortBy, sortDir) => api.get('/cheat/page', { params: { page, size, sortBy, sortDir } }),
  getByPlayerUuid: (uuid) => api.get(`/cheat/player/${uuid}`),
  getByType: (type, page, size) => api.get(`/cheat/type/${type}`, { params: { page, size } }),
  delete: (id) => api.delete(`/cheat/${id}`)
}

export const reportApi = {
  getAll: () => api.get('/report/all'),
  getPendingCount: () => api.get('/report/count/pending'),
  handle: (id, data) => api.post(`/report/handle/${id}`, data),
  delete: (id) => api.delete(`/report/${id}`)
}

export const punishmentApi = {
  getAll: () => api.get('/punishment/all'),
  ban: (data) => api.post('/punishment/ban', data),
  unban: (id) => api.post(`/punishment/unban/${id}`, {}),
  delete: (id) => api.delete(`/punishment/${id}`),
  getByUuid: (uuid) => api.get(`/punishment/uuid/${uuid}`),
  checkBanStatus: (uuid) => api.get(`/punishment/check/${uuid}`)
}

export const whitelistApi = {
  getAll: () => api.get('/whitelist/all'),
  add: (data) => api.post('/whitelist/add', data),
  remove: (uuid) => api.post(`/whitelist/remove/${uuid}`),
  delete: (id) => api.delete(`/whitelist/${id}`),
  check: (uuid) => api.get(`/whitelist/check/${uuid}`)
}

export const statsApi = {
  getOverview: () => api.get('/stats/overview'),
  getCheatTypes: () => api.get('/stats/cheat-types'),
  getRecent: (hours) => api.get('/stats/recent', { params: { hours } })
}

export const settingsApi = {
  getAll: () => api.get('/settings'),
  get: (key) => api.get(`/settings/${key}`),
  save: (key, value) => api.put(`/settings/${key}`, { value }),
  batchSave: (settingsMap) => api.put('/settings', settingsMap)
}

export const aiApi = {
  getStatus: () => api.get('/ai/status'),
  analyzeCheat: (playerUuid, playerName) => api.post('/ai/analyze/cheat', { playerUuid, playerName }),
  analyzeReport: (reportId) => api.post('/ai/analyze/report', { reportId }),
  analyzeDashboard: () => api.post('/ai/analyze/dashboard', {}, { timeout: 60000 }),
  evaluateBan: (playerUuid, playerName) => api.post('/ai/analyze/ban-evaluation', { playerUuid, playerName }, { timeout: 60000 }),
  chat: (message, history, playerName, sessionId) => api.post('/ai/chat', { message, history, playerName, sessionId }, { timeout: 60000 }),
  clearSession: (sessionId) => api.delete(`/ai/session/${sessionId}`)
}

export default api
