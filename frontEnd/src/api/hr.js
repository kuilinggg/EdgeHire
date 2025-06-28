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
  }
}

// 简历推荐API
export const resumeRecommendationApi = {
  // 获取简历推荐
  getRecommendations(userId, page, size) {
    // 模拟数据
    const mockData = {
      data: [
        {
          resumeId: 1,
          userId: 101,
          name: '张三',
          avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=张三',
          expectedPosition: '前端开发工程师',
          age: 25,
          education: 3,
          expectedCity: '上海',
          skills: ['Vue.js', 'React', 'JavaScript', 'TypeScript'],
          matchScore: 95,
          createTime: '2024-01-15T10:30:00',
          averageScore: 4.5
        },
        {
          resumeId: 2,
          userId: 102,
          name: '李四',
          avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=李四',
          expectedPosition: '后端开发工程师',
          age: 28,
          education: 4,
          expectedCity: '北京',
          skills: ['Java', 'Spring Boot', 'MySQL', 'Redis'],
          matchScore: 88,
          createTime: '2024-01-14T15:20:00',
          averageScore: 4.2
        },
        {
          resumeId: 3,
          userId: 103,
          name: '王五',
          avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=王五',
          expectedPosition: 'UI设计师',
          age: 26,
          education: 3,
          expectedCity: '深圳',
          skills: ['Figma', 'Sketch', 'Photoshop', 'Illustrator'],
          matchScore: 92,
          createTime: '2024-01-13T09:15:00',
          averageScore: 4.3
        }
      ],
      total: 3,
      page: page,
      size: size
    }
    
    return Promise.resolve({ data: mockData })
  },

  // 搜索简历
  searchResumes(userId, searchParams) {
    // 模拟搜索数据
    const mockData = {
      data: [
        {
          resumeId: 4,
          userId: 104,
          name: '赵六',
          avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=赵六',
          expectedPosition: '产品经理',
          age: 30,
          education: 4,
          expectedCity: '杭州',
          skills: ['产品设计', '用户研究', '数据分析', '项目管理'],
          matchScore: 85,
          createTime: '2024-01-12T14:45:00',
          averageScore: 4.1
        },
        {
          resumeId: 5,
          userId: 105,
          name: '钱七',
          avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=钱七',
          expectedPosition: '测试工程师',
          age: 27,
          education: 3,
          expectedCity: '广州',
          skills: ['自动化测试', '性能测试', 'Selenium', 'JMeter'],
          matchScore: 78,
          createTime: '2024-01-11T11:30:00',
          averageScore: 3.9
        }
      ],
      total: 2,
      page: searchParams.page,
      size: searchParams.size
    }
    
    return Promise.resolve({ data: mockData })
  }
}
