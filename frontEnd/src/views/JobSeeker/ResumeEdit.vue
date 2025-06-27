<template>
  <div class="resume-edit-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">填写简历</span>
          <el-button type="success" size="small" class="ai-btn" @click="onAiOptimize">
            <el-icon style="margin-right:4px;"><User /></el-icon>AI优化
          </el-button>
        </div>
      </template>
      <el-row :gutter="24">
        <el-col :span="18">
          <el-form :model="form.Resume" :rules="rules" ref="formRef" label-width="100px" class="resume-form" @change="autoSave">
            <el-divider>基本信息</el-divider>
            <el-form-item label="姓名" prop="姓名">
              <el-input v-model="form.Resume.姓名" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="求职意向" prop="求职意向">
              <el-input v-model="form.Resume.求职意向" placeholder="如：前端开发实习生" />
            </el-form-item>
            <el-divider>个人信息</el-divider>
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="年龄" prop="个人信息.年龄"><el-input v-model="form.Resume.个人信息.年龄" placeholder="如：22" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="电话" prop="个人信息.电话"><el-input v-model="form.Resume.个人信息.电话" placeholder="如：138****8888" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="民族" prop="个人信息.民族"><el-input v-model="form.Resume.个人信息.民族" placeholder="如：汉族" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="邮箱" prop="个人信息.邮箱"><el-input v-model="form.Resume.个人信息.邮箱" placeholder="如：xxx@email.com" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="政治面貌" prop="个人信息.政治面貌"><el-input v-model="form.Resume.个人信息.政治面貌" placeholder="如：共青团员" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="籍贯" prop="个人信息.籍贯"><el-input v-model="form.Resume.个人信息.籍贯" placeholder="如：江苏南京" /></el-form-item></el-col>
            </el-row>
            <el-divider>教育背景</el-divider>
            <el-form-item label="学校" prop="教育背景.学校">
              <el-input v-model="form.Resume.教育背景.学校" placeholder="如：武汉大学" />
            </el-form-item>
            <el-form-item label="学历" prop="教育背景.学历">
              <el-input v-model="form.Resume.教育背景.学历" placeholder="如：本科" />
            </el-form-item>
            <el-form-item label="专业" prop="教育背景.专业">
              <el-input v-model="form.Resume.教育背景.专业" placeholder="如：软件工程" />
            </el-form-item>
            <el-form-item label="时间" prop="教育背景.时间">
              <el-row style="width:100%" align="middle">
                <el-col :span="10">
                  <el-input v-model="form.Resume.教育背景.start" placeholder="起始时间" />
                </el-col>
                <el-col :span="4" style="text-align:center;line-height:32px;">-</el-col>
                <el-col :span="10">
                  <el-input v-model="form.Resume.教育背景.end" placeholder="结束时间" />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item label="GPA" prop="教育背景.GPA"><el-input v-model="form.Resume.教育背景.GPA" placeholder="如：3.8/4.0" /></el-form-item>
            <el-form-item label="主修课程" prop="教育背景.主修课程">
              <div style="width:100%">
                <el-input v-for="(course, idx) in form.Resume.教育背景.主修课程" :key="idx" v-model="form.Resume.教育背景.主修课程[idx]" :placeholder="'主修课程'+(idx+1)+'（如：数据结构）'" style="margin-bottom:4px;width:90%;display:inline-block;" />
                <el-button type="primary" size="small" @click="addCourse" style="margin-left:8px;">添加</el-button>
                <el-button v-if="form.Resume.教育背景.主修课程.length > 1" type="danger" size="small" @click="removeCourse" style="margin-left:4px;">删除</el-button>
              </div>
            </el-form-item>
            <el-form-item label="个人荣誉" prop="教育背景.个人荣誉">
              <el-input v-model="form.Resume.教育背景.个人荣誉" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" placeholder="如：国家奖学金、三好学生等" />
            </el-form-item>
            <el-divider>任职情况</el-divider>
            <div v-for="(job, idx) in form.Resume.任职情况" :key="idx" style="margin-bottom:12px;">
              <el-row :gutter="12">
                <el-col :span="8"><el-form-item :label="'单位'+(idx+1)" :prop="'任职情况.'+idx+'.单位'">
                  <el-input v-model="job.单位" placeholder="如：字节跳动" />
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="职位" :prop="'任职情况.'+idx+'.职位'">
                  <el-input v-model="job.职位" placeholder="如：前端开发实习生" />
                </el-form-item></el-col>
                <el-col :span="8">
                  <el-form-item label="时间" :prop="'任职情况.'+idx+'.时间'">
                    <el-row align="middle">
                      <el-col :span="10">
                        <el-input v-model="job.start" placeholder="起始时间（如：2022-07）" />
                      </el-col>
                      <el-col :span="4" style="text-align:center;line-height:32px;">-</el-col>
                      <el-col :span="10">
                        <el-input v-model="job.end" placeholder="结束时间（如：2022-09）" />
                      </el-col>
                    </el-row>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="职责" :prop="'任职情况.'+idx+'.职责'">
                <el-input v-model="job.职责" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" placeholder="如：参与企业级管理后台开发" />
              </el-form-item>
              <el-form-item v-if="job.活动描述 !== undefined" label="活动描述" :prop="'任职情况.'+idx+'.活动描述'">
                <el-input v-model="job.活动描述" placeholder="如：组织技术分享活动" />
              </el-form-item>
              <el-form-item v-if="job.结果 !== undefined" label="结果" :prop="'任职情况.'+idx+'.结果'">
                <el-input v-model="job.结果" placeholder="如：提升团队协作效率" />
              </el-form-item>
              <el-button v-if="form.Resume.任职情况.length > 1" type="danger" size="small" @click="removeJob(idx)" style="margin-bottom:8px;">删除</el-button>
            </div>
            <el-button type="primary" size="small" @click="addJob" style="margin-bottom:12px;">添加单位</el-button>
            <el-divider>实习/兼职</el-divider>
            <div v-for="(exp, idx) in form.Resume.实习_兼职" :key="idx" style="margin-bottom:12px;">
              <el-row :gutter="12">
                <el-col :span="8"><el-form-item :label="'单位'+(idx+1)" :prop="'实习_兼职.'+idx+'.单位'">
                  <el-input v-model="exp.单位" placeholder="如：腾讯" />
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="职位" :prop="'实习_兼职.'+idx+'.职位'">
                  <el-input v-model="exp.职位" placeholder="如：兼职助教" />
                </el-form-item></el-col>
                <el-col :span="8">
                  <el-form-item label="时间" :prop="'实习_兼职.'+idx+'.时间'">
                    <el-row align="middle">
                      <el-col :span="10">
                        <el-input v-model="exp.start" placeholder="起始时间（如：2023-01）" />
                      </el-col>
                      <el-col :span="4" style="text-align:center;line-height:32px;">-</el-col>
                      <el-col :span="10">
                        <el-input v-model="exp.end" placeholder="结束时间（如：2023-03）" />
                      </el-col>
                    </el-row>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="职责" :prop="'实习_兼职.'+idx+'.职责'">
                <el-input v-model="exp.职责" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" placeholder="如：协助课程答疑" />
              </el-form-item>
              <el-button v-if="form.Resume.实习_兼职.length > 1" type="danger" size="small" @click="removeIntern(idx)" style="margin-bottom:8px;">删除</el-button>
            </div>
            <el-button type="primary" size="small" @click="addIntern" style="margin-bottom:12px;">添加单位</el-button>
            <el-divider>自我评价</el-divider>
            <el-form-item label="自我评价" prop="自我评价">
              <el-input v-model="form.Resume.自我评价" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" placeholder="如：学习能力强、沟通能力好等" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSubmit">提交</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="6">
          <div class="avatar-box">
            <el-upload
              class="avatar-uploader"
              action=""
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-change="handleAvatarChange"
            >
              <el-avatar v-if="form.avatar" :src="form.avatar" size="large" />
              <el-icon v-else><User /></el-icon>
              <div v-if="!form.avatar" class="el-upload__text">点击上传头像</div>
            </el-upload>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import { createResume, updateResume, getResumeByUserId } from '../../api/resume'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const defaultForm = {
  Resume: {
    姓名: '',
    求职意向: '',
    个人信息: {
      年龄: '', 电话: '', 民族: '', 邮箱: '', 政治面貌: '', 籍贯: ''
    },
    教育背景: {
      学校: '', 学历: '', 专业: '', start: '', end: '', GPA: '', 主修课程: [''], 个人荣誉: ''
    },
    任职情况: [
      { 单位: '', 职位: '', start: '', end: '', 职责: '' }
    ],
    实习_兼职: [
      { 单位: '', 职位: '', start: '', end: '', 职责: '' }
    ],
    自我评价: ''
  },
  avatar: ''
}
const form = ref(JSON.parse(JSON.stringify(defaultForm)))
const resumeId = ref(null)

