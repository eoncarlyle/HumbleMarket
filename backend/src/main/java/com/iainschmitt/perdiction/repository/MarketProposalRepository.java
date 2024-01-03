package com.iainschmitt.prediction.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.prediction.model.MarketProposal;

@Component
@Repository
public interface MarketProposalRepository extends MongoRepository<MarketProposal, String> {
    Boolean existsByQuestion(String question);
}
