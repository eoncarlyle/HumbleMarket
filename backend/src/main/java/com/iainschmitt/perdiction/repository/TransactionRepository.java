package com.iainschmitt.perdiction.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.iainschmitt.perdiction.model.MarketTransaction;

public interface TransactionRepository extends MongoRepository<MarketTransaction, String> {

}
