package com.iainschmitt.perdiction.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.iainschmitt.perdiction.model.WhitelistEmail;

@Repository
public interface WhitelistEmailRepository extends MongoRepository<WhitelistEmail, String> {
    public Boolean existsByEmailAddress(String emailAddress);
}
