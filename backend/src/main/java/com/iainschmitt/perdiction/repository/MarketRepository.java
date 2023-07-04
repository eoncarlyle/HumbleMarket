package com.iainschmitt.perdiction.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.perdiction.model.Market;

@Component
@Repository
public interface MarketRepository extends MongoRepository<Market, String> {
    Boolean existsByQuestion(String question);

    //! This relies on unique questions for each market, so this needs to be enforced and tested 
    Market findByQuestion(String question);

    Market findBySeqId(int seqId);

    List<Market> findByIsClosedAndIsResolved(boolean isClosed, boolean isResolved);

    List<Market> findByIsClosedFalseAndCloseDateLessThan(long closeDate);

}
