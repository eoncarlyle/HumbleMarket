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

    public User getUserByUserName(String userName) {
        var value = Optional.ofNullable(userRepository.findByUserName(userName));
        return value
            .orElseThrow(() -> new NotFoundException("User with name '%s' does not exist", userName));
    }

}
