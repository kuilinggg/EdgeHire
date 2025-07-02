import axios from 'axios'
import { AXIOS_CONFIG } from '../config'

// 创建axios实例
const api = axios.create(AXIOS_CONFIG)

const API_BASE = '/guidance'

/**
 * 提交求职指导申请
 * @param {Object} data 申请数据
 * @returns {Promise}
 */
export function submitGuidanceRequest(data) {
  return api.post(`${API_BASE}/submit`, data)
}

/**
 * 获取用户历史指导记录
 * @param {number} userId 用户ID
 * @param {number} page 页码 (可选)
 * @param {number} size 每页大小 (可选)
 * @returns {Promise}
 */
export function getUserGuidanceHistory(userId, page = 1, size = 10) {
  return api.get(`${API_BASE}/history/${userId}`, {
    params: { page, size }
  })
}

/**
 * 根据ID获取指导申请详情
 * @param {number} id 申请ID
 * @returns {Promise}
 */
export function getGuidanceRequestDetail(id) {
  return api.get(`${API_BASE}/detail/${id}`)
}

/**
 * 检查用户是否可以提交申请
 * @param {number} userId 用户ID
 * @returns {Promise}
 */
export function checkCanSubmit(userId) {
  return api.get(`${API_BASE}/check/${userId}`)
}

/**
 * 取消申请
 * @param {number} requestId 申请ID
 * @param {number} userId 用户ID
 * @returns {Promise}
 */
export function cancelGuidanceRequest(requestId, userId) {
  return api.post(`${API_BASE}/cancel`, {
    requestId,
    userId
  })
}

/**
 * 用户评价指导服务
 * @param {number} requestId 申请ID
 * @param {number} rating 评分(1-5)
 * @param {string} comment 评价内容
 * @returns {Promise}
 */
export function rateGuidanceService(requestId, rating, comment) {
  return api.post(`${API_BASE}/rating`, {
    requestId,
    rating,
    comment
  })
}

// =================
// HR相关接口
// =================

/**
 * HR获取分配的指导记录
 * @param {number} hrUserId HR用户ID
 * @returns {Promise}
 */
export function getHRGuidanceList(hrUserId) {
  return api.get(`${API_BASE}/hr/${hrUserId}`)
}

/**
 * 获取待分配的申请列表
 * @returns {Promise}
 */
export function getPendingGuidanceRequests() {
  return api.get(`${API_BASE}/pending`)
}

/**
 * 分配HR
 * @param {number} requestId 申请ID
 * @param {number} hrUserId HR用户ID
 * @returns {Promise}
 */
export function assignHR(requestId, hrUserId) {
  return api.post(`${API_BASE}/assign`, {
    requestId,
    hrUserId
  })
}

/**
 * HR提交指导反馈
 * @param {number} requestId 申请ID
 * @param {string} feedback 指导反馈
 * @returns {Promise}
 */
export function submitGuidanceFeedback(requestId, feedback) {
  return api.post(`${API_BASE}/feedback`, {
    requestId,
    feedback
  })
}

// 添加请求拦截器
api.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 登录过期，跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
