package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Service.ResumeRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/resume")
@CrossOrigin(origins = "*")
public class ResumeRecommendationController {
    
    @Autowired
    private ResumeRecommendationService recommendationService;

    /**
     * 为HR推荐简历
     */
    @GetMapping("/recommend/{hrId}")
    public ResponseEntity<?> recommendResumes(
            @PathVariable Integer hrId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, Object>> recommendations = 
                recommendationService.recommendResumesForHr(hrId, page, size);
            return ResponseEntity.ok(Map.of(
                "data", recommendations,
                "page", page,
                "size", size,
                "total", recommendations.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 搜索简历
     */
    @GetMapping("/search/{hrId}")
    public ResponseEntity<?> searchResumes(
            @PathVariable Integer hrId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minEducation,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, Object>> results = recommendationService.searchResumes(
                hrId, keyword, minEducation, position, page, size);
            return ResponseEntity.ok(Map.of(
                "data", results,
                "page", page,
                "size", size,
                "total", results.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
