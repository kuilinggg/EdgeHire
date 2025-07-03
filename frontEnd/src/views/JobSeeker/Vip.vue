<template>
  <div class="vip-container">
    <div v-if="loading" class="loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <template v-else>
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">
            <el-icon class="crown-icon"><svg viewBox="0 0 24 24" width="32" height="32"><path fill="#FFD700" d="M2 7l5.5 7 4.5-7 4.5 7L22 7l-2 12H4L2 7z"/></svg></el-icon>
            会员中心
          </h1>
          <p class="page-subtitle">解锁更多专属功能，提升求职成功率</p>
        </div>
      </div>

      <!-- 会员状态卡片 -->
      <el-card class="status-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h3>
              <el-icon><User /></el-icon>
              会员状态
            </h3>
          </div>
        </template>
        
        <div class="status-content">
          <div class="status-info">
            <div class="current-status">
              <span class="status-label">当前状态：</span>
              <el-tag 
                :type="seekerInfo.membership === 0 ? 'info' : 'success'" 
                size="large"
                class="status-tag"
              >
                <el-icon v-if="seekerInfo.membership !== 0" class="vip-icon">
                  <svg viewBox="0 0 24 24" width="18" height="18">
                    <circle cx="12" cy="12" r="10" fill="#FFD700"/>
                  </svg>
                </el-icon>
                {{ seekerInfo.membership === 0 ? '普通会员' : '高级会员' }}
              </el-tag>
            </div>
            
            <div v-if="seekerInfo.membership === 0" class="upgrade-prompt">
              <p>升级为高级会员，解锁更多专属功能！</p>
            </div>
            <div v-else class="vip-welcome">
              <p>恭喜您！您已是高级会员，享受专属服务</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 会员权益介绍 -->
      <el-card class="benefits-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h3>
              <el-icon><Star /></el-icon>
              会员专属权益
            </h3>
            <p>高级会员享有以下专属服务与功能</p>
          </div>
        </template>
        
        <div class="benefits-content">
          <div class="benefits-grid">
            <div class="benefit-item">
              <div class="benefit-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="benefit-text">
                <h4>专属求职指导</h4>
                <p>资深HR一对一个性化指导服务</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="benefit-text">
                <h4>AI简历调优</h4>
                <p>智能简历分析与优化建议</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <el-icon><Service /></el-icon>
              </div>
              <div class="benefit-text">
                <h4>专属客服支持</h4>
                <p>7x24小时专属客服在线服务</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="benefit-text">
                <h4>数据分析报告</h4>
                <p>个人求职数据深度分析</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <el-icon><Search /></el-icon>
              </div>
              <div class="benefit-text">
                <h4>优先推荐</h4>
                <p>优质岗位优先推荐匹配</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <el-icon><More /></el-icon>
              </div>
              <div class="benefit-text">
                <h4>更多功能</h4>
                <p>持续解锁更多专属功能...</p>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 升级按钮区域 -->
      <el-card v-if="seekerInfo.membership === 0" class="upgrade-card" shadow="never">
        <div class="upgrade-content">
          <div class="upgrade-text">
            <h3>立即升级为高级会员</h3>
            <p>仅需 <span class="price">¥99</span>，即可享受所有专属权益</p>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            class="upgrade-btn"
            @click="showPayDialog = true"
          >
            <el-icon><CreditCard /></el-icon>
            立即升级
          </el-button>
        </div>
      </el-card>

      <!-- 支付弹窗 -->
      <el-dialog 
        v-model="showPayDialog" 
        title="升级为高级会员" 
        width="420px" 
        :close-on-click-modal="false"
        class="pay-dialog"
      >
        <div class="pay-content">
          <div class="pay-header">
            <el-icon class="pay-crown">
              <svg viewBox="0 0 24 24" width="48" height="48">
                <path fill="#FFD700" d="M2 7l5.5 7 4.5-7 4.5 7L22 7l-2 12H4L2 7z"/>
              </svg>
            </el-icon>
            <h3>开通高级会员</h3>
          </div>
          
          <div class="pay-info">
            <div class="price-info">
              <span class="price-label">会员价格：</span>
              <span class="price-value">¥99</span>
            </div>
            <div class="pay-desc">
              <p>✓ 享受全部专属权益</p>
              <p>✓ 专业HR指导服务</p>
              <p>✓ AI简历智能优化</p>
              <p>✓ 优先岗位推荐</p>
            </div>
          </div>
        </div>
        
        <template #footer>
          <div class="pay-footer">
            <el-button @click="showPayDialog = false" size="large">
              取消
            </el-button>
            <el-button 
              type="primary" 
              @click="onPayConfirm" 
              :loading="paying"
              size="large"
              class="pay-confirm-btn"
            >
              <el-icon v-if="!paying"><CreditCard /></el-icon>
              {{ paying ? '支付中...' : '确认支付' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSeekerByUserId, updateSeeker } from '../../api/seeker'
import { useAuthStore } from '../../stores/authStore'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Star, 
  ChatDotRound, 
  Document, 
  Service, 
  TrendCharts, 
  Search, 
  More, 
  CreditCard,
  Loading
} from '@element-plus/icons-vue'

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
.vip-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: #f8fafe;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 80px 0;
  font-size: 18px;
  color: #667eea;
}

