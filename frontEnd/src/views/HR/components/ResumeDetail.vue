<template>
  <div class="resume-detail">
    <!-- 顶部：t_info展示 -->
    <div class="detail-header">
      <div class="user-info">
        <el-avatar :src="tInfo.avatar || resume.avatar" :size="80" />
        <div class="user-basic">
          <h2 class="user-name">{{ tInfo.realname || tInfo.name || parsedResumeContent.姓名 || resume.name || '姓名' }}</h2>
          <p class="user-position">{{ tInfo.position || parsedResumeContent.求职意向 || resume.expectedPosition || '求职意向' }}</p>
          <div class="user-tags">
            <el-tag class="basic-tag" v-if="tInfo.age">年龄：{{ tInfo.age }}</el-tag>
            <el-tag class="basic-tag" v-if="tInfo.gender">{{ tInfo.gender === 1 ? '男' : tInfo.gender === 2 ? '女' : '保密' }}</el-tag>
            <el-tag class="basic-tag" v-if="tInfo.phone">{{ tInfo.phone }}</el-tag>
            <el-tag class="basic-tag" v-if="tInfo.email">{{ tInfo.email }}</el-tag>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="startChat">
          <el-icon><ChatDotRound /></el-icon>
          发起沟通
        </el-button>
      </div>
    </div>

    <!-- 简历A4纸风格内容区 -->
    <div class="resume-a4-wrapper">
      <div class="resume-a4-paper">
        <div class="resume-a4-header">
          <div class="resume-a4-title">
            <div class="resume-a4-name">{{ parsedResumeContent.姓名 || '姓名' }}</div>
            <div class="resume-a4-job">{{ parsedResumeContent.求职意向 || '求职意向' }}</div>
          </div>
          <div class="resume-a4-avatar">
            <el-avatar v-if="resume.avatar" :src="resume.avatar" size="large" />
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
        创建时间：<span class="resume-label">{{ formatDate(resume.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  ChatDotRound,
  User,
  Aim,
  Briefcase,
  School,
  Monitor,
  Medal,
  ChatLineRound
} from '@element-plus/icons-vue'

const props = defineProps({
  resume: {
    type: Object,
    required: true
  },
  tInfo: {
    type: Object,
    required: false,
    default: () => ({})
  }
})

const emit = defineEmits(['start-chat'])

// 解析简历内容（严格仿照JobSeeker/ResumeView.vue）
const parsedResumeContent = computed(() => {
  if (!props.resume || !props.resume.content) return {}
  try {
    const obj = JSON.parse(props.resume.content)
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return { 内容: props.resume.content }
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

const startChat = () => {
  emit('start-chat', props.resume)
}
</script>

<style scoped>
.resume-detail {
  max-height: 80vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f0ff 100%);
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-basic {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.user-position {
  font-size: 16px;
  color: #3a36db;
  font-weight: 500;
  margin: 0;
}

.user-tags {
  display: flex;
  gap: 8px;
}

.basic-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.resume-a4-wrapper {
  padding: 24px;
}

.resume-a4-paper {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.resume-a4-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.resume-a4-title {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resume-a4-name {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.resume-a4-job {
  font-size: 16px;
  color: #666;
}

.resume-a4-avatar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resume-a4-section {
  margin-bottom: 24px;
}

.resume-a4-section-title {
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0ff;
}

.resume-a4-section-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resume-a4-field {
  display: flex;
  align-items: center;
}

.resume-a4-job-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resume-create-time-a4 {
  margin-top: 24px;
  text-align: right;
}

.resume-label {
  font-size: 14px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .action-buttons {
    width: 100%;
    justify-content: center;
  }

  .resume-a4-header {
    flex-direction: column;
    gap: 20px;
  }

  .resume-a4-avatar {
    flex-direction: column;
    text-align: center;
  }
}
</style>
