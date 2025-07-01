<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">查看简历</span>
        </div>
      </template>
      <el-row :gutter="32" class="resume-main-row">
        <template v-if="resumeList.length > 0">
          <el-col :span="5" class="resume-list-col">
            <el-menu :default-active="selectedIndex" @select="handleSelect" class="resume-list-menu"
              style="height: 100%">
              <el-menu-item v-for="(item, idx) in resumeList" :key="item.id" :index="String(idx)"
                class="resume-menu-item">
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;">
                    <Document />
                  </el-icon>
                  <span>简历{{ idx + 1 }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="19" class="resume-a4-col">
            <div v-if="selectedResume" class="resume-a4-wrapper">
              <ResumeA4Paper :content="parsedResumeContent" :avatar="selectedResume.avatar" />
              <div class="resume-a4-ops-bar">
                <div class="resume-create-time-a4">
                  创建时间：<span class="resume-label">{{ formatDate(selectedResume.createTime) }}</span>
                </div>
                <div style="margin-top: 32px; text-align: center; display: flex; justify-content: center; gap: 16px;">
                  <el-button type="success" size="large" icon="Document" @click="exportPdf">
                    导出为PDF
                  </el-button>
                  <el-button type="primary" size="large" icon="Edit" class="edit-btn-main" @click="onEditSelected">
                    编辑
                  </el-button>
                  <el-popconfirm title="确定删除该简历？" @confirm="onDeleteSelected">
                    <template #reference>
                      <el-button type="danger" size="large" icon="Delete" class="delete-btn-main">
                        删除当前简历
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </div>
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
import { ref, onMounted, computed } from 'vue'
import { getResumesByUserId, deleteResume } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { Document, Delete, Edit, Calendar, Phone, Message, Location, User, InfoFilled, Reading, Briefcase, Suitcase, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import html2pdf from 'html2pdf.js'
import domtoimage from 'dom-to-image'
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const router = useRouter()

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

const onDeleteSelected = async () => {
  if (!selectedResume.value) return
  const id = selectedResume.value.id
  try {
    await deleteResume(id)
    ElMessage.success('删除成功')
    // 删除成功后刷新简历列表
    await refreshResumeList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const onEditSelected = () => {
  if (!selectedResume.value) return
  router.push({
    path: '/jobseeker/resume-edit',
    query: { id: selectedResume.value.id }
  })
}

// 拉取简历列表并刷新选中项
const refreshResumeList = async () => {
  const userId = authStore.userId || authStore.user?.id
  try {
    const res = await getResumesByUserId(userId)
    if (res.data && res.data.length > 0) {
      resumeList.value = res.data
      selectedResume.value = res.data[0]
      selectedIndex.value = '0'
    } else {
      resumeList.value = []
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch (e) {
    resumeList.value = []
    selectedResume.value = null
    selectedIndex.value = '0'
  }
}

const parsedResumeContent = computed(() => {
  if (!selectedResume.value || !selectedResume.value.content) return {}
  try {
    const obj = JSON.parse(selectedResume.value.content)
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return { 内容: selectedResume.value.content }
  }
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${day}日 ${h}:${min}`
}

async function coverSectionTitlesWithImages() {
  const nodes = Array.from(document.querySelectorAll('.resume-a4-section-title'));
  for (const node of nodes) {
    if (node.querySelector('.section-title-img-cover')) continue;
    const dataUrl = await domtoimage.toPng(node, {bgcolor: null});
    const img = document.createElement('img');
    img.src = dataUrl;
    img.className = 'section-title-img-cover';
    img.style.position = 'absolute';
    img.style.left = '0';
    img.style.top = '0';
    img.style.width = node.offsetWidth + 'px';
    img.style.height = node.offsetHeight + 'px';
    img.style.zIndex = '10';
    img.style.pointerEvents = 'none';
    node.style.position = 'relative';
    node.appendChild(img);
  }
}
function removeSectionTitleImageCovers() {
  document.querySelectorAll('.section-title-img-cover').forEach(img => img.remove());
}

//导出简历为PDF
async function exportPdf() {
  await coverSectionTitlesWithImages();
  setTimeout(() => {
    const element = document.querySelector('.resume-a4-paper')
    if (!element) {
      ElMessage.error('没有可导出的简历内容')
      removeSectionTitleImageCovers();
      return
    }
    html2pdf()
      .from(element)
      .set({
        margin: 0,
        filename: (parsedResumeContent.value.姓名 || '简历') + '.pdf',
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: {
          unit: 'mm', format: 'a4', orientation: 'portrait',
        },
        pagebreak: { mode: ['avoid-all'] },
        pagesplit: false
      })
      .toPdf()
      .get('pdf')
      .then(function(pdf){
        // 删除多余的页面
        while (pdf.internal.getNumberOfPages() > 1) {
          pdf.deletePage(2)
        }
        return pdf
      })
      .save()
      .then(() => {
        ElMessage.success('导出成功')
        removeSectionTitleImageCovers();
      })
      .catch((err) => {
        console.error('导出失败:', err)
        ElMessage.error('导出失败，请稍后重试')
        removeSectionTitleImageCovers();
      })
  }, 100)
}

onMounted(refreshResumeList)
</script>

<style scoped>
.resume-view-container {
  padding: 32px 0;
  min-height: 100vh;
  background: #f4f6fa;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.el-card {
  box-shadow: 0 4px 32px rgba(64, 158, 255, 0.10), 0 1.5px 6px 0 rgba(0, 0, 0, 0.04);
  border-radius: 18px;
  width: 1100px;
  max-width: 98vw;
  margin: 0 auto;
  background: #f9fafb;
}

.resume-main-row {
  min-height: 900px;
  align-items: flex-start;
}

.resume-list-col {
  min-width: 180px;
  max-width: 260px;
  height: 900px;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.resume-list-menu {
  border-radius: 14px;
  background: #f7f8fa;
  box-shadow: 0 1px 4px rgba(99,102,241,0.03);
  padding: 0;
  height: 100%;
  border: none;
  transition: background 0.2s, box-shadow 0.2s;
}

.resume-menu-item {
  border-radius: 0 8px 8px 0;
  margin: 2px 0;
  font-size: 16px;
  transition: background 0.2s, color 0.2s;
  padding: 8px 12px 8px 0;
  color: #333;
  position: relative;
  overflow: visible;
}

.resume-menu-item.is-active,
.resume-menu-item:hover {
  background: linear-gradient(90deg, #e6eaff 0%, #f4f6fb 100%);
  color: #4f46e5;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99,102,241,0.06);
}

.resume-menu-item.is-active::before,
.resume-menu-item:hover::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 4px;
  background: #6366f1;
  z-index: 1;
}

.menu-item-flex {
  padding-left: 16px;
  display: flex;
  align-items: center;
}

.resume-a4-col {
  min-width: 740px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.resume-a4-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 auto;
}

.resume-create-time-a4 {
  margin-top: 16px;
  font-size: 15px;
  color: #888;
  text-align: left;
  width: 794px;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
}

.resume-a4-ops-bar {
  width: 794px;
  max-width: 100%;
  margin: 0 auto 18px auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

@media (max-width: 1200px) {
  .el-card {
    width: 100vw;
    min-width: unset;
    max-width: 100vw;
    border-radius: 0;
  }
  .resume-a4-ops-bar, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 0 2vw;
  }
}

@media (max-width: 900px) {
  .resume-view-container {
    padding: 8px 0;
  }
  .resume-a4-col {
    min-width: unset;
  }
  .resume-a4-ops-bar, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 0;
  }
}
</style>
