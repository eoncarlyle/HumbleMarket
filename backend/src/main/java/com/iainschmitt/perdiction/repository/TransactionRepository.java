package com.iainschmitt.prediction.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.iainschmitt.prediction.model.MarketTransaction;

public interface TransactionRepository extends MongoRepository<MarketTransaction, String> {

}
