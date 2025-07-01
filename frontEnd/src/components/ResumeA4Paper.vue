<template>
  <div class="resume-a4-paper">
    <div class="resume-a4-content">
      <!-- 个人信息顶部模块 -->
      <div class="resume-a4-header-info">
        <div class="resume-a4-personal">
          <div class="resume-a4-name">{{ content.姓名 || '姓名' }}</div>
          <div class="resume-a4-job">{{ content.求职意向 || '求职意向' }}</div>
          <div class="resume-a4-info-grid" v-if="content['个人信息']">
            <div v-for="(v, k) in content['个人信息']" :key="k" class="resume-a4-info-item">
              <div class="resume-a4-info-icon">
                <el-icon v-if="k.includes('年龄')"><Calendar /></el-icon>
                <el-icon v-else-if="k.includes('民族')"><User /></el-icon>
                <el-icon v-else-if="k.includes('电话') || k.includes('联系')"><Phone /></el-icon>
                <el-icon v-else-if="k.includes('邮箱') || k.includes('邮件') || k.includes('mail')"><Message /></el-icon>
                <el-icon v-else-if="k.includes('籍贯') || k.includes('地区') || k.includes('地址')"><Location /></el-icon>
                <el-icon v-else><InfoFilled /></el-icon>
              </div>
              <span class="resume-a4-info-label">{{ k }}：</span>
              <span class="resume-a4-info-value">{{ v }}</span>
            </div>
          </div>
        </div>
        <div class="resume-a4-avatar">
          <el-avatar v-if="avatar" :src="avatar" size="large" shape="square" class="resume-avatar"/>
          <span v-else>无</span>
        </div>
      </div>
      <div class="resume-a4-section" v-if="content['教育背景']">
        <div class="resume-a4-section-title">
          <div class="section-title-box">
            <div class="section-icon">
              <el-icon><Reading /></el-icon>
            </div>
            教育背景
          </div>
        </div>
        <div class="resume-a4-section-content">
          <span v-for="(v, k) in content['教育背景']" :key="k" class="resume-a4-field">
            <span class="field-label">{{ k }}：</span>
            <span class="field-value">
              <template v-if="Array.isArray(v)">{{ v.join('，') }}</template>
              <template v-else>{{ v }}</template>
            </span>
          </span>
        </div>
      </div>
      <div class="resume-a4-section" v-if="content['任职情况'] && content['任职情况'].length">
        <div class="resume-a4-section-title">
          <div class="section-title-box">
            <div class="section-icon">
              <el-icon><Briefcase /></el-icon>
            </div>
            任职情况
          </div>
        </div>
        <div class="resume-a4-section-content">
          <div v-for="(job, idx) in content['任职情况']" :key="idx" class="resume-a4-job-block">
            <span v-for="(v, k) in job" :key="k" class="resume-a4-job-field">
              <span class="field-label">{{ k }}：</span>
              <span class="field-value">{{ v }}</span>
            </span>
          </div>
        </div>
      </div>
      <div class="resume-a4-section" v-if="content['实习_兼职'] && content['实习_兼职'].length">
        <div class="resume-a4-section-title">
          <div class="section-title-box">
            <div class="section-icon">
              <el-icon><Suitcase /></el-icon>
            </div>
            实习/兼职
          </div>
        </div>
        <div class="resume-a4-section-content">
          <div v-for="(exp, idx) in content['实习_兼职']" :key="idx" class="resume-a4-job-block">
            <span v-for="(v, k) in exp" :key="k" class="resume-a4-job-field">
              <span class="field-label">{{ k }}：</span>
              <span class="field-value">{{ v }}</span>
            </span>
          </div>
        </div>
      </div>
      <div class="resume-a4-section" v-if="content['自我评价']">
        <div class="resume-a4-section-title">
          <div class="section-title-box">
            <div class="section-icon">
              <el-icon><Star /></el-icon>
            </div>
            自我评价
          </div>
        </div>
        <div class="resume-a4-section-content">
          <div class="resume-a4-self-eval">
            <span class="field-label">自我评价：</span>
            <span class="field-value">{{ content['自我评价'] }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Calendar, User, Phone, Message, Location, InfoFilled, Reading, Briefcase, Suitcase, Star } from '@element-plus/icons-vue'
import { ElAvatar, ElIcon } from 'element-plus'
const props = defineProps({
  content: { type: Object, required: true },
  avatar: { type: String, default: '' }
})
</script>

