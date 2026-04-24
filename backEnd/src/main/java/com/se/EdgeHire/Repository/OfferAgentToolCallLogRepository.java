package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.OfferAgentToolCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferAgentToolCallLogRepository extends JpaRepository<OfferAgentToolCallLog, Long> {
}
