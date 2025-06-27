package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.*;
import com.se.EdgeHire.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeRecommendationService {
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private SeekerInfoRepository seekerInfoRepository;
    
    @Autowired
    private InfoRepository infoRepository;
    
    @Autowired
    private HrInfoRepository hrInfoRepository;
    
    @Autowired
    private ResumeCommentRepository resumeCommentRepository;

    /**
     * 为HR推荐简历
     */
    public List<Map<String, Object>> recommendResumesForHr(Integer hrId, int page, int size) {
        // 获取HR信息
        Optional<HrInfo> hrInfo = hrInfoRepository.findByUserId(hrId);
        if (hrInfo.isEmpty()) {
            throw new IllegalArgumentException("HR信息不存在");
        }
        
        // 分页获取所有简历
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Resume> resumePage = resumeRepository.findAll(pageable);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        for (Resume resume : resumePage.getContent()) {
            Map<String, Object> recommendation = buildResumeRecommendation(resume, hrInfo.get());
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }
        
        // 按匹配度排序
        recommendations.sort((a, b) -> 
            Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore")));
        
        return recommendations;
    }

    /**
     * 搜索简历
     */
    public List<Map<String, Object>> searchResumes(Integer hrId, String keyword, Integer minEducation, 
                                                   String position, int page, int size) {
        // 获取HR信息
        Optional<HrInfo> hrInfo = hrInfoRepository.findByUserId(hrId);
        if (hrInfo.isEmpty()) {
            throw new IllegalArgumentException("HR信息不存在");
        }
        
        // 根据条件搜索求职者
        List<SeekerInfo> seekers = seekerInfoRepository.findAll();
        
        // 过滤条件
        if (minEducation != null) {
            seekers = seekers.stream()
                .filter(s -> s.getEducation() != null && s.getEducation() >= minEducation)
                .collect(Collectors.toList());
        }
        
        if (position != null && !position.trim().isEmpty()) {
            seekers = seekers.stream()
                .filter(s -> s.getFavor() != null && s.getFavor().contains(position.trim()))
                .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (SeekerInfo seeker : seekers) {
            // 获取该求职者的简历
            List<Resume> resumes = resumeRepository.findByUserId(seeker.getUserId());
            for (Resume resume : resumes) {
                // 如果有关键词，在简历内容中搜索
                if (keyword != null && !keyword.trim().isEmpty()) {
                    if (!resume.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                        continue;
                    }
                }
                
                Map<String, Object> result = buildResumeRecommendation(resume, hrInfo.get());
                if (result != null) {
                    results.add(result);
                }
            }
        }
        
        // 分页
        int start = page * size;
        int end = Math.min(start + size, results.size());
        if (start >= results.size()) {
            return new ArrayList<>();
        }
        
        return results.subList(start, end);
    }

    /**
     * 构建简历推荐信息
     */
    private Map<String, Object> buildResumeRecommendation(Resume resume, HrInfo hrInfo) {
        try {
            // 获取求职者信息
            Optional<SeekerInfo> seekerInfo = seekerInfoRepository.findByUserId(resume.getUserId());
            List<Info> userInfos = infoRepository.findByUserId(resume.getUserId());
            
            if (userInfos.isEmpty()) {
                return null;
            }
            
            Info userInfo = userInfos.get(0);
            
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("resumeId", resume.getId());
            recommendation.put("userId", resume.getUserId());
            recommendation.put("name", userInfo.getRealname());
            recommendation.put("avatar", resume.getAvatar() != null ? resume.getAvatar() : userInfo.getAvatar());
            recommendation.put("age", userInfo.getAge());
            recommendation.put("gender", userInfo.getGender());
            recommendation.put("phone", userInfo.getPhone());
            recommendation.put("email", userInfo.getEmail());
            recommendation.put("createTime", resume.getCreateTime());
            
            if (seekerInfo.isPresent()) {
                SeekerInfo seeker = seekerInfo.get();
                recommendation.put("education", seeker.getEducation());
                recommendation.put("school", seeker.getSchool());
                recommendation.put("expectedPosition", seeker.getFavor());
                recommendation.put("membership", seeker.getMembership());
                
                // 计算匹配度
                int matchScore = calculateMatchScore(seeker, hrInfo);
                recommendation.put("matchScore", matchScore);
            } else {
                recommendation.put("matchScore", 50); // 默认匹配度
            }
            
            // 获取平均评分
            Double avgScore = resumeCommentRepository.getAverageScoreByResumeId(resume.getId());
            recommendation.put("averageScore", avgScore);
            
            // 检查当前HR是否已评论
            ResumeComment hrComment = resumeCommentRepository.findByResumeIdAndHrIdAndStatus(
                resume.getId(), hrInfo.getUserId(), 1);
            recommendation.put("hasCommented", hrComment != null);
            
            return recommendation;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算匹配度算法
     */
    private int calculateMatchScore(SeekerInfo seeker, HrInfo hrInfo) {
        int score = 50; // 基础分
        
        // 岗位匹配度 (40分)
        if (seeker.getFavor() != null && hrInfo.getPosition() != null) {
            String seekerPosition = seeker.getFavor().toLowerCase();
            String hrPosition = hrInfo.getPosition().toLowerCase();
            
            if (seekerPosition.contains(hrPosition) || hrPosition.contains(seekerPosition)) {
                score += 40;
            } else if (hasCommonKeywords(seekerPosition, hrPosition)) {
                score += 20;
            }
        }
        
        // 学历加分 (30分)
        if (seeker.getEducation() != null) {
            if (seeker.getEducation() >= 6) { // 硕士及以上
                score += 30;
            } else if (seeker.getEducation() >= 5) { // 本科
                score += 20;
            } else if (seeker.getEducation() >= 4) { // 大专
                score += 10;
            }
        }
        
        // 会员加分 (20分)
        if (seeker.getMembership() != null && seeker.getMembership() > 0) {
            score += 20;
        }
        
        return Math.min(score, 100); // 最高100分
    }

    /**
     * 检查是否有共同关键词
     */
    private boolean hasCommonKeywords(String str1, String str2) {
        String[] keywords1 = str1.split("[\\s,，、]+");
        String[] keywords2 = str2.split("[\\s,，、]+");
        
        for (String keyword1 : keywords1) {
            for (String keyword2 : keywords2) {
                if (keyword1.length() > 1 && keyword2.length() > 1 && 
                    (keyword1.contains(keyword2) || keyword2.contains(keyword1))) {
                    return true;
                }
            }
        }
        return false;
    }
}
