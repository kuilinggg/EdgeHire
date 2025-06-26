import axios from 'axios'

// 简历相关API
export const resumeApi = {
  // 获取简历列表
  getResumeList(params = {}) {
    return axios.get('/resumes', { params })
  },

  // 获取简历详情
  getResumeDetail(id) {
    return axios.get(`/resumes/${id}`)
  },

  // 搜索简历
  searchResumes(keyword, filters = {}) {
    return axios.get('/resumes/search', {
      params: {
        keyword,
        ...filters
      }
    })
  },

  // 收藏/取消收藏简历
  toggleFavorite(resumeId) {
    return axios.post(`/resumes/${resumeId}/favorite`)
  },

  // 获取推荐简历
  getRecommendedResumes(hrId) {
    return axios.get(`/resumes/recommended/${hrId}`)
  },

  // 提交简历修改建议
  submitSuggestion(resumeId, suggestion) {
    return axios.post(`/resumes/${resumeId}/suggestions`, {
      content: suggestion,
      type: 'modification'
    })
  }
}

// 求职指导相关API
export const guidanceApi = {
  // 获取指导请求列表
  getGuidanceRequests(params = {}) {
    return axios.get('/guidance/requests', { params })
  },

  // 处理指导请求
  handleGuidanceRequest(requestId, action) {
    return axios.post(`/guidance/requests/${requestId}/${action}`)
  },

  // 完成指导请求
  completeGuidanceRequest(requestId, feedback) {
    return axios.post(`/guidance/requests/${requestId}/complete`, {
      feedback
    })
  },

  // 获取指导请求详情
  getGuidanceRequestDetail(requestId) {
    return axios.get(`/guidance/requests/${requestId}`)
  }
}
