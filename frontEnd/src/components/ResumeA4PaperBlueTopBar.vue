<template>
    <div class="resume-blue-topbar-a4">
        <!-- 顶部深蓝横条 -->
        <div class="topbar-bar">
            <span class="topbar-title">个人简历</span>
        </div>
        <!-- 白色信息区 -->
        <div class="header-info-row">
            <div class="avatar-box">
                <el-avatar :src="avatar" shape="square" class="avatar-img" />
            </div>
            <div class="header-main">
                <div class="resume-title">{{ content.姓名 || '姓名' }}</div>
                <div class="job-intention">{{ content.求职意向 || '求职目标' }}</div>
                <div class="info-list" v-if="content['个人信息']">
                    <div v-for="(v, k, idx) in content['个人信息']" :key="k" class="info-item">
                        <span class="info-icon">
                            <el-icon v-if="k.includes('年龄')">
                                <Calendar />
                            </el-icon>
                            <el-icon v-else-if="k.includes('民族')">
                                <User />
                            </el-icon>
                            <el-icon v-else-if="k.includes('电话') || k.includes('联系')">
                                <Phone />
                            </el-icon>
                            <el-icon v-else-if="k.includes('邮箱') || k.includes('邮件') || k.includes('mail')">
                                <Message />
                            </el-icon>
                            <el-icon v-else-if="k.includes('籍贯') || k.includes('地区') || k.includes('地址')">
                                <Location />
                            </el-icon>
                            <el-icon v-else>
                                <InfoFilled />
                            </el-icon>
                        </span>
                        <span class="info-label">{{ k }}：</span>
                        <span class="info-value">{{ v }}</span>
                    </div>
                </div>
            </div>
        </div>
        <!-- 教育背景 -->
        <div class="section" v-if="content['教育背景']">
            <div class="section-title">
                <span class="section-icon"><el-icon>
                        <Reading />
                    </el-icon></span>
                教育背景
            </div>
            <div class="section-content">
                <div class="edu-row edu-row-grid">
                    <span class="edu-date">{{ content['教育背景'].时间 }}</span>
                    <span class="edu-school">{{ content['教育背景'].学校 }}</span>
                    <span class="edu-major">{{ content['教育背景'].专业 }}</span>
                    <span class="edu-degree">{{ content['教育背景'].学历 }}</span>
                </div>
                <ul class="edu-list">
                    <li v-if="content['教育背景'].GPA"><span class="edu-dot">●</span><b>GPA：</b>{{ content['教育背景'].GPA }}</li>
                    <li v-if="content['教育背景'].主修课程 && content['教育背景'].主修课程.length">
                        <span class="edu-dot">●</span><b>主修课程：</b>{{ content['教育背景'].主修课程.join('，') }}
                    </li>
                    <li v-if="content['教育背景'].个人荣誉">
                        <span class="edu-dot">●</span><b>个人荣誉：</b>{{ content['教育背景'].个人荣誉 }}
                    </li>
                </ul>
            </div>
        </div>
        <!-- 工作经历 -->
        <div class="section" v-if="content['任职情况'] && content['任职情况'].length">
            <div class="section-title">
                <span class="section-icon"><el-icon>
                        <Briefcase />
                    </el-icon></span>
                工作经历
            </div>
            <div class="section-content">
                <div v-for="(job, idx) in content['任职情况']" :key="idx" class="exp-block">
                    <div class="exp-row exp-row-grid">
                        <span class="exp-date">{{ job.时间 }}</span>
                        <span class="exp-company">{{ job.单位 }}</span>
                        <span class="exp-job">{{ job.职位 }}</span>
                    </div>
                    <ul class="exp-desc-list" v-if="job.职责">
                        <li v-for="(desc, i) in job.职责.split(/[；;\n]/).filter(Boolean)" :key="i">
                            <span class="exp-dot">●</span>
                            <span class="exp-desc-text"><b>职责：</b>{{ desc }}</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <!-- 实习/兼职 -->
        <div class="section" v-if="content['实习_兼职'] && content['实习_兼职'].length">
            <div class="section-title">
                <span class="section-icon"><el-icon>
                        <Suitcase />
                    </el-icon></span>
                实习/兼职
            </div>
            <div class="section-content">
                <div v-for="(exp, idx) in content['实习_兼职']" :key="idx" class="exp-block">
                    <div class="exp-row exp-row-grid">
                        <span class="exp-date">{{ exp.时间 }}</span>
                        <span class="exp-company">{{ exp.单位 }}</span>
                        <span class="exp-job">{{ exp.职位 }}</span>
                    </div>
                    <ul class="exp-desc-list" v-if="exp.职责">
                        <li v-for="(desc, i) in exp.职责.split(/[；;\n]/).filter(Boolean)" :key="i">
                            <span class="exp-dot">●</span>
                            <span class="exp-desc-text"><b>职责：</b>{{ desc }}</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <!-- 技能证书 -->
        <div class="section" v-if="content['技能证书']">
            <div class="section-title">
                <span class="section-icon"><el-icon>
                        <Star />
                    </el-icon></span>
                技能证书
            </div>
            <div class="section-content">
                <ul class="list-block">
                    <li v-for="(item, idx) in (Array.isArray(content['技能证书']) ? content['技能证书'] : String(content['技能证书']).split(/[；;\n]/).filter(Boolean))"
                        :key="idx">
                        <span class="list-dot">●</span>
                        <span class="list-text">{{ item }}</span>
                    </li>
                </ul>
            </div>
        </div>
        <!-- 自我评价 -->
        <div class="section" v-if="content['自我评价']">
            <div class="section-title">
                <span class="section-icon"><el-icon>
                        <InfoFilled />
                    </el-icon></span>
                自我评价
            </div>
            <div class="section-content">
                <div class="self-eval">{{ content['自我评价'] }}</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ElAvatar, ElIcon } from 'element-plus'
