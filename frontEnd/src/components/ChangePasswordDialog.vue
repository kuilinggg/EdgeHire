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
        <el-form-item label="确认新密码" prop="confirmPassword">
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
watch(() => props.visible, v => visible.value = v)
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