package com.iainschmitt.perdiction;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void deleteAll(){
        userRepository.deleteAll();
    }

    public void createUser(User user){
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        var value = Optional.ofNullable(userRepository.findByEmail(email));
        return value
            .orElseThrow(() -> new NotFoundException("User with email '%s' does not exist", email));
    }

    public Boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }
}
