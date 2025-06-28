<template>
  <div class="hr-profile">
    <!-- 顶部标题栏 -->
    <div class="top-bar">
      <div class="page-title">个人信息</div>
      <div class="page-desc">完善您的个人信息，提升专业形象</div>
    </div>

    <!-- 个人信息表单 -->
    <div class="profile-content">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag v-if="!isProfileComplete" type="warning">信息不完整</el-tag>
            <el-tag v-else type="success">信息完整</el-tag>
          </div>
        </template>

        <el-form 
          :model="userForm" 
          :rules="userRules" 
          ref="userFormRef" 
          label-width="100px"
          class="profile-form"
        >
          <!-- 头像上传 -->
          <el-form-item label="头像">
            <div class="avatar-section">
              <el-avatar :src="avatarPreview || userForm.avatar" :size="80" class="avatar-display">
                <el-icon v-if="!userForm.avatar && !avatarPreview"><User /></el-icon>
              </el-avatar>
              <div class="avatar-actions">
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-change="handleAvatarChange"
                  accept="image/*"
                >
                  <el-button size="small" type="primary">更换头像</el-button>
                </el-upload>
                <p class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
              </div>
            </div>
          </el-form-item>

          <!-- 基本信息 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realname">
                <el-input v-model="userForm.realname" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄" prop="age">
                <el-input-number 
                  v-model="userForm.age" 
                  :min="18" 
                  :max="65" 
                  placeholder="请输入年龄"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="userForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                  <el-radio :label="0">保密</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="userForm.phone" placeholder="请输入手机号码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="邮箱地址" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱地址" />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- HR专业信息 -->
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>HR专业信息</span>
          </div>
        </template>

        <el-form 
          :model="hrForm" 
          :rules="hrRules" 
          ref="hrFormRef" 
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="所属公司" prop="company">
            <el-input v-model="hrForm.company" placeholder="请输入所属公司名称" />
          </el-form-item>

          <el-form-item label="招聘岗位" prop="position">
            <el-input v-model="hrForm.position" placeholder="请输入主要负责的招聘岗位" />
          </el-form-item>

          <el-form-item label="工作资历" prop="experience">
            <el-input 
              v-model="hrForm.experience" 
              type="textarea"
              :rows="3"
              placeholder="请简要描述您的HR工作经验和专业背景"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button @click="resetForms">重置</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving">
          保存信息
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { hrApi } from '../../api/hr.js'
import { getInfoByUserId, createInfo, updateInfo } from '../../api/info.js'
import { useAuthStore } from '../../stores/authStore.js'
import axios from 'axios'

const authStore = useAuthStore()

// 响应式数据
const userFormRef = ref()
const hrFormRef = ref()
const saving = ref(false)
const loading = ref(false)
const avatarFile = ref(null)
const avatarPreview = ref('')

// 从 localStorage 初始化表单数据
const getStoredFormData = () => {
  const storedUserForm = localStorage.getItem('hrUserForm')
  const storedHrForm = localStorage.getItem('hrHrForm')
  
  return {
    userForm: storedUserForm ? JSON.parse(storedUserForm) : {
      realname: '',
      avatar: '',
      age: null,
      gender: 0,
      phone: '',
      email: ''
    },
    hrForm: storedHrForm ? JSON.parse(storedHrForm) : {
      company: '',
      position: '',
      experience: ''
    }
  }
}

// 保存表单数据到 localStorage
const saveFormDataToStorage = () => {
  localStorage.setItem('hrUserForm', JSON.stringify(userForm))
  localStorage.setItem('hrHrForm', JSON.stringify(hrForm))
}

// 清除 localStorage 中的表单数据
const clearStoredFormData = () => {
  localStorage.removeItem('hrUserForm')
  localStorage.removeItem('hrHrForm')
}

// 用户基本信息表单
const userForm = reactive({
  ...getStoredFormData().userForm
})

// HR专业信息表单
const hrForm = reactive({
  ...getStoredFormData().hrForm
})

// 监听表单数据变化，自动保存到 localStorage
watch(userForm, () => {
  saveFormDataToStorage()
}, { deep: true })

watch(hrForm, () => {
  saveFormDataToStorage()
}, { deep: true })

