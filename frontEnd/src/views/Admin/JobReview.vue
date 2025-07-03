<template>
  <div class="job-review">
    <h2>求职信息审查</h2>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户...(用户名)" style="width: 300px" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button 
          type="info" 
          @click="filterRecentResumes"
          :class="{ 'active-filter': isRecentFilterActive }"
          :disabled="isRecentFilterActive"
        >
          近三日简历
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
        <el-table-column label="头像">
        <template #default="scope">
        <el-image
        style="width: 50px; height: 50px"
        :src="scope.row.avatar"
        :preview-src-list="scope.row.avatar ? [scope.row.avatar] : []"
        :z-index="3000"
        :preview-teleported="true"
        hide-on-click-modal
        >
        <template #error>
          <div class="image-placeholder"></div>
        </template>
      </el-image>
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

      <!-- 弹窗 -->
      <el-dialog
        title="简历详情"
        v-model="dialogVisible"
        width="50%"
        center
      >
        <el-scrollbar height="300px">
          <p style="white-space: pre-wrap">{{ selectedResumeContent }}</p>
        </el-scrollbar>

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
import{ ref , computed , onMounted }from 'vue'
import { getUser } from '../../api/user';
import { getAllResumes , deleteResume } from '../../api/resume';
import { ElMessage ,ElMessageBox } from 'element-plus';
import dayjs from 'dayjs'
import axios from 'axios'

const dialogVisible=ref(false);
const resumes = ref([]);
const filteredResumes = ref([]); 
const selectedResumeContent = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const isRecentFilterActive = ref(false) // 近三日筛选状态

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

//近三日筛选功能
const filterRecentResumes = () => {
  if (!isRecentFilterActive.value) {
    isRecentFilterActive.value = true
    currentPage.value = 1
    filterResumes()
    ElMessage.success('已筛选近三日简历')
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

const isStrictJSON=(str)=> {
  try {
    const parsed = JSON.parse(str);
    return typeof parsed === 'object' && parsed !== null;
  } catch (e) {
    return false;
  }
}

const jsonToTxt = (obj, indent = "") => {
  let result = "";
  
  for (const key in obj) {
    if (Array.isArray(obj[key])) {
      // 处理数组类型的值
      result += `${indent}${key}：\n`;
      
      obj[key].forEach((item, index) => {
        // 如果是对象，递归处理
        if (typeof item === "object") {
          result += `${indent}  ${index + 1}.\n${jsonToTxt(item, indent + "    ")}`;
        } 
        // 如果是基本类型，直接显示
        else {
          result += `${indent}  ${index + 1}. ${item}\n`;
        }
      });
      
    } else if (typeof obj[key] === "object" && obj[key] !== null) {
      // 处理普通对象
      result += `${indent}${key}：\n${jsonToTxt(obj[key], indent + "  ")}`;
    } else {
      // 处理基本类型值
      result += `${indent}${key}：${obj[key]}\n`;
    }
  }
  
  return result;
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
  
  // 应用近三日筛选
  if (isRecentFilterActive.value) {
    const threeDaysAgo = dayjs().subtract(3, 'day').startOf('day')
    result = result.filter(resume => 
      dayjs(resume.createTime).isAfter(threeDaysAgo))
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
  console.log(row.content)
  if (!row || !row.content) {
    selectedResumeContent.value = '暂无简历内容'
    dialogVisible.value = true
    return
  }
  
  // 尝试解析JSON
  if(isStrictJSON(row.content))
  { 
    const parsedData = JSON.parse(row.content);
    //转化后赋值
    selectedResumeContent.value = jsonToTxt(parsedData);
  }
  else
    selectedResumeContent.value = row.content
  
  dialogVisible.value = true
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

.image-placeholder {
  width: 50px;
  height: 50px;
  background: #f5f7fa; /* 浅灰色背景 */
  display: flex;
  align-items: center;
  justify-content: center;
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

  :deep(.el-image-viewer__wrapper) {
  --el-image-viewer-index-text-color: #fff;
  --el-image-viewer-index-font-size: 16px;
  --el-image-viewer-index-text-shadow: 0 1px 1px #000;
}

:deep(.el-image-viewer__mask) {
  background: rgba(0, 0, 0, 0.8);
  opacity: 1;
}

:deep(.el-image-viewer__btn) {
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

:deep(.el-image-viewer__btn:hover) {
  background-color: rgba(0, 0, 0, 0.8);
}
}
</style>