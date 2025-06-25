<template>
    <div class="login-container">
        <div class="login-box">
            <div class="login-header">
                <h2>EdgeHire</h2>
                <p class="subtitle">欢迎使用EdgeHire求职加油站</p>
            </div>

            <el-tabs v-model="role" class="role-tabs" type="card">
                <el-tab-pane label="求职者" name="jobseeker"></el-tab-pane>
                <el-tab-pane label="HR" name="hr"></el-tab-pane>
                <el-tab-pane label="管理员" name="admin"></el-tab-pane>
            </el-tabs>

            <el-form :model="form" :rules="rules" ref="loginForm" class="login-form">
                <el-form-item prop="username">
                    <el-input v-model="form.username" placeholder="请输入账号" prefix-icon="User" size="large" />
                </el-form-item>

                <el-form-item prop="password">
                    <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                        size="large" show-password />
                </el-form-item>

                <div class="form-action">
                    <el-button type="primary" @click="onLogin" class="submit-btn" size="large"
                        :loading="loading">登录</el-button>
                    <div class="register-link" v-if="role !== 'admin'">
                        <span>没有账号？</span>
                        <el-button type="text" @click="goRegister" class="register-btn">立即注册</el-button>
                    </div>
                </div>
            </el-form>
        </div>

        <div class="login-footer">
            <p>© 2025 EdgeHire. All Rights Reserved.</p>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '../../api/auth'

const router = useRouter()
const role = ref('jobseeker')
const roleMap = { jobseeker: 1, hr: 2, admin: 0 }
const loading = ref(false)
const form = reactive({
    username: '',
    password: '',
})
const rules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onLogin = async () => {
    if (!form.username || !form.password) {
        ElMessage.error('请输入账号和密码')
        return
    }
    try {
        const res = await authApi.login(form.username, form.password)
        if (res.data && res.data.id) {
            // 校验后端返回的role和当前选择的role是否一致
            if (res.data.role !== roleMap[role.value]) {
                ElMessage.error('账号角色与当前选择不一致，请检查！')
                return
            }

            ElMessage.success('登录成功')
            if (res.data.role === 1) {
                router.push('/jobseeker')
            } else if (res.data.role === 2) {
                router.push('/hr')
            } else if (res.data.role === 0) {
                router.push('/admin')
            } else {
                ElMessage.error('用户角色非法')
            }
        } else {
            ElMessage.error('登录失败')
        }
    } catch (err) {
        ElMessage.error(err?.response?.data?.message || '登录失败')
    }
}

const goRegister = () => {
    router.push('/register')
}
</script>

<style scoped>
.login-container {
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

.login-box {
    background: rgba(255, 255, 255, 0.98);
    padding: 40px;
    border-radius: 12px;
    box-shadow: 0 12px 50px rgba(0, 0, 0, 0.15);
    width: 420px;
    max-width: 90vw;
}

.login-header {
    text-align: center;
    margin-bottom: 20px;
}

.login-header h2 {
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

.login-form :deep(.el-form-item) {
    margin-bottom: 22px;
}

.login-form :deep(.el-input__wrapper) {
    padding: 0 15px;
    box-shadow: 0 0 0 1px #e5e7eb inset;
    border-radius: 8px;
    transition: all 0.2s;
}

.login-form :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #d1d5db inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 2px #4f46e5 inset !important;
}

.form-action {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 32px;
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

.register-link {
    margin-top: 16px;
    font-size: 14px;
    color: #6b7280;
}

.register-btn {
    color: #4f46e5;
    font-weight: 500;
    padding: 0;
    margin-left: 4px;
    font-size: 14px;
}

.register-btn:hover {
    color: #4338ca;
    text-decoration: underline;
}

.login-footer {
    position: absolute;
    bottom: 20px;
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
}
</style>
