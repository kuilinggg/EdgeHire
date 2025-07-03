<template>
  <div class="match-container">
    <el-alert v-if="showFillAlert" title="请先完善求职信息" type="warning" show-icon class="top-alert" />
    
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><User /></el-icon>
          求职信息管理
        </h1>
        <p class="page-subtitle">完善您的求职信息，提升匹配成功率</p>
      </div>
    </div>

    <!-- 信息展示卡片 -->
    <el-card v-if="!editing" class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Document /></el-icon>
            当前求职信息
          </h3>
          <el-button type="primary" class="edit-btn" @click="startEdit">
            <el-icon><Edit /></el-icon>
            编辑信息
          </el-button>
        </div>
      </template>
      
      <div class="info-display">
        <div class="info-grid">
          <div class="info-item">
            <div class="info-label">学历水平</div>
            <div class="info-value">{{ educationText }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">毕业院校</div>
            <div class="info-value">{{ form.school || '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">会员类型</div>
            <div class="info-value">
              <el-tag :type="form.membership === 0 ? 'info' : 'success'">
                {{ form.membership === 0 ? '普通会员' : '高级会员' }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="info-item full-width">
          <div class="info-label">理想岗位</div>
          <div class="info-value">
            <template v-if="favorList.length > 0">
              <div class="skills-display">
                <el-tag 
                  v-for="(item, idx) in favorList" 
                  :key="idx" 
                  type="primary" 
                  effect="light"
                  class="skill-tag"
                >
                  {{ item }}
                </el-tag>
              </div>
            </template>
            <template v-else>
              <span class="empty-text">暂未设置理想岗位</span>
            </template>
          </div>
        </div>
      </div>
    </el-card>
    <!-- 编辑表单 -->
    <el-card v-else class="edit-form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Edit /></el-icon>
            编辑求职信息
          </h3>
          <p>请完善您的求职信息，以便获得更精准的职位匹配</p>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px" :rules="rules" ref="matchForm" class="edit-form">
        <el-form-item label="学历水平" prop="education" required>
          <el-select v-model="form.education" placeholder="请选择您的学历" style="width: 100%">
            <el-option v-for="item in educationOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="毕业院校" prop="school" required>
          <el-input v-model="form.school" placeholder="请输入毕业院校名称" />
        </el-form-item>
        
        <el-form-item label="理想岗位" prop="favor" required>
          <div class="favor-input-container">
            <!-- 已添加的岗位标签 -->
            <div v-if="favorEditList.length > 0" class="skills-display">
              <el-tag
                v-for="(item, idx) in favorEditList"
                :key="idx"
                closable
                @close="removeFavor(idx)"
                type="primary"
                effect="light"
                class="skill-tag"
              >
                {{ item }}
              </el-tag>
            </div>
            
            <!-- 输入框和控制按钮 -->
            <div class="input-controls">
              <el-input
                v-if="showInput"
                v-model="favorInput"
                placeholder="请输入理想岗位名称"
                style="width: 200px; margin-right: 8px;"
                @keyup.enter="confirmAdd"
                @blur="confirmAdd"
                ref="favorInputRef"
              />
              
              <el-button 
                v-if="!showInput" 
                type="primary" 
                @click="showAddInput"
                :icon="Plus"
                size="small"
              >
                {{ favorEditList.length === 0 ? '添加岗位' : '添加更多' }}
              </el-button>
              
              <div v-if="showInput" class="button-group">
                <el-button type="success" size="small" @click="confirmAdd" :icon="Check">确认</el-button>
                <el-button size="small" @click="cancelAdd" :icon="Close">取消</el-button>
              </div>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="onSaveClick" :loading="saveLoading" size="large">
              <el-icon><Check /></el-icon>
              保存信息
            </el-button>
            <el-button @click="cancelEdit" size="large">
              <el-icon><Close /></el-icon>
              取消编辑
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getSeekerByUserId, createSeeker, updateSeeker } from '../../api/seeker'
import { User, Document, Edit, Plus, Check, Close } from '@element-plus/icons-vue'

const user_id = localStorage.getItem('userId')
const form = reactive({
  id: null,
  userId: user_id,
  education: '',
  school: '',
  favor: '',
  membership: 0
})
const showFillAlert = ref(false)
const editing = ref(false)
const original = ref({})
const matchForm = ref(null)
const saveLoading = ref(false)

const favorEditList = ref([])
const favorInput = ref('')
const showInput = ref(false)
const favorInputRef = ref(null)
const favorList = computed(() => {
  if (!form.favor) return []
  try {
    const arr = JSON.parse(form.favor)
    return Array.isArray(arr) ? arr : []
  } catch {
    return form.favor ? [form.favor] : []
  }
})

// 显示添加输入框
function showAddInput() {
  showInput.value = true
  favorInput.value = ''
  nextTick(() => {
    if (favorInputRef.value) {
      favorInputRef.value.focus()
    }
  })
}

// 确认添加岗位
function confirmAdd() {
  const val = favorInput.value && favorInput.value.trim()
  if (val && !favorEditList.value.includes(val)) {
    favorEditList.value.push(val)
    favorInput.value = ''
    showInput.value = false
    console.log('添加岗位后的列表:', favorEditList.value)
  } else if (val && favorEditList.value.includes(val)) {
    ElMessage.warning('岗位已存在')
    favorInput.value = ''
  } else {
    // 如果输入为空，直接关闭输入框
    showInput.value = false
    favorInput.value = ''
  }
}

// 取消添加
function cancelAdd() {
  favorInput.value = ''
  showInput.value = false
}

function removeFavor(idx) {
  favorEditList.value.splice(idx, 1)
  
  // 如果删除后没有岗位了，自动显示输入框
  if (favorEditList.value.length === 0) {
    showInput.value = true
    nextTick(() => {
      if (favorInputRef.value) {
        favorInputRef.value.focus()
      }
    })
  }
}

// 开始编辑时同步数据
function startEdit() {
  try {
    favorEditList.value = form.favor ? JSON.parse(form.favor) : []
  } catch {
    favorEditList.value = form.favor ? [form.favor] : []
  }
  favorInput.value = ''
  
  // 新用户（没有岗位）默认显示输入框
  if (favorEditList.value.length === 0) {
    showInput.value = true
    nextTick(() => {
      if (favorInputRef.value) {
        favorInputRef.value.focus()
      }
    })
  } else {
    // 老用户不显示输入框
    showInput.value = false
  }
  
  editing.value = true
}

const educationOptions = [
  { value: 1, label: '小学' },
  { value: 2, label: '初中' },
  { value: 3, label: '高中' },
  { value: 4, label: '大专' },
  { value: 5, label: '本科' },
  { value: 6, label: '硕士' },
  { value: 7, label: '博士' }
]

const educationText = computed(() => {
  const found = educationOptions.find(e => e.value === form.education)
  return found ? found.label : '-'
})
const rules = {
  education: [ { required: true, message: '请选择学历', trigger: 'change' } ],
  school: [ { required: true, message: '请输入毕业学校', trigger: 'blur' } ],
  favor: [ { required: true, message: '请至少添加一个理想岗位', trigger: 'blur' } ]
}

async function fetchSeekerInfo() {
  try {
    const { data } = await getSeekerByUserId(user_id)
    if (data && data.id) {
      Object.assign(form, data)
      original.value = { ...data }
      showFillAlert.value = false
      editing.value = false
    } else {
      showFillAlert.value = true
      editing.value = true
      favorEditList.value = []
      // 新用户默认显示输入框
      showInput.value = true
    }
  } catch (e) {
    showFillAlert.value = true
    editing.value = true
    favorEditList.value = []
    // 新用户默认显示输入框
    showInput.value = true
  }
}

function cancelEdit() {
  Object.assign(form, original.value)
  // 恢复编辑列表
  try {
    favorEditList.value = original.value.favor ? JSON.parse(original.value.favor) : []
  } catch {
    favorEditList.value = original.value.favor ? [original.value.favor] : []
  }
  favorInput.value = ''
  showInput.value = false
  editing.value = false
}

async function onSaveClick() {
  // 如果当前输入框有内容，先确认添加
  if (showInput.value && favorInput.value && favorInput.value.trim()) {
    const val = favorInput.value.trim()
    if (!favorEditList.value.includes(val)) {
      favorEditList.value.push(val)
      favorInput.value = ''
      showInput.value = false
      console.log('保存时自动添加岗位:', val)
    }
  }
  
  // 校验理想岗位不能为空
  if (!favorEditList.value || favorEditList.value.length === 0) {
    ElMessage.error('请至少添加一个理想岗位')
    // 如果没有岗位，显示输入框让用户添加
    if (!showInput.value) {
      showInput.value = true
      nextTick(() => {
        if (favorInputRef.value) {
          favorInputRef.value.focus()
        }
      })
    }
    console.log('favorEditList:', favorEditList.value) // 调试用
    return
  }
  
  // 设置 favor 字段用于表单验证
  form.favor = JSON.stringify(favorEditList.value)
  
  await nextTick()
  matchForm.value.validate(async (valid) => {
    if (valid) {
      try {
        form.userId = user_id
        if (form.id) {
          await updateSeeker(form.id, form)
        } else {
          await createSeeker(form)
        }
        ElMessage.success('保存成功')
        showFillAlert.value = false
        editing.value = false
        showInput.value = false
        original.value = { ...form }
      } catch (e) {
        ElMessage.error('保存失败')
        console.error('保存错误:', e)
      }
    } else {
      console.log('表单验证失败')
    }
  })
}

onMounted(() => {
  fetchSeekerInfo()
})
</script>

<style scoped>
.match-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: #f8fafe;
  min-height: 100vh;
}

.top-alert {
  margin-bottom: 24px;
  border-radius: 12px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px 32px;
  color: #fff;
  text-align: center;
  margin-bottom: 24px;
}

.header-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.info-card,
.edit-form-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  margin: 8px 0 0 0;
}

.edit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  border-radius: 8px;
}

