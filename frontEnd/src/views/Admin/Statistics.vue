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
            <div class="stat-title">HR数量</div>
            <div class="stat-value">{{ HRnum }}</div>
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
            <div class="stat-title">求职申请数</div>
            <div class="stat-value">{{ resumenum }}</div>
          </el-card>
        </el-col>
        
      </el-row>
    </div>
    <el-row :gutter="20" class="chart-container" style="margin-top: 30px">
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>年简历投递数</h3>
          <div ref="resumeChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>用户画像(求职者和HR占比)</h3>
          <div ref="userChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>用户年龄分布</h3>
          <div class="chart-placeholder">图表1</div>
        </el-card>
      </el-col>
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>示例图表</h3>
           <div ref="chartRef2" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
  </div>
</template>

<script setup>
import { ref,onMounted,onBeforeUnmount } from 'vue';
import { getUsers } from '../../api/user';
import { getInfoByUserId } from '../../api/info';
import { getAllResumes } from '../../api/resume';
import { ElMessage } from 'element-plus';
import { usePieChart } from '../../util/usePieChart';
import * as echarts from 'echarts';

const userList=ref([])
const HRList=ref([])
const seekerList=ref([])
const resumeList=ref([])
const usernum=ref(0)
const HRnum=ref(33)
const seekernum=ref(667)
const resumenum=ref(0)

const userChartRef = ref(null)
const chartRef2 = ref(null)
const resumeChartRef=ref(null)
const userChartData = ref([
  { name: '求职者', value: 0 },
  { name: 'HR', value: 0 }
])
const chartData2 = ref([
  { name: '本科', value: 120 },
  { name: '硕士', value: 80 },
  { name: '博士', value: 30 }
])

const load = async () => {
  try {
    const res1 = await getUsers()
    userList.value = Array.isArray(res1.data) ? res1.data : [res1.data]
    usernum.value=userList.value.length
    //seeker和HR处理
    const res4 = await getAllResumes()
    resumeList.value = Array.isArray(res4.data) ? res4.data : [res4.data]
    resumenum.value=resumeList.value.length
    userChartData.value = [
      { name: '求职者', value: seekernum.value },
      { name: 'HR', value: HRnum.value }
    ]
    initChart();
    initChart2();
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const {
  initChart,
  updateChart,
  resizeChart,
  disposeChart
} = usePieChart({
  el: userChartRef,
  dataRef: userChartData,
  legendPosition: 'right',
  isDonut: true
})
const {
  initChart: initChart2,
  updateChart: updateChart2,
  resizeChart: resizeChart2,
  disposeChart: disposeChart2
} = usePieChart({
  el: chartRef2,
  dataRef: chartData2,
  legendPosition: 'right',
  isDonut: true
})

const loadChart = async () => {
  const { data } = await getAllResumes();

  // 按年份分组统计
  const yearCountMap = {};
  data.forEach(resume => {
    const year = new Date(resume.createTime).getFullYear();
    yearCountMap[year] = (yearCountMap[year] || 0) + 1;
  });

  const years = Object.keys(yearCountMap).sort();
  const colors = ['#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272', '#FC8452', '#9A60B4', '#ea7ccc'];
  const barData = years.map((year, index) => ({
  value: yearCountMap[year],
  itemStyle: {
    color: colors[index % colors.length] // 循环使用颜色
  }
}));

  const chart = echarts.init(resumeChartRef.value);
  chart.setOption({
    tooltip: {},
    xAxis: {
      type: 'category',
      data: years
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '简历数',
      type: 'bar',
      data: barData
    }]
  });
};

onMounted(() => {
  load()
  loadChart()
  window.addEventListener('resize', resizeChart)
  window.addEventListener('resize', resizeChart2)
})

onBeforeUnmount(() => {
  disposeChart()
  disposeChart2()
  window.removeEventListener('resize', resizeChart)
  window.removeEventListener('resize', resizeChart2)
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