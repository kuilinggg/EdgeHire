<template>
    <div class="register-container">
        <div class="register-box">
            <div class="register-header">
                <h2>EdgeHire</h2>
                <p class="subtitle">创建您的EdgeHire账号</p>
            </div>
            <el-tabs v-model="role" class="role-tabs" type="card">
                <el-tab-pane label="求职者" name="jobseeker"></el-tab-pane>
                <el-tab-pane label="HR" name="hr"></el-tab-pane>
            </el-tabs>
            <el-form :model="form" :rules="rules" ref="registerForm" class="register-form"
                @keyup.enter.native="onRegister">
                <el-form-item prop="username">
                    <el-input v-model="form.username" placeholder="请输入账号" prefix-icon="User" size="large" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                        size="large" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="Key"
                        size="large" show-password />
                </el-form-item>
                <div class="form-action">
                    <el-button type="primary" @click="onRegister" class="submit-btn" size="large"
                        :loading="loading">注册</el-button>
                    <div class="login-link">
                        <span>已有账号？</span>
                        <el-button type="text" @click="goLogin" class="login-btn">返回登录</el-button>
                    </div>
                </div>
            </el-form>
        </div>
        <div class="register-footer">
            <p>© 2025 EdgeHire. All Rights Reserved.</p>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, watch} from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import { authApi } from '../../api/auth'

const route = useRoute()
const router = useRouter()
const role = ref('jobseeker')
if (route.query.role && ['jobseeker', 'hr'].includes(route.query.role)) {
    role.value = route.query.role
}

const roleMap = { jobseeker: 1, hr: 2 }
const loading = ref(false)
const registerForm = ref(null)
const form = reactive({
    username: '',
    password: '',
    confirmPassword: ''
})

const rules = {
    username: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 20, message: '账号长度应在3到20个字符之间', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value !== form.password) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            }, trigger: 'blur'
        }
    ]
}

const onRegister = async () => {
    if( loading.value) return // 防止重复提交
    if (!form.username || !form.password || !form.confirmPassword) {
        ElMessage.error('请填写完整信息')
        return
    }
    if (form.password !== form.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
    }
    loading.value = true
    try {
        await authApi.register(form.username, form.password, roleMap[role.value])
        ElMessage.success('注册成功，请登录')
        router.push('/login')
    } catch (err) {
        ElMessage.error(err?.response?.data?.message || '注册失败')
    }finally {
        loading.value = false
    }
}

watch(role, () => {
    form.username = ''
    form.password = ''
    form.confirmPassword = ''
    if(registerForm.value) {
        registerForm.value.clearValidate()
    }
})

const goLogin = () => {
    router.push({ path: '/login', query: { role: role.value } })
}
</script>

<style scoped>
.register-container {
    min-height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%);
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 0;
}

.register-box {
    background: rgba(255, 255, 255, 0.98);
    padding: 40px;
    border-radius: 12px;
    box-shadow: 0 12px 50px rgba(0, 0, 0, 0.15);
    width: 420px;
    max-width: 90vw;
}

.register-header {
    text-align: center;
    margin-bottom: 20px;
}

.register-header h2 {
    color: #4f46e5;
    font-size: 2.2rem;
    font-weight: 700;
    margin: 0 0 8px 0;
    letter-spacing: -0.5px;
}

.subtitle {
    color: #6b7280;
    font-size: 0.95rem;
    margin: 0 0 16px 0;
}

.role-tabs {
    margin-bottom: 24px;
}

.role-tabs :deep(.el-tabs__header) {
    margin-bottom: 20px;
}

.role-tabs :deep(.el-tabs__item) {
    font-size: 15px;
    padding: 0 15px;
    height: 36px;
    line-height: 36px;
}

.role-tabs :deep(.el-tabs__item.is-active) {
    color: #4f46e5;
    font-weight: 600;
}

.role-tabs :deep(.el-tabs__active-bar) {
    background-color: #4f46e5;
    height: 3px;
    border-radius: 3px;
}

.register-form :deep(.el-form-item) {
    margin-bottom: 18px;
}

.register-form :deep(.el-input__wrapper) {
    padding: 0 15px;
    box-shadow: 0 0 0 1px #e5e7eb inset;
    border-radius: 8px;
    transition: all 0.2s;
}

.register-form :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #d1d5db inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 2px #4f46e5 inset !important;
}

.form-action {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 28px;
}

.submit-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 8px;
    background: #4f46e5;
    border: none;
    transition: all 0.3s;
}

.submit-btn:hover {
    background: #4338ca;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}

.login-link {
    margin-top: 16px;
    font-size: 14px;
    color: #6b7280;
}

.login-btn {
    color: #4f46e5;
    font-weight: 500;
    padding: 0;
    margin-left: 4px;
    font-size: 14px;
}

.login-btn:hover {
    color: #4338ca;
    text-decoration: underline;
}

.register-footer {
    position: absolute;
    bottom: 20px;
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
}
</style>
