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
            <div class="stat-title">HR总数</div>
            <div class="stat-value">{{ HRnum }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">在线人数</div>
            <div class="stat-value">{{onlineUserCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">求职申请数</div>
            <div class="stat-value">{{ resumenum}}</div>
          </el-card>
        </el-col>
        
      </el-row>
    </div>
    <el-row :gutter="20" class="chart-container" style="margin-top: 30px">
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>近12个月月简历投递数</h3>
          <div ref="resumeChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>用户画像(求职者和HR占比)</h3>
          <div ref="userChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="11">
        <el-card class="chart-card">
          <h3>图表</h3>
          <div class="chart-placeholder">图表1</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>用户年龄分布</h3>
           <div ref="ageChartRef" style="width: 100%; height: 300px;"></div>
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
import axios from 'axios';


const userList=ref([])
const HRList=ref([])
const seekerList=ref([])
const resumeList=ref([])
const usernum=ref(0)
const HRnum=ref(33)
const seekernum=ref(667)
const resumenum=ref(0)
const onlineUserCount=ref(100)

const resumeChartRef=ref(null)
const userChartRef = ref(null)
const ageChartRef = ref(null)
const userChartData = ref([
  { name: '求职者', value: 0 },
  { name: 'HR', value: 0 }
])
const ageChartData = ref([
  { name: '本科', value: 120 },
  { name: '硕士', value: 80 },
  { name: '博士', value: 30 }
])

//页面加载
const load = async () => {
  try {
    const res1 = await getUsers()
    userList.value = Array.isArray(res1.data) ? res1.data : [res1.data]
    usernum.value=userList.value.length
    // 统计 role 为 1 的 HR 数量
    seekernum.value = userList.value.filter(user => user.role === 1).length;
    // 统计 role 为 2 的求职者数量
    HRnum.value = userList.value.filter(user => user.role === 2).length;

    const res2 = await getAllResumes()
    resumeList.value = Array.isArray(res2.data) ? res2.data : [res2.data]
    resumenum.value=resumeList.value.length
    userChartData.value = [
      { name: '求职者', value: seekernum.value },
      { name: 'HR', value: HRnum.value }
    ]
    initUserChart();
    initAgeChart();
  } catch (e) {
    ElMessage.error('加载失败')
  }
  for(i=0;i<usernum.value;i++){

  }
}

const {
  initChart: initUserChart,
  updateChart: updateUserChart,
  resizeChart: resizeUserChart,
  disposeChart: disposeUserChart
} = usePieChart({
  el: userChartRef,
  dataRef: userChartData,
  legendPosition: 'right',
  isDonut: true
})
const {
  initChart: initAgeChart,
  updateChart: updateAgeChart,
  resizeChart: resizeAgeChart,
  disposeChart: disposeAgeChart
} = usePieChart({
  el: ageChartRef,
  dataRef: ageChartData,
  legendPosition: 'right',
  isDonut: true
})

const loadBarChart = async () => {
  const { data } = await getAllResumes();

  // 获取当前日期和12个月前的日期
  const now = new Date();
  const twelveMonthsAgo = new Date();
  twelveMonthsAgo.setMonth(now.getMonth() - 11); // 包含当前月所以减11
  
  // 初始化最近12个月的数据结构
  const monthCountMap = {};
  for (let i = 0; i < 12; i++) {
    const date = new Date(twelveMonthsAgo);
    date.setMonth(date.getMonth() + i);
    const yearMonth = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`;
    monthCountMap[yearMonth] = 0;
  }

  // 统计符合条件的简历
  data.forEach(resume => {
    const resumeDate = new Date(resume.createTime);
    if (resumeDate >= twelveMonthsAgo) {
      const yearMonth = `${resumeDate.getFullYear()}-${(resumeDate.getMonth() + 1).toString().padStart(2, '0')}`;
      if (monthCountMap.hasOwnProperty(yearMonth)) {
        monthCountMap[yearMonth]++;
      }
    }
  });

  // 准备图表数据
  const months = Object.keys(monthCountMap);
  const colors = ['#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272', '#FC8452', '#9A60B4', '#ea7ccc'];
  const barData = months.map((month, index) => ({
    value: monthCountMap[month],
    itemStyle: {
      color: colors[index % colors.length]
    }
  }));

  // 格式化月份显示（可选：显示为"2023-01"或"1月"等形式）
  const formattedMonths = months.map(month => {
    const [year, m] = month.split('-');
    return `${year}年${m}月`; // 或 `${parseInt(m)}月`
  });

  const chart = echarts.init(resumeChartRef.value);
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>简历数: {c}'
    },
    xAxis: {
      type: 'category',
      data: formattedMonths,
      axisLabel: {
        rotate: 30 // 如果月份标签太长可以旋转
      }
    },
    yAxis: {
      type: 'value',
      name: '简历数'
    },
    series: [{
      name: '简历数',
      type: 'bar',
      data: barData,
      label: {
        show: true,
        position: 'top'
      }
    }]
  });
};

const fetchOnlineCount = async () => {
  try {
    const response = await axios.get('/onlineCount')
    onlineUserCount.value = response.data
    console.log('当前在线人数:', onlineUserCount.value)
  } catch (error) {
    console.error('获取在线人数失败:', error)
  }
}

onMounted(() => {
  load()
  loadBarChart()
  window.addEventListener('resize', resizeUserChart)
  window.addEventListener('resize', resizeAgeChart)
  fetchOnlineCount()
})

onBeforeUnmount(() => {
  disposeUserChart()
  disposeAgeChart()
  window.removeEventListener('resize', resizeUserChart)
  window.removeEventListener('resize', resizeAgeChart)
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