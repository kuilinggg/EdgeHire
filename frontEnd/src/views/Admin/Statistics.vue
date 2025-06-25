<template>
  <div class="statistics">
    <h2>统计分析</h2>
    <div class="cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">用户总数</div>
            <div class="stat-value">1,234</div>
            <div class="stat-change">↑ 12% 较上月</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">HR数量</div>
            <div class="stat-value">56</div>
            <div class="stat-change">↑ 5% 较上月</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">求职申请</div>
            <div class="stat-value">789</div>
            <div class="stat-change">↓ 3% 较上月</div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <div class="chart-container">
      <div class="chart-card">
        <h3>用户增长趋势</h3>
        <div class="chart-placeholder">这里放置用户增长图表</div>
      </div>
      <div class="chart-card">
        <h3>求职申请状态分布</h3>
        <div class="chart-placeholder">这里放置求职申请状态饼图</div>
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

<script>
export default {
  data() {
    return {
      activeUsers: [
        { rank: 1, username: 'user1', loginCount: 42, lastLogin: '2023-05-03 14:30' },
        { rank: 2, username: 'hr1', loginCount: 38, lastLogin: '2023-05-03 09:15' },
        { rank: 3, username: 'user2', loginCount: 35, lastLogin: '2023-05-02 18:20' },
        { rank: 4, username: 'admin', loginCount: 30, lastLogin: '2023-05-03 10:00' },
        { rank: 5, username: 'user3', loginCount: 28, lastLogin: '2023-05-01 16:45' },
        { rank: 6, username: 'hr2', loginCount: 25, lastLogin: '2023-04-30 11:30' },
        { rank: 7, username: 'user4', loginCount: 22, lastLogin: '2023-04-29 13:20' },
        { rank: 8, username: 'user5', loginCount: 20, lastLogin: '2023-04-28 15:10' },
        { rank: 9, username: 'user6', loginCount: 18, lastLogin: '2023-04-27 09:30' },
        { rank: 10, username: 'user7', loginCount: 15, lastLogin: '2023-04-26 17:40' }
      ]
    }
  }
}
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