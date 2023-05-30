package com.iainschmitt.perdiction.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.perdiction.model.Market;

@Component
@Repository
public interface MarketRepository extends MongoRepository<Market, String> {
    Boolean existsByQuestion(String question);

    Market findByQuestion(String question);

    Market findBySeqId(int seqId);
}
