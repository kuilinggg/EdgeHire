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
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 简历详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="简历详情"
      width="80%"
      :before-close="handleCloseDetail"
      destroy-on-close
    >
      <ResumeDetail
        v-if="selectedResume"
        :resume="selectedResume"
        @start-chat="handleStartChat"
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
import { resumeRecommendationApi } from '../../api/hr.js'
import { useAuthStore } from '../../stores/authStore.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

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

// 简历数据
const allResumes = ref([])
const totalCount = ref(0)

// 筛选后的简历列表（现在直接使用API返回的数据）
const filteredResumes = computed(() => {
  return allResumes.value
})

// 当前页显示的简历（API已经处理了分页）
const displayResumes = computed(() => {
  return allResumes.value
})

// 方法
const loadResumeData = async (isSearch = false) => {
  try {
    loading.value = true
    const userId = authStore.userId || '1'

    let response
    if (isSearch && (searchKeyword.value || hasActiveFilters())) {
      // 搜索模式
      const searchParams = {
        keyword: searchKeyword.value,
        minEducation: getEducationValue(filters.value.education),
        position: filters.value.industry,
        page: currentPage.value - 1, // 后端使用0基索引
        size: pageSize.value
      }
      response = await resumeRecommendationApi.searchResumes(userId, searchParams)
    } else {
      // 推荐模式
      response = await resumeRecommendationApi.getRecommendations(
        userId,
        currentPage.value - 1,
        pageSize.value
      )
    }

    if (response.data && response.data.data) {
      // 转换后端数据格式为前端格式
      allResumes.value = response.data.data.map(item => ({
        id: item.resumeId,
        userId: item.userId,
        name: item.name,
        avatar: item.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${item.name}`,
        expectedPosition: item.expectedPosition,
        experience: getExperienceText(item.age), // 根据年龄推算经验
        education: getEducationText(item.education),
        expectedCity: item.expectedCity || '未填写',
        skills: item.skills || ['技能待完善'],
        isFavorited: false, // 后续可以从收藏API获取
        matchScore: item.matchScore,
        updateTime: item.createTime,
        averageScore: item.averageScore
      }))
      totalCount.value = response.data.total
    }

    dataLoaded.value = true
  } catch (error) {
    console.error('加载简历数据失败:', error)
    ElMessage.error('加载简历数据失败，请稍后重试')
    // 使用默认数据作为fallback
    allResumes.value = []
  } finally {
    loading.value = false
  }
}

// 辅助方法
const hasActiveFilters = () => {
  return filters.value.experience ||
         filters.value.education ||
         filters.value.industry ||
         filters.value.city
}

const getEducationValue = (education) => {
  const educationMap = {
    'high_school': 1,
    'college': 2,
    'bachelor': 3,
    'master': 4,
    'doctor': 5
  }
  return educationMap[education] || null
}

const getEducationText = (educationValue) => {
  const educationMap = {
    1: '高中',
    2: '大专',
    3: '本科',
    4: '硕士',
    5: '博士'
  }
  return educationMap[educationValue] || '未填写'
}

const getExperienceText = (age) => {
  if (age <= 22) return '应届毕业生'
  if (age <= 25) return '1-3年经验'
  if (age <= 30) return '3-5年经验'
  if (age <= 35) return '5-10年经验'
  return '10年以上经验'
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadResumeData(true)
  ElMessage.success(`搜索到 ${allResumes.value.length} 份简历`)
}

const resetFilters = async () => {
  searchKeyword.value = ''
  filters.value = {
    experience: '',
    education: '',
    industry: '',
    city: ''
  }
  currentPage.value = 1
  await loadResumeData(false) // 重新加载推荐数据
}

const toggleFavorite = async (resume) => {
  try {
    // 这里可以调用收藏API
    // await favoriteApi.toggle(resume.id)
    resume.isFavorited = !resume.isFavorited
    ElMessage.success(resume.isFavorited ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
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

const handleSizeChange = async (val) => {
  pageSize.value = val
  currentPage.value = 1
  await loadResumeData(isSearchMode.value)
}

const handleCurrentChange = async (val) => {
  currentPage.value = val
  await loadResumeData(isSearchMode.value)
}

// 监听路由查询参数
watch(() => route.query, async (newQuery) => {
  if (newQuery.recommend === 'true') {
    await resetFilters()
  } else if (newQuery.searchFocus === 'true') {
    // 聚焦搜索框
    setTimeout(() => {
      const searchInput = document.querySelector('.search-input input')
      if (searchInput) searchInput.focus()
    }, 100)
  }
}, { immediate: true })

onMounted(async () => {
  // 初始化数据加载
  await loadResumeData()
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
