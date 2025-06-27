package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    List<Resume> findByUserId(Integer userId);

    // 统计指定时间后创建的简历数量
    @Query("SELECT COUNT(r) FROM Resume r WHERE r.createTime >= :startTime")
    long countByCreateTimeAfter(@Param("startTime") LocalDateTime startTime);
}
