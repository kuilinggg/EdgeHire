<template>
  <div class="api-test">
    <div class="test-header">
      <h2>API连接测试</h2>
      <p>测试后端API连接状态</p>
    </div>

    <div class="test-section">
      <h3>基础连接测试</h3>
      <el-button @click="testConnection" :loading="testing.connection">
        测试后端连接
      </el-button>
      <div v-if="results.connection" class="result">
        <el-alert 
          :title="results.connection.success ? '连接成功' : '连接失败'" 
          :type="results.connection.success ? 'success' : 'error'"
          :description="results.connection.message"
          show-icon
        />
      </div>
    </div>

    <div class="test-section">
      <h3>HR KPI数据测试</h3>
      <el-input v-model="testUserId" placeholder="输入用户ID" style="width: 200px; margin-right: 12px;" />
      <el-button @click="testKpiData" :loading="testing.kpi">
        测试KPI数据
      </el-button>
      <div v-if="results.kpi" class="result">
        <el-alert 
          :title="results.kpi.success ? 'KPI数据获取成功' : 'KPI数据获取失败'" 
          :type="results.kpi.success ? 'success' : 'error'"
          show-icon
        />
        <pre v-if="results.kpi.data" class="json-data">{{ JSON.stringify(results.kpi.data, null, 2) }}</pre>
        <p v-if="results.kpi.error" class="error-message">{{ results.kpi.error }}</p>
      </div>
    </div>

    <div class="test-section">
      <h3>简历推荐测试</h3>
      <el-button @click="testResumeRecommendation" :loading="testing.resume">
        测试简历推荐
      </el-button>
      <div v-if="results.resume" class="result">
        <el-alert 
          :title="results.resume.success ? '简历推荐获取成功' : '简历推荐获取失败'" 
          :type="results.resume.success ? 'success' : 'error'"
          show-icon
        />
        <pre v-if="results.resume.data" class="json-data">{{ JSON.stringify(results.resume.data, null, 2) }}</pre>
        <p v-if="results.resume.error" class="error-message">{{ results.resume.error }}</p>
      </div>
    </div>

    <div class="test-section">
      <h3>模拟数据模式</h3>
      <el-switch 
        v-model="useMockData" 
        active-text="使用模拟数据" 
        inactive-text="使用真实API"
        @change="handleMockDataChange"
      />
      <p class="mock-tip">开启后将使用前端模拟数据，关闭后尝试连接真实后端API</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { hrApi, resumeRecommendationApi } from '../../api/hr.js'

const testUserId = ref('1')
const useMockData = ref(true)

const testing = reactive({
  connection: false,
  kpi: false,
  resume: false
})

const results = reactive({
  connection: null,
  kpi: null,
  resume: null
})

const testConnection = async () => {
  testing.connection = true
  try {
    // 简单的连接测试
    const response = await fetch('http://localhost:8080/api/t_user/1', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      results.connection = {
        success: true,
        message: `后端服务器连接成功 (状态码: ${response.status})`
      }
    } else {
      results.connection = {
        success: false,
        message: `连接失败，状态码: ${response.status}`
      }
    }
  } catch (error) {
    results.connection = {
      success: false,
      message: `连接错误: ${error.message}`
    }
  } finally {
    testing.connection = false
  }
}

const testKpiData = async () => {
  testing.kpi = true
  try {
    const response = await hrApi.getKpiData(testUserId.value)
    results.kpi = {
      success: true,
      data: response.data
    }
  } catch (error) {
    results.kpi = {
      success: false,
      error: error.message,
      details: error.response?.data || error
    }
  } finally {
    testing.kpi = false
  }
}

const testResumeRecommendation = async () => {
  testing.resume = true
  try {
    const response = await resumeRecommendationApi.getRecommendations(testUserId.value, 0, 5)
    results.resume = {
      success: true,
      data: response.data
    }
  } catch (error) {
    results.resume = {
      success: false,
      error: error.message,
      details: error.response?.data || error
    }
  } finally {
    testing.resume = false
  }
}

const handleMockDataChange = (value) => {
  if (value) {
    ElMessage.info('已切换到模拟数据模式')
  } else {
    ElMessage.info('已切换到真实API模式')
  }
}
</script>

<style scoped>
.api-test {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.test-header {
  text-align: center;
  margin-bottom: 32px;
}

.test-header h2 {
  color: #3a36db;
  margin-bottom: 8px;
}

.test-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.test-section h3 {
  color: #333;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.result {
  margin-top: 16px;
}

.json-data {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 12px;
  margin-top: 12px;
  font-size: 12px;
  overflow-x: auto;
}

.error-message {
  color: #f56c6c;
  margin-top: 8px;
  font-size: 14px;
}

.mock-tip {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}
</style>
