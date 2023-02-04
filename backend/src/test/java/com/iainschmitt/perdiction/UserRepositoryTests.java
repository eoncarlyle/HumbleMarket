package com.iainschmitt.perdiction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserRepositoryTests {

    @Autowired
    private UserService userService;

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    void createUserDocument_Success() {
        var users = new User[] {
            new User("user1@iainschmitt.com"),
            new User("user2@iainschmitt.com"),
        };

        User storedUser;
        for (final User user: users) {
            userService.createUser(user);
            storedUser = userService.getUserByEmail(user.getEmail());
            assertThat(storedUser.getEmail()).isEqualTo(user.getEmail());
        }
    }

    @Test
    void fetchUser_UserNotFound() {
        var messageTemplate = "User with email '%s' does not exist";
        var userEmail = "uncreatedUser@iainschmitt.com";
        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> userService.getUserByEmail(userEmail))
            .withMessageContaining(String.format(messageTemplate, userEmail));
    }
}
