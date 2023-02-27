package com.iainschmitt.perdiction.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.error.Mark;

import com.iainschmitt.perdiction.model.Market;

@Component
@Repository
public interface MarketRepository extends MongoRepository<Market, String> {
    Boolean existsByQuestion(String question);

    Market findByQuestion(String question);
}
