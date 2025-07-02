<template>
  <div class="resume-blue-a4">
    <div class="left-panel">
      <div class="avatar-box">
        <el-avatar :src="avatar" shape="square" class="avatar-img" />
      </div>
      <div class="name-section">
        <div class="resume-title">{{ content.姓名 || '姓名' }}</div>
        <div class="job-intention">{{ content.求职意向 || '求职意向' }}</div>
      </div>
      <div class="block" v-if="content['教育背景']">
        <div class="block-title">教育经历</div>
        <ul class="block-list">
          <li v-if="content['教育背景'].学校"><span class="field-value">{{ content['教育背景'].学校 }}</span></li>
          <li v-if="content['教育背景'].学历"><span class="field-value">{{ content['教育背景'].学历 }}</span></li>
          <li v-if="content['教育背景'].专业"><span class="field-value">{{ content['教育背景'].专业 }}</span></li>
          <li v-if="content['教育背景'].时间"><span class="field-value">{{ content['教育背景'].时间 }}</span></li>
          <li v-if="content['教育背景'].GPA" class="gpa-item"><span class="gpa-label">GPA:</span><span class="field-value">{{ content['教育背景'].GPA }}</span></li>
        </ul>
      </div>
      <div class="block" v-if="content['教育背景'] && content['教育背景'].主修课程 && content['教育背景'].主修课程.length">
        <div class="block-title">主修课程</div>
        <ul class="block-list">
          <li v-for="(course, idx) in content['教育背景'].主修课程" :key="idx">
            <span class="field-value">{{ course }}</span>
          </li>
        </ul>
      </div>
      <div class="block" v-if="content['教育背景'] && content['教育背景'].个人荣誉">
        <div class="block-title">个人荣誉</div>
        <ul class="block-list">
          <li><span class="field-value">{{ content['教育背景'].个人荣誉 }}</span></li>
        </ul>
      </div>
    </div>
    <div class="right-panel">
      <div class="section personal-info-section" v-if="content['个人信息']">
        <div class="section-title">个人信息</div>
        <ul class="personal-info-list">
          <li v-for="(v, k) in content['个人信息']" :key="k">
            <span class="field-label">{{ k }}：</span>
            <span class="field-value">{{ v }}</span>
          </li>
        </ul>
      </div>
      <div class="section" v-if="content['任职情况'] && content['任职情况'].length">
        <div class="section-title">工作经历</div>
        <div v-for="(job, idx) in content['任职情况']" :key="idx" class="exp-block">
          <div class="exp-header">
            <span v-if="job.时间" class="exp-date">{{ job.时间 }}</span>
            <span v-if="job.单位" class="exp-company">{{ job.单位 }}</span>
            <span v-if="job.职位" class="exp-job">{{ job.职位 }}</span>
          </div>
          <div class="exp-desc" v-if="job.职责">{{ job.职责 }}</div>
        </div>
      </div>
      <div class="section" v-if="content['实习_兼职'] && content['实习_兼职'].length">
        <div class="section-title">实习/兼职</div>
        <div v-for="(exp, idx) in content['实习_兼职']" :key="idx" class="exp-block">
          <div class="exp-header">
            <span v-if="exp.时间" class="exp-date">{{ exp.时间 }}</span>
            <span v-if="exp.单位" class="exp-company">{{ exp.单位 }}</span>
            <span v-if="exp.职位" class="exp-job">{{ exp.职位 }}</span>
          </div>
          <div class="exp-desc" v-if="exp.职责">{{ exp.职责 }}</div>
        </div>
      </div>
      <div class="section" v-if="content['自我评价']">
        <div class="section-title">自我评价</div>
        <div class="self-eval">{{ content['自我评价'] }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElAvatar } from 'element-plus'
const props = defineProps({
  content: { type: Object, required: true },
  avatar: { type: String, default: '' }
})
</script>

<style scoped>
.resume-blue-a4 {
  width: 794px;
  height: 1123px;
  background: #fff;
  display: flex;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  color: #222;
  box-shadow: 0 0 8px #e0e6ed;
}
.left-panel {
  width: 190px; /* 蓝色区域宽度 */
  background: #4a90e2;
  color: #fff;
  padding: 32px 12px 24px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}
