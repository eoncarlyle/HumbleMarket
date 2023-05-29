package com.iainschmitt.perdiction.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.MarketTransaction;

public interface TransactionRepository extends MongoRepository<MarketTransaction, String> {

}
