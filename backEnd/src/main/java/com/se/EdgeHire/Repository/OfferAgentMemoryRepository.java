package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.OfferAgentMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfferAgentMemoryRepository extends JpaRepository<OfferAgentMemory, Long> {
    Optional<OfferAgentMemory> findByUserId(Integer userId);
}
