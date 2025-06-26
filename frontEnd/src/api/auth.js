//占位使用，以防止报错
import axios from 'axios'
import { API_BASE_URL } from '../config'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const authApi = {
  // 用户登录
  login(username, password) {
    return apiClient.post('/auth/login', { username, password })
  },
  // 用户登出
  logout() {
    // 可根据后端实际情况调整为POST或GET
    return apiClient.post('/auth/logout')
  },
  // 用户注册
  register(username, password, role) {
    return apiClient.post('/auth/register', { username, password, role })
  },
}
