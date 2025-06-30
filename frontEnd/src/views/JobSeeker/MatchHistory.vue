<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">历史匹配</span>
        </div>
      </template>
      <el-row :gutter="32" class="resume-main-row">
        <template v-if="matchList.length > 0">
          <el-col :span="5" class="resume-list-col">
            <el-menu
              :default-active="selectedIndex"
              @select="handleSelect"
              class="resume-list-menu"
              style="height: 100%"
            >
              <el-menu-item
                v-for="(item, idx) in matchList"
                :key="item.id"
                :index="String(idx)"
                class="resume-menu-item"
              >
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;"><Document /></el-icon>
                  <span>匹配{{ idx + 1 }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="19" class="resume-a4-col">
            <div v-if="selectedResume" class="resume-a4-wrapper">
              <div class="resume-a4-paper">
                <!-- 求职者信息卡片，放在简历上方 -->
                <el-card class="seekerinfo-card" style="margin-bottom: 18px;">
                  <div class="profile-view">
                    <p><strong>学历：</strong>{{ educationText }}</p>
                    <p><strong>学校：</strong>{{ seekerInfo.school || '-' }}</p>
                    <p><strong>理想岗位：</strong>
                      <template v-if="favorList.length > 0">
                        <el-tag v-for="(item, idx) in favorList" :key="idx" type="info" style="margin-right: 8px;">{{ item }}</el-tag>
                      </template>
                      <template v-else>-</template>
                    </p>
                    <p><strong>会员类型：</strong>{{ seekerInfo.membership == 0 ? '普通会员': '高级会员' }}</p>
                  </div>
                </el-card>
                <!-- 简历内容卡片 -->
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
            <el-empty v-else description="请选择左侧历史匹配" />
          </el-col>
        </template>
        <template v-else>
          <el-col :span="24">
            <el-empty description="暂无历史匹配数据" />
          </el-col>
        </template>
      </el-row>
      <div class="button-area" v-if="matchList.length > 0">
        <el-button type="danger" size="large" @click="onDeleteMatch" :disabled="!selectedMatch">删除匹配</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPostsByUserId, deletePost } from '../../api/post'
import { getResumeById } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { getSeekerById } from '../../api/seeker'

const matchList = ref([]) // 历史匹配列表
const selectedIndex = ref('0')
const selectedMatch = ref(null)
const selectedResume = ref(null)
const authStore = useAuthStore()

const seekerInfo = ref({ education: '', school: '', favor: '', membership: 0 })
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

const refreshMatchList = async () => {
  const userId = authStore.userId || authStore.user?.id
  try {
    const res = await getPostsByUserId(userId)
    if (res.data && res.data.length > 0) {
      matchList.value = res.data
      // 默认选中第一个
      await handleSelect('0')
    } else {
      matchList.value = []
      selectedMatch.value = null
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch (e) {
    matchList.value = []
    selectedMatch.value = null
    selectedResume.value = null
    selectedIndex.value = '0'
  }
}

const handleSelect = async (idx) => {
  selectedIndex.value = idx
  const match = matchList.value[Number(idx)]
  selectedMatch.value = match
  if (match) {
    // 更新求职者信息
    if (match.seekerInfoId) {
      const seekerRes = await getSeekerById(match.seekerInfoId)
      seekerInfo.value = seekerRes.data || { education: '', school: '', favor: '', membership: 0 }
    }
    // 更新简历
    if (match.resumeId) {
      const resumeRes = await getResumeById(match.resumeId)
      selectedResume.value = resumeRes.data || null
    } else {
      selectedResume.value = null
    }
  } else {
    selectedResume.value = null
  }
}

// 拉取历史匹配列表并刷新选中项
const fetchSeekerInfo = async () => {
  // 默认显示第一个匹配的求职者信息，已由 handleSelect 处理
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

const onDeleteMatch = async () => {
  if (!selectedMatch.value || !selectedMatch.value.id) return
  try {
    await deletePost(selectedMatch.value.id)
    ElMessage.success('删除成功')
    await refreshMatchList()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await refreshMatchList()
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
.button-area {
  text-align: right;
  padding: 16px 0;
}
</style>
