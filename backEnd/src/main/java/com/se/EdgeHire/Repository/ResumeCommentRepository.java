package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.ResumeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeCommentRepository extends JpaRepository<ResumeComment, Integer> {
    
    // 根据简历ID查找所有评论
    List<ResumeComment> findByResumeIdAndStatus(Integer resumeId, Integer status);
    
    // 根据HR ID查找所有评论
    List<ResumeComment> findByHrIdAndStatus(Integer hrId, Integer status);
    
    // 根据简历ID和HR ID查找评论（一个HR对一份简历只能评论一次）
    ResumeComment findByResumeIdAndHrIdAndStatus(Integer resumeId, Integer hrId, Integer status);
    
    // 统计HR的评论数量
    @Query("SELECT COUNT(rc) FROM ResumeComment rc WHERE rc.hrId = :hrId AND rc.status = 1")
    Long countByHrId(@Param("hrId") Integer hrId);
    
    // 获取简历的平均评分
    @Query("SELECT AVG(rc.score) FROM ResumeComment rc WHERE rc.resumeId = :resumeId AND rc.status = 1 AND rc.score IS NOT NULL")
    Double getAverageScoreByResumeId(@Param("resumeId") Integer resumeId);
}
