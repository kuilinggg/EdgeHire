<template>
  <div class="profile-container">
    <el-alert v-if="showFillAlert" title="请先完善个人信息" type="warning" show-icon class="top-alert" />
    
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><User /></el-icon>
          个人信息管理
        </h1>
        <p class="page-subtitle">完善您的个人信息，提升求职成功率</p>
      </div>
    </div>

    <!-- 信息展示卡片 -->
    <el-card v-if="!editing" class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><User /></el-icon>
            基本信息
          </h3>
          <el-button type="primary" class="edit-btn" @click="editing = true">
            <el-icon><Edit /></el-icon>
            编辑信息
          </el-button>
        </div>
      </template>
      
      <div class="info-display">
        <div class="avatar-section">
          <el-avatar :src="form.avatar" size="large" class="user-avatar" />
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <div class="info-label">姓名</div>
            <div class="info-value">{{ form.realname || '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">性别</div>
            <div class="info-value">{{ genderText }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">年龄</div>
            <div class="info-value">{{ form.age != null ? form.age + ' 岁' : '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">手机号</div>
            <div class="info-value">{{ form.phone || '-' }}</div>
          </div>
          <div class="info-item full-width">
            <div class="info-label">邮箱地址</div>
            <div class="info-value">{{ form.email || '-' }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 编辑表单 -->
    <el-card v-else class="edit-form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Edit /></el-icon>
            编辑个人信息
          </h3>
          <p>请完善您的个人信息，以便获得更好的求职体验</p>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px" @change="autoSave" @submit.prevent :rules="rules" ref="profileForm" class="edit-form">
        <el-form-item label="头像">
          <div class="avatar-upload-section">
            <el-upload
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar :src="avatarPreview||form.avatar" size="large" class="upload-avatar" />
              <div class="upload-overlay">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
            <div class="upload-tip">点击头像上传照片，支持 JPG/PNG 格式，最大 2MB</div>
          </div>
        </el-form-item>
        
        <el-form-item label="姓名" prop="realname" required>
          <el-input v-model="form.realname" placeholder="请输入您的真实姓名" />
        </el-form-item>
        
        <el-form-item label="邮箱地址" prop="email" required>
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone" required>
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="0" :max="120" placeholder="请选择年龄" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="onSaveClick" :loading="saveLoading" size="large">
              <el-icon><Check /></el-icon>
              保存信息
            </el-button>
            <el-button @click="cancelEdit" size="large">
              <el-icon><Close /></el-icon>
              取消编辑
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { getInfoByUserId, createInfo, updateInfo } from '../../api/info'
import { uploadFile } from '../../util/upload'
import { User, Edit, Check, Close, Camera } from '@element-plus/icons-vue'

const user_id = localStorage.getItem('userId')
const form = reactive({
  id: null,
  userId: user_id,
  realname: '',
  avatar: '',
  age: null,
  gender: 0,
  status: 1,
  phone: '',
  email: ''
})
const showFillAlert = ref(false)
const editing = ref(false)
const original = ref({})
const profileForm = ref(null)
const saveLoading = ref(false)

const genderText = computed(() => {
  if (form.gender === 1) return '男'
  if (form.gender === 2) return '女'
  return '未知'
})

const rules = {
  realname: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const avatarFile = ref(null)
const avatarPreview = ref('')

async function fetchProfile() {
  try {
    const { data } = await getInfoByUserId(user_id)
    if (data && data.id) {
      Object.assign(form, data)
      original.value = { ...data }
      showFillAlert.value = false
      editing.value = false
    } else {
      showFillAlert.value = true
      editing.value = true
    }
  } catch (e) {
    showFillAlert.value = true
    editing.value = true
  }
}

let autoSaveTimer = null
function autoSave() {
  if (!editing.value) return
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(async () => {
    try {
      form.userId = user_id // 确保userId始终有值
      if (form.id) {
        await updateInfo(form.id, form)
      } else {
        await createInfo(form)
      }
      // 自动保存成功不做任何提示，也不退出编辑状态
      if (form.realname && form.email && form.phone) {
        showFillAlert.value = false
      }
      // 不设置 editing.value = false
      original.value = { ...form }
    } catch (e) {
      ElMessage.error('自动保存失败')
    }
  }, 5000)
}

async function saveProfile() {
  try {
    saveLoading.value = true

    // 如果头像文件存在，先上传头像
    if(avatarFile.value) {
      const formData = new FormData()
      formData.append('file', avatarFile.value)
      const url = await uploadFile(formData)
      if(url){
        form.avatar = url.data
      } else {
        ElMessage.error('头像上传失败')
        return
      }
    }

    //保存个人信息
    form.userId = user_id // 确保userId始终有值
    if (form.id) {
      await updateInfo(form.id, form)
    } else {
      await createInfo(form)
    }
    ElMessage.success('保存成功')
    // 判断信息是否完善，完善后关闭提醒
    if (form.realname && form.email && form.phone) {
      showFillAlert.value = false
    }
    editing.value = false
    original.value = { ...form }
  } catch (e) {
    console.log(e)
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

function cancelEdit() {
  Object.assign(form, original.value)
  editing.value = false
}

async function onSaveClick() {
  await nextTick()
  profileForm.value.validate(async (valid) => {
    if (valid) {
      await saveProfile()
    }
  })
}

function handleAvatarChange(file) {
  avatarFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

function beforeAvatarUpload(file) {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElMessage.error('仅支持JPG/PNG格式!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
  }
  return isJPG && isLt2M
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: #f8fafe;
  min-height: 100vh;
}

.top-alert {
  margin-bottom: 24px;
  border-radius: 12px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px 32px;
  color: #fff;
  text-align: center;
  margin-bottom: 24px;
}

.header-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.info-card,
.edit-form-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header p {
  font-size: 14px;
  color: #666;
  margin: 8px 0 0 0;
}

.edit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  border-radius: 8px;
}

.edit-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.info-display {
  padding: 8px 0;
}

.avatar-section {
  text-align: center;
  margin-bottom: 32px;
}

.user-avatar {
  border: 3px solid #e8f2ff;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.info-value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.edit-form-card {
  background: #fff;
}

.edit-form {
  margin-top: 24px;
}

.edit-form .el-form-item {
  margin-bottom: 24px;
}

.edit-form .el-form-item__label {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.edit-form .el-input__inner,
.edit-form .el-input-number,
.edit-form .el-radio-group {
  border-radius: 8px;
  border: 1px solid #e0e6ed;
  transition: all 0.3s ease;
}

.edit-form .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
}

.upload-avatar {
  border: 3px dashed #d9d9d9;
  transition: border-color 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.avatar-uploader:hover .upload-avatar {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 20px;
}

.avatar-uploader:hover .upload-overlay {
  opacity: 1;
}

.upload-tip {
  color: #666;
  font-size: 13px;
  margin-top: 8px;
  text-align: center;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
}

.form-actions .el-button {
  border-radius: 8px;
  font-weight: 600;
  padding: 12px 24px;
  font-size: 16px;
}

.form-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.form-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.form-actions .el-button:not(.el-button--primary) {
  color: #666;
  border-color: #ddd;
}

.form-actions .el-button:not(.el-button--primary):hover {
  color: #333;
  border-color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .page-header {
    padding: 32px 24px;
  }

  .header-content h1 {
    font-size: 24px;
    flex-direction: column;
    gap: 8px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .form-actions {
    flex-direction: column;
    align-items: center;
  }

  .form-actions .el-button {
    width: 100%;
    max-width: 300px;
  }
}
</style>
