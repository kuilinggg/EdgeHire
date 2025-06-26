import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '', // 从 localStorage 初始化 token
    userId: localStorage.getItem('userId') || null, // 从 localStorage 初始化 userId
    role: localStorage.getItem('role') || null, // 从 localStorage 初始化 role
  }),
  actions: {
    setToken(newToken) {
      this.token = newToken
      localStorage.setItem('token', newToken)
    },
    setUserId(newUserId) {
      this.userId = newUserId
      localStorage.setItem('userId', newUserId)
    },
    setRole(newRole) {
      this.role = newRole
      localStorage.setItem('role', newRole)
    },
    clearToken() {
      this.token = ''
      this.userId = null
      this.role = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
    },
  },
  getters: {
    isAuthenticated: (state) => !!state.token, // 判断用户是否已登录
  },
})