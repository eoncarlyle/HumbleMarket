package com.iainschmitt.perdiction.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.perdiction.model.MarketProposal;
import com.iainschmitt.perdiction.model.rest.MarketProposalData;

@Component
@Repository
public interface MarketProposalRepository extends MongoRepository<MarketProposal, String> {
    Boolean existsByQuestion(String question);
}