// 表单验证规则
const userRules = {
  realname: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 18, max: 65, message: '年龄必须在 18 到 65 之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const hrRules = {
  company: [
    { required: true, message: '请输入所属公司', trigger: 'blur' }
  ],
  position: [
    { required: true, message: '请输入招聘岗位', trigger: 'blur' }
  ],
  experience: [
    { required: true, message: '请输入工作资历', trigger: 'blur' },
    { min: 10, message: '请详细描述您的工作经验，至少10个字符', trigger: 'blur' }
  ]
}

// 计算属性
const isProfileComplete = computed(() => {
  return userForm.realname && 
         userForm.age && 
         userForm.phone && 
         userForm.email && 
         hrForm.company && 
         hrForm.position && 
         hrForm.experience
})

// 方法
const loadUserInfo = async () => {
  try {
    loading.value = true
    const userId = authStore.userId || '1'
    
    // 加载用户基本信息
    try {
      const userResponse = await getInfoByUserId(userId)
      if (userResponse.data) {
        Object.assign(userForm, userResponse.data)
        // 服务器数据加载成功，清除 localStorage 中的临时数据
        localStorage.removeItem('hrUserForm')
      }
    } catch (error) {
      if (error.response?.status !== 404) {
        console.error('加载用户信息失败:', error)
      }
    }

    // 加载HR专业信息
    try {
      const hrResponse = await hrApi.getHrInfo(userId)
      if (hrResponse.data) {
        Object.assign(hrForm, hrResponse.data)
        // 服务器数据加载成功，清除 localStorage 中的临时数据
        localStorage.removeItem('hrHrForm')
      }
    } catch (error) {
      if (error.response?.status !== 404) {
        console.error('加载HR信息失败:', error)
      }
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarChange = (file) => {
  avatarFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const saveProfile = async () => {
  try {
    // 验证表单
    await Promise.all([
      userFormRef.value.validate(),
      hrFormRef.value.validate()
    ])

    saving.value = true
    const userId = authStore.userId || '1'

    // 如果头像文件存在，先上传头像
    if (avatarFile.value) {
      const formData = new FormData()
      formData.append('file', avatarFile.value)
      const url = await axios.post('http://localhost:8080/api/files/upload', formData)
      if (url && url.data) {
        userForm.avatar = url.data
      } else {
        ElMessage.error('头像上传失败')
        return
      }
    }

    // 保存用户基本信息
    const userInfoData = {
      userId: parseInt(userId),
      status: 1,
      realname: userForm.realname,
      avatar: userForm.avatar,
      age: userForm.age,
      gender: userForm.gender,
      phone: userForm.phone,
      email: userForm.email
    }

    try {
      // 先尝试获取现有记录
      const existingUserInfo = await getInfoByUserId(userId)
      if (existingUserInfo.data) {
        // 如果存在记录，使用记录的 id 进行更新
        await updateInfo(existingUserInfo.data.id, userInfoData)
      } else {
        // 如果不存在记录，创建新记录
        await createInfo(userInfoData)
      }
    } catch (error) {
      if (error.response?.status === 404) {
        // 如果获取记录失败，创建新记录
        await createInfo(userInfoData)
      } else {
        throw error
      }
    }

    // 保存HR专业信息
    const hrInfoData = {
      userId: parseInt(userId),
      ...hrForm
    }

    try {
      await hrApi.updateHrInfoByUserId(userId, hrInfoData)
    } catch (error) {
      if (error.response?.status === 404) {
        // 如果HR信息不存在，则创建新的
        await hrApi.createHrInfo(hrInfoData)
      } else {
        throw error
      }
    }

    ElMessage.success('个人信息保存成功')
    
    // 通知其他组件信息已完善
    authStore.setProfileComplete(true)
    
    // 保存成功后清除 localStorage 中的临时数据
    clearStoredFormData()
    
    // 清除头像文件引用
    avatarFile.value = null
    avatarPreview.value = ''
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const resetForms = () => {
  userFormRef.value?.resetFields()
  hrFormRef.value?.resetFields()
  // 清除 localStorage 中的临时数据
  clearStoredFormData()
  loadUserInfo()
}

// 页面初始化
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.hr-profile {
  box-sizing: border-box;
  padding: 0 24px 16px 16px;
  min-height: calc(100vh - 16px);
  background: #f5f7fa;
  max-width: 1400px;
}

.top-bar {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3a36db;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #666;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.profile-form {
  padding: 0 8px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-display {
  border: 2px solid #f0f0f0;
  background: #fafafa;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.action-buttons .el-button {
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hr-profile {
    padding: 0 12px 12px 12px;
  }

  .top-bar {
    padding: 16px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
