package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.OfferAgentRetrievalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferAgentRetrievalLogRepository extends JpaRepository<OfferAgentRetrievalLog, Long> {
}
