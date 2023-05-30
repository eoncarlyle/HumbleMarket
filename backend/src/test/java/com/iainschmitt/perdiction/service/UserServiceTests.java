package com.iainschmitt.perdiction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.iainschmitt.perdiction.exceptions.NotFoundException;
import com.iainschmitt.perdiction.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    void createUserDocument_Success() {
        var users = new User[] { new User("user1@iainschmitt.com"), new User("user2@iainschmitt.com"), };
        User storedUser;

        for (final User user : users) {
            userService.saveUser(user);
            storedUser = userService.getUserByEmail(user.getEmail());
            assertThat(storedUser.getEmail()).isEqualTo(user.getEmail());
        }
    }

    @Test
    void fetchUser_UserNotFound() {
        var messageTemplate = "User with email '%s' does not exist";
        var userEmail = "uncreatedUser@iainschmitt.com";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userService.getUserByEmail(userEmail))
                .withMessageContaining(String.format(messageTemplate, userEmail));
    }
}