import { Calendar, User, Phone, Message, Location, InfoFilled, Reading, Briefcase, Star, Suitcase } from '@element-plus/icons-vue'
const props = defineProps({
    content: { type: Object, required: true },
    avatar: { type: String, default: '' }
})
</script>

<style scoped>
.resume-blue-topbar-a4 {
    width: 794px;
    min-height: 1123px;
    background: #fff;
    font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
    color: #222;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.13);
    border: 1.5px solid #205080;
    margin: 0 auto 18px auto;
    padding-bottom: 12px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    font-size: 15px;
}

.topbar-bar {
    width: 100%;
    height: 38px;
    background: #205080;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    position: relative;
}

.topbar-title {
    color: #fff;
    font-size: 20px;
    font-weight: bold;
    letter-spacing: 2px;
    padding: 0 32px 0 0;
}

.header-info-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 18px 38px 8px 38px;
    background: #fff;
    border-bottom: 2px solid #205080;
    margin-bottom: 0;
}

.avatar-box {
    width: 110px;
    height: 146px;
    background: #fff;
    overflow: hidden;
    margin-right: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(32, 80, 128, 0.08);
}

.avatar-img {
    width: 110px !important;
    height: 146px !important;
    object-fit: cover;
}

.header-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    color: #222;
    height: 146px;
    /* 头像高度对齐 */
}

.resume-title {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 2px;
    letter-spacing: 1px;
    color: #205080;
}

.job-intention {
    font-size: 15px;
    margin-bottom: 8px;
    color: #205080;
    font-weight: 500;
}

.info-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(160px, 1fr));
    gap: 6px 24px;
    font-size: 14px;
    margin-top: 2px;
}

.info-item {
    display: flex;
    align-items: center;
    min-width: 160px;
    margin-bottom: 0;
    position: relative;
    padding-left: 0;
}

