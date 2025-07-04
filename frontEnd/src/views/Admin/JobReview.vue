<template>
  <div class="job-review">
    <h2>求职信息审查</h2>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户...(用户名)" style="width: 300px" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button 
          type="success"
          @click="filterRecentResumes"
          :class="{ 'active-filter': isRecentFilterActive }"
          :disabled="isRecentFilterActive"
        >
          今日简历
        </el-button>
        <el-button 
          type="warning" 
          @click="resetFilters"
          :disabled="!isFilterActive"
        >
          重置筛选
        </el-button>
      </div>

      <el-table :data="paginatedResumes" style="width: 100%">
        <el-table-column prop="id" label="ID" width="180"/>
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column label="用户名">
          <template #default="scope">
            {{ scope.row.username || '加载中...' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button type="success" @click="selectedResumeContent='',contentCheck(scope.row)">查看简历内容</el-button>
            <el-button type="danger" @click="handleDelete(scope.row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

     <!-- 简历详情弹窗 -->
      <el-dialog
      v-model="dialogVisible"
      title="简历详情"
      :fullscreen="resumeType === 'template' || isMobile"
      class="resume-dialog"
      center
      destroy-on-close
    >
      <div class="resume-dialog-content">
        <div v-if="selectedResume">
          <!-- PDF类型简历 -->
          <div v-if="resumeType === 'pdf'" class="pdf-preview-wrapper">
            <iframe
            v-if="pdfUrl"
            :src="encodeURI(pdfUrl)"
            style="width: 100%; min-height: 1200px; border: none; display: block;"
            title="PDF简历预览"
          ></iframe>
            <el-empty v-else description="PDF加载失败" />
          </div>

          <!-- 模板类型简历 -->
          <div v-else-if="resumeType === 'template'" class="resume-template-wrapper">
            <component
              :is="resumeA4Component"
              :content="parsedResumeContent"
              :avatar="selectedResume.avatar"
              class="resume-a4-paper"
            />
          </div>

          <!-- 其他格式 -->
          <div v-else class="other-resume-content">
            <el-scrollbar height="500px">
              <pre>{{ selectedResume.content }}</pre>
            </el-scrollbar>
          </div>
        </div>
        <el-empty v-else description="暂无简历内容" />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">返回</el-button>
      </template>
    </el-dialog>

      <el-pagination
        style="margin-top: 20px; text-align: center"
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="filteredResumes.length"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import{ ref , computed , onMounted, watch }from 'vue'
import { getUser } from '../../api/user';
import { getAllResumes , deleteResume } from '../../api/resume';
import { ElMessage ,ElMessageBox } from 'element-plus';
import dayjs from 'dayjs'
import axios from 'axios'
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../components/ResumeA4PaperBlueLeft.vue'
import ResumeA4PaperBlueTopBar from '../../components/ResumeA4PaperBlueTopBar.vue'

const dialogVisible=ref(false);
const resumes = ref([]);
const filteredResumes = ref([]); 
const selectedResumeContent = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const isRecentFilterActive = ref(false) // 今日筛选状态
const selectedResume = ref(null)
const resumeType = ref(''); // 'pdf', 'template', 'other'
const pdfUrl = ref('');
const resumeTemplate = ref('');
const isMobile = ref(window.innerWidth < 768);

// 解析简历内容
const parseResumeContent = () => {
  if (!selectedResume.value || !selectedResume.value.content) {
    resumeType.value = 'other';
    return;
  }
  
  const content = selectedResume.value.content;
  if(!isStrictJSON(content)) {
    resumeType.value = 'other';
    return;
  }

  // 尝试解析为 JSON
  try {
    const jsonObj = JSON.parse(content);
    
    const keys = Object.keys(jsonObj);
    if (keys.length > 0) {
      if ('importType' in jsonObj) {
        if (jsonObj.importType === 'pdf' && jsonObj.pdfUrl) {
          resumeType.value = 'pdf';
          pdfUrl.value = jsonObj.pdfUrl;
          return;
        }
      } 
      else if ('template' in jsonObj) {
        resumeType.value = 'template';
        resumeTemplate.value = jsonObj.template || '1';
        return;
      }
    }
  } catch (e) {
    console.warn('简历内容不是有效的 JSON:', e);
  }
  
  // 其他情况
  resumeType.value = 'other';
};

const parsedResumeContent = computed(() => {
  if (!selectedResume.value || !selectedResume.value.content) return {};
  try {
    return JSON.parse(selectedResume.value.content);
  } catch {
    return { 内容: selectedResume.value.content };
  }
});


// 计算属性
const paginatedResumes = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredResumes.value.slice(start, end)
})

