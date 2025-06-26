<template>
  <div class="user-management">
    <h2>用户信息管理</h2>
    <div class="card">
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户..." style="width: 300px" />
        <el-button type="primary" @click="loadUsers">搜索</el-button>
      </div>

      <el-table :data="paginatedUsers" style="width: 100%">
        <el-table-column prop="id" label="ID" width="180" />
        <el-table-column prop="username" label="用户名" />
       <el-table-column prop="password" label="密码" />
        <el-table-column prop="role" label="角色">
        <template #default="scope">
             <span>{{ scope.row.role == '0' ? '管理员' : scope.row.role == '2' ? 'HR' : '求职者' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="mini" @click="handleCheck(scope.row)">查看</el-button>
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
        <template #footer>
          <el-button @click="checkDialogVisible = false">退出</el-button>
        </template>
      </el-dialog>                          

      <el-dialog title="编辑用户" v-model="editDialogVisible">
        <el-form :model="editUser">
          <el-form-item label="用户名：">
            <el-input v-model="editUser.username" />
          </el-form-item>
          <el-form-item label="密码  ：">
            <el-input v-model="editUser.password" />
          </el-form-item>
          <el-form-item label="角色  ：">
            <el-input v-model="editUser.role" />
          </el-form-item>
          <el-form-item label="状态  ：">
            <el-switch v-model="editUser.status" active-text="正常" inactive-text="冻结" />
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

<script>
import { getUsers, updateUser, deleteUser } from '../../api/user';
//import { getInfos, getInfosByUserId, deleteInfo } from '../../api/info';

export default {
  data() {
    return {
      users: [],
      currentPage: 1,
      pageSize: 10,
      searchKeyword: '',
      editDialogVisible: false,
      checkDialogVisible:false,
      editUser: {},
      infoList: []
    };
  },
  computed: {
    paginatedUsers() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.users.slice(start, end);
    }
  },
  methods: {
    async loadUsers() {
      try {
        const res = await getUsers();
        this.users = res.data.filter(user =>
          user.username.includes(this.searchKeyword)
        );
      } catch (e) {
        this.$message.error('加载用户失败');
      }
    },
   async handleCheck(user){
      /* try {
        const res = await getInfosByUserId(user.id);
        this.infoList = Array.isArray(res.data) ? res.data : [res.data]; // 使用 this 访问，兼容性
      } catch (error) {
        this.$message.error('获取用户详情失败');
        console.error(error);
      }*/
      this.checkDialogVisible = true;
   },
    async handleEdit(user) {
      this.editUser = { ...user }; // 克隆对象避免直接修改
      this.editDialogVisible = true;
    },
    async saveEdit() {
      try {
        await updateUser(this.editUser.id, this.editUser);
        this.$message.success('用户更新成功');
        this.editDialogVisible = false;
        this.loadUsers(); // 刷新数据
      } catch (e) {
        this.$message.error('更新失败');
      }
    },
    async handleDelete(user) {
      try {
        await this.$confirm(`确定删除用户 ${user.username} 吗？`, '提示', {
        type: 'warning'
      });
      await deleteUser(user.id);
      this.$message.success('删除成功');
      this.loadUsers();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    },
    handlePageChange(page) {
    this.currentPage = page;
    }
  },
  mounted() {
    this.loadUsers();
  }
};
</script>

 
