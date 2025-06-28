<template>
  <div class="profile-container">
    <el-alert v-if="showFillAlert" title="请先完善求职信息" type="warning" show-icon class="top-alert" />
    <div class="header-bar">
      <span class="resume-title">求职信息</span>
      <el-button type="primary" class="create-btn" @click="onCreateClick" v-if="!editing" style="margin-left: 24px;">新建</el-button>
      <el-button type="primary" class="edit-btn" @click="editing = true" v-if="!editing" style="margin-left: 12px;">编辑</el-button>
    </div>
    <el-card v-if="!editing">
      <div class="profile-view">
        <p><strong>学历：</strong>{{ educationText }}</p>
        <p><strong>学校：</strong>{{ form.school || '-' }}</p>
        <p><strong>理想岗位：</strong>{{ form.favor || '-' }}</p>
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
        <el-input v-model="form.favor" placeholder="请输入理想岗位" />
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="onSaveClick" :loading="saveLoading">保存</el-button>
        <el-button @click="cancelEdit" style="margin-left:8px;">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 求职者信息列表+选取卡片 -->
    <el-card style="margin-top: 32px;">
      <template #header>
        <div class="header-bar">
          <span class="resume-title">全部求职信息</span>
        </div>
      </template>
      <el-row :gutter="32" class="seeker-main-row-short">
        <template v-if="seekerList.length > 0">
          <el-col :span="5" class="seeker-list-col-scroll">
            <el-menu
              :default-active="selectedSeekerIndex"
              @select="handleSeekerSelect"
              class="resume-list-menu"
              style="height: 100%"
            >
              <el-menu-item
                v-for="(item, idx) in seekerList"
                :key="item.id"
                :index="String(idx)"
                class="resume-menu-item"
              >
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;"><Document /></el-icon>
                  <span>{{ item.favor || '未填写岗位' }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="19">
            <el-card v-if="selectedSeeker" class="seeker-single-card">
              <div><strong>学历：</strong>{{ educationOptions.find(e => e.value === selectedSeeker.education)?.label || '-' }}</div>
              <div><strong>学校：</strong>{{ selectedSeeker.school || '-' }}</div>
              <div><strong>理想岗位：</strong>{{ selectedSeeker.favor || '-' }}</div>
              <div><strong>会员类型：</strong>{{ selectedSeeker.membership === 0 ? '普通会员' : '高级会员' }}</div>
            </el-card>
            <el-empty v-else description="请选择左侧求职者" />
          </el-col>
        </template>
        <template v-else>
          <el-col :span="24">
            <el-empty description="暂无求职者数据" />
          </el-col>
        </template>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllSeekers,getSeekerByUserId, getSeekerById,createSeeker,updateSeeker } from '../../api/seeker'
import { Document } from '@element-plus/icons-vue'

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
const seekerList = ref([])
const selectedSeekerIndex = ref('0')
const selectedSeeker = ref(null)

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
  favor: [ { required: true, message: '请输入理想岗位', trigger: 'blur' } ]
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
    }
  } catch (e) {
    showFillAlert.value = true
    editing.value = true
  }
}

const fetchSeekerList = async () => {
  try {
    const res = await getAllSeekers()
    seekerList.value = res.data || []
    if (seekerList.value.length > 0) {
      selectedSeeker.value = seekerList.value[0]
      selectedSeekerIndex.value = '0'
    } else {
      selectedSeeker.value = null
      selectedSeekerIndex.value = '0'
    }
  } catch {
    seekerList.value = []
    selectedSeeker.value = null
    selectedSeekerIndex.value = '0'
  }
}

const handleSeekerSelect = (idx) => {
  selectedSeekerIndex.value = idx
  selectedSeeker.value = seekerList.value[Number(idx)]
}

function cancelEdit() {
  Object.assign(form, original.value)
  editing.value = false
}

async function onSaveClick() {
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
        original.value = { ...form }
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }
  })
}
/*-----------------------此处新建未完成--------------------------------------------------*/
function onCreateClick() {
  // 先请求后端创建一条空的求职信息，再进入编辑
  createSeeker({
    userId: user_id,
    education: '',
    school: '',
    favor: '',
    membership: 0
  }).then(res => {
    if (res.data && res.data.id) {
      Object.assign(form, res.data)
      original.value = { ...res.data }
      editing.value = true
      showFillAlert.value = false
      ElMessage.success('新建成功，请完善信息后保存')
    } else {
      ElMessage.error('新建失败')
    }
  }).catch(() => {
    ElMessage.error('新建失败')
  })
}

onMounted(() => {
  fetchSeekerInfo()
  fetchSeekerList()
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
.seeker-main-row-short {
  min-height: unset;
  height: 220px;
  align-items: flex-start;
}
.seeker-list-col-scroll {
  min-width: 180px;
  max-width: 260px;
  height: 200px;
  overflow-y: auto;
}
.seeker-single-card {
  min-height: 180px;
  max-width: 400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.create-btn {
  margin-left: 24px;
}
</style>