// 新增：是否有筛选条件激活
const isFilterActive = computed(() => {
  return searchKeyword.value || isRecentFilterActive.value
})

// 搜索函数
const handleSearch = () => {
  currentPage.value = 1 // 搜索时重置到第一页
  filterResumes()
}

//今日筛选功能
const filterRecentResumes = () => {
  if (!isRecentFilterActive.value) {
    isRecentFilterActive.value = true
    currentPage.value = 1
    filterResumes()
    ElMessage.success('已筛选今日简历')
  }
}

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  isRecentFilterActive.value = false
  currentPage.value = 1
  filterResumes()
  ElMessage.success('筛选条件已重置')
}

const resumeA4Component = computed(() => {
  const template = parsedResumeContent.value.template || '1'
  if (template === '2') return ResumeA4PaperBlueLeft
  else if (template === '3') return ResumeA4PaperBlueTopBar
  // 未来可扩展更多模板
  return ResumeA4Paper
})

const isStrictJSON=(str)=> {
  try {
    const parsed = JSON.parse(str);
    return typeof parsed === 'object' && parsed !== null;
  } catch (e) {
    return false;
  }
}

// 获取用户信息并合并到简历数据
const enhanceResumesWithUserInfo = async (resumesData) => {
  // 使用Promise.all并行获取所有用户信息
  const enhancedResumes = await Promise.all(
    resumesData.map(async (resume) => {
      try {
        const response = await getUser(resume.userId)
        return {
          ...resume,
          username: response.data.username || '未知用户'
        }
      } catch (error) {
        return {
          ...resume,
          username: '加载失败'
        }
      }
    })
  )
  
  return enhancedResumes
}

// 过滤函数
const filterResumes = () => {
  let result = [...resumes.value]
  
  // 应用搜索关键词筛选
  if (searchKeyword.value) {
    result = result.filter(resume => 
      String(resume.username).includes(searchKeyword.value))
  }
  
  // 应用今日筛选
  if (isRecentFilterActive.value) {
    const oneDayAgo = dayjs().subtract(0, 'day').startOf('day')
    result = result.filter(resume => 
      dayjs(resume.createTime).isAfter(oneDayAgo))
  }
  
  filteredResumes.value = result
}

const loadResumes = async () => {
  try {
    const res = await getAllResumes()
    const rawResumes = Array.isArray(res.data) ? res.data : [res.data]

    // 增强简历数据
    const enhancedResumes = await enhanceResumesWithUserInfo(rawResumes)
    resumes.value = enhancedResumes

    filterResumes() // 初始加载时也执行过滤
  } catch (e) {
    ElMessage.error('加载简历失败')
  }
}

//简历内容查询
const contentCheck = async (row) => {
  selectedResume.value = row;
  parseResumeContent(); 
  dialogVisible.value = true;
}

const handlePageChange = (page) => {
  currentPage.value = page
}

// 消息发送
const sendReminder = async (userId, content) => {
  try {
    var res = await axios.post(`/admin/sysMsg/${userId}/${content}`)
  } catch (e) {
    return false
  }
  return true
}

