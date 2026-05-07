import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: 'dashboard', icon: 'chart' }
  },
  {
    path: '/players',
    name: 'players',
    component: () => import('../views/Players.vue'),
    meta: { title: 'players', icon: 'user' }
  },
  {
    path: '/cheats',
    name: 'cheats',
    component: () => import('../views/Cheats.vue'),
    meta: { title: 'cheats', icon: 'alert' }
  },
  {
    path: '/reports',
    name: 'reports',
    component: () => import('../views/Reports.vue'),
    meta: { title: 'reports', icon: 'file' }
  },
  {
    path: '/punishments',
    name: 'punishments',
    component: () => import('../views/Punishments.vue'),
    meta: { title: 'punishments', icon: 'ban' }
  },
  {
    path: '/whitelist',
    name: 'whitelist',
    component: () => import('../views/Whitelist.vue'),
    meta: { title: 'whitelist', icon: 'shield' }
  },
  {
    path: '/audit',
    name: 'audit',
    component: () => import('../views/AuditLog.vue'),
    meta: { title: 'audit', icon: 'log' }
  },
  {
    path: '/appeals',
    name: 'appeals',
    component: () => import('../views/Appeals.vue'),
    meta: { title: 'appeals', icon: 'appeal' }
  },
  {
    path: '/settings',
    name: 'settings',
    component: () => import('../views/Settings.vue'),
    meta: { title: 'settings', icon: 'settings' }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: 'profile', icon: 'profile' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
