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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="resume-form">
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入简历内容（如教育经历、项目经验等）" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-change="handleAvatarChange"
          >
            <el-avatar v-if="form.avatar" :src="form.avatar" size="large" />
            <el-icon v-else><User /></el-icon>
            <div class="el-upload__text">点击上传头像</div>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import { createResume } from '../../api/resume'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const form = ref({
  content: '',
  avatar: ''
})
const rules = {
  content: [
    { required: true, message: '请输入简历内容', trigger: 'blur' }
  ]
}
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

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const userId = authStore.userId || authStore.user?.id
    if (!userId) {
      ElMessage.error('请先登录')
      router.push('/login')
      return
    }
    try {
      await createResume({
        userId,
        content: form.value.content,
        avatar: form.value.avatar,
        createTime: new Date().toISOString()
      })
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
</script>

<style scoped>
.resume-edit-container {
  padding: 32px;
  max-width: 600px;
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
.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
