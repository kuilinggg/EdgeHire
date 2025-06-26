<template>
  <div class="resume-detail">
    <!-- 顶部操作区 -->
    <div class="detail-header">
      <div class="user-info">
        <el-avatar :src="resume.avatar" :size="80" />
        <div class="user-basic">
          <h2 class="user-name">{{ resume.name }}</h2>
          <p class="user-position">{{ resume.expectedPosition }}</p>
          <div class="user-tags">
            <el-tag class="basic-tag">{{ resume.experience }}</el-tag>
            <el-tag class="basic-tag">{{ resume.education }}</el-tag>
            <el-tag class="basic-tag">{{ resume.expectedCity }}</el-tag>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="startChat">
          <el-icon><ChatDotRound /></el-icon>
          发起沟通
        </el-button>
        <el-button type="success" size="large" @click="giveSuggestion">
          <el-icon><EditPen /></el-icon>
          提出修改建议
        </el-button>
      </div>
    </div>

    <!-- 详细信息区 -->
    <div class="detail-content">
      <!-- 基本信息 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><User /></el-icon>
          基本信息
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">姓名：</span>
            <span class="value">{{ resumeDetail.basicInfo.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">性别：</span>
            <span class="value">{{ resumeDetail.basicInfo.gender }}</span>
          </div>
          <div class="info-item">
            <span class="label">年龄：</span>
            <span class="value">{{ resumeDetail.basicInfo.age }}岁</span>
          </div>
          <div class="info-item">
            <span class="label">手机：</span>
            <span class="value">{{ resumeDetail.basicInfo.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱：</span>
            <span class="value">{{ resumeDetail.basicInfo.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">地址：</span>
            <span class="value">{{ resumeDetail.basicInfo.address }}</span>
          </div>
        </div>
      </div>

      <!-- 求职意向 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><Aim /></el-icon>
          求职意向
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">期望职位：</span>
            <span class="value">{{ resumeDetail.jobIntention.position }}</span>
          </div>
          <div class="info-item">
            <span class="label">期望薪资：</span>
            <span class="value">{{ resumeDetail.jobIntention.salary }}</span>
          </div>
          <div class="info-item">
            <span class="label">工作地点：</span>
            <span class="value">{{ resumeDetail.jobIntention.location }}</span>
          </div>
          <div class="info-item">
            <span class="label">工作性质：</span>
            <span class="value">{{ resumeDetail.jobIntention.type }}</span>
          </div>
        </div>
      </div>

      <!-- 工作经历 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><Briefcase /></el-icon>
          工作经历
        </h3>
        <div class="experience-list">
          <div 
            v-for="(exp, index) in resumeDetail.workExperience" 
            :key="index"
            class="experience-item"
          >
            <div class="exp-header">
              <div class="exp-title">
                <h4>{{ exp.position }}</h4>
                <span class="company">{{ exp.company }}</span>
              </div>
              <span class="exp-duration">{{ exp.duration }}</span>
            </div>
            <div class="exp-content">
              <p>{{ exp.description }}</p>
              <ul v-if="exp.achievements">
                <li v-for="achievement in exp.achievements" :key="achievement">
                  {{ achievement }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 教育背景 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><School /></el-icon>
          教育背景
        </h3>
        <div class="education-list">
          <div 
            v-for="(edu, index) in resumeDetail.education" 
            :key="index"
            class="education-item"
          >
            <div class="edu-header">
              <div class="edu-title">
                <h4>{{ edu.school }}</h4>
                <span class="major">{{ edu.major }}</span>
              </div>
              <span class="edu-duration">{{ edu.duration }}</span>
            </div>
            <div class="edu-content">
              <p><strong>学历：</strong>{{ edu.degree }}</p>
              <p v-if="edu.gpa"><strong>GPA：</strong>{{ edu.gpa }}</p>
              <p v-if="edu.courses"><strong>主修课程：</strong>{{ edu.courses }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 项目经历 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><Monitor /></el-icon>
          项目经历
        </h3>
        <div class="project-list">
          <div 
            v-for="(project, index) in resumeDetail.projects" 
            :key="index"
            class="project-item"
          >
            <div class="project-header">
              <h4>{{ project.name }}</h4>
              <span class="project-duration">{{ project.duration }}</span>
            </div>
            <div class="project-content">
              <p><strong>项目描述：</strong>{{ project.description }}</p>
              <p><strong>技术栈：</strong>{{ project.technologies.join(', ') }}</p>
              <p><strong>个人职责：</strong>{{ project.responsibility }}</p>
              <p v-if="project.achievements"><strong>项目成果：</strong>{{ project.achievements }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 技能证书 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><Medal /></el-icon>
          技能证书
        </h3>
        <div class="skills-content">
          <div class="skills-group">
            <h4>专业技能</h4>
            <div class="skill-tags">
              <el-tag 
                v-for="skill in resumeDetail.skills.professional" 
                :key="skill.name"
                :type="getSkillType(skill.level)"
                class="skill-tag"
              >
                {{ skill.name }} ({{ skill.level }})
              </el-tag>
            </div>
          </div>
          <div class="skills-group" v-if="resumeDetail.skills.languages">
            <h4>语言能力</h4>
            <div class="skill-tags">
              <el-tag 
                v-for="lang in resumeDetail.skills.languages" 
                :key="lang.name"
                type="info"
                class="skill-tag"
              >
                {{ lang.name }} ({{ lang.level }})
              </el-tag>
            </div>
          </div>
          <div class="certificates" v-if="resumeDetail.certificates">
            <h4>证书资质</h4>
            <ul>
              <li v-for="cert in resumeDetail.certificates" :key="cert">
                {{ cert }}
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 自我评价 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><ChatLineRound /></el-icon>
          自我评价
        </h3>
        <div class="self-evaluation">
          <p>{{ resumeDetail.selfEvaluation }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { 
  ChatDotRound, 
  EditPen, 
  User, 
  Aim, 
  Briefcase, 
  School, 
  Monitor, 
  Medal,
  ChatLineRound
} from '@element-plus/icons-vue'

const props = defineProps({
  resume: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['start-chat', 'give-suggestion'])

// 模拟详细简历数据
const resumeDetail = ref({
  basicInfo: {
    name: '张三',
    gender: '男',
    age: 25,
    phone: '138****8888',
    email: 'zhangsan@example.com',
    address: '上海市浦东新区'
  },
  jobIntention: {
    position: '前端开发工程师',
    salary: '15K-25K',
    location: '上海',
    type: '全职'
  },
  workExperience: [
    {
      position: '前端开发工程师',
      company: 'ABC科技有限公司',
      duration: '2022.03 - 至今',
      description: '负责公司主要产品的前端开发工作，参与需求分析、技术选型、代码实现等全流程。',
      achievements: [
        '独立完成用户管理系统前端开发，提升用户体验30%',
        '优化页面加载速度，首屏渲染时间减少40%',
        '参与技术分享，帮助团队成员提升开发效率'
      ]
    },
    {
      position: '前端实习生',
      company: 'XYZ互联网公司',
      duration: '2021.07 - 2022.02',
      description: '参与公司官网和内部管理系统的前端开发工作。',
      achievements: [
        '完成官网响应式改造，适配多种设备',
        '协助开发内部CRM系统，提升工作效率'
      ]
    }
  ],
  education: [
    {
      school: '上海交通大学',
      major: '计算机科学与技术',
      degree: '本科',
      duration: '2018.09 - 2022.06',
      gpa: '3.8/4.0',
      courses: 'Java程序设计、数据结构、算法分析、数据库原理、软件工程'
    }
  ],
  projects: [
    {
      name: '电商管理平台',
      duration: '2023.01 - 2023.06',
      description: '基于Vue3+Element Plus开发的电商后台管理系统',
      technologies: ['Vue3', 'Element Plus', 'TypeScript', 'Vite', 'Pinia'],
      responsibility: '负责整体架构设计和核心功能开发',
      achievements: '系统上线后，管理效率提升50%'
    },
    {
      name: '在线学习平台',
      duration: '2022.09 - 2022.12',
      description: '面向学生的在线学习和考试平台',
      technologies: ['React', 'Ant Design', 'Redux', 'Webpack'],
      responsibility: '负责前端页面开发和用户交互优化',
      achievements: '用户满意度达到95%'
    }
  ],
  skills: {
    professional: [
      { name: 'Vue.js', level: '熟练' },
      { name: 'React', level: '熟练' },
      { name: 'JavaScript', level: '精通' },
      { name: 'TypeScript', level: '熟练' },
      { name: 'Node.js', level: '了解' }
    ],
    languages: [
      { name: '英语', level: 'CET-6' },
      { name: '日语', level: 'N2' }
    ]
  },
  certificates: [
    '软件设计师（中级）',
    '阿里云开发者认证',
    'PMP项目管理认证'
  ],
  selfEvaluation: '本人性格开朗，工作认真负责，具有良好的团队合作精神。在前端开发领域有扎实的基础和丰富的实践经验，熟悉主流前端框架和工具。善于学习新技术，能够快速适应项目需求。希望能够在贵公司发挥自己的专业技能，与团队共同成长。'
})

const getSkillType = (level) => {
  const typeMap = {
    '精通': 'danger',
    '熟练': 'warning',
    '了解': 'info'
  }
  return typeMap[level] || 'info'
}

const startChat = () => {
  emit('start-chat', props.resume)
}

const giveSuggestion = () => {
  emit('give-suggestion', props.resume)
}
</script>

<style scoped>
.resume-detail {
  max-height: 80vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f0ff 100%);
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-basic {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.user-position {
  font-size: 16px;
  color: #3a36db;
  font-weight: 500;
  margin: 0;
}

.user-tags {
  display: flex;
  gap: 8px;
}

.basic-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.detail-content {
  padding: 0 24px 24px;
}

.info-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0ff;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
  flex: 1;
}

.experience-list,
.education-list,
.project-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.experience-item,
.education-item,
.project-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  border-left: 4px solid #3a36db;
}

.exp-header,
.edu-header,
.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.exp-title,
.edu-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.exp-title h4,
.edu-title h4,
.project-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.company,
.major {
  font-size: 14px;
  color: #666;
}

.exp-duration,
.edu-duration,
.project-duration {
  font-size: 14px;
  color: #999;
  white-space: nowrap;
}

.exp-content,
.edu-content,
.project-content {
  color: #666;
  line-height: 1.6;
}

.exp-content ul,
.edu-content ul {
  margin: 8px 0 0 20px;
  padding: 0;
}

.exp-content li,
.edu-content li {
  margin-bottom: 4px;
}

.skills-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skills-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skills-group h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  font-size: 12px;
}

.certificates ul {
  margin: 12px 0 0 20px;
  padding: 0;
}

.certificates li {
  margin-bottom: 8px;
  color: #666;
}

.self-evaluation p {
  color: #666;
  line-height: 1.8;
  text-indent: 2em;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .action-buttons {
    width: 100%;
    justify-content: center;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .exp-header,
  .edu-header,
  .project-header {
    flex-direction: column;
    gap: 8px;
  }

  .skill-tags {
    justify-content: center;
  }
}
</style>