.avatar-box {
  width: 120px;
  height: 160px;
  margin-bottom: 18px;
  overflow: hidden;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-img {
  width: 120px !important;
  height: 160px !important;
  object-fit: cover;
  border: none;
}
.name-section {
  text-align: center;
  margin-bottom: 24px;
}
.resume-title {
  font-size: 28px;
  font-weight: bold;
  letter-spacing: 2px;
  margin-bottom: 8px;
}
.job-intention {
  font-size: 16px;
  margin-bottom: 12px;
}
.block {
  margin-bottom: 22px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.block-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  background: #fff;
  color: #4a90e2;
  padding: 4px 0;
  width: 80%;
  display: block;
  margin-left: auto;
  margin-right: auto;
  text-align: center;
  border-left: 3px solid #fff;
  border-right: 3px solid #fff;
}
.block-list {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 90%;
}
.block-list li {
  font-size: 14px;
  margin-bottom: 6px;
  padding-left: 18px;
  position: relative;
  color: #fff;
}
.block-list li::before {
  content: '■';
  color: #fff;
  position: absolute;
  left: 0;
  font-size: 10px;
}
.field-label {
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  letter-spacing: 0.3px;
}
.field-value {
  color: #fff;
  letter-spacing: 0.2px;
}
.gpa-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.gpa-label {
  color: #fff;
  font-weight: normal;
  font-size: 14px;
  white-space: nowrap;
  letter-spacing: 0.3px;
}
.right-panel {
  flex: 1;
  padding: 36px 36px 24px 36px;
  display: flex;
  flex-direction: column;
}
.section {
  margin-bottom: 28px;
}
.section-title {
  font-size: 18px;
  font-weight: bold; /* 保留标题粗体，确保层次分明 */
  color: #4a90e2;
  background: #eaf4fd;
  padding: 6px 18px;
  margin-bottom: 12px;
  display: inline-block;
  border-bottom: 2px solid #4a90e2;
}
.exp-block {
  margin-bottom: 16px;
  padding-bottom: 4px;
  border-bottom: 1px dotted #eaeaea;
}
.exp-block:last-child {
  border-bottom: none;
}
.exp-header {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  font-size: 15px;
  font-weight: normal;
  color: #222;
  margin-bottom: 6px;
}
.exp-date {
  color: #4a90e2;
  font-size: 15.5px;
}
.exp-company {
  color: #4a90e2;
  font-size: 15.5px;
}
.exp-job {
  color: #4a90e2;
  font-size: 15.5px;
}
.exp-desc {
  font-size: 14.5px;
  color: #333;
  margin-bottom: 6px;
  line-height: 1.5;
  padding-top: 4px;
  text-align: justify;
  position: relative;
  padding-left: 18px;
}
.exp-desc::before {
  content: '♦';
  color: #4a90e2;
  position: absolute;
  left: 0;
  top: 0px;
  font-size: 18px;
}
.self-eval {
  font-size: 15px;
  color: #333;
  line-height: 1.7;
  background: #f7fbff;
  border-radius: 0;
  padding: 12px 18px;
  border-left: 2px solid #eaf4fd;
}
.personal-info-section {
  margin-bottom: 18px;
}
.personal-info-list {
  list-style: none;
  padding: 0;
  margin: 0 0 0 2px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 32px;
}
.personal-info-list li {
  font-size: 15px;
  color: #333;
  min-width: 180px;
  margin-bottom: 4px;
  display: flex;
  align-items: baseline;
  position: relative;
  padding-left: 18px;
}
.personal-info-list li::before {
  content: '♦';
  color: #4a90e2;
  position: absolute;
  left: 0;
  top: -4px;
  font-size: 18px;
}
.personal-info-list .field-label {
  color: #222;
  font-weight: normal;
  margin-right: 4px;
}
.personal-info-list .field-value {
  color: #222;
}
</style>