.loading-icon {
  font-size: 32px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px 32px;
  color: #fff;
  text-align: center;
}

.header-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.crown-icon {
  font-size: 32px;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

/* 卡片通用样式 */
.status-card,
.benefits-card,
.upgrade-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  background: #fff;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  margin: 0;
}

/* 会员状态卡片 */
.status-content {
  padding: 24px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.current-status {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 18px;
}

.status-label {
  font-weight: 600;
  color: #333;
}

.status-tag {
  font-size: 16px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 20px;
}

.vip-icon {
  margin-right: 6px;
}

.upgrade-prompt {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  border: 1px solid #ffeaa7;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.upgrade-prompt p {
  margin: 0;
  font-size: 16px;
  color: #856404;
  font-weight: 500;
}

.vip-welcome {
  background: linear-gradient(135deg, #d4edda 0%, #a3d9a4 100%);
  border: 1px solid #a3d9a4;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.vip-welcome p {
  margin: 0;
  font-size: 16px;
  color: #155724;
  font-weight: 500;
}

/* 会员权益卡片 */
.benefits-content {
  padding: 24px;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafe;
  border-radius: 12px;
  border: 1px solid #e8f2ff;
  transition: all 0.3s ease;
}

.benefit-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.benefit-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.benefit-text {
  flex: 1;
}

.benefit-text h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
}

.benefit-text p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

/* 升级卡片 */
.upgrade-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  gap: 24px;
}

.upgrade-text h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.upgrade-text p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.price {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.upgrade-btn {
  font-size: 16px;
  font-weight: 600;
  padding: 12px 32px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.upgrade-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

/* 支付弹窗 */
.pay-dialog {
  border-radius: 16px;
}

.pay-content {
  padding: 24px;
}

.pay-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.pay-crown {
  font-size: 48px;
  color: #ffd700;
}

.pay-header h3 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.pay-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.price-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 18px;
}

.price-label {
  color: #666;
  font-weight: 500;
}

.price-value {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.pay-desc {
  background: #f8fafe;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e8f2ff;
}

.pay-desc p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pay-desc p:last-child {
  margin-bottom: 0;
}

.pay-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 16px;
}

.pay-confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  padding: 0 24px;
  transition: all 0.3s ease;
}

.pay-confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .vip-container {
    max-width: 100%;
    padding: 20px;
  }
  
  .benefits-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .vip-container {
    padding: 16px;
    gap: 20px;
  }
  
  .page-header {
    padding: 32px 24px;
  }
  
  .header-content h1 {
    font-size: 28px;
    flex-direction: column;
    gap: 12px;
  }
  
  .crown-icon {
    font-size: 28px;
  }
  
  .benefits-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .benefit-item {
    padding: 16px;
  }
  
  .benefit-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .upgrade-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .upgrade-btn {
    width: 100%;
  }
  
  .current-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .status-tag {
    font-size: 14px;
    padding: 6px 12px;
  }
}

@media (max-width: 480px) {
  .vip-container {
    padding: 12px;
  }
  
  .page-header {
    padding: 24px 16px;
  }
  
  .header-content h1 {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .benefit-text h4 {
    font-size: 15px;
  }
  
  .benefit-text p {
    font-size: 13px;
  }
  
  .upgrade-text h3 {
    font-size: 18px;
  }
  
  .upgrade-text p {
    font-size: 14px;
  }
  
  .price {
    font-size: 20px;
  }
  
  .upgrade-btn {
    font-size: 14px;
    padding: 10px 24px;
  }
}
</style>