// 页面加载时获取当前用户简历，优先本地草稿
async function loadResume() {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) return
  // 优先读取本地草稿
  const draftKey = `resume_draft_${userId}`
  const draft = localStorage.getItem(draftKey)
  if (draft) {
    try {
      const parsed = JSON.parse(draft)
      form.value = parsed
      return
    } catch (e) {
      // 草稿损坏则忽略
    }
  }
  // 无草稿再查后端
  try {
    const res = await getResumeByUserId(userId)
    if (res && res.data) {
      form.value.Resume = JSON.parse(res.data.content)
      form.value.avatar = res.data.avatar || ''
      resumeId.value = res.data.id
    }
  } catch (e) {
    // 没有简历则保持默认
  }
}
loadResume()

function getDynamicRules(listKey, fields, labelMap) {
  const rules = {}
  const list = form.value.Resume[listKey]
  for (let i = 0; i < list.length; i++) {
    fields.forEach(field => {
      const prop = `${listKey}.${i}.${field}`
      rules[prop] = [{ required: true, message: `${labelMap[field]}未填写`, trigger: 'blur' }]
    })
  }
  return rules
}

function getCourseRules() {
  const rules = {}
  const courses = form.value.Resume.教育背景.主修课程
  for (let i = 0; i < courses.length; i++) {
    rules[`教育背景.主修课程.${i}`] = [{ required: true, message: `主修课程${i+1}未填写`, trigger: 'blur' }]
  }
  return rules
}