<style scoped>
/* ResumeA4Paper 独立样式，来自 ResumeView.vue */
.resume-a4-paper {
  width: 794px; /* 210mm * 3.78px/mm = 794px */
  min-height: 1123px; /* 297mm * 3.78px/mm = 1123px */
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.13);
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 0;
  margin-bottom: 18px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.resume-a4-content {
  display: flex;
  flex-direction: column;
  min-height: 1123px;
  height: 100%;
  flex: 1 1 auto;
}
.resume-a4-paper::before {
  content: "Resume";
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 40px;
  background: #3c4b64;
  color: white;
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 1px;
  position: relative;
  border-radius: 10px 10px 0 0;
}
.resume-a4-paper::after {
  content: "";
  position: absolute;
  top: 40px;
  left: 50%;
  width: 35px;
  height: 18px;
  background: #fff;
  transform: translateX(-50%) rotate(45deg);
  z-index: 1;
}
.resume-a4-header-info {
  display: flex;
  align-items: flex-start;
  margin: 30px 40px 5px 40px;
  justify-content: space-between;
  position: relative;
  padding-bottom: 5px;
}
.resume-a4-personal {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex: 1;
}
.resume-a4-name {
  font-size: 28px;
  font-weight: bold;
  color: #111827;
  margin-bottom: 4px;
  letter-spacing: 1px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}
.resume-a4-job {
  font-size: 16px;
  color: #4f46e5;
  font-weight: 500;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}
.resume-a4-avatar {
  margin-left: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 160px;
  align-self: center;
}
.resume-a4-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-top: 6px;
}
.resume-a4-info-item {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
  font-size: 14px;
  color: #333;
}
.resume-a4-info-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #f0f2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  color: #4f46e5;
}
.resume-a4-info-label {
  font-weight: 600;
  color: #2c3e50;
  margin-right: 4px;
  white-space: nowrap;
  letter-spacing: 0.3px;
}
.resume-a4-info-value {
  color: #3a3a3a;
  letter-spacing: 0.2px;
}
.field-label {
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  letter-spacing: 0.3px;
}
.field-value {
  color: #3a3a3a;
  letter-spacing: 0.2px;
}
.resume-a4-job-field {
  display: flex;
  margin-right: 16px;
  align-items: baseline;
  min-width: 140px;
}
.resume-a4-section {
  margin: 5px 40px;
}
.resume-a4-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  position: relative;
  display: flex;
  align-items: flex-end;
  border-radius: 0;
  padding: 0;
  background: transparent;
  height: 28px;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.7);
}
.resume-a4-section-title::after {
  content: "";
  height: 8px;
  background: #3c4b64;
  flex: 1;
  position: relative;
  bottom: 0;
  clip-path: polygon(0 0, 100% 0, 100% 100%, 1% 100%);
}
.resume-a4-section-title .section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
  color: white;
  width: 20px;
  height: 20px;
}
.section-title-box {
  background: #3c4b64;
  color: white;
  display: flex;
  align-items: center;
  padding: 5px 5px 5px 10px;
  position: relative;
  clip-path: polygon(0 0, calc(100% - 20px) 0, 100% 100%, 0 100%);
  min-width: 120px;
  height: 20px;
  font-weight: 500;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}
.resume-a4-section-title .section-icon .el-icon {
  font-size: 14px;
  width: 100%;
  height: 100%;
}
.resume-a4-section-content {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  padding: 3px 8px;
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}
.resume-a4-field {
  min-width: 140px;
  margin-right: 16px;
  display: flex;
  align-items: baseline;
}
.resume-a4-job-block {
  margin-bottom: 8px;
  padding-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px 14px;
  border-bottom: 1px dashed #e5e7eb;
  width: 100%;
  transition: all 0.2s ease;
}
.resume-a4-job-block:last-child {
  border-bottom: none;
}
.resume-create-time-a4 {
  margin-top: 16px;
  font-size: 15px;
  color: #888;
  text-align: left;
  width: 794px;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
}
.resume-avatar {
  width: 120px !important;
  height: 160px !important;
  object-fit: cover;
  border: 1px solid #e5e7eb;
  background: #fff;
}
.resume-a4-self-eval {
  width: 100%;
  display: flex;
  align-items: baseline;
}
.resume-a4-self-eval .field-label {
  margin-right: 6px;
  flex-shrink: 0;
}
.resume-a4-self-eval .field-value {
  line-height: 1.7;
  text-align: justify;
}
@media (max-width: 1200px) {
  .resume-a4-paper, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 16px 2vw 16px 2vw;
  }
}
@media (max-width: 900px) {
  .resume-a4-paper, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 8px 0 8px 0;
  }
}
</style>
