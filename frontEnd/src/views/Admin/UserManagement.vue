<template>
  <div class="user-management">
    <h2>用户信息管理</h2>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户..." style="width: 300px" />
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
            <el-button size="mini" @click="
              infoList=[],detailInfoList=[],
              handleCheck(scope.row.id,scope.row.role)"
              >查看</el-button>
            <el-button 
            v-if="scope.row.role !== 0"
            size="mini" 
            @click="handleEdit(scope.row)"
            type="warning"
            >编辑</el-button>
            <el-button 
            v-if="scope.row.role !== 0"
            size="mini" 
            type="danger" 
            @click="handleDelete(scope.row)"
            >删除</el-button>
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

        <el-dialog title="查看用户详细信息" v-model="checkDialogVisible">
        <el-table :data="infoList" style="width: 100%" >
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="realname" label="真实姓名"/>
        <el-table-column prop="age" label="年龄" /> 
        <el-table-column prop="gender" label="性别">
        <template #default="scope">
             <span>{{ scope.row.gender === undefined || scope.row.gender === null
      ? '  '
      : scope.row.gender == '0'
        ? '未知'
        : scope.row.gender == '1'
          ? '男性'
          : '女性'}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
        <template #default="scope">
             <span>{{ scope.row.status === undefined ||scope.row.status===null
        ? '  '
        : scope.row.status =='0' ? '冻结' : '正常' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话号码"/>
        <el-table-column prop="email" label="邮箱"/>
      </el-table>
      
      <el-table v-if="userrole === 1" :data="detailInfoList" style="width: 100%" >
        <el-table-column prop="education" label="学历" width="225">
       <template #default="scope">
       <span>{{ scope.row?.education ? educationMap[scope.row.education] : '未知学历' }}</span>
      </template>
       </el-table-column> 
        <el-table-column prop="school" label="毕业院校" width="225"/> 
        <el-table-column prop="favor" label="理想岗位" width="225"/>
        <el-table-column prop="membership" label="会员等级" width="225">
       <template #default="scope">
       <span>{{ scope.row?.membership === undefined || scope.row?.membership === null
      ? '  '
      : scope.row?.membership == '0' ? '普通会员' : '高级会员' }}</span>
        </template>
       </el-table-column>
      </el-table>
        <el-table v-else-if="userrole === 2" :data="detailInfoList" style="width: 100%">
                <el-table-column prop="company" label="所属公司" width="300" />
        <el-table-column prop="position" label="招聘岗位" width="300" />
        <el-table-column prop="experience" label="资历" width="300" />
      </el-table>
        <template #footer>
          <el-button @click="checkDialogVisible = false">返回</el-button>
        </template>
      </el-dialog>                          

      <el-dialog title="编辑用户" v-model="editDialogVisible">
        <el-form :model="editUser">
          <el-form-item label="用户名：">
            <el-input v-model="editUser.username" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUser, deleteUser } from '../../api/user'
import { getInfoByUserId } from '../../api/info'
import { getSeekerByUserId } from '../../api/seeker'
import { hrApi } from '../../api/hr'

// 响应式数据
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterRole=ref('')
const editDialogVisible = ref(false)
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
      users.value = res.data.filter(user => {
        const matchKeyword = user.username.includes(searchKeyword.value);
        const matchRole = filterRole.value === '' || user.role === filterRole.value;
        return matchKeyword && matchRole;
      });
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

const handleEdit = (user) => {
  editUser.value = { ...user }
  editDialogVisible.value = true
}

const saveEdit = async () => {
  try {
    await updateUser(editUser.value.id, editUser.value)
    ElMessage.success('用户更新成功')
    editDialogVisible.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error('更新失败')
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

// 生命周期
onMounted(() => {
  loadUsers()
})

onBeforeUnmount(() => {
  isMounted.value = false
})
</script>
 
<style scoped>
.card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>