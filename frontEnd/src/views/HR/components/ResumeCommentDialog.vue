<template>
  <el-dialog
    v-model="visible"
    title="简历评价与建议"
    width="600px"
    :before-close="handleClose"
  >
    <div class="comment-form">
      <div class="resume-info">
        <div class="user-basic">
          <el-avatar :src="resume.avatar" :size="50" />
          <div class="user-details">
            <h4>{{ resume.name }}</h4>
            <p>{{ resume.expectedPosition }}</p>
          </div>
        </div>
      </div>

      <el-form :model="commentForm" :rules="rules" ref="commentFormRef" label-width="80px">
        <el-form-item label="评分" prop="score">
          <el-rate
            v-model="commentForm.score"
            :max="10"
            show-score
            text-color="#ff9900"
            score-template="{value}分"
          />
          <div class="score-tips">
            <span class="tip">请为该简历打分（1-10分）</span>
          </div>
        </el-form-item>

        <el-form-item label="评价建议" prop="comment">
          <el-input
            v-model="commentForm.comment"
            type="textarea"
            :rows="6"
            placeholder="请输入您对该简历的评价和修改建议..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="建议类型">
          <el-checkbox-group v-model="commentForm.tags">
            <el-checkbox label="格式优化">格式优化</el-checkbox>
            <el-checkbox label="内容完善">内容完善</el-checkbox>
            <el-checkbox label="技能突出">技能突出</el-checkbox>
            <el-checkbox label="经历描述">经历描述</el-checkbox>
            <el-checkbox label="项目经验">项目经验</el-checkbox>
            <el-checkbox label="其他建议">其他建议</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="submitting">
          提交评价
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { resumeCommentApi } from '../../../api/hr.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  resume: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const submitting = ref(false)
const commentFormRef = ref()

const commentForm = reactive({
  score: 8,
  comment: '',
  tags: []
})

const rules = {
  score: [
    { required: true, message: '请为简历打分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评价建议', trigger: 'blur' },
    { min: 10, message: '评价建议至少10个字符', trigger: 'blur' }
  ]
}

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
  if (!newVal) {
    resetForm()
  }
})

const resetForm = () => {
  commentForm.score = 8
  commentForm.comment = ''
  commentForm.tags = []
  if (commentFormRef.value) {
    commentFormRef.value.clearValidate()
  }
}

const handleClose = () => {
  visible.value = false
}

const submitComment = async () => {
  try {
    await commentFormRef.value.validate()
    
    submitting.value = true
    const userId = localStorage.getItem('userId') || '1'
    
    const commentData = {
      resumeId: props.resume.id,
      hrId: parseInt(userId),
      comment: commentForm.comment,
      score: commentForm.score,
      tags: commentForm.tags.join(',') // 将标签数组转为字符串
    }
    
    await resumeCommentApi.createComment(commentData)
    
    ElMessage.success('评价提交成功')
    emit('success', commentData)
    handleClose()
  } catch (error) {
    console.error('提交评价失败:', error)
    if (error.response && error.response.data && error.response.data.error) {
      ElMessage.error(error.response.data.error)
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.comment-form {
  padding: 0 8px;
}

.resume-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.user-basic {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.user-details p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.score-tips {
  margin-top: 8px;
}

.tip {
  font-size: 12px;
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.el-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.el-checkbox {
  margin-right: 0;
}
</style>
