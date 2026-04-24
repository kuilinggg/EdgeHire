package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.OfferAgentKnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferAgentKnowledgeDocRepository extends JpaRepository<OfferAgentKnowledgeDoc, Long> {
}
