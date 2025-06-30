<template>
  <div class="job-review">
    <h2>求职信息审查</h2>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索特定用户" style="width: 300px" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
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
            :preview-src-list="[scope.row.avatar]"
          />
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
      :total="resumes.length"
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

const dialogVisible=ref(false);
const resumes = ref([]);
const filteredResumes = ref([]); 
const selectedResumeContent = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')


// 计算属性
const paginatedResumes = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredResumes.value.slice(start, end)
})

// 搜索函数
const handleSearch = () => {
  currentPage.value = 1 // 搜索时重置到第一页
  filterResumes()
}

const isStrictJSON=(str)=> {
  try {
    const parsed = JSON.parse(str);
    return typeof parsed === 'object' && parsed !== null;
  } catch (e) {
    return false;
  }
}

const jsonToTxt= (obj, indent = "")=> {
  let result = "";
  for (const key in obj) {
    if (Array.isArray(obj[key])) {
      result += `${indent}${key}：${obj[key].join(", ")}\n`;
    } else if (typeof obj[key] === "object") {
      result += `${indent}${key}：\n${jsonToTxt(obj[key], indent + "  ")}`;
    } else {
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
  if (!searchKeyword.value) {
    filteredResumes.value = [...resumes.value]
    return
  }
  
  filteredResumes.value = resumes.value.filter(resume => 
    String(resume.username).includes(searchKeyword.value)
  )
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

const handleDelete=async(resume)=>{
  try {
    await ElMessageBox.confirm(
      `确定驳回该简历吗？`,
      '提示',
      { type: 'warning' }
    )
    await deleteResume(resume.id)
    ElMessage.success('驳回成功')
    loadResumes()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('驳回失败')
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
}

.card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>