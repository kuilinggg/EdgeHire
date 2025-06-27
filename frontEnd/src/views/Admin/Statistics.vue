<template>
  <div class="statistics">
    <h2>统计分析</h2>
    <div class="cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">用户总数</div>
            <div class="stat-value">1234</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">求职者总数</div>
            <div class="stat-value">1,234</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">HR数量</div>
            <div class="stat-value">56</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">求职申请</div>
            <div class="stat-value">789</div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <div class="chart-container">
      <div class="chart-card">
        <h3>图表</h3>
        <div class="chart-placeholder">图表1</div>
      </div>
      <div class="chart-card">
        <h3>饼图</h3>
        <div class="chart-placeholder">饼图1</div>
      </div>
    </div>
    
    <div class="table-card">
      <h3>活跃用户TOP10</h3>
      <el-table :data="activeUsers" style="width: 100%">
        <el-table-column prop="rank" label="排名" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="loginCount" label="登录次数"></el-table-column>
        <el-table-column prop="lastLogin" label="最后登录时间"></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue';
import { getUsers } from '../../api/user';

const userList=ref([])
const HRList=ref([])
const seekerList=ref([])

const load = async () => {
  try {
    const res = await getUsers()
    userList.value = res.data.filter(user => 
      user.username.includes(searchKeyword.value)
    
    )
  } catch (e) {
    ElMessage.error('加载用户失败')
  }
}
onMounted(() => {
  load()
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.cards {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-title {
  color: #666;
  font-size: 14px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0;
}

.stat-change {
  color: #67c23a;
  font-size: 12px;
}

.chart-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.chart-placeholder {
  height: 300px;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-top: 10px;
}

.table-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>