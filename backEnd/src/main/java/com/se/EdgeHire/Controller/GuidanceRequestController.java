package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.GuidanceRequest;
import com.se.EdgeHire.Service.GuidanceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 求职指导控制器
 */
@RestController
@RequestMapping("/api/guidance")
@CrossOrigin
public class GuidanceRequestController {

    @Autowired
    private GuidanceRequestService guidanceRequestService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 提交求职指导申请
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitRequest(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 构造申请对象
            GuidanceRequest request = new GuidanceRequest();
            request.setUserId(Long.valueOf(requestData.get("userId").toString()));
            request.setTargetPosition((String) requestData.get("targetPosition"));
            request.setGuidanceType((String) requestData.get("guidanceType"));
            request.setPhone((String) requestData.get("phone"));
            request.setEmail((String) requestData.get("email"));
            request.setDetailedDescription((String) requestData.get("detailedDescription"));
            request.setExperience((String) requestData.get("experience"));
            request.setEducation((String) requestData.get("education"));
            
            // 处理技能列表
            if (requestData.get("skills") != null) {
                List<String> skills = (List<String>) requestData.get("skills");
                request.setSkillsJson(objectMapper.writeValueAsString(skills));
            }

            GuidanceRequest saved = guidanceRequestService.submitRequest(request);
            
            response.put("success", true);
            response.put("message", "申请提交成功");
            response.put("data", saved);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户历史记录
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<GuidanceRequest> history = guidanceRequestService.getHistoryByUserId(userId);
            response.put("success", true);
            response.put("data", history);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * HR获取分配的指导记录
     */
    @GetMapping("/hr/{hrUserId}")
    public ResponseEntity<Map<String, Object>> getHrRequests(@PathVariable Long hrUserId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<GuidanceRequest> requests = guidanceRequestService.getRequestsByHrUserId(hrUserId);
            response.put("success", true);
            response.put("data", requests);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取待分配的申请列表
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingRequests() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<GuidanceRequest> requests = guidanceRequestService.getPendingRequests();
            response.put("success", true);
            response.put("data", requests);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 分配HR
     */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignHR(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long requestId = Long.valueOf(data.get("requestId").toString());
            Long hrUserId = Long.valueOf(data.get("hrUserId").toString());
            
            GuidanceRequest updated = guidanceRequestService.assignHR(requestId, hrUserId);
            
            response.put("success", true);
            response.put("message", "HR分配成功");
            response.put("data", updated);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * HR提交指导反馈
     */
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long requestId = Long.valueOf(data.get("requestId").toString());
            String feedback = (String) data.get("feedback");
            
            GuidanceRequest updated = guidanceRequestService.submitFeedback(requestId, feedback);
            
            response.put("success", true);
            response.put("message", "反馈提交成功");
            response.put("data", updated);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 用户评价指导服务
     */
    @PostMapping("/rating")
    public ResponseEntity<Map<String, Object>> submitRating(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long requestId = Long.valueOf(data.get("requestId").toString());
            Integer rating = Integer.valueOf(data.get("rating").toString());
            String comment = (String) data.get("comment");
            
            GuidanceRequest updated = guidanceRequestService.submitRating(requestId, rating, comment);
            
            response.put("success", true);
            response.put("message", "评价提交成功");
            response.put("data", updated);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 取消申请
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelRequest(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long requestId = Long.valueOf(data.get("requestId").toString());
            Long userId = Long.valueOf(data.get("userId").toString());
            
            GuidanceRequest updated = guidanceRequestService.cancelRequest(requestId, userId);
            
            response.put("success", true);
            response.put("message", "申请已取消");
            response.put("data", updated);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取申请详情
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            GuidanceRequest request = guidanceRequestService.getById(id);
            response.put("success", true);
            response.put("data", request);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户是否可以提交申请
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Map<String, Object>> checkCanSubmit(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean canSubmit = guidanceRequestService.canSubmitRequest(userId);
            response.put("success", true);
            response.put("canSubmit", canSubmit);
            response.put("message", "高级会员可以提交申请");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
