package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.GuidanceRequest;
import java.util.List;

/**
 * 求职指导服务接口
 */
public interface GuidanceRequestService {

    /**
     * 提交求职指导申请
     */
    GuidanceRequest submitRequest(GuidanceRequest request);

    /**
     * 根据用户ID查询历史记录
     */
    List<GuidanceRequest> getHistoryByUserId(Long userId);

    /**
     * 根据HR用户ID查询分配的指导记录
     */
    List<GuidanceRequest> getRequestsByHrUserId(Long hrUserId);

    /**
     * 查询待分配的指导申请
     */
    List<GuidanceRequest> getPendingRequests();

    /**
     * 为申请分配HR
     */
    GuidanceRequest assignHR(Long requestId, Long hrUserId);

    /**
     * HR提交指导反馈
     */
    GuidanceRequest submitFeedback(Long requestId, String feedback);

    /**
     * 用户评价指导服务
     */
    GuidanceRequest submitRating(Long requestId, Integer rating, String comment);

    /**
     * 取消申请
     */
    GuidanceRequest cancelRequest(Long requestId, Long userId);

    /**
     * 根据ID查询详情
     */
    GuidanceRequest getById(Long id);

    /**
     * 检查用户当月申请次数
     */
    boolean canSubmitRequest(Long userId);
}
