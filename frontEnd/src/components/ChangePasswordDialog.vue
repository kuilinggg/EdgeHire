<template>
  <el-dialog 
    v-model="visible" 
    title="修改密码" 
    width="470px" 
    :close-on-click-modal="false"
    custom-class="change-password-dialog">
    <div class="password-form-container">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="form.oldPassword" 
            type="password" 
            autocomplete="off"
            placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="form.newPassword" 
            type="password" 
            autocomplete="off"
            placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            autocomplete="off"
            placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取 消</el-button>
        <el-button type="primary" @click="handleChangePwd">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/authStore'
import { authApi } from '../api/auth'

const authStore = useAuthStore()
const userId = authStore.userId

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['update:visible'])

const visible = ref(props.visible)
watch(() => props.visible, v => {
  visible.value = v
  if (v) {
    // 弹窗打开时重置表单
    form.value.oldPassword = ''
    form.value.newPassword = ''
    form.value.confirmPassword = ''
    formRef.value && formRef.value.clearValidate && formRef.value.clearValidate()
  }
})
watch(visible, v => emit('update:visible', v))

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (rule, value) => value === form.value.newPassword, message: '两次输入不一致', trigger: 'blur' }
  ]
}
const formRef = ref()

async function handleChangePwd() {
  await formRef.value.validate()
  try {
    await authApi.changePassword(userId,form.value.oldPassword, form.value.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    visible.value = false
    // 自动登出
    authApi.logout()
  } catch (e) {
    console.log(e)
    ElMessage.error(e?.response?.data?.message || '修改失败')
  }
}
</script>

<style scoped>
.change-password-dialog {
  border-radius: 16px;
  box-shadow: 0 12px 50px rgba(79, 70, 229, 0.12);
  background: #fff;
  padding-top: 8px;
}
.password-form-container {
  padding: 24px 0 0 0;
  background: #fff;
  border-radius: 12px;
}
.el-form {
  max-width: 340px;
  margin: 0 auto;
}
.el-form-item {
  margin-bottom: 22px;
}
.el-form-item__label {
  color: #4f46e5;
  font-weight: 500;
  font-size: 15px;
}
.el-input__wrapper {
  padding: 0 15px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  border-radius: 8px !important;
  background: #f7f8fa;
  transition: box-shadow 0.2s;
}
.el-input__wrapper:hover {
  box-shadow: 0 0 0 1.5px #d1d5db inset;
}
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px #4f46e5 inset !important;
}
.el-input__inner {
  padding: 10px 12px;
  font-size: 15px;
  background: transparent;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 8px 16px 16px 16px;
}
.el-button {
  border-radius: 8px;
  min-width: 80px;
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s, box-shadow 0.2s;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.06);
}
.el-button--primary {
  background: linear-gradient(90deg, #4f46e5 0%, #3b82f6 100%);
  border: none;
}
.el-button--primary:hover {
  background: linear-gradient(90deg, #3b82f6 0%, #4f46e5 100%);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.15);
}
</style>