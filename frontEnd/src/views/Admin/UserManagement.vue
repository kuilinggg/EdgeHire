<template>
  <div class="user-management">
    <h2>用户信息管理</h2>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户...(用户名)" style="width: 300px" />
        <el-button type="primary" @click="loadUsers">搜索</el-button>
        <el-select v-model="filterRole" placeholder="筛选角色" style="width: 150px; margin-left: 10px;" @change="loadUsers">
        <el-option
         v-for="item in roleOptions"
         :key="item.value"
         :label="item.label"
         :value="item.value"
         />
       </el-select>
       <el-button @click="resetFilters" type="warning" plain>重置筛选</el-button>
      </div>

      <el-table :data="paginatedUsers" style="width: 100%">
        <el-table-column prop="id" label="ID" width="180" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色">
        <template #default="scope">
             <span>{{ scope.row.role == '0' ? '管理员' : scope.row.role == '2' ? 'HR' : '求职者' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="380">
          <template #default="scope">
            <el-button 
            v-if="scope.row.role !== 0"
            size="mini" 
            @click="handleEdit(scope.row)"
            type="warning"
            >重置密码</el-button>
            <el-button size="mini" @click="
              infoList=[],detailInfoList=[],
              handleCheck(scope.row.id,scope.row.role)"
              >查看用户</el-button>
            <el-button 
            v-if="scope.row.role !== 0"
            size="mini" 
            type="danger" 
            @click="handleDelete(scope.row)"
            >删除用户</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
      style="margin-top: 20px; text-align: center"
      background
      layout="prev, pager, next"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="users.length"
      @current-change="handlePageChange"
      />

        <el-dialog title="查看用户详细信息" v-model="checkDialogVisible" :width="dialogWidth">
  <div class="dialog-desc-block">
    <el-descriptions :column="1" border class="dialog-desc">
      <el-descriptions-item label="ID">{{ infoList[0]?.id ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="用户ID">{{ infoList[0]?.userId ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="真实姓名">{{ infoList[0]?.realname ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="年龄">{{ infoList[0]?.age ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="性别">
        {{ infoList[0]?.gender == null ? '-' : infoList[0]?.gender == '0' ? '未知' : infoList[0]?.gender == '1' ? '男性' : '女性' }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        {{ infoList[0]?.status == null ? '-' : infoList[0]?.status == '0' ? '冻结' : '正常' }}
      </el-descriptions-item>
      <el-descriptions-item label="电话号码">{{ infoList[0]?.phone ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ infoList[0]?.email ?? '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-descriptions v-if="userrole === 1 && detailInfoList[0]" :column="1" border class="dialog-desc" title="求职者详细信息">
      <el-descriptions-item label="学历">{{ detailInfoList[0]?.education ? educationMap[detailInfoList[0].education] : '未知学历' }}</el-descriptions-item>
      <el-descriptions-item label="毕业院校">{{ detailInfoList[0]?.school ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="理想岗位">
        {{ (() => {
          const val = detailInfoList[0]?.favor;
          if (!val) return '暂无';
          try {
            const parsed = JSON.parse(val);
            if (Array.isArray(parsed)) {
              return parsed.length > 0 ? parsed.join('、') : '暂无';
            }
            return String(parsed);
          } catch {
            return val || '暂无';
          }
        })() }}
      </el-descriptions-item>
      <el-descriptions-item label="会员等级">{{ detailInfoList[0]?.membership == null ? '-' : detailInfoList[0]?.membership == '0' ? '普通会员' : '高级会员' }}</el-descriptions-item>
    </el-descriptions>
    <el-descriptions v-else-if="userrole === 2 && detailInfoList[0]" :column="1" border class="dialog-desc" title="HR详细信息">
      <el-descriptions-item label="所属公司">{{ detailInfoList[0]?.company ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="招聘岗位">{{ detailInfoList[0]?.position ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="资历">{{ detailInfoList[0]?.experience ?? '-' }}</el-descriptions-item>
    </el-descriptions>
  </div>
  <template #footer>
    <el-button @click="checkDialogVisible = false">返回</el-button>
  </template>
</el-dialog>                          

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox,ElNotification } from 'element-plus'
import { getUsers, updateUser, deleteUser } from '../../api/user'
import { getInfoByUserId } from '../../api/info'
import { getSeekerByUserId } from '../../api/seeker'
import { hrApi } from '../../api/hr'
import axios from 'axios'


// 响应式数据
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterRole=ref('')
const checkDialogVisible = ref(false)
const editUser = ref({})
const infoList = ref([])
const detailInfoList=ref([]) 
const userrole = ref(0)
const isMounted = ref(true)

const roleOptions = [
  { value: 1, label: '求职者' },
  { value: 2, label: 'HR' }
]

const educationMap = {
  1: '小学',
  2: '初中',
  3: '高中',
  4: '大专',
  5: '本科',
  6: '硕士',
  7: '博士'
}

// 计算属性
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return users.value.slice(start, end)
})
const dialogWidth = computed(() => {
  if (window.innerWidth < 600) return '100vw';
  if (window.innerWidth < 1000) return '98vw';
  return '900px';
})
// 方法
const isEmptyContent = (arr) => {
  return arr.length === 0 || arr.every(item => 
    item === null || 
    item === undefined ||
    (typeof item === 'object' && Object.keys(item).length === 0)
)};

const loadUsers = async () => {
  try {
    const res = await getUsers()
    //搜索和筛选
    if(isMounted.value) {
      users.value = res.data.filter(user =>
        user.role !== 0 &&
        user.username.includes(searchKeyword.value) &&
        (filterRole.value === '' || user.role === filterRole.value)
      );
    }
  } catch (e) {
    if(isMounted.value) ElMessage.error('加载用户失败')
  }
}

//还原回原状态
const resetFilters = () => {
  searchKeyword.value = '';
  filterRole.value = '';
  loadUsers();
  ElMessage.success('筛选条件已重置')
};

const handleCheck = async (id,role) => {
  try {
    const res = await getInfoByUserId(id)
    infoList.value = Array.isArray(res.data) ? res.data : [res.data]
  } catch (error) {
    console.error(error)
  }

  //detailInfoList处理
  if(role===1)
  {
    try {
      const res1 = await getSeekerByUserId(id)
      detailInfoList.value = Array.isArray(res1.data) ? res1.data : [res1.data]
    } catch (error) {
      if (error.response?.status === 404) {
      console.error('求职者信息不存在'); 
      } else {
      console.error('请求失败:', error);
      }
    }
  }
  if(role===2)
  {
    try {
      const res2 = await hrApi.getHrInfo(id);
      detailInfoList.value = Array.isArray(res2.data) ? res2.data : [res2.data]
    } catch (error) {
      if (error.response?.status === 404) {
      console.error('HR信息不存在'); 
      } else {
      console.error('请求失败:', error);
      }
    }
  }

  if(role!==0&&(isEmptyContent(infoList.value) || isEmptyContent(detailInfoList.value))) {
    ElMessage.error('用户详细信息不完整')
    return;
  }
  if(role===0&&isEmptyContent(infoList.value)){
    ElMessage.error('用户详细信息不完整')
    return;
  }
  
  userrole.value = role
  checkDialogVisible.value = true
}

const handleEdit =async (user) => {
  editUser.value = { ...user }
  try {
    await ElMessageBox.confirm(
      `确定重置用户 ${user.username}的密码吗？`,
      '提示',
      { type: 'warning' }
    )
    editUser.value.password ="$2a$10$O7Yo4qvs5j8EL7NicSHkGewR6InCxFLy06tMJCcaQdqRietabAkzC"
    await updateUser(editUser.value.id, editUser.value)
    ElMessage.success('密码重置成功')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('密码重置失败')
  }
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定删除用户 ${user.username} 吗？`,
      '提示',
      { type: 'warning' }
    )
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
}

// 消息发送
const sendReminder = async (userId, content) => {
  try {
    var res = await axios.post(`/admin/sysMsg/${userId}/${content}`)
  } catch (e) {
    return false
  }
  return true
}

// 主流程
onMounted(async () => {
  await loadUsers()
})

onBeforeUnmount(() => {
  isMounted.value = false
})
</script>
 
<style scoped>
.user-management{
  padding: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
}

.card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  font-size: 16px;
}

.el-table {
  table-layout: fixed; /* 固定表格布局 */
}

.el-dialog {
  width: 80% !important; /* 固定对话框宽度 */
  max-width: 1200px !important;
}

/* 仅弹窗美化，主列表表格样式不变 */
:deep(.el-dialog__header) {
  text-align: center;
  font-size: 20px;
  font-weight: 500;
  color: #333;
  border-bottom: 1.5px solid #f0f0f0;
  padding-bottom: 12px;
  margin-bottom: 18px;
}
:deep(.el-dialog__body) {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(102,126,234,0.08);
  padding: 32px 32px 18px 32px !important;
}
.dialog-desc-block {
  padding: 4px 0 0 0;
}
.dialog-desc {
  margin-bottom: 18px;
  border-radius: 12px;
  background: #fff;
  --el-descriptions-border-color: #f0f0f0;
  --el-descriptions-label-bg-color: #f6f8fc;
  --el-descriptions-label-color: #4f46e5;
  --el-descriptions-content-color: #222;
  --el-descriptions-title-font-size: 17px;
  --el-descriptions-label-font-size: 15px;
  --el-descriptions-content-font-size: 15px;
  --el-descriptions-label-font-weight: 500;
  --el-descriptions-content-font-weight: 500;
}
:deep(.dialog-desc .el-descriptions__label) {
  color: #4f46e5 !important;
  background: #f6f8fc !important;
  font-weight: 500;
  font-size: 15px;
  width: 120px;
}
:deep(.dialog-desc .el-descriptions__content) {
  color: #222 !important;
  font-weight: 500;
  font-size: 15px;
  word-break: break-all;
}
:deep(.dialog-desc .el-descriptions__title) {
  color: #333;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
}
:deep(.el-dialog__footer) {
  text-align: center;
  padding-top: 10px;
}
:deep(.el-dialog__footer .el-button) {
  border-radius: 8px;
  font-size: 16px;
  padding: 8px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  font-weight: 600;
  margin: 0 8px;
}
:deep(.el-dialog__footer .el-button:hover) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}
@media (max-width: 900px) {
  :deep(.el-dialog__body) {
    padding: 10px 2vw 10px 2vw !important;
  }
  .dialog-desc {
    font-size: 13px;
  }
  :deep(.dialog-desc .el-descriptions__label),
  :deep(.dialog-desc .el-descriptions__content) {
    font-size: 13px;
    padding: 8px 4px;
  }
}
</style>