const handleDelete = async (resume) => {
  try {
    // 使用 prompt 获取驳回原因
    const { value: rejectReason } = await ElMessageBox.prompt(
      '请输入驳回原因',
      '驳回确认',
      {
        type: 'warning',
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputPattern: /\S+/,  // 非空校验
        inputErrorMessage: '驳回原因不能为空'
      }
    )

    // 执行删除操作
    await deleteResume(resume.id)
    
    // 发送包含驳回原因的通知
    await sendReminder(
      resume.userId,
      `【系统提醒】您创建时间为 ${formatDate(resume.createTime)} 的简历已被驳回！\n驳回原因: ${rejectReason}`
    )

    ElMessage.success('驳回成功')
    loadResumes()
  } catch (e) {
    if (e === 'cancel' || e === 'close') {
      ElMessage.info('已取消驳回操作')
    } 
    // 网络错误或其他异常
    else {
      ElMessage.error('驳回失败: ' + (e.message || '未知错误'))
    }
  }
}

function formatDate(datetime) {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(()=>{
  loadResumes();
})

watch(resumeType, (val) => {
  if (val === 'template') {
    isMobile.value = true
  }
})
</script>


<style scoped>
.job-review {
  padding: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
}

.card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.resume-content {
  white-space: pre-wrap;
  font-size: 16px;
  line-height: 1.6; 
  padding: 0 12px; 
  color: var(--el-text-color-primary);
}

/* 筛选按钮激活状态的样式 */
.active-filter {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

/* 弹窗内容 */
.resume-dialog :deep(.el-dialog) {
  height: auto !important; /* 自动高度由内容决定 */
  max-height: none !important;
}

.resume-dialog :deep(.el-dialog__body) {
  padding: 0 !important;
  margin: 0 !important;
  background: white;
  overflow: unset !important; /* 禁止 dialog body 的内滚动 */
  display: block;
}


.resume-dialog :deep(.el-dialog__body) {
  padding: 0 !important;
  margin: 0 !important;
  background: transparent;
  overflow: unset; /* 防止内部自己滚动 */
  display: block;
}

.resume-dialog :deep(.el-dialog__footer) {
  background: white;
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

.resume-dialog-content {
  width: 100%;
  padding: 0;
  margin: 0;
  background: white;
  overflow: hidden;
}

.resume-a4-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 10px 0;
}

.resume-preview-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 900px;
}

.resume-template-wrapper {
  width: 100%;
  margin: 0;
  padding: 0;
  background: white;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.resume-template-wrapper .resume-a4-paper {
  background: white;
  box-shadow: none;
  width: auto;
  max-width: 794px;
  min-height: 1123px;
  margin: 0 !important;
  padding: 0 !important;
}

/* PDF简历容器样式 */
.pdf-preview-wrapper {
  width: 100%;
  background: white;
  overflow: hidden;
}

.pdf-preview-wrapper iframe {
  width: 100%;
  min-height: 1200px; /* 保证 iframe 初始能撑高显示 */
  border: none;
  display: block;
}

.pdf-preview-wrapper,
.resume-template-wrapper {
  width: 100%;
  margin: 0;
  padding: 0;
  background: white;
  overflow: unset; /* 禁用内层滚动 */
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .resume-template-wrapper .resume-a4-paper {
    width: 100%;
    min-height: auto;
    aspect-ratio: 1/1.414; /* 保持A4比例 */
  }
}

@media (max-width: 992px) {
  .resume-dialog {
    width: 95% !important;
  }
  
  .pdf-preview-wrapper {
    height: calc(100vh - 180px);
  }
}

@media (max-width: 768px) {
  .resume-dialog {
    width: 100% !important;
    margin: 0 !important;
  }
  
  .resume-dialog :deep(.el-dialog) {
    width: 100vw;
  }
  
  .resume-template-wrapper .resume-a4-paper {
    font-size: 14px;
  }
  
  .pdf-preview-wrapper {
    height: calc(100vh - 160px);
  }
}

.resume-template-info {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: center;
  font-size: 16px;
}

.resume-dialog-content pre {
  white-space: pre-wrap;
  word-break: break-all;
  padding: 15px;
  background: #f8f8f8;
  border-radius: 4px;
  font-family: monospace;
}
</style>