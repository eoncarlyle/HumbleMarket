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
public class UserRepositoryTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    void createUserDocument_Success() {
        var users = new User[] {
            new User("user1", "user1@iainschmitt.com"),
            new User("user2", "user2@iainschmitt.com"),
        };

        User storedUser;
        for (final User user: users) {
            userService.createUser(user);
            storedUser = userService.getUserByUserName(user.getUserName());
            assertThat(storedUser.getUserName()).isEqualTo(user.getUserName());
            assertThat(storedUser.getEmail()).isEqualTo(user.getEmail());
        }
    }

    @Test
    void fetchUser_UserNotFound() {
        var messageTemplate = "User with name '%s' does not exist";
        var userName = "uncreatedUser";
        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> userService.getUserByUserName(userName))
            .withMessageContaining(String.format(messageTemplate, userName));
    }
}