function computeRules() {
  return {
    ...staticRules,
    ...getDynamicRules('任职情况', ['单位', '职位', 'start', 'end', '职责'], { 单位: '单位', 职位: '职位', start: '起始时间', end: '结束时间', 职责: '职责' }),
    ...getDynamicRules('实习_兼职', ['单位', '职位', 'start', 'end', '职责'], { 单位: '单位', 职位: '职位', start: '起始时间', end: '结束时间', 职责: '职责' }),
    ...getCourseRules()
  }
}

const staticRules = {
  姓名: [{ required: true, message: '姓名未填写', trigger: 'blur' }],
  求职意向: [{ required: true, message: '求职意向未填写', trigger: 'blur' }],
  '个人信息.年龄': [{ required: true, message: '年龄未填写', trigger: 'blur' }],
  '个人信息.电话': [{ required: true, message: '电话未填写', trigger: 'blur' }],
  '个人信息.民族': [{ required: true, message: '民族未填写', trigger: 'blur' }],
  '个人信息.邮箱': [
    { required: true, message: '邮箱未填写', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  '个人信息.政治面貌': [{ required: true, message: '政治面貌未填写', trigger: 'blur' }],
  '个人信息.籍贯': [{ required: true, message: '籍贯未填写', trigger: 'blur' }],
  '教育背景.学校': [{ required: true, message: '学校未填写', trigger: 'blur' }],
  '教育背景.学历': [{ required: true, message: '学历未填写', trigger: 'blur' }],
  '教育背景.专业': [{ required: true, message: '专业未填写', trigger: 'blur' }],
  '教育背景.start': [{ required: true, message: '起始时间未填写', trigger: 'blur' }],
  '教育背景.end': [{ required: true, message: '结束时间未填写', trigger: 'blur' }],
  '教育背景.GPA': [{ required: true, message: 'GPA未填写', trigger: 'blur' }],
  '教育背景.主修课程': [{ required: true, message: '主修课程未填写', trigger: 'blur' }],
  '教育背景.个人荣誉': [{ required: true, message: '个人荣誉未填写', trigger: 'blur' }],
  自我评价: [{ required: true, message: '自我评价未填写', trigger: 'blur' }],
}

const rules = computed(computeRules)
const formRef = ref()
const authStore = useAuthStore()
const router = useRouter()

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  return isImage
}

const handleAvatarChange = (file) => {
  // 这里只做本地预览，实际项目应上传到服务器后返回url
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const LOCAL_DRAFT_KEY = computed(() => {
  const userId = authStore.userId || authStore.user?.id || 'guest'
  return `resume_draft_${userId}`
})

// 保存草稿到 localStorage
function saveDraft() {
  try {
    const data = JSON.parse(JSON.stringify(form.value))
    localStorage.setItem(LOCAL_DRAFT_KEY.value, JSON.stringify(data))
  } catch (e) {}
}
// 读取草稿
function loadDraft() {
  try {
    const str = localStorage.getItem(LOCAL_DRAFT_KEY.value)
    if (str) {
      const draft = JSON.parse(str)
      form.value = draft
      return true
    }
  } catch (e) {}
  return false
}
// 清除草稿
function clearDraft() {
  localStorage.removeItem(LOCAL_DRAFT_KEY.value)
}

let autoSaveTimer = null
function autoSave() {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(() => {
    saveDraft()
  }, 1000)
}

// 页面加载时优先加载草稿，无草稿再查后端
async function loadResumeWithDraft() {
  if (loadDraft()) return
  await loadResume()
}
loadResumeWithDraft()

const onSubmit = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const userId = authStore.userId || authStore.user?.id
    if (!userId) {
      ElMessage.error('请先登录')
      router.push('/login')
      return
    }
    // 处理时间字段拼接
    const data = JSON.parse(JSON.stringify(form.value))
    data.Resume.教育背景.时间 = (data.Resume.教育背景.start || '') + ' - ' + (data.Resume.教育背景.end || '')
    delete data.Resume.教育背景.start
    delete data.Resume.教育背景.end
    data.Resume.任职情况.forEach(job => {
      job.时间 = (job.start || '') + ' - ' + (job.end || '')
      delete job.start
      delete job.end
    })
    data.Resume.实习_兼职.forEach(exp => {
      exp.时间 = (exp.start || '') + ' - ' + (exp.end || '')
      delete exp.start
      delete exp.end
    })
    try {
      let resp
      if (resumeId.value) {
        resp = await updateResume({
          id: resumeId.value,
          userId,
          content: JSON.stringify(data.Resume),
          avatar: data.avatar
        })
      } else {
        resp = await createResume({
          userId,
          content: JSON.stringify(data.Resume),
          avatar: data.avatar
        })
        if (resp && resp.data && resp.data.id) resumeId.value = resp.data.id
      }
      // 提交成功后清除本地草稿
      clearDraft()
      ElMessage.success('简历提交成功！')
      router.push('/jobseeker/resume-view')
    } catch (e) {
      ElMessage.error('提交失败，请重试')
    }
  })
}

