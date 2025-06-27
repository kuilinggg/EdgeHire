package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.HrInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrInfoRepository extends JpaRepository<HrInfo, Integer> {
    Optional<HrInfo> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
}
