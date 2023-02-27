package com.iainschmitt.perdiction.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iainschmitt.perdiction.exceptions.NotFoundException;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        var value = Optional.ofNullable(userRepository.findByEmail(email));
        return value.orElseThrow(() -> new NotFoundException("User with email '%s' does not exist", email));
    }

    public User getUserById(String id) {
        var value = userRepository.findById(id);
        return value.orElseThrow(() -> new NotFoundException("User with email '%s' does not exist", id));
    }

    public Boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }
}
