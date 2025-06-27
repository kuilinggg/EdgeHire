<template>
  <div class="profile-container">
    <el-alert v-if="showFillAlert" title="请先完善个人信息" type="warning" show-icon class="top-alert" />
    <h2>个人信息</h2>
    <el-card v-if="!editing">
      <div class="profile-view">
        <el-avatar :src="form.avatar" size="large" style="margin-bottom:16px;" />
        <p><strong>姓名：</strong>{{ form.realname || '-' }}</p>
        <p><strong>邮箱：</strong>{{ form.email || '-' }}</p>
        <p><strong>手机号：</strong>{{ form.phone || '-' }}</p>
        <p><strong>年龄：</strong>{{ form.age != null ? form.age : '-' }}</p>
        <p><strong>性别：</strong>{{ genderText }}</p>
      </div>
    </el-card>
    <el-form v-else :model="form" label-width="80px" @change="autoSave" @submit.prevent :rules="rules" ref="profileForm">
      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :http-request="customAvatarUpload"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar :src="form.avatar" size="large" style="cursor:pointer;" />
          <template #tip>
            <div class="el-upload__tip">点击头像上传，仅支持jpg/png，最大2MB</div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="姓名" prop="realname" :required="true">
        <el-input v-model="form.realname" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email" :required="true">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone" :required="true">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="年龄">
        <el-input-number v-model="form.age" :min="0" :max="120" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
          <el-radio :label="0">未知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="onSaveClick" :loading="saveLoading">保存</el-button>
        <el-button @click="cancelEdit" style="margin-left:8px;">取消</el-button>
      </el-form-item>
    </el-form>
    <div v-if="!editing" class="edit-btn-wrapper">
      <el-button type="primary" class="edit-btn" @click="editing = true">编辑</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { getInfoByUserId, createInfo, updateInfo, uploadFile } from '../../api/info'

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

const uploadHeaders = { }
function handleAvatarSuccess(res) {
  // 假设后端返回 { url: 'xxx' }
  if (res && res.url) {
    form.avatar = res.url
    ElMessage.success('头像上传成功')
    // 不再自动保存，等待用户点击保存按钮
  } else {
    ElMessage.error('头像上传失败')
  }
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

function customAvatarUpload(option) {
  // option.file 是上传的文件
  uploadFile(option.file)
    .then(res => {
      if (res.data && res.data.url) {
        handleAvatarSuccess(res.data)
        option.onSuccess(res.data)
      } else {
        option.onError(new Error('上传失败'))
      }
    })
    .catch(() => {
      option.onError(new Error('上传失败'))
    })
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-container {
  padding: 40px 24px 32px 24px;
  max-width: 500px;
  margin: 32px auto 0 auto;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08), 0 1.5px 6px 0 rgba(0,0,0,0.04);
  position: relative;
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
.edit-btn-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
.edit-btn, .el-button[type="success"] {
  min-width: 140px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.13);
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  transition: background 0.3s, box-shadow 0.3s;
}
.edit-btn:hover, .el-button[type="success"]:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}
.el-button {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  font-size: 16px;
}
.el-button + .el-button {
  margin-left: 14px !important;
}
.top-alert {
  margin-bottom: 22px;
  border-radius: 8px;
}
.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-uploader .el-avatar {
  border: 2.5px dashed #d9d9d9;
  transition: border-color 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
  margin-bottom: 8px;
  background: #f4f8ff;
}
.avatar-uploader:hover .el-avatar {
  border-color: #409EFF;
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}
.el-upload__tip {
  color: #909399;
  font-size: 13px;
  margin-top: 2px;
}
.el-form {
  background: #f8fbff;
  border-radius: 12px;
  padding: 24px 18px 12px 18px;
  box-shadow: 0 1.5px 6px 0 rgba(0,0,0,0.03);
}
.el-form-item {
  margin-bottom: 18px;
}
.el-form-item__label {
  font-weight: 500;
  color: #222;
  font-size: 15px;
}
.el-input, .el-input-number, .el-radio-group {
  width: 100%;
}
.el-button[type="success"] {
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  color: #fff;
  font-weight: 500;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
}
.el-button[type="success"]:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
}
.el-button {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  font-size: 16px;
}
</style>
