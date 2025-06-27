<template>
  <div class="statistics">
    <h2>数据统计分析</h2>
    <div class="cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">用户总数</div>
            <div class="stat-value">{{ usernum }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">求职者数量</div>
            <div class="stat-value">{{ seekernum }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">HR数量</div>
            <div class="stat-value">{{ HRnum }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">求职申请数</div>
            <div class="stat-value">{{ resumenum }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <el-row :gutter="20" class="chart-container" style="margin-top: 30px">
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>图表</h3>
          <div class="chart-placeholder">图表1</div>
        </el-card>
      </el-col>
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>饼图</h3>
          <div class="chart-placeholder">饼图1</div>
        </el-card>
      </el-col>
    </el-row>
    
   
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue';
import { getUsers } from '../../api/user';
import { getAllRusumes } from '../../api/resume';
import { ElMessage } from 'element-plus';

const userList=ref([])
const HRList=ref([])
const seekerList=ref([])
const resumeList=ref([])
const usernum=ref(0)
const HRnum=ref(0)
const seekernum=ref(0)
const resumenum=ref(0)

const load = async () => {
  try {
    const res1 = await getUsers()
    userList.value = Array.isArray(res1.data) ? res1.data : [res1.data]
    usernum.value=userList.value.length
    const res4 = await getAllRusumes()
    resumeList.value = Array.isArray(res4.data) ? res4.data : [res4.data]
    resumenum.value=resumeList.value.length
  } catch (e) {
    ElMessage.error('加载失败')
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