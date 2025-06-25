<template>
  <div class="job-review">
    <h2>求职信息审查</h2>
    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核" name="pending">
          <el-table :data="pendingJobs" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="title" label="职位名称"></el-table-column>
            <el-table-column prop="company" label="公司"></el-table-column>
            <el-table-column prop="applicant" label="申请人"></el-table-column>
            <el-table-column prop="date" label="申请日期"></el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="success">通过</el-button>
                <el-button type="danger">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已通过" name="approved">
          <el-table :data="approvedJobs" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="title" label="职位名称"></el-table-column>
            <el-table-column prop="company" label="公司"></el-table-column>
            <el-table-column prop="applicant" label="申请人"></el-table-column>
            <el-table-column prop="date" label="申请日期"></el-table-column>
            <el-table-column prop="reviewer" label="审核人"></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已拒绝" name="rejected">
          <el-table :data="rejectedJobs" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="title" label="职位名称"></el-table-column>
            <el-table-column prop="company" label="公司"></el-table-column>
            <el-table-column prop="applicant" label="申请人"></el-table-column>
            <el-table-column prop="date" label="申请日期"></el-table-column>
            <el-table-column prop="reviewer" label="审核人"></el-table-column>
            <el-table-column prop="reason" label="拒绝原因"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 'pending',
      pendingJobs: [
        { id: 1, title: '前端开发工程师', company: 'ABC科技', applicant: '张三', date: '2023-05-01' },
        { id: 2, title: '产品经理', company: 'XYZ公司', applicant: '李四', date: '2023-05-02' },
        { id: 3, title: 'UI设计师', company: '设计之家', applicant: '王五', date: '2023-05-03' }
      ],
      approvedJobs: [
        { id: 4, title: 'Java开发', company: '软件集团', applicant: '赵六', date: '2023-04-28', reviewer: 'admin' },
        { id: 5, title: '测试工程师', company: '质量保证', applicant: '钱七', date: '2023-04-29', reviewer: 'admin' }
      ],
      rejectedJobs: [
        { id: 6, title: '销售代表', company: '营销公司', applicant: '孙八', date: '2023-04-25', reviewer: 'admin', reason: '不符合要求' }
      ]
    }
  }
}
</script>

<style scoped>
.job-review {
  padding: 20px;
}

.card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>