# EdgeHire HR模块 API 使用示例

## 1. HR信息管理

### 创建HR信息
```http
POST /api/hr/info
Content-Type: application/json

{
  "userId": 1,
  "company": "阿里巴巴",
  "position": "Java开发工程师",
  "experience": "5年招聘经验"
}
```

### 获取HR信息
```http
GET /api/hr/info/1
```

### 更新HR信息
```http
PUT /api/hr/info/user/1
Content-Type: application/json

{
  "company": "腾讯",
  "position": "高级Java开发工程师",
  "experience": "8年招聘经验"
}
```

## 2. 简历推荐

### 获取推荐简历
```http
GET /api/hr/resume/recommend/1?page=0&size=10
```

响应示例：
```json
{
  "data": [
    {
      "resumeId": 1,
      "userId": 2,
      "name": "张三",
      "avatar": "/uploads/avatar1.jpg",
      "age": 25,
      "gender": 1,
      "education": 5,
      "school": "清华大学",
      "expectedPosition": "Java开发",
      "matchScore": 95,
      "averageScore": 8.5,
      "hasCommented": false,
      "createTime": "2024-01-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "total": 1
}
```

### 搜索简历
```http
GET /api/hr/resume/search/1?keyword=Java&minEducation=5&position=开发&page=0&size=10
```

## 3. 简历评论

### 创建评论
```http
POST /api/resume/comment
Content-Type: application/json

{
  "resumeId": 1,
  "hrId": 1,
  "comment": "简历整体不错，建议在项目经验部分增加更多技术细节。",
  "score": 8
}
```

### 获取简历的所有评论
```http
GET /api/resume/comment/resume/1
```

### 获取简历平均评分
```http
GET /api/resume/comment/resume/1/score
```

响应示例：
```json
{
  "averageScore": 8.5
}
```

## 4. HR统计数据

### 获取KPI数据
```http
GET /api/hr/kpi/1
```

响应示例：
```json
{
  "newGuidance": 5,
  "newContacts": 8,
  "completedGuidance": 3,
  "unreadMessages": 2
}
```

### 获取详细统计
```http
GET /api/hr/stats/1
```

响应示例：
```json
{
  "totalComments": 15,
  "monthlyMessages": 25,
  "todayMessages": 3,
  "totalResumes": 100,
  "totalSeekers": 80
}
```

## 5. 求职者信息管理

### 创建求职者信息
```http
POST /api/seeker/info
Content-Type: application/json

{
  "userId": 2,
  "education": 5,
  "school": "清华大学",
  "favor": "Java后端开发",
  "membership": 0
}
```

### 搜索求职者
```http
GET /api/seeker/search?minEducation=5&favor=Java
```

## 6. 前端集成示例

### Vue.js 组件示例

```javascript
// 在HR主页组件中获取KPI数据
import { hrApi } from '@/api/hr'

export default {
  data() {
    return {
      kpiData: {
        newGuidance: 0,
        newContacts: 0,
        completedGuidance: 0,
        unreadMessages: 0
      }
    }
  },
  async mounted() {
    await this.loadKpiData()
  },
  methods: {
    async loadKpiData() {
      try {
        const hrId = localStorage.getItem('userId')
        const response = await hrApi.getKpiData(hrId)
        this.kpiData = response.data
      } catch (error) {
        console.error('加载KPI数据失败:', error)
      }
    }
  }
}
```

### 简历推荐组件示例

```javascript
// 在简历列表组件中获取推荐简历
import { resumeRecommendationApi } from '@/api/hr'

export default {
  data() {
    return {
      resumes: [],
      loading: false,
      pagination: {
        page: 0,
        size: 10,
        total: 0
      }
    }
  },
  async mounted() {
    await this.loadRecommendations()
  },
  methods: {
    async loadRecommendations() {
      this.loading = true
      try {
        const hrId = localStorage.getItem('userId')
        const response = await resumeRecommendationApi.getRecommendations(
          hrId, 
          this.pagination.page, 
          this.pagination.size
        )
        this.resumes = response.data.data
        this.pagination.total = response.data.total
      } catch (error) {
        console.error('加载推荐简历失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    async searchResumes(searchParams) {
      this.loading = true
      try {
        const hrId = localStorage.getItem('userId')
        const response = await resumeRecommendationApi.searchResumes(hrId, {
          ...searchParams,
          page: this.pagination.page,
          size: this.pagination.size
        })
        this.resumes = response.data.data
        this.pagination.total = response.data.total
      } catch (error) {
        console.error('搜索简历失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
```

## 7. 错误处理

所有API都会返回标准的错误格式：

```json
{
  "error": "错误信息描述"
}
```

常见错误码：
- 400: 请求参数错误
- 401: 未授权访问
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

## 8. 数据库初始化

在使用API之前，请确保运行了数据库初始化脚本：

```bash
mysql -u root -p EdgeHire < database_init.sql
```

这将创建必要的表结构和示例数据。
