package com.se.EdgeHire.Service;

import com.se.EdgeHire.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class HrStatisticsService {
    
    @Autowired
    private ResumeCommentRepository resumeCommentRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private SeekerInfoRepository seekerInfoRepository;

    /**
     * 获取HR主页的KPI数据
     */
    public Map<String, Object> getHrKpiData(Integer hrId) {
        Map<String, Object> kpiData = new HashMap<>();
        
        // 新的指导请求数量（本周新增的简历数量）
        LocalDateTime weekStart = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        long newResumes = resumeRepository.countByCreateTimeAfter(weekStart);
        kpiData.put("newGuidance", newResumes);
        
        // 本周沟通人数（本周发送或接收消息的不重复用户数）
        long weeklyContacts = messageRepository.countDistinctContactsThisWeek(hrId, weekStart);
        kpiData.put("newContacts", weeklyContacts);
        
        // 已完成的指导数量（HR的评论数量）
        Long completedGuidance = resumeCommentRepository.countByHrId(hrId);
        kpiData.put("completedGuidance", completedGuidance != null ? completedGuidance : 0);
        
        // 未读消息数量
        long unreadMessages = messageRepository.countByReceiverIdAndIsRead(hrId, 0);
        kpiData.put("unreadMessages", unreadMessages);
        
        return kpiData;
    }

    /**
     * 获取HR的详细统计数据
     */
    public Map<String, Object> getHrDetailedStats(Integer hrId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        Long totalComments = resumeCommentRepository.countByHrId(hrId);
        stats.put("totalComments", totalComments != null ? totalComments : 0);
        
        // 本月统计
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long monthlyMessages = messageRepository.countBySenderIdAndTimeAfter(hrId, monthStart);
        stats.put("monthlyMessages", monthlyMessages);
        
        // 今日统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayMessages = messageRepository.countBySenderIdAndTimeAfter(hrId, todayStart);
        stats.put("todayMessages", todayMessages);
        
        // 平台总体数据
        long totalResumes = resumeRepository.count();
        long totalSeekers = seekerInfoRepository.count();
        stats.put("totalResumes", totalResumes);
        stats.put("totalSeekers", totalSeekers);
        
        return stats;
    }

    /**
     * 获取简历评分分布
     */
    public Map<String, Object> getScoreDistribution(Integer hrId) {
        Map<String, Object> distribution = new HashMap<>();
        
        // 这里可以添加更复杂的统计查询
        // 暂时返回基础数据
        distribution.put("excellent", 0); // 9-10分
        distribution.put("good", 0);      // 7-8分
        distribution.put("average", 0);   // 5-6分
        distribution.put("poor", 0);      // 1-4分
        
        return distribution;
    }
}
