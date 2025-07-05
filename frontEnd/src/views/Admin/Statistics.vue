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
            <div class="stat-title">求职申请总数</div>
            <div class="stat-value">{{ resumenum}}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-title">在线人数</div>
            <div class="stat-value">{{ onlineUserCount }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <el-row :gutter="20" class="chart-container">
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>近12个月月简历成功投递数</h3>
          <div ref="resumeChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <h3>用户画像</h3>
            <el-radio-group v-model="activeChart" size="small">
              <el-radio-button label="role">身份分布</el-radio-button>
              <el-radio-button label="gender">性别分布</el-radio-button>
              <el-radio-button label="age">年龄分布</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="userChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>求职者学历分布</h3>
          <div ref="educationChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>核心算法效果图</h3>
          <div ref="" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { getUsers } from '../../api/user';
import { getAllInfos } from '../../api/info';
import { getAllResumes } from '../../api/resume';
import { getAllSeekers } from '../../api/seeker';
import { ElMessage } from 'element-plus';
import { usePieChart } from '../../util/usePieChart';
import * as echarts from 'echarts';
import axios from 'axios';

const userList = ref([]);
const resumeList = ref([]);
const usernum = ref(0);
const HRnum = ref(0);
const seekernum = ref(0);
const resumenum = ref(0);
const onlineUserCount = ref(0);
const activeChart = ref('role'); // 'role', 'gender', 'age'

const resumeChartRef = ref(null);
const userChartRef = ref(null);
const educationChartRef = ref(null);
const rawData = ref([]);
const seekerData = ref([]);

// 用户角色数据
const userRoleData = ref([
  { name: '求职者', value: 0 },
  { name: 'HR', value: 0 }
]);

// 性别数据
const genderData = ref([
  { name: '男', value: 0 },
  { name: '女', value: 0 },
  { name: '未知', value: 0}
]);

// 年龄数据
const ageData = ref([
  { name: '18岁以下', value: 0 },
  { name: '18-35岁', value: 0 },
  { name: '35-45岁', value: 0 },
  { name: '45-55岁', value: 0 },
  { name: '55岁以上', value: 0 }
]);

// 学历数据
const educationData = ref([
  {name: '小学', value: 0},
  {name: '初中', value: 0},
  {name: '高中', value: 0},
  {name: '大专', value: 0},
  {name: '本科', value: 0},
  {name: '硕士', value: 0},
  {name: '博士', value: 0},
  {name: '未知', value: 0}
])

// 初始化图表
const { 
  initChart: initUserChart, 
  updateChart: updateUserChart,
  resizeChart: resizeUserChart,
  disposeChart: disposeUserChart 
} = usePieChart({
  el: userChartRef,
  dataRef: userRoleData,
  legendPosition: 'right',
  isDonut: true
});

const { 
  initChart: initEducationChart, 
  updateChart: updateEducationChart,
  resizeChart: resizeEducationChart,
  disposeChart: disposeEducationChart 
} = usePieChart({
  el: educationChartRef,
  dataRef: educationData,
  legendPosition: 'right',
  isDonut: true
});

// 页面加载
const load = async () => {
  try {
    // 加载用户数据
    const res1 = await getUsers();
    userList.value = Array.isArray(res1.data) ? res1.data : [res1.data];
    usernum.value = userList.value.length;
    seekernum.value = userList.value.filter(user => user.role === 1).length;
    HRnum.value = userList.value.filter(user => user.role === 2).length;
    
    userRoleData.value = [
      { name: '求职者', value: seekernum.value },
      { name: 'HR', value: HRnum.value }
    ];

    // 加载简历数据
    const res2 = await getAllResumes();
    resumeList.value = Array.isArray(res2.data) ? res2.data : [res2.data];
    resumenum.value = resumeList.value.length;

    // 加载用户详细信息
    const res3 = await getAllInfos();
    rawData.value = res3.data;
    
    // 处理性别数据
    const genderStats = { 男: 0, 女: 0,未知: 0};
    rawData.value.forEach(item => {
      if (item.gender === 1) genderStats['男']++;
      else if (item.gender === 2) genderStats['女']++;
      else genderStats['未知']++;
    });
    genderData.value = [
      { name: '男', value: genderStats['男'] },
      { name: '女', value: genderStats['女'] },
      { name: '未知',value:genderStats['未知'] }
    ];

    // 处理年龄数据
    const ageStats = {
      '18岁以下': 0,
      '18-35岁': 0,
      '35-45岁': 0,
      '45-55岁': 0,
      '55岁以上': 0
    };
    rawData.value.forEach(item => {
      const age = item.age || 0;
      if (age < 18) ageStats['18岁以下']++;
      else if (age <= 35) ageStats['18-35岁']++;
      else if (age <= 45) ageStats['35-45岁']++;
      else if (age <= 55) ageStats['45-55岁']++;
      else ageStats['55岁以上']++;
    });
    ageData.value = Object.entries(ageStats).map(([name, value]) => ({ name, value }));

    //加载求职者信息
    const res4=await getAllSeekers();
    seekerData.value=res4.data;

    // 处理学历数据
    const educationStats = {
      '小学': 0,
      '初中': 0,
      '高中': 0,
      '大专': 0,
      '本科': 0,
      '硕士': 0,
      '博士': 0,
      '未知': 0
    };
    seekerData.value.forEach(item => {
      const education = item.education || 0;
      if (education === 1) educationStats['小学']++;
      else if (education === 2) educationStats['初中']++;
      else if (education === 3) educationStats['高中']++;
      else if (education === 4) educationStats['大专']++;
      else if (education === 5) educationStats['本科']++;
      else if (education === 6) educationStats['硕士']++;
      else if (education === 7) educationStats['博士']++;
      else educationStats['未知']++;
    });
    educationData.value = Object.entries(educationStats).map(([name, value]) => ({ name, value }));

    // 初始化图表
    initUserChart();
    initEducationChart();
    updateCurrentChart();
  } catch (e) {
    ElMessage.error('加载失败');
    console.error(e);
  }
};

// 更新当前显示的图表
const updateCurrentChart = () => {
  let currentData;
  switch (activeChart.value) {
    case 'role':
      currentData = userRoleData.value;
      break;
    case 'gender':
      currentData = genderData.value;
      break;
    case 'age':
      currentData = ageData.value;
      break;
  }
  updateUserChart(currentData);
};

// 加载柱状图
const loadBarChart = async () => {
  const { data } = await getAllResumes();

  // 获取当前日期和12个月前的日期
  const now = new Date();
  const twelveMonthsAgo = new Date();
  twelveMonthsAgo.setMonth(now.getMonth() - 11);
  
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

  // 格式化月份显示
  const formattedMonths = months.map(month => {
    const [year, m] = month.split('-');
    return `${year}年${m}月`;
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
        rotate: 30
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

// 获取在线人数
const fetchOnlineCount = async () => {
  try {
    const response = await axios.get('/onlineCount');
    onlineUserCount.value = response.data;
  } catch (error) {
    console.error('获取在线人数失败:', error);
  }
};

// 监听图表切换
watch(activeChart, () => {
  updateCurrentChart();
});

onMounted(async () => {
  await load();
  loadBarChart();
  fetchOnlineCount();
  window.addEventListener('resize', resizeUserChart);
  window.addEventListener('resize', resizeEducationChart);
});

onBeforeUnmount(() => {
  disposeUserChart();
  disposeEducationChart();
  window.removeEventListener('resize', resizeUserChart);
  window.removeEventListener('resize', resizeEducationChart);
});

</script>

<style scoped>
.statistics {
  padding: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
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

.chart-container {
  margin-top: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
</style>