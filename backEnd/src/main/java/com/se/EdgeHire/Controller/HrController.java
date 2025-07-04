package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.HrInfo;
import com.se.EdgeHire.Repository.MessageRepository;
import com.se.EdgeHire.Repository.GuidanceRequestRepository;
import com.se.EdgeHire.Service.HrService;
import com.se.EdgeHire.Service.MessageService;
import com.se.EdgeHire.WebSocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class HrController {
    
    @Autowired
    private HrService hrService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private GuidanceRequestRepository guidanceRequestRepository;

    /**
     * 处理预检请求
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    /**
     * 根据用户ID获取HR信息
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<HrInfo> getHrInfoByUserId(@PathVariable Integer userId) {
        Optional<HrInfo> hrInfo = hrService.getHrInfoByUserId(userId);
        return hrInfo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建HR信息
     */
    @PostMapping("/info")
    public ResponseEntity<?> createHrInfo(@RequestBody HrInfo hrInfo) {
        try {
            HrInfo created = hrService.createHrInfo(hrInfo);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新HR信息
     */
    @PutMapping("/info/{id}")
    public ResponseEntity<?> updateHrInfo(@PathVariable Integer id, @RequestBody HrInfo hrInfo) {
        try {
            HrInfo updated = hrService.updateHrInfo(id, hrInfo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据用户ID更新HR信息
     */
    @PutMapping("/info/user/{userId}")
    public ResponseEntity<?> updateHrInfoByUserId(@PathVariable Integer userId, @RequestBody HrInfo hrInfo) {
        try {
            System.out.println("更新HR信息 - userId: " + userId + ", hrInfo: " + hrInfo);
            HrInfo updated = hrService.updateHrInfoByUserId(userId, hrInfo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            System.err.println("更新HR信息失败 - IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("更新HR信息失败 - 未知异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 验证用户是否为HR
     */
    @GetMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Boolean>> verifyHr(@PathVariable Integer userId) {
        boolean isHr = hrService.isHr(userId);
        return ResponseEntity.ok(Map.of("isHr", isHr));
    }

    /**
     * HR发起沟通
     * @param hrUserId HR用户ID
     * @param targetUserId 目标求职者用户ID
     * @param customMessage 自定义消息内容（可选）
     * @return 响应结果
     */
    @PostMapping("/initiate-chat/{hrUserId}/{targetUserId}")
    public ResponseEntity<?> initiateChat(
            @PathVariable Integer hrUserId,
            @PathVariable Integer targetUserId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            // 验证用户是否为HR
            if (!hrService.isHr(hrUserId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户不是HR角色"));
            }

            // 构造消息内容
            String greetingMessage;
            if (requestBody != null && requestBody.containsKey("customMessage") &&
                requestBody.get("customMessage") != null && !requestBody.get("customMessage").trim().isEmpty()) {
                // 使用自定义消息
                greetingMessage = requestBody.get("customMessage");
            } else {
                // 使用默认问候消息
                Optional<HrInfo> hrInfoOpt = hrService.getHrInfoByUserId(hrUserId);
                String companyName = "未知公司";
                if (hrInfoOpt.isPresent() && hrInfoOpt.get().getCompany() != null && !hrInfoOpt.get().getCompany().trim().isEmpty()) {
                    companyName = hrInfoOpt.get().getCompany();
                }
                greetingMessage = "你好，我是" + companyName + "的HR，对你的简历很感兴趣，希望能和你聊一聊。";
            }

            // 通过WebSocket发送消息
            WebSocketHandler webSocketHandler = WebSocketHandler.getInstance();
            if (webSocketHandler != null) {
                webSocketHandler.sendMessageToUser(hrUserId, targetUserId, greetingMessage);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "沟通已发起",
                    "greetingMessage", greetingMessage
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "WebSocket服务不可用"));
            }

        } catch (Exception e) {
            System.err.println("发起沟通失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "发起沟通失败: " + e.getMessage()));
        }
    }

    /**
     * 获取HR的KPI数据
     * @param userId HR用户ID
     * @return KPI数据
     */
    @GetMapping("/kpi/{userId}")
    public ResponseEntity<?> getKpiData(@PathVariable Integer userId) {
        try {
            // 验证用户是否为HR
            if (!hrService.isHr(userId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户不是HR角色"));
            }

            Map<String, Object> kpiData = new HashMap<>();

            // 1. 统计本周联系的不重复用户数
            LocalDateTime weekStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(6);
            long weeklyContacts = messageRepository.countDistinctContactsThisWeek(userId, weekStart);
            kpiData.put("newContacts", weeklyContacts);

            // 2. 统计未读消息数量
            long unreadMessages = messageRepository.countByReceiverIdAndIsRead(userId, 0);
            kpiData.put("unreadMessages", unreadMessages);

            // 3. 统计新的指导请求数量（待分配的）
            long newGuidanceRequests = guidanceRequestRepository.findPendingRequests().size();
            kpiData.put("newGuidance", newGuidanceRequests);

            // 4. 统计本周完成的指导数量
            LocalDateTime weekStartForGuidance = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(6);
            long completedGuidance = guidanceRequestRepository.countByHrUserIdAndStatusAndCompletedTimeBetween(
                Long.valueOf(userId), "completed", weekStartForGuidance, LocalDateTime.now());
            kpiData.put("completedGuidance", completedGuidance);

            return ResponseEntity.ok(kpiData);

        } catch (Exception e) {
            System.err.println("获取KPI数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "获取KPI数据失败: " + e.getMessage()));
        }
    }
}
