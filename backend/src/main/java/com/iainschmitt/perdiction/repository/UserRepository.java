package com.iainschmitt.prediction.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.prediction.model.User;

@Component
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);

    Boolean existsByEmail(String email);
}
