package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.OfferAgentToolCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferAgentToolCallLogRepository extends JpaRepository<OfferAgentToolCallLog, Long> {
    List<OfferAgentToolCallLog> findTop50ByConversationIdOrderByCreatedAtDesc(String conversationId);
}
