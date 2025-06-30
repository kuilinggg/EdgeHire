<template>
  <div class="teachvip-container">
    <div v-if="loading" class="loading">加载中...</div>
    <template v-else>
      <div v-if="seekerInfo.membership === 0" class="not-vip">
        <el-alert title="请先升级为高级会员后使用本功能" type="warning" show-icon class="vip-alert" />
        <el-button type="warning" size="large" class="go-vip-btn" @click="goToVip">去升级会员</el-button>
      </div>
      <div v-else class="vip-feature">
        <el-alert title="欢迎使用高级会员求职指导服务" type="success" show-icon class="vip-alert" />
        <div class="feature-content">
          <h2>专属求职指导</h2>
          <ul>
            <li>一对一职业规划建议</li>
            <li>AI智能简历优化</li>
            <li>专属顾问答疑</li>
            <li>更多功能持续开放...</li>
          </ul>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSeekerByUserId } from '../../api/seeker'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'

const seekerInfo = ref({ membership: 0 })
const loading = ref(true)
const authStore = useAuthStore()
const router = useRouter()

onMounted(async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) {
    loading.value = false
    return
  }
  try {
    const { data } = await getSeekerByUserId(userId)
    seekerInfo.value = data || { membership: 0 }
  } catch {
    seekerInfo.value = { membership: 0 }
  } finally {
    loading.value = false
  }
})

function goToVip() {
  router.push('/jobseeker/vip')
}
</script>

<style scoped>
.teachvip-container {
  max-width: 600px;
  margin: 48px auto 0 auto;
  padding: 32px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08), 0 1.5px 6px 0 rgba(0,0,0,0.04);
  min-height: 320px;
}
.loading {
  text-align: center;
  font-size: 18px;
  color: #888;
  margin: 40px 0;
}
.not-vip {
  text-align: center;
  margin-top: 32px;
}
.vip-alert {
  margin-bottom: 24px;
}
.go-vip-btn {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 600;
  padding: 10px 32px;
}
.vip-feature {
  text-align: left;
}
.feature-content {
  margin-top: 24px;
}
.feature-content h2 {
  font-size: 22px;
  color: #409EFF;
  font-weight: 700;
  margin-bottom: 16px;
}
.feature-content ul {
  font-size: 17px;
  color: #333;
  line-height: 2.1;
  padding-left: 20px;
}
</style>
