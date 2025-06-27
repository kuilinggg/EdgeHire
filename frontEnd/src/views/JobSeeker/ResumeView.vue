<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">查看简历</span>
        </div>
      </template>
      <el-row :gutter="32" class="resume-main-row">
        <template v-if="resumeList.length > 0">
          <el-col :span="5" class="resume-list-col">
            <el-menu
              :default-active="selectedIndex"
              @select="handleSelect"
              class="resume-list-menu"
              style="height: 100%"
            >
              <el-menu-item
                v-for="(item, idx) in resumeList"
                :key="item.id"
                :index="String(idx)"
                class="resume-menu-item"
              >
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;"><Document /></el-icon>
                  <span>简历{{ idx + 1 }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="19" class="resume-a4-col">
            <div v-if="selectedResume" class="resume-a4-wrapper">
              <div class="resume-a4-paper">
                <div class="resume-a4-header">
                  <div class="resume-a4-title">
                    <div class="resume-a4-name">{{ parsedResumeContent.姓名 || '姓名' }}</div>
                    <div class="resume-a4-job">{{ parsedResumeContent.求职意向 || '求职意向' }}</div>
                  </div>
                  <div class="resume-a4-avatar">
                    <el-avatar v-if="selectedResume.avatar" :src="selectedResume.avatar" size="large" />
                    <span v-else>无</span>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['个人信息']">
                  <div class="resume-a4-section-title">个人信息</div>
                  <div class="resume-a4-section-content">
                    <span v-for="(v, k) in parsedResumeContent['个人信息']" :key="k" class="resume-a4-field">{{ k }}：{{ v }}</span>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['教育背景']">
                  <div class="resume-a4-section-title">教育背景</div>
                  <div class="resume-a4-section-content">
                    <span v-for="(v, k) in parsedResumeContent['教育背景']" :key="k" class="resume-a4-field">{{ k }}：<template v-if="Array.isArray(v)">{{ v.join('，') }}</template><template v-else>{{ v }}</template></span>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['任职情况'] && parsedResumeContent['任职情况'].length">
                  <div class="resume-a4-section-title">任职情况</div>
                  <div class="resume-a4-section-content">
                    <div v-for="(job, idx) in parsedResumeContent['任职情况']" :key="idx" class="resume-a4-job-block">
                      <span v-for="(v, k) in job" :key="k">{{ k }}：{{ v }}</span>
                    </div>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['实习_兼职'] && parsedResumeContent['实习_兼职'].length">
                  <div class="resume-a4-section-title">实习/兼职</div>
                  <div class="resume-a4-section-content">
                    <div v-for="(exp, idx) in parsedResumeContent['实习_兼职']" :key="idx" class="resume-a4-job-block">
                      <span v-for="(v, k) in exp" :key="k">{{ k }}：{{ v }}</span>
                    </div>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['自我评价']">
                  <div class="resume-a4-section-title">自我评价</div>
                  <div class="resume-a4-section-content">{{ parsedResumeContent['自我评价'] }}</div>
                </div>
              </div>
              <div class="resume-create-time-a4">
                创建时间：<span class="resume-label">{{ formatDate(selectedResume.createTime) }}</span>
              </div>
              <div style="margin-top: 32px; text-align: center; display: flex; justify-content: center; gap: 16px;">
                <el-button type="primary" size="large" icon="Edit" class="edit-btn-main" @click="onEditSelected">
                  编辑
                </el-button>
                <el-popconfirm title="确定删除该简历？" @confirm="onDeleteSelected">
                  <template #reference>
                    <el-button type="danger" size="large" icon="Delete" class="delete-btn-main">
                      删除当前简历
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-else description="请选择左侧简历" />
          </el-col>
        </template>
        <template v-else>
          <el-col :span="24">
            <el-empty description="暂无简历数据" />
          </el-col>
        </template>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getResumesByUserId, deleteResume } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { Document, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const router = useRouter()

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

const onDeleteSelected = async () => {
  if (!selectedResume.value) return
  const id = selectedResume.value.id
  try {
    await deleteResume(id)
    ElMessage.success('删除成功')
    // 删除成功后刷新简历列表
    await refreshResumeList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const onEditSelected = () => {
  if (!selectedResume.value) return
  router.push({
    path: '/jobseeker/resume-edit',
    query: { id: selectedResume.value.id }
  })
}

// 拉取简历列表并刷新选中项
const refreshResumeList = async () => {
  const userId = authStore.userId || authStore.user?.id
  try {
    const res = await getResumesByUserId(userId)
    if (res.data && res.data.length > 0) {
      resumeList.value = res.data
      selectedResume.value = res.data[0]
      selectedIndex.value = '0'
    } else {
      resumeList.value = []
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch (e) {
    resumeList.value = []
    selectedResume.value = null
    selectedIndex.value = '0'
  }
}

const parsedResumeContent = computed(() => {
  if (!selectedResume.value || !selectedResume.value.content) return {}
  try {
    const obj = JSON.parse(selectedResume.value.content)
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return { 内容: selectedResume.value.content }
  }
})

const formatDate = (dateStr) => {
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

onMounted(refreshResumeList)
</script>

<style scoped>
.resume-view-container {
  padding: 32px;
  max-width: 900px;
  min-width: 700px;
  margin: 0 auto;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  background: #f8f9fa;
}
.resume-edit-container {
  padding: 32px;
  max-width: 900px;
  min-width: 700px;
  margin: 0 auto;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  background: #f8f9fa;
}
.profile-container {
  padding: 32px;
  max-width: 900px;
  min-width: 700px;
  margin: 32px auto 0 auto;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08), 0 1.5px 6px 0 rgba(0,0,0,0.04);
  position: relative;
}
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.resume-title {
  font-size: 22px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
}
.resume-main-row {
  min-height: 800px;
}
.resume-list-col {
  min-width: 180px;
  max-width: 260px;
}
.resume-a4-col {
  min-width: 700px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.resume-a4-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 auto;
}
.resume-a4-paper {
  width: 210mm;
  max-width: 100%;
  min-height: 297mm;
  background: #fff;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  border-radius: 8px;
  padding: 32px 36px 24px 36px;
  margin-bottom: 18px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}
.resume-a4-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  justify-content: space-between;
}
.resume-a4-title {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
}
.resume-a4-avatar {
  margin-left: 32px;
  margin-right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 90px;
  height: 90px;
}
.resume-a4-name {
  font-size: 28px;
  font-weight: bold;
  color: #222;
  margin-bottom: 8px;
}
.resume-a4-job {
  font-size: 18px;
  color: #666;
}
.resume-a4-section {
  margin-bottom: 18px;
}
.resume-a4-section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3a4b;
  margin-bottom: 8px;
  border-left: 4px solid #409EFF;
  padding-left: 8px;
}
.resume-a4-section-content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  font-size: 15px;
  color: #333;
  line-height: 1.8;
}
.resume-a4-field {
  min-width: 120px;
}
.resume-a4-job-block {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
}
.resume-create-time-a4 {
  margin-top: 12px;
  font-size: 15px;
  color: #888;
  text-align: left;
  width: 210mm;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
}
</style>
