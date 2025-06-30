<template>
  <div class="resume-view-container">
    <!-- 顶部提示 -->
    <el-alert
      title="请在下方选择需要提交的简历"
      type="warning"
      show-icon
      center
      class="top-tip-el-alert"
      :closable="false"
    />
    <!-- 求职信息单卡片展示 -->
    <el-card style="margin-bottom: 24px;">
      <template #header>
        <div class="header-bar">
          <span class="resume-title">求职信息</span>
        </div>
      </template>
      <el-row :gutter="32" class="seeker-main-row-short">
        <el-col :span="24">
          <el-card v-if="seekerInfo && seekerInfo.id" class="seeker-single-card">
            <div><strong>学历：</strong>{{ educationOptions.find(e => e.value === seekerInfo.education)?.label || '-' }}</div>
            <div><strong>学校：</strong>{{ seekerInfo.school || '-' }}</div>
            <div><strong>理想岗位：</strong>
              <template v-if="favorList.length > 0">
                <el-tag v-for="(item, idx) in favorList" :key="idx" type="info" style="margin-right: 8px;">{{ item }}</el-tag>
              </template>
              <template v-else>-</template>
            </div>
            <div><strong>会员类型：</strong>{{ seekerInfo.membership === 0 ? '普通会员' : '高级会员' }}</div>
          </el-card>
          <el-empty v-else description="暂无求职信息" />
        </el-col>
      </el-row>
    </el-card>
    <!-- 新建匹配卡片 -->
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">简历列表</span>
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
      <div style="margin-top: 32px; text-align: center; display: flex; justify-content: center; gap: 16px;">
        <el-button type="primary" size="large" @click="onSubmitMatch" :disabled="!selectedResume || !seekerInfo || !seekerInfo.id">提交匹配</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createPost } from '../../api/post'
import { useAuthStore } from '../../stores/authStore'
import { getSeekerByUserId } from '../../api/seeker'
import { getResumesByUserId } from '../../api/resume'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const seekerInfo = ref({ id: null, education: '', school: '', favor: '', membership: 0 })
const educationOptions = [
  { value: 1, label: '小学' },
  { value: 2, label: '初中' },
  { value: 3, label: '高中' },
  { value: 4, label: '大专' },
  { value: 5, label: '本科' },
  { value: 6, label: '硕士' },
  { value: 7, label: '博士' }
]

const educationText = computed(() => {
  const found = educationOptions.find(e => e.value === seekerInfo.value.education)
  return found ? found.label : '-'
})

const favorList = computed(() => {
  if (!seekerInfo.value.favor) return []
  try {
    const arr = JSON.parse(seekerInfo.value.favor)
    return Array.isArray(arr) ? arr : [seekerInfo.value.favor]
  } catch {
    return seekerInfo.value.favor ? [seekerInfo.value.favor] : []
  }
})

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

const fetchSeekerInfo = async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) return
  try {
    const { data } = await getSeekerByUserId(userId)
    seekerInfo.value = data || { id: null, education: '', school: '', favor: '', membership: 0 }
  } catch {
    seekerInfo.value = { id: null, education: '', school: '', favor: '', membership: 0 }
  }
}
const fetchResumeList = async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) return
  try {
    const res = await getResumesByUserId(userId)
    resumeList.value = res.data || []
    if (resumeList.value.length > 0) {
      selectedResume.value = resumeList.value[0]
      selectedIndex.value = '0'
    } else {
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch {
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

const onSubmitMatch = async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId || !seekerInfo.value?.id || !selectedResume.value?.id) {
    ElMessage.error('信息不完整，无法提交')
    return
  }
  const postData = {
    userId: userId,
    seekerInfoId: seekerInfo.value.id,
    resumeId: selectedResume.value.id
  }
  try {
    await createPost(postData)
    ElMessage.success('匹配提交成功')
  } catch {
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  fetchSeekerInfo()
  fetchResumeList()
})
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
.seekerinfo-card {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08), 0 1.5px 6px 0 rgba(0,0,0,0.04);
}
.profile-view {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px 0 8px 0;
  width: 100%;
}
.profile-view p {
  margin: 10px 0 4px 0;
  font-size: 16px;
  color: #333;
  letter-spacing: 0.5px;
  text-align: left;
  width: 100%;
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
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 64px;
}
.resume-title {
  font-size: 24px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
  line-height: 1.2;
}
.seeker-main-row-short {
  min-height: unset;
  height: 220px;
  align-items: flex-start;
}
.seeker-list-col-scroll {
  min-width: 180px;
  max-width: 260px;
  height: 200px;
  overflow-y: auto;
}
.seeker-single-card {
  min-height: 180px;
  max-width: 400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.top-tip-el-alert {
  margin-bottom: 18px;
  font-size: 18px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}
</style>
