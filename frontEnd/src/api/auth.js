import axios from 'axios';
import { API_BASE_URL } from '../config';

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 认证相关 API
export const authApi = {
  // 用户登录
  login(username, password) {
    return apiClient.post('/auth/login', {
      username,
      password,
    }).then((response) => {
      return response
    })
  },

  // 用户登出
  logout() {
    window.location.href = '/login' // 重定向到登录页面
  },

  // 用户注册
  register(username, password, role) {
    return apiClient.post('/auth/register', {
      username,
      password,
      role
    })
  },
}
