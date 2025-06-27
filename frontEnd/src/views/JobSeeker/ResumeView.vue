<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <span class="resume-title">查看简历</span>
      </template>
      <el-row :gutter="20">
        <template v-if="resumeList.length > 0">
          <el-col :span="6">
            <el-menu
              :default-active="selectedIndex"
              @select="handleSelect"
              class="resume-list-menu"
              style="height: 100%"
            >
              <el-menu-item
                v-for="(item, idx) in resumeList"
                :key="item.id"
                :index="String(idx)"
              >
                <el-icon style="margin-right: 4px;"><Document /></el-icon>
                简历{{ idx + 1 }}
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="18">
            <el-descriptions v-if="selectedResume" :column="1" border class="resume-detail">
              <el-descriptions-item label="内容">
                <el-input type="textarea" :value="selectedResume.content" autosize readonly class="resume-content" />
              </el-descriptions-item>
              <el-descriptions-item label="头像">
                <el-avatar v-if="selectedResume.avatar" :src="selectedResume.avatar" size="large" />
                <span v-else>无</span>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                <span class="resume-label">{{ selectedResume.createTime }}</span>
              </el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="请选择左侧简历" />
          </el-col>
        </template>
        <template v-else>
          <el-col :span="24">
            <el-empty description="暂无简历数据" />
          </el-col>
        </template>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getResumesByUserId } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { Document } from '@element-plus/icons-vue'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const router = useRouter()

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

onMounted(async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) {
    router.push('/login')
    return
  }
  try {
    const res = await getResumesByUserId(userId)
    if (res.data && res.data.length > 0) {
      resumeList.value = res.data
      selectedResume.value = res.data[0]
      selectedIndex.value = '0'
    }
  } catch (e) {
    resumeList.value = []
    selectedResume.value = null
  }
})
</script>

<style scoped>
.resume-view-container {
  padding: 32px;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  background: #f8f9fa;
}
.resume-title {
  font-size: 22px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
}
.resume-list-menu {
  min-width: 140px;
  border-right: 1px solid #ebeef5;
  height: 100%;
  background: #f4f6fb;
}
.resume-detail {
  font-size: 16px;
}
.resume-label {
  color: #333;
  font-weight: 500;
}
.resume-content {
  font-size: 15px;
  color: #222;
}
</style>
