package com.se.EdgeHire.Service.impl;

import com.se.EdgeHire.Entity.GuidanceRequest;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.GuidanceRequestRepository;
import com.se.EdgeHire.Repository.UserRepository;
import com.se.EdgeHire.Service.GuidanceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 求职指导服务实现类
 */
@Service
public class GuidanceRequestServiceImpl implements GuidanceRequestService {

    @Autowired
    private GuidanceRequestRepository guidanceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public GuidanceRequest submitRequest(GuidanceRequest request) {
        // 检查是否超过月申请限制
        if (!canSubmitRequest(request.getUserId())) {
            throw new RuntimeException("您当月的申请次数已达上限，请下月再试");
        }

        // 设置默认状态
        request.setStatus("pending");
        request.setRequestTime(LocalDateTime.now());
        
        // 保存申请
        GuidanceRequest saved = guidanceRequestRepository.save(request);
        
        // 填充用户信息
        fillUserInfo(saved);
        
        return saved;
    }

    @Override
    public List<GuidanceRequest> getHistoryByUserId(Long userId) {
        List<GuidanceRequest> requests = guidanceRequestRepository
                .findByUserIdAndDeletedAtIsNullOrderByRequestTimeDesc(userId);
        
        // 填充用户和HR信息
        requests.forEach(this::fillAllInfo);
        
        return requests;
    }

    @Override
    public List<GuidanceRequest> getRequestsByHrUserId(Long hrUserId) {
        List<GuidanceRequest> requests = guidanceRequestRepository
                .findByHrUserIdAndDeletedAtIsNullOrderByRequestTimeDesc(hrUserId);
        
        // 填充用户和HR信息
        requests.forEach(this::fillAllInfo);
        
        return requests;
    }

    @Override
    public List<GuidanceRequest> getPendingRequests() {
        List<GuidanceRequest> requests = guidanceRequestRepository.findPendingRequests();
        
        // 填充用户信息
        requests.forEach(this::fillUserInfo);
        
        return requests;
    }

    @Override
    public GuidanceRequest assignHR(Long requestId, Long hrUserId) {
        Optional<GuidanceRequest> optionalRequest = guidanceRequestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }

        GuidanceRequest request = optionalRequest.get();
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该申请已被处理，无法重新分配");
        }

        // 验证HR用户是否存在
        Optional<User> hrUser = userRepository.findById(hrUserId.intValue());
        if (hrUser.isEmpty()) {
            throw new RuntimeException("HR用户不存在");
        }

        // 分配HR
        request.setHrUserId(hrUserId);
        request.setStatus("processing");
        request.setAssignedTime(LocalDateTime.now());

        GuidanceRequest saved = guidanceRequestRepository.save(request);
        fillAllInfo(saved);
        
        return saved;
    }

    @Override
    public GuidanceRequest submitFeedback(Long requestId, String feedback) {
        Optional<GuidanceRequest> optionalRequest = guidanceRequestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }

        GuidanceRequest request = optionalRequest.get();
        if (!"processing".equals(request.getStatus())) {
            throw new RuntimeException("该申请不在指导中状态");
        }

        // 提交反馈
        request.setFeedback(feedback);
        request.setStatus("completed");
        request.setCompletedTime(LocalDateTime.now());

        GuidanceRequest saved = guidanceRequestRepository.save(request);
        fillAllInfo(saved);
        
        return saved;
    }

    @Override
    public GuidanceRequest submitRating(Long requestId, Integer rating, String comment) {
        Optional<GuidanceRequest> optionalRequest = guidanceRequestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }

        GuidanceRequest request = optionalRequest.get();
        if (!"completed".equals(request.getStatus())) {
            throw new RuntimeException("该申请尚未完成，无法评价");
        }

        // 提交评价
        request.setRating(rating);
        request.setUserComment(comment);

        GuidanceRequest saved = guidanceRequestRepository.save(request);
        fillAllInfo(saved);
        
        return saved;
    }

    @Override
    public GuidanceRequest cancelRequest(Long requestId, Long userId) {
        Optional<GuidanceRequest> optionalRequest = guidanceRequestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }

        GuidanceRequest request = optionalRequest.get();
        if (!request.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该申请");
        }

        if ("completed".equals(request.getStatus()) || "cancelled".equals(request.getStatus())) {
            throw new RuntimeException("该申请已完成或已取消，无法重复操作");
        }

        // 取消申请
        request.setStatus("cancelled");

        GuidanceRequest saved = guidanceRequestRepository.save(request);
        fillAllInfo(saved);
        
        return saved;
    }

    @Override
    public GuidanceRequest getById(Long id) {
        Optional<GuidanceRequest> optionalRequest = guidanceRequestRepository.findById(id);
        if (optionalRequest.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }

        GuidanceRequest request = optionalRequest.get();
        fillAllInfo(request);
        
        return request;
    }

    @Override
    public boolean canSubmitRequest(Long userId) {
        // 高级会员没有次数限制，直接返回true
        return true;
    }

    /**
     * 填充用户信息
     */
    private void fillUserInfo(GuidanceRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId().intValue());
        if (user.isPresent()) {
            request.setUserName(user.get().getUsername());
            // 可以从Info表获取头像等信息
        }

        // 解析技能JSON
        if (request.getSkillsJson() != null && !request.getSkillsJson().isEmpty()) {
            try {
                List<String> skills = objectMapper.readValue(
                        request.getSkillsJson(), new TypeReference<List<String>>() {});
                request.setSkills(skills);
            } catch (JsonProcessingException e) {
                // 解析失败时设置空列表
                request.setSkills(List.of());
            }
        }
    }

    /**
     * 填充HR信息
     */
    private void fillHrInfo(GuidanceRequest request) {
        if (request.getHrUserId() != null) {
            Optional<User> hrUser = userRepository.findById(request.getHrUserId().intValue());
            if (hrUser.isPresent()) {
                request.setHrName(hrUser.get().getUsername());
                // 可以从Info表获取头像等信息
            }
        }
    }

    /**
     * 填充所有信息
     */
    private void fillAllInfo(GuidanceRequest request) {
        fillUserInfo(request);
        fillHrInfo(request);
    }
}