.info-icon {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #eaf2fa;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 6px;
    color: #205080;
    font-size: 15px;
}

.info-label {
    font-weight: bold;
    color: #205080;
    margin-right: 2px;
}

.info-value {
    color: #222;
}

.section {
    margin: 15px 38px 0 38px;
}

.section-title {
    font-size: 16px;
    font-weight: bold;
    color: #fff;
    background: #205080;
    padding: 6px 24px 6px 18px;
    display: flex;
    align-items: center;
    margin-bottom: 0;
    letter-spacing: 1px;
    box-shadow: 0 2px 8px rgba(32, 80, 128, 0.04);
}

.section-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 8px;
    color: #fff;
    font-size: 16px;
}

.section-content {
    background: #fff;
    border-radius: 0 0 4px 4px;
    padding: 12px 22px 8px 22px;
    font-size: 14px;
    color: #222;
    border-top: 2px solid #205080;
    margin-bottom: 0;
}

.edu-row {
    display: flex;
    flex-wrap: wrap;
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 2px;
}

.edu-row-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 1fr 1fr 1fr;
    justify-items: center;
    align-items: center;
    text-align: center;
    gap: 0;
    margin-bottom: 2px;
}

.edu-date {
    justify-self: start;
    text-align: left;
    padding-left: 0;
    margin-left: 0;
    color: #205080;
    font-weight: bold;
}

.edu-school,
.edu-major,
.edu-degree {
    justify-self: center;
    color: #205080;
    font-weight: bold;
}

.edu-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.edu-list li {
    display: flex;
    align-items: flex-start;
    margin-bottom: 2px;
    color: #222;
    font-size: 14px;
}

.edu-dot {
    color: #205080;
    font-size: 12px;
    margin-right: 6px;
    line-height: 1.7;
}

.exp-block {
    margin-bottom: 5px;
}

.exp-block:last-child {
    margin-bottom: 0;
    border-bottom: none;
}

.exp-row {
    display: flex;
    flex-wrap: wrap;
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 2px;
}

.exp-row-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 1fr 1fr;
    justify-items: center;
    align-items: center;
    text-align: center;
    gap: 0;
    margin-bottom: 2px;
}

.exp-date {
    justify-self: start;
    text-align: left;
    padding-left: 0;
    margin-left: 0;
    color: #205080;
    font-weight: bold;
}

.exp-company,
.exp-job {
    color: #205080;
    font-weight: bold;
}

.exp-label {
    font-size: 14px;
    color: #205080;
    font-weight: bold;
    margin: 2px 0 2px 0;
}

.exp-desc-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.exp-desc-list li {
    display: flex;
    align-items: flex-start;
    margin-bottom: 2px;
}

.exp-dot {
    color: #205080;
    font-size: 12px;
    margin-right: 6px;
    line-height: 1.7;
}

.exp-desc-text {
    color: #222;
    font-size: 14px;
    line-height: 1.7;
}

.list-block {
    list-style: none;
    padding: 0;
    margin: 0;
}

.list-block li {
    display: flex;
    align-items: flex-start;
    margin-bottom: 2px;
}

.list-dot {
    color: #205080;
    font-size: 12px;
    margin-right: 6px;
    line-height: 1.7;
}

.list-text {
    color: #222;
    font-size: 14px;
    line-height: 1.7;
}

.self-eval {
    color: #205080;
    font-size: 14.5px;
    line-height: 1.7;
    padding: 2px 0 2px 0;
}

@media (max-width: 1200px) {
    .resume-blue-topbar-a4 {
        width: 100vw !important;
        min-width: unset;
        border-radius: 0;
        padding: 8px 2vw 8px 2vw;
    }
}

@media (max-width: 900px) {
    .resume-blue-topbar-a4 {
        width: 100vw !important;
        min-width: unset;
        border-radius: 0;
        padding: 4px 0 4px 0;
    }
}
</style>
