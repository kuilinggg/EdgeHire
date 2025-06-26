<template>
  <div class="resume-list">
    <!-- 顶部标题与搜索栏 -->
    <div class="top-bar">
      <div class="page-title">浏览简历</div>
      <div class="search-section">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="输入关键词搜索，如'岗位'、'技能'、'城市'等"
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch" class="search-btn">
            搜索
          </el-button>
        </div>
        <!-- 高级筛选器 -->
        <div class="filter-bar">
          <el-select v-model="filters.experience" placeholder="工作经验" clearable class="filter-item">
            <el-option label="应届毕业生" value="0" />
            <el-option label="1-3年" value="1-3" />
            <el-option label="3-5年" value="3-5" />
            <el-option label="5-10年" value="5-10" />
            <el-option label="10年以上" value="10+" />
          </el-select>
          <el-select v-model="filters.education" placeholder="学历要求" clearable class="filter-item">
            <el-option label="高中及以下" value="high_school" />
            <el-option label="大专" value="college" />
            <el-option label="本科" value="bachelor" />
            <el-option label="硕士" value="master" />
            <el-option label="博士" value="doctor" />
          </el-select>
          <el-select v-model="filters.industry" placeholder="行业领域" clearable class="filter-item">
            <el-option label="互联网/IT" value="it" />
            <el-option label="金融" value="finance" />
            <el-option label="教育" value="education" />
            <el-option label="医疗健康" value="healthcare" />
            <el-option label="制造业" value="manufacturing" />
            <el-option label="其他" value="other" />
          </el-select>
          <el-select v-model="filters.city" placeholder="期望城市" clearable class="filter-item">
            <el-option label="北京" value="beijing" />
            <el-option label="上海" value="shanghai" />
            <el-option label="广州" value="guangzhou" />
            <el-option label="深圳" value="shenzhen" />
            <el-option label="杭州" value="hangzhou" />
            <el-option label="成都" value="chengdu" />
          </el-select>
          <el-button @click="resetFilters" class="reset-btn">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容区：简历列表 -->
    <div class="content-area">
      <!-- 列表头部信息 -->
      <div class="list-header">
        <div class="result-info">
          <span v-if="!isSearchMode" class="recommend-tag">
            <el-icon><Star /></el-icon>
            为您推荐
          </span>
          <span v-else class="search-result">
            找到 <strong>{{ filteredResumes.length }}</strong> 份符合条件的简历
          </span>
        </div>
        <div class="sort-options">
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-select">
            <el-option label="匹配度优先" value="match" />
            <el-option label="更新时间" value="update_time" />
            <el-option label="工作经验" value="experience" />
          </el-select>
        </div>
      </div>

      <!-- 简历卡片列表 -->
      <div class="resume-cards" v-loading="loading" element-loading-text="正在加载简历数据...">
        <div v-if="!loading && filteredResumes.length === 0" class="empty-state">
          <el-empty description="暂无符合条件的简历" />
        </div>
        <el-card
          v-for="resume in displayResumes"
          :key="resume.id"
          class="resume-card"
          shadow="hover"
        >
          <div class="card-content">
            <!-- 左侧头像 -->
            <div class="avatar-section">
              <el-avatar :src="resume.avatar" :size="60" />
            </div>
            
            <!-- 主体内容 -->
            <div class="main-content">
              <!-- 顶部：姓名和期望职位 -->
              <div class="header-info">
                <h3 class="name">{{ resume.name }}</h3>
                <span class="position">{{ resume.expectedPosition }}</span>
              </div>
              
              <!-- 中间：核心信息标签 -->
              <div class="info-tags">
                <el-tag class="info-tag">{{ resume.experience }}</el-tag>
                <el-tag class="info-tag">{{ resume.education }}</el-tag>
                <el-tag class="info-tag">期望城市：{{ resume.expectedCity }}</el-tag>
              </div>
              
              <!-- 底部：技能标签 -->
              <div class="skills-section">
                <el-tag 
                  v-for="skill in resume.skills.slice(0, 4)" 
                  :key="skill" 
                  class="skill-tag"
                  type="info"
                  size="small"
                >
                  {{ skill }}
                </el-tag>
                <span v-if="resume.skills.length > 4" class="more-skills">
                  +{{ resume.skills.length - 4 }}
                </span>
              </div>
            </div>
            
            <!-- 右侧操作区 -->
            <div class="action-section">
              <el-button type="primary" @click="viewResumeDetail(resume)">
                查看详情
              </el-button>
              <el-button 
                :icon="resume.isFavorited ? 'StarFilled' : 'Star'" 
                circle 
                :type="resume.isFavorited ? 'warning' : 'default'"
                @click="toggleFavorite(resume)"
                class="favorite-btn"
              />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredResumes.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 简历详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      title="简历详情"
      width="80%"
      :before-close="handleCloseDetail"
      class="resume-detail-dialog"
    >
      <ResumeDetail 
        v-if="selectedResume" 
        :resume="selectedResume"
        @start-chat="handleStartChat"
        @give-suggestion="handleGiveSuggestion"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage, ElLoading } from 'element-plus'
