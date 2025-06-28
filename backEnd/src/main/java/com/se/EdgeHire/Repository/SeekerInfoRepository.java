package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.SeekerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeekerInfoRepository extends JpaRepository<SeekerInfo, Integer> {
    Optional<SeekerInfo> findByUserId(Integer userId);
}
