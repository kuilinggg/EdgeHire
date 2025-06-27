package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.ResumeComment;
import com.se.EdgeHire.Repository.ResumeCommentRepository;
import com.se.EdgeHire.Repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeCommentService {
    
    @Autowired
    private ResumeCommentRepository resumeCommentRepository;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private HrService hrService;

    /**
     * 创建简历评论
     */
    public ResumeComment createComment(ResumeComment comment) {
        // 验证简历是否存在
        if (!resumeRepository.existsById(comment.getResumeId())) {
            throw new IllegalArgumentException("简历不存在");
        }
        
        // 验证HR身份
        if (!hrService.isHr(comment.getHrId())) {
            throw new IllegalArgumentException("只有HR可以评论简历");
        }
        
        // 检查是否已经评论过
        ResumeComment existingComment = resumeCommentRepository.findByResumeIdAndHrIdAndStatus(
            comment.getResumeId(), comment.getHrId(), 1);
        if (existingComment != null) {
            throw new IllegalArgumentException("您已经对此简历进行过评论");
        }
        
        comment.setCreateTime(LocalDateTime.now());
        comment.setStatus(1);
        return resumeCommentRepository.save(comment);
    }

    /**
     * 更新简历评论
     */
    public ResumeComment updateComment(Integer commentId, ResumeComment comment) {
        Optional<ResumeComment> existingComment = resumeCommentRepository.findById(commentId);
        if (existingComment.isEmpty()) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        ResumeComment toUpdate = existingComment.get();
        toUpdate.setComment(comment.getComment());
        toUpdate.setScore(comment.getScore());
        
        return resumeCommentRepository.save(toUpdate);
    }

    /**
     * 根据简历ID获取所有评论
     */
    public List<ResumeComment> getCommentsByResumeId(Integer resumeId) {
        return resumeCommentRepository.findByResumeIdAndStatus(resumeId, 1);
    }

    /**
     * 根据HR ID获取所有评论
     */
    public List<ResumeComment> getCommentsByHrId(Integer hrId) {
        return resumeCommentRepository.findByHrIdAndStatus(hrId, 1);
    }

    /**
     * 获取简历的平均评分
     */
    public Double getAverageScore(Integer resumeId) {
        return resumeCommentRepository.getAverageScoreByResumeId(resumeId);
    }

    /**
     * 统计HR的评论数量
     */
    public Long countCommentsByHr(Integer hrId) {
        return resumeCommentRepository.countByHrId(hrId);
    }

    /**
     * 删除评论（软删除）
     */
    public void deleteComment(Integer commentId, Integer hrId) {
        Optional<ResumeComment> comment = resumeCommentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        ResumeComment toDelete = comment.get();
        if (!toDelete.getHrId().equals(hrId)) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }
        
        toDelete.setStatus(0);
        resumeCommentRepository.save(toDelete);
    }

    /**
     * 获取HR对特定简历的评论
     */
    public ResumeComment getCommentByResumeAndHr(Integer resumeId, Integer hrId) {
        return resumeCommentRepository.findByResumeIdAndHrIdAndStatus(resumeId, hrId, 1);
    }
}
