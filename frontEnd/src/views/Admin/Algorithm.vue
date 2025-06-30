<template>
    <div class="algorithm">
    <h2>算法监控</h2>
    <el-row :gutter="20" class="chart-container">
    <el-col :span="12">
        <el-card class="chart-card">
          <h3>图表1</h3>
          <div ref="" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>图表2</h3>
          <div ref="" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
    </div>
</template>