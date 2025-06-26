package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.Info;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfoRepository extends JpaRepository<Info, Integer> {
    List<Info> findByUserId(Integer userId);
}