.edit-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.info-display {
  padding: 8px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.info-value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.empty-text {
  color: #999;
  font-style: italic;
}

.skills-display {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.skill-tag {
  margin: 0;
  border-radius: 6px;
}

.edit-form-card {
  background: #fff;
}

.edit-form {
  margin-top: 24px;
}

.edit-form .el-form-item {
  margin-bottom: 24px;
}

.edit-form .el-form-item__label {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.edit-form .el-input__inner,
.edit-form .el-select {
  border-radius: 8px;
  border: 1px solid #e0e6ed;
  transition: all 0.3s ease;
}

.edit-form .el-input__inner:focus,
.edit-form .el-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.favor-input-container {
  width: 100%;
}

.input-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.button-group {
  display: flex;
  gap: 8px;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
}

.form-actions .el-button {
  border-radius: 8px;
  font-weight: 600;
  padding: 12px 24px;
  font-size: 16px;
}

.form-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.form-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.form-actions .el-button:not(.el-button--primary) {
  color: #666;
  border-color: #ddd;
}

.form-actions .el-button:not(.el-button--primary):hover {
  color: #333;
  border-color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .match-container {
    padding: 16px;
  }

  .page-header {
    padding: 32px 24px;
  }

  .header-content h1 {
    font-size: 24px;
    flex-direction: column;
    gap: 8px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .form-actions {
    flex-direction: column;
    align-items: center;
  }

  .form-actions .el-button {
    width: 100%;
    max-width: 300px;
  }

  .input-controls {
    flex-direction: column;
    align-items: flex-start;
  }

  .input-controls .el-input {
    width: 100% !important;
  }
}
</style>
