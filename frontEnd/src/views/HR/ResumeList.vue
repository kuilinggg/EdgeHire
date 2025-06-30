<template>
  <div class="resume-list">
    <!-- 顶部标题与搜索栏 -->
    <div class="top-bar">
      <div class="page-title">浏览简历</div>
      <div class="search-section">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="输入姓名/手机号/邮箱/学校/岗位关键词"
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" @click="handleSearch" class="search-btn">搜索</el-button>
        </div>
        <div class="filter-bar">
          <el-select v-model="filters.education" placeholder="学历要求" clearable class="filter-item">
            <el-option label="小学" value="1" />
            <el-option label="初中" value="2" />
            <el-option label="高中" value="3" />
            <el-option label="大专" value="4" />
            <el-option label="本科" value="5" />
            <el-option label="硕士" value="6" />
            <el-option label="博士" value="7" />
          </el-select>
          <el-button type="primary" @click="handleSearch" class="search-btn">筛选</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容区：简历列表 -->
    <div class="content-area">
      <div class="resume-cards" v-loading="loading" element-loading-text="正在加载简历数据...">
        <div v-if="!loading && filteredPosts.length === 0" class="empty-state">
          <el-empty description="暂无符合条件的简历" />
        </div>
        <el-card
          v-for="post in displayPosts"
          :key="post.id"
          class="resume-card"
          shadow="hover"
        >
          <div class="card-content">
            <!-- 左侧头像 -->
            <div class="avatar-section">
              <el-avatar :src="userInfoMap[post.userId]?.avatar || post.resume?.avatar" :size="60" />
            </div>
            <!-- 主体内容 -->
            <div class="main-content">
              <div class="header-info">
                <h3 class="name">
                  <span v-html="highlight(userInfoMap[post.userId]?.realname || post.seekerInfo?.realname || '未知用户', searchKeyword)"></span>
                </h3>
                <div class="favor-tags">
                  <el-tag v-for="favor in parseFavor(post.seekerInfo?.favor)" :key="favor" class="favor-tag" effect="plain">
                    <span v-html="highlight(favor, searchKeyword)"></span>
                  </el-tag>
                </div>
              </div>
              <div class="info-tags">
                <el-tag class="info-tag">学历：{{ getEducationText(post.seekerInfo?.education) }}</el-tag>
                <el-tag class="info-tag">
                  毕业院校：<span v-html="highlight(post.seekerInfo?.school || '未填写', searchKeyword)"></span>
                </el-tag>
                <el-tag class="info-tag" v-if="userInfoMap[post.userId]?.phone">
                  手机号：<span v-html="highlight(userInfoMap[post.userId]?.phone, searchKeyword)"></span>
                </el-tag>
                <el-tag class="info-tag" v-if="userInfoMap[post.userId]?.email">
                  邮箱：<span v-html="highlight(userInfoMap[post.userId]?.email, searchKeyword)"></span>
                </el-tag>
              </div>
              <div class="create-time">创建时间：{{ formatDate(post.resume?.createTime) }}</div>
            </div>
            <div class="action-section">
              <el-button type="primary" @click="viewResumeDetail(post)">查看详情</el-button>
            </div>
          </div>
        </el-card>
      </div>
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
        :info="selectedInfo"
        :seeker-info="selectedSeekerInfo"
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
import { useAuthStore } from '../../stores/authStore.js'
import { getPostsWithDetails, getPostsByFilter } from '../../api/post.js'
import { getInfoByUserId } from '../../api/info.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const searchKeyword = ref('')
const filters = ref({
  education: ''
})
const sortBy = ref('match')
const currentPage = ref(1)
const pageSize = ref(10)
const showDetailDialog = ref(false)
const selectedResume = ref(null)
const selectedInfo = ref(null)
const selectedSeekerInfo = ref(null)
const loading = ref(false)
const dataLoaded = ref(false)

// 用户信息缓存（userId -> t_info）
const userInfoMap = ref({})

