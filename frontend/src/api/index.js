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
  getAll: (config) => api.get('/player/all', config),
  getByUuid: (uuid, config) => api.get(`/player/${uuid}`, config),
  delete: (id, config) => api.delete(`/player/${id}`, config),
  updateRiskScore: (id, score, config) => api.put(`/player/${id}/risk`, { score }, config)
}

export const cheatApi = {
  getAll: (params, config) => api.get('/cheat/all', { params, ...config }),
  getByPage: (page, size, sortBy, sortDir, config) => api.get('/cheat/page', { params: { page, size, sortBy, sortDir }, ...config }),
  getByPlayerUuid: (uuid, config) => api.get(`/cheat/player/${uuid}`, config),
  getByType: (type, page, size, config) => api.get(`/cheat/type/${type}`, { params: { page, size }, ...config }),
  delete: (id, config) => api.delete(`/cheat/${id}`, config)
}

export const reportApi = {
  getAll: (config) => api.get('/report/all', config),
  getPendingCount: (config) => api.get('/report/count/pending', config),
  handle: (id, data, config) => api.post(`/report/handle/${id}`, data, config),
  delete: (id, config) => api.delete(`/report/${id}`, config)
}

export const punishmentApi = {
  getAll: (config) => api.get('/punishment/all', config),
  getPaged: (page, size, config) => api.get('/punishment/page', { params: { page, size }, ...config }),
  ban: (data, config) => api.post('/punishment/ban', data, config),
  unban: (id, config) => api.post(`/punishment/unban/${id}`, {}, config),
  delete: (id, config) => api.delete(`/punishment/${id}`, config),
  getByUuid: (uuid, config) => api.get(`/punishment/uuid/${uuid}`, config),
  checkBanStatus: (uuid, config) => api.get(`/punishment/check/${uuid}`, config)
}

export const whitelistApi = {
  getAll: (config) => api.get('/whitelist/all', config),
  add: (data, config) => api.post('/whitelist/add', data, config),
  remove: (uuid, config) => api.post(`/whitelist/remove/${uuid}`, {}, config),
  delete: (id, config) => api.delete(`/whitelist/${id}`, config),
  check: (uuid, config) => api.get(`/whitelist/check/${uuid}`, config)
}

export const statsApi = {
  getOverview: (config) => api.get('/stats/overview', config),
  getCheatTypes: (config) => api.get('/stats/cheat-types', config),
  getRecent: (hours, config) => api.get('/stats/recent', { params: { hours }, ...config })
}

export const settingsApi = {
  getAll: () => api.get('/settings'),
  get: (key) => api.get(`/settings/${key}`),
  save: (key, value) => api.put(`/settings/${key}`, { value }),
  batchSave: (settingsMap) => api.put('/settings', settingsMap),
  sync: () => api.post('/settings/sync'),
  getPluginSettings: () => api.get('/settings/plugin'),
  savePluginSettings: (settingsMap) => api.put('/settings/plugin', settingsMap)
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
