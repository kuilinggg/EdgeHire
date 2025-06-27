package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.ResumeComment;
import com.se.EdgeHire.Service.ResumeCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume/comment")
@CrossOrigin(origins = "*")
public class ResumeCommentController {
    
    @Autowired
    private ResumeCommentService resumeCommentService;

    /**
     * 创建简历评论
     */
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody ResumeComment comment) {
        try {
            ResumeComment created = resumeCommentService.createComment(comment);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新简历评论
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Integer commentId, @RequestBody ResumeComment comment) {
        try {
            ResumeComment updated = resumeCommentService.updateComment(commentId, comment);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据简历ID获取所有评论
     */
    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<List<ResumeComment>> getCommentsByResumeId(@PathVariable Integer resumeId) {
        List<ResumeComment> comments = resumeCommentService.getCommentsByResumeId(resumeId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 根据HR ID获取所有评论
     */
    @GetMapping("/hr/{hrId}")
    public ResponseEntity<List<ResumeComment>> getCommentsByHrId(@PathVariable Integer hrId) {
        List<ResumeComment> comments = resumeCommentService.getCommentsByHrId(hrId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 获取简历的平均评分
     */
    @GetMapping("/resume/{resumeId}/score")
    public ResponseEntity<Map<String, Object>> getAverageScore(@PathVariable Integer resumeId) {
        Double avgScore = resumeCommentService.getAverageScore(resumeId);
        return ResponseEntity.ok(Map.of("averageScore", avgScore != null ? avgScore : 0.0));
    }

    /**
     * 获取HR对特定简历的评论
     */
    @GetMapping("/resume/{resumeId}/hr/{hrId}")
    public ResponseEntity<ResumeComment> getCommentByResumeAndHr(
            @PathVariable Integer resumeId, @PathVariable Integer hrId) {
        ResumeComment comment = resumeCommentService.getCommentByResumeAndHr(resumeId, hrId);
        if (comment != null) {
            return ResponseEntity.ok(comment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}/hr/{hrId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId, @PathVariable Integer hrId) {
        try {
            resumeCommentService.deleteComment(commentId, hrId);
            return ResponseEntity.ok(Map.of("message", "评论删除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 统计HR的评论数量
     */
    @GetMapping("/hr/{hrId}/count")
    public ResponseEntity<Map<String, Object>> countCommentsByHr(@PathVariable Integer hrId) {
        Long count = resumeCommentService.countCommentsByHr(hrId);
        return ResponseEntity.ok(Map.of("count", count != null ? count : 0));
    }
}