import ResumeDetail from './components/ResumeDetail.vue'
import { resumeApi } from '../../api/resume.js'

const router = useRouter()
const route = useRoute()

// 响应式数据
const searchKeyword = ref('')
const filters = ref({
  experience: '',
  education: '',
  industry: '',
  city: ''
})
const sortBy = ref('match')
const currentPage = ref(1)
const pageSize = ref(10)
const showDetailDialog = ref(false)
const selectedResume = ref(null)
const loading = ref(false)
const dataLoaded = ref(false)

// 计算属性
const isSearchMode = computed(() => {
  return searchKeyword.value || 
         filters.value.experience || 
         filters.value.education || 
         filters.value.industry || 
         filters.value.city
})

// 模拟简历数据
const mockResumes = ref([
  {
    id: 1,
    name: '张三',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=zhang',
    expectedPosition: '前端开发工程师',
    experience: '3年经验',
    education: '本科',
    expectedCity: '上海',
    skills: ['Vue.js', 'React', 'JavaScript', 'TypeScript', 'Node.js'],
    isFavorited: false,
    matchScore: 95,
    updateTime: '2024-01-15'
  },
  {
    id: 2,
    name: '李四',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=li',
    expectedPosition: 'Java后端开发',
    experience: '5年经验',
    education: '硕士',
    expectedCity: '北京',
    skills: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Docker'],
    isFavorited: true,
    matchScore: 88,
    updateTime: '2024-01-14'
  },
  {
    id: 3,
    name: '王五',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=wang',
    expectedPosition: '产品经理',
    experience: '2年经验',
    education: '本科',
    expectedCity: '深圳',
    skills: ['产品设计', '需求分析', 'Axure', 'Figma', '数据分析'],
    isFavorited: false,
    matchScore: 82,
    updateTime: '2024-01-13'
  }
])

const allResumes = ref([...mockResumes.value])

// 筛选后的简历列表
const filteredResumes = computed(() => {
  let result = [...allResumes.value]
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(resume => 
      resume.name.toLowerCase().includes(keyword) ||
      resume.expectedPosition.toLowerCase().includes(keyword) ||
      resume.skills.some(skill => skill.toLowerCase().includes(keyword))
    )
  }
  
  // 筛选条件
  if (filters.value.experience) {
    result = result.filter(resume => resume.experience.includes(filters.value.experience))
  }
  if (filters.value.education) {
    result = result.filter(resume => resume.education === filters.value.education)
  }
  if (filters.value.city) {
    result = result.filter(resume => resume.expectedCity === filters.value.city)
  }
  
  // 排序
  if (sortBy.value === 'match') {
    result.sort((a, b) => b.matchScore - a.matchScore)
  } else if (sortBy.value === 'update_time') {
    result.sort((a, b) => new Date(b.updateTime) - new Date(a.updateTime))
  }
  
  return result
})

// 当前页显示的简历
const displayResumes = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredResumes.value.slice(start, end)
})