const onAiOptimize = () => {
  ElMessage.info('AI优化功能开发中...')
}

function addCourse() {
  form.value.Resume.教育背景.主修课程.push('')
}
function removeCourse() {
  if (form.value.Resume.教育背景.主修课程.length > 1) {
    form.value.Resume.教育背景.主修课程.pop()
  }
}
function addJob() {
  form.value.Resume.任职情况.push({ 单位: '', 职位: '', start: '', end: '', 职责: '' })
}
function removeJob(idx) {
  if (form.value.Resume.任职情况.length > 1) {
    form.value.Resume.任职情况.splice(idx, 1)
  }
}
function addIntern() {
  form.value.Resume.实习_兼职.push({ 单位: '', 职位: '', start: '', end: '', 职责: '' })
}
function removeIntern(idx) {
  if (form.value.Resume.实习_兼职.length > 1) {
    form.value.Resume.实习_兼职.splice(idx, 1)
  }
}


// 页面加载时仅加载后端简历
loadResume()
// 已移除草稿相关函数、变量、事件监听、合并逻辑等
</script>

<style scoped>
.resume-edit-container {
  padding: 32px;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  background: #f8f9fa;
}
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.resume-title {
  font-size: 22px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
}
.ai-btn {
  margin-left: 16px;
}
.resume-form {
  margin-top: 24px;
}
.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 12px;
}
.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
</style>
