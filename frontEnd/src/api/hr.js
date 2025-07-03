import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

// HR信息管理API
export const hrApi = {
  // 获取HR信息
  getHrInfo(userId) {
    return axios.get(`${API_BASE}/hr/info/${userId}`)
  },

  // 创建HR信息
  createHrInfo(data) {
    return axios.post(`${API_BASE}/hr/info`, data)
  },

  // 更新HR信息
  updateHrInfo(id, data) {
    return axios.put(`${API_BASE}/hr/info/${id}`, data)
  },

  // 根据用户ID更新HR信息
  updateHrInfoByUserId(userId, data) {
    return axios.put(`${API_BASE}/hr/info/user/${userId}`, data)
  },

  // 验证用户是否为HR
  verifyHr(userId) {
    return axios.get(`${API_BASE}/hr/verify/${userId}`)
  },

  // 获取KPI数据
  getKpiData(userId) {
    return axios.get(`${API_BASE}/hr/kpi/${userId}`)
  },

  // HR发起沟通
  initiateChat(hrUserId, targetUserId, customMessage = null) {
    const data = customMessage ? { customMessage } : null
    return axios.post(`${API_BASE}/hr/initiate-chat/${hrUserId}/${targetUserId}`, data)
  },

  // HR联系求职者（求职指导场景）
  contactSeeker(hrUserId, seekerUserId, message = "你好，我已通过你的求职指导请求，现在我们可以开始交流了。") {
    return this.initiateChat(hrUserId, seekerUserId, message)
  }
}
