import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '', // 从 localStorage 初始化 token
    userId: localStorage.getItem('userId') || null, // 从 localStorage 初始化 userId
    role: localStorage.getItem('role') || null, // 从 localStorage 初始化 role
    profileComplete: localStorage.getItem('profileComplete') === 'true', // 个人信息是否完整
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
    setProfileComplete(isComplete) {
      this.profileComplete = isComplete
      localStorage.setItem('profileComplete', isComplete.toString())
    },
    clearToken() {
      this.token = ''
      this.userId = null
      this.role = null
      this.profileComplete = false
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
      localStorage.removeItem('profileComplete')
    },
  },
  getters: {
    isAuthenticated: (state) => !!state.token, // 判断用户是否已登录
  },
})