// 方法
const loadResumeData = async () => {
  try {
    loading.value = true
    // 这里可以根据实际需要调用API
    // const response = await resumeApi.getResumeList()
    // allResumes.value = response.data

    // 目前使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟加载延迟
    dataLoaded.value = true
  } catch (error) {
    console.error('加载简历数据失败:', error)
    ElMessage.error('加载简历数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  ElMessage.success(`搜索到 ${filteredResumes.value.length} 份简历`)
}

const resetFilters = () => {
  searchKeyword.value = ''
  filters.value = {
    experience: '',
    education: '',
    industry: '',
    city: ''
  }
  currentPage.value = 1
}

const toggleFavorite = (resume) => {
  resume.isFavorited = !resume.isFavorited
  ElMessage.success(resume.isFavorited ? '已收藏' : '已取消收藏')
}

const viewResumeDetail = (resume) => {
  selectedResume.value = resume
  showDetailDialog.value = true
}

const handleCloseDetail = () => {
  showDetailDialog.value = false
  selectedResume.value = null
}

const handleStartChat = (resume) => {
  router.push(`/chat?userId=${resume.id}`)
  handleCloseDetail()
}

const handleGiveSuggestion = (resume) => {
  router.push(`/hr/guidance?resumeId=${resume.id}`)
  handleCloseDetail()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 监听路由查询参数
watch(() => route.query, (newQuery) => {
  if (newQuery.recommend === 'true') {
    resetFilters()
  } else if (newQuery.searchFocus === 'true') {
    // 聚焦搜索框
    setTimeout(() => {
      const searchInput = document.querySelector('.search-input input')
      if (searchInput) searchInput.focus()
    }, 100)
  }
}, { immediate: true })

onMounted(() => {
  // 初始化数据加载
  loadResumeData()
})
</script>

<style scoped>
.resume-list {
  box-sizing: border-box;
  padding: 0 24px 16px 16px;
  min-height: calc(100vh - 16px);
  background: #f5f7fa;
  max-width: 1400px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3a36db;
  margin-right: 24px;
  white-space: nowrap;
}

.search-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex: 1;
  max-width: 600px;
}

.search-btn {
  background: #3a36db;
  border-color: #3a36db;
  padding: 0 24px;
  height: 40px;
}

.search-btn:hover {
  background: #2d29b8;
  border-color: #2d29b8;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  width: 140px;
}

.reset-btn {
  color: #666;
  border-color: #ddd;
}

.content-area {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.recommend-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #3a36db;
  font-weight: 600;
  font-size: 16px;
}

.search-result {
  color: #666;
  font-size: 14px;
}

.sort-select {
  width: 140px;
}

.resume-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
  min-height: 200px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.resume-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.resume-card:hover {
  border-color: #3a36db;
  box-shadow: 0 4px 16px 0 rgba(58,54,219,0.1);
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.avatar-section {
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.position {
  color: #3a36db;
  font-weight: 500;
  font-size: 14px;
  background: #f0f0ff;
  padding: 4px 12px;
  border-radius: 16px;
}

.info-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.info-tag {
  background: #f5f5f5;
  color: #666;
  border: none;
  font-size: 13px;
}

.skills-section {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: wrap;
}

.skill-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  font-size: 12px;
}

.more-skills {
  color: #999;
  font-size: 12px;
  margin-left: 4px;
}

.action-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.favorite-btn {
  width: 36px;
  height: 36px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.resume-detail-dialog {
  border-radius: 12px;
}

.resume-detail-dialog .el-dialog__header {
  background: #f8f9fa;
  border-bottom: 1px solid #f0f0f0;
}

.resume-detail-dialog .el-dialog__body {
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .resume-list {
    padding: 0 12px 12px 12px;
  }

  .top-bar {
    flex-direction: column;
    gap: 16px;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar {
    justify-content: center;
  }

  .card-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .header-info {
    flex-direction: column;
    gap: 8px;
  }

  .action-section {
    flex-direction: row;
    justify-content: center;
  }
}
</style>
