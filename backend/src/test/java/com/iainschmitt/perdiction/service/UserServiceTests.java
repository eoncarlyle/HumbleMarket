package com.iainschmitt.perdiction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.iainschmitt.perdiction.exceptions.NotFoundException;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private MarketRepository marketRepository;
    @Autowired
    @InjectMocks
    private UserService userService;

    @Test
    void createUserDocument_Success() {
        var users = new User[] { User.of("user1@iainschmitt.com"), User.of("user2@iainschmitt.com"), };
        User storedUser;

        for (final User user : users) {
            when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
            storedUser = userService.getUserByEmail(user.getEmail());
            assertThat(storedUser.getEmail()).isEqualTo(user.getEmail());
        }
    }

    @Test
    void fetchUser_UserNotFound() {
        var messageTemplate = "User with email '%s' does not exist";
        var userEmail = "uncreatedUser@iainschmitt.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(null);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userService.getUserByEmail(userEmail))
                .withMessageContaining(String.format(messageTemplate, userEmail));
    }
}