// 批量拉取所有卡片涉及的userId的t_info
async function fetchUserInfos(posts) {
  const userIds = Array.from(new Set(posts.map(p => p.userId)))
  const promises = userIds.map(async (uid) => {
    if (!userInfoMap.value[uid]) {
      try {
        const res = await getInfoByUserId(uid)
        if (res.data) userInfoMap.value[uid] = res.data
      } catch {}
    }
  })
  await Promise.all(promises)
}

// 计算属性
const isSearchMode = computed(() => {
  return searchKeyword.value || 
         filters.value.experience || 
         filters.value.education || 
         filters.value.industry || 
         filters.value.city
})

// 简历数据
const allPosts = ref([])
const totalCount = ref(0)

// 筛选后的简历列表（现在直接使用API返回的数据）
const filteredPosts = computed(() => {
  return allPosts.value
})

// 当前页显示的简历（API已经处理了分页）
const displayPosts = computed(() => {
  return allPosts.value
})

// 方法
const loadResumeData = async (isSearch = false) => {
  try {
    loading.value = true
    let response
    if (isSearch && (searchKeyword.value || filters.value.education)) {
      response = await getPostsByFilter({
        keyword: searchKeyword.value,
        education: filters.value.education
      })
    } else {
      response = await getPostsWithDetails()
    }
    if (response.data) {
      allPosts.value = response.data
      totalCount.value = response.data.length
      await fetchUserInfos(response.data)
    }
    dataLoaded.value = true
  } catch (error) {
    console.error('加载简历数据失败:', error)
    ElMessage.error('加载简历数据失败，请稍后重试')
    allPosts.value = []
  } finally {
    loading.value = false
  }
}

function getEducationText(educationValue) {
  const educationMap = {
    1: '小学',
    2: '初中',
    3: '高中',
    4: '大专',
    5: '本科',
    6: '硕士',
    7: '博士'
  }
  return educationMap[educationValue] || '未填写'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${day}日 ${h}:${min}`
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadResumeData(true)
}

const toggleFavorite = async (post) => {
  try {
    // 这里可以调用收藏API
    // await favoriteApi.toggle(resume.id)
    post.isFavorited = !post.isFavorited
    ElMessage.success(post.isFavorited ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const viewResumeDetail = (post) => {
  selectedResume.value = post.resume
  selectedInfo.value = userInfoMap.value[post.userId] || {}
  selectedSeekerInfo.value = post.seekerInfo || {}
  showDetailDialog.value = true
}

const handleCloseDetail = () => {
  showDetailDialog.value = false
  selectedResume.value = null
  selectedInfo.value = null
  selectedSeekerInfo.value = null
}

const handleStartChat = (resume) => {
  router.push(`/chat?userId=${resume.id}`)
  handleCloseDetail()
}

const handleSizeChange = async (val) => {
  pageSize.value = val
  currentPage.value = 1
  await loadResumeData()
}

const handleCurrentChange = async (val) => {
  currentPage.value = val
  await loadResumeData()
}

// 监听路由查询参数
watch(() => route.query, async (newQuery) => {
  if (newQuery.searchFocus === 'true') {
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

// 解析favor为数组
function parseFavor(favor) {
  if (!favor) return []
  try {
    const arr = JSON.parse(favor)
    return Array.isArray(arr) ? arr : [favor]
  } catch {
    return favor ? [favor] : []
  }
}

// 高亮关键词
function highlight(text, keyword) {
  if (!keyword) return text
  const reg = new RegExp(keyword, 'ig')
  return String(text).replace(reg, m => `<span style='color:#f56c6c;background:rgba(255,230,230,0.7)'>${m}</span>`)
}
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

.favor-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 2px;
}

.favor-tag {
  background: #eaf3ff;
  color: #3a36db;
  border: 1px solid #e0e7ef;
  font-size: 12px;
  font-weight: 500;
  border-radius: 12px;
  padding: 2px 12px;
  transition: background 0.2s, color 0.2s, border 0.2s;
  box-shadow: none;
  margin: 0;
}

.favor-tag:hover {
  background: #d2e6ff;
  color: #222;
  border-color: #b3bff7;
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

.create-time {
  color: #888;
  font-size: 13px;
  margin-top: 8px;
}
</style>
