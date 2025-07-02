package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.GuidanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuidanceRequestRepository extends JpaRepository<GuidanceRequest, Long> {

    /**
     * 根据用户ID查询指导申请记录
     */
    List<GuidanceRequest> findByUserIdAndDeletedAtIsNullOrderByRequestTimeDesc(Long userId);

    /**
     * 根据HR用户ID查询指导申请记录
     */
    List<GuidanceRequest> findByHrUserIdAndDeletedAtIsNullOrderByRequestTimeDesc(Long hrUserId);

    /**
     * 根据状态查询指导申请记录
     */
    List<GuidanceRequest> findByStatusAndDeletedAtIsNullOrderByRequestTimeDesc(String status);

    /**
     * 查询待分配的指导申请
     */
    @Query("SELECT gr FROM GuidanceRequest gr WHERE gr.status = 'pending' AND gr.deletedAt IS NULL ORDER BY gr.requestTime ASC")
    List<GuidanceRequest> findPendingRequests();

    /**
     * 统计用户当月申请次数
     */
    @Query("SELECT COUNT(gr) FROM GuidanceRequest gr WHERE gr.userId = :userId AND gr.requestTime >= :startTime AND gr.requestTime <= :endTime AND gr.deletedAt IS NULL")
    long countByUserIdAndRequestTimeBetween(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据ID查询单个记录
     */
    Optional<GuidanceRequest> findById(Long id);
}
