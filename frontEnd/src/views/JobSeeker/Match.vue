<template>
  <div class="profile-container">
    <el-alert v-if="showFillAlert" title="请先完善求职信息" type="warning" show-icon class="top-alert" />
    <div class="header-bar">
      <span class="resume-title">求职信息</span>
    </div>
    <el-card v-if="!editing">
      <div class="profile-view">
        <p><strong>学历：</strong>{{ educationText }}</p>
        <p><strong>学校：</strong>{{ form.school || '-' }}</p>
        <p><strong>理想岗位：</strong>
          <template v-if="favorList.length > 0">
            <el-tag v-for="(item, idx) in favorList" :key="idx" type="info" style="margin-right: 8px;">{{ item }}</el-tag>
          </template>
          <template v-else>-</template>
        </p>
        <p><strong>会员类型：</strong>{{ form.membership === 0 ? '普通会员' : '高级会员' }}</p>
      </div>
    </el-card>
    <el-form v-else :model="form" label-width="80px" :rules="rules" ref="matchForm" class="edit-form">
      <el-form-item label="学历" prop="education" :required="true">
        <el-select v-model="form.education" placeholder="请选择学历">
          <el-option v-for="item in educationOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="学校" prop="school" :required="true">
        <el-input v-model="form.school" placeholder="请输入毕业学校" />
      </el-form-item>
      <el-form-item label="理想岗位" prop="favor" :required="true">
        <div style="display: flex; flex-wrap: wrap; align-items: center; gap: 8px;">
          <!-- 已保存的岗位标签 -->
          <el-tag
            v-for="(item, idx) in favorEditList"
            :key="idx"
            closable
            @close="removeFavor(idx)"
            type="info"
            style="margin-bottom: 4px;"
          >{{ item }}</el-tag>
          
          <!-- 当前编辑中的输入框 -->
          <el-input
            v-if="showInput"
            v-model="favorInput"
            placeholder="请输入理想岗位"
            size="small"
            style="width: 160px;"
            @keyup.enter.native="confirmAdd"
            @blur="confirmAdd"
            ref="favorInputRef"
          />
          
          <!-- 添加按钮 -->
          <el-button 
            v-if="!showInput && favorEditList.length > 0" 
            type="primary" 
            size="small" 
            @click="showAddInput"
            icon="el-icon-plus"
          >
            添加岗位
          </el-button>
          
          <!-- 当没有岗位且不显示输入框时的提示按钮 -->
          <el-button 
            v-if="!showInput && favorEditList.length === 0" 
            type="primary" 
            size="small" 
            @click="showAddInput"
            icon="el-icon-plus"
          >
            添加第一个岗位
          </el-button>
          
          <!-- 确认和取消按钮 -->
          <div v-if="showInput" style="display: flex; gap: 4px;">
            <el-button type="success" size="small" @click="confirmAdd">确认</el-button>
            <el-button size="small" @click="cancelAdd">取消</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="onSaveClick" :loading="saveLoading">保存</el-button>
        <el-button @click="cancelEdit" style="margin-left:8px;">取消</el-button>
      </el-form-item>
    </el-form>
    <div style="display: flex; justify-content: center; margin-top: 18px;">
      <el-button type="primary" class="edit-btn" @click="startEdit" v-if="!editing" style="margin-top: 18px; align-self: flex-end;">编辑</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getSeekerByUserId, createSeeker, updateSeeker } from '../../api/seeker'

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
.profile-container {
  padding: 32px;
  max-width: 900px;
  min-width: 700px;
  margin: 0 auto;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08), 0 1.5px 6px 0 rgba(0,0,0,0.04);
  position: relative;
}
.header-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 20px;
  padding-left: 0;
  min-height: 40px;
}
.el-card {
  margin-bottom: 24px;
}
.profile-view {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px 0 8px 0;
  width: 100%;
}
.profile-view p {
  margin: 10px 0 4px 0;
  font-size: 16px;
  color: #333;
  letter-spacing: 0.5px;
  text-align: left;
  width: 100%;
}
.edit-btn, .el-button[type="success"] {
  min-width: 140px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.13);
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  transition: background 0.3s, box-shadow 0.3s;
}
.edit-btn:hover, .el-button[type="success"]:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}
.header-bar .el-button.create-btn,
.header-bar .el-button.edit-btn {
  min-width: 80px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.13);
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  transition: background 0.3s, box-shadow 0.3s;
}
.header-bar .el-button.create-btn:hover,
.header-bar .el-button.edit-btn:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}
.el-button {
  font-weight: 600;
  letter-spacing: 2px;
  font-size: 16px;
}
.el-button + .el-button {
  margin-left: 14px !important;
}
.top-alert {
  margin-bottom: 22px;
  border-radius: 8px;
}
.resume-title {
  font-size: 24px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
  line-height: 1.2;
}
.edit-form {
  background: #f8fbff;
  padding: 24px 18px 12px 18px;
  box-shadow: 0 1.5px 6px 0 rgba(0,0,0,0.03);
  border-radius: 4px;
  margin-bottom: 24px;
}
.el-form-item {
  margin-bottom: 18px;
}
.el-form-item__label {
  font-weight: 500;
  color: #222;
  font-size: 15px;
}
.el-input, .el-select {
  width: 100%;
}
</style>
