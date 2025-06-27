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

  // 获取HR KPI数据
  getKpiData(hrId) {
    return axios.get(`${API_BASE}/hr/kpi/${hrId}`)
  },

  // 获取HR详细统计
  getDetailedStats(hrId) {
    return axios.get(`${API_BASE}/hr/stats/${hrId}`)
  },

  // 验证用户是否为HR
  verifyHr(userId) {
    return axios.get(`${API_BASE}/hr/verify/${userId}`)
  }
}

// 简历推荐API
export const resumeRecommendationApi = {
  // 获取推荐简历
  getRecommendations(hrId, page = 0, size = 10) {
    return axios.get(`${API_BASE}/hr/resume/recommend/${hrId}`, {
      params: { page, size }
    })
  },

  // 搜索简历
  searchResumes(hrId, params = {}) {
    const { keyword, minEducation, position, page = 0, size = 10 } = params
    return axios.get(`${API_BASE}/hr/resume/search/${hrId}`, {
      params: { keyword, minEducation, position, page, size }
    })
  }
}

// 简历评论API
export const resumeCommentApi = {
  // 创建评论
  createComment(data) {
    return axios.post(`${API_BASE}/resume/comment`, data)
  },

  // 更新评论
  updateComment(commentId, data) {
    return axios.put(`${API_BASE}/resume/comment/${commentId}`, data)
  },

  // 获取简历的所有评论
  getCommentsByResumeId(resumeId) {
    return axios.get(`${API_BASE}/resume/comment/resume/${resumeId}`)
  },

  // 获取HR的所有评论
  getCommentsByHrId(hrId) {
    return axios.get(`${API_BASE}/resume/comment/hr/${hrId}`)
  },

  // 获取简历平均评分
  getAverageScore(resumeId) {
    return axios.get(`${API_BASE}/resume/comment/resume/${resumeId}/score`)
  },

  // 获取HR对特定简历的评论
  getCommentByResumeAndHr(resumeId, hrId) {
    return axios.get(`${API_BASE}/resume/comment/resume/${resumeId}/hr/${hrId}`)
  },

  // 删除评论
  deleteComment(commentId, hrId) {
    return axios.delete(`${API_BASE}/resume/comment/${commentId}/hr/${hrId}`)
  },

  // 统计HR评论数量
  countCommentsByHr(hrId) {
    return axios.get(`${API_BASE}/resume/comment/hr/${hrId}/count`)
  }
}

// 求职者信息API
export const seekerApi = {
  // 获取求职者信息
  getSeekerInfo(userId) {
    return axios.get(`${API_BASE}/seeker/info/${userId}`)
  },

  // 创建求职者信息
  createSeekerInfo(data) {
    return axios.post(`${API_BASE}/seeker/info`, data)
  },

  // 更新求职者信息
  updateSeekerInfo(id, data) {
    return axios.put(`${API_BASE}/seeker/info/${id}`, data)
  },

  // 根据用户ID更新求职者信息
  updateSeekerInfoByUserId(userId, data) {
    return axios.put(`${API_BASE}/seeker/info/user/${userId}`, data)
  },

  // 搜索求职者
  searchSeekers(params = {}) {
    const { minEducation, favor } = params
    return axios.get(`${API_BASE}/seeker/search`, {
      params: { minEducation, favor }
    })
  },

  // 根据会员等级获取求职者
  getSeekersByMembership(membership) {
    return axios.get(`${API_BASE}/seeker/membership/${membership}`)
  },

  // 获取所有求职者
  getAllSeekers() {
    return axios.get(`${API_BASE}/seeker/all`)
  },

  // 验证用户是否为求职者
  verifySeeker(userId) {
    return axios.get(`${API_BASE}/seeker/verify/${userId}`)
  }
}
