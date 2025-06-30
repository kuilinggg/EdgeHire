<template>
  <el-row justify="center" align="middle" style="min-height: 100vh; background: #fff;">
    <el-col :span="12" :xs="22" :sm="16" :md="12" :lg="8">
      <el-card class="vip-el-card" shadow="hover">
        <div class="vip-header-el">
          <el-icon class="vip-crown"><svg viewBox="0 0 24 24" width="28" height="28"><path fill="#FFD700" d="M2 7l5.5 7 4.5-7 4.5 7L22 7l-2 12H4L2 7z"/></svg></el-icon>
          <span class="vip-title-el">会员中心</span>
        </div>
        <el-divider />
        <div class="vip-status-row-el">
          <span class="vip-status-label">当前会员状态：</span>
          <el-tag :type="seekerInfo.membership === 0 ? 'info' : 'success'" size="large">
            <el-icon v-if="seekerInfo.membership !== 0" style="margin-right:4px;"><svg viewBox="0 0 24 24" width="18" height="18"><circle cx="12" cy="12" r="10" fill="#FFD700"/></svg></el-icon>
            {{ seekerInfo.membership === 0 ? '普通会员' : '高级会员' }}
          </el-tag>
        </div>
        <el-divider content-position="left"><span class="benefits-title-el">会员专属权益</span></el-divider>
        <el-row gutter="0" class="vip-benefits-el">
          <el-col :span="24">
            <el-list class="benefits-list-el">
              <el-list-item class="benefit-item-el">
                <el-icon><svg viewBox="0 0 24 24" width="18" height="18"><circle cx="12" cy="12" r="8" fill="#67C23A"/></svg></el-icon>
                <span class="benefit-text">专属求职指导</span>
              </el-list-item>
              <el-list-item class="benefit-item-el">
                <el-icon><svg viewBox="0 0 24 24" width="18" height="18"><circle cx="12" cy="12" r="8" fill="#409EFF"/></svg></el-icon>
                <span class="benefit-text">AI简历调优</span>
              </el-list-item>
              <el-list-item class="benefit-item-el">
                <el-icon><svg viewBox="0 0 24 24" width="18" height="18"><circle cx="12" cy="12" r="8" fill="#E6A23C"/></svg></el-icon>
                <span class="benefit-text">专属客服支持</span>
              </el-list-item>
              <el-list-item class="benefit-item-el">
                <el-icon><svg viewBox="0 0 24 24" width="18" height="18"><circle cx="12" cy="12" r="8" fill="#F56C6C"/></svg></el-icon>
                <span class="benefit-text">更多功能持续解锁...</span>
              </el-list-item>
            </el-list>
          </el-col>
        </el-row>
        <div v-if="seekerInfo.membership === 0" class="upgrade-btn-area">
          <el-button type="warning" size="large" class="upgrade-btn" @click="showPayDialog = true">升级为高级会员</el-button>
        </div>
        <slot />
      </el-card>
      <!-- 支付弹窗 -->
      <el-dialog v-model="showPayDialog" title="升级为高级会员" width="380px" :close-on-click-modal="false">
        <div class="pay-content">
          <el-icon style="font-size:38px;color:#FFD700;margin-bottom:12px;"><svg viewBox="0 0 24 24" width="38" height="38"><path fill="#FFD700" d="M2 7l5.5 7 4.5-7 4.5 7L22 7l-2 12H4L2 7z"/></svg></el-icon>
          <div class="pay-title">开通高级会员</div>
          <div class="pay-desc">仅需 <span class="pay-price">¥99</span>，享受全部专属权益！</div>
        </div>
        <template #footer>
          <el-button @click="showPayDialog = false">取消</el-button>
          <el-button type="primary" @click="onPayConfirm" :loading="paying">确认支付</el-button>
        </template>
      </el-dialog>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSeekerByUserId, updateSeeker } from '../../api/seeker'
import { useAuthStore } from '../../stores/authStore'
import { ElMessage } from 'element-plus'

const seekerInfo = ref({ membership: 0 })
const loading = ref(true)
const authStore = useAuthStore()
const showPayDialog = ref(false)
const paying = ref(false)

onMounted(async () => {
  await fetchSeeker()
})

async function fetchSeeker() {
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
}

async function onPayConfirm() {
  paying.value = true
  try {
    // 实际项目应调用后端支付接口，支付成功后再升级
    // 这里只做本地模拟升级
    const userId = authStore.userId || authStore.user?.id
    if (!userId) throw new Error('未获取到用户ID')
    await updateSeeker(seekerInfo.value.id, { ...seekerInfo.value, membership: 1 })
    ElMessage.success('支付成功，已升级为高级会员！')
    showPayDialog.value = false
    await fetchSeeker()
  } catch {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.vip-el-card {
  border-radius: 18px;
  box-shadow: 0 6px 32px 0 rgba(64,158,255,0.13), 0 2px 8px 0 rgba(0,0,0,0.04);
  padding: 36px 32px 32px 32px;
  min-height: 420px;
}
.vip-header-el {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.vip-crown {
  margin-right: 10px;
  vertical-align: middle;
}
.vip-title-el {
  font-size: 32px;
  font-weight: 800;
  color: #222;
  letter-spacing: 1.5px;
}
.vip-status-row-el {
  margin: 24px 0 32px 0;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.vip-status-label {
  color: #666;
  font-weight: 600;
  font-size: 18px;
}
.benefits-title-el {
  font-size: 22px;
  font-weight: 700;
  color: #409EFF;
  letter-spacing: 1px;
}
.vip-benefits-el {
  margin-top: 8px;
}
.benefits-list-el {
  width: 100%;
  margin-top: 8px;
}
.benefit-item-el {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  color: #333;
  font-weight: 500;
  line-height: 2.2;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 6px;
}
.benefit-item-el:last-child {
  border-bottom: none;
}
.benefit-text {
  font-size: 18px;
  color: #333;
  margin-left: 8px;
  font-weight: 500;
}
.upgrade-btn-area {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  margin-bottom: 8px;
}
.upgrade-btn {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 8px 32px;
}
.pay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 18px 0 8px 0;
}
.pay-title {
  font-size: 22px;
  font-weight: 700;
  color: #222;
  margin-bottom: 8px;
}
.pay-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}
.pay-price {
  color: #F56C6C;
  font-size: 20px;
  font-weight: bold;
}
</style>
