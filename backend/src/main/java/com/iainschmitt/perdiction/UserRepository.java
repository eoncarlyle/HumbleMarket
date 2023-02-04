package com.iainschmitt.perdiction;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByEmail(String email);

    Boolean existsByEmail(String email);
}
