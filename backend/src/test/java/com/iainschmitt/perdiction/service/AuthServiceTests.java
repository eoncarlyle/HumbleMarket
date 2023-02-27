package com.iainschmitt.perdiction.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import com.iainschmitt.perdiction.model.rest.MarketData;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.iainschmitt.perdiction.exceptions.NotAuthorizedException;
import com.iainschmitt.perdiction.model.rest.AuthData;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    public void decodeJwt_SignatureVerificationSuccess() {
        var user = new User("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, 60L);
        assertThat(authService.authenticateToken(jwsString, AuthService.KEY)).isTrue();
    }

    @Test
    public void decodeJwt_SignatureVerificationFailureBadKey() {
        var user = new User("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, 60L);
        var badKey = Keys.hmacShaKeyFor(
                "___354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370".getBytes(StandardCharsets.UTF_8));
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void decodeJwt_SignatureVerificationFailureExpired() {
        var user = new User("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, -10L);
        var badKey = Keys.hmacShaKeyFor(
                "___354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370".getBytes(StandardCharsets.UTF_8));
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void userAccountCreation_Success() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!A_Minimal_Password_Really");

        // Anoynmous class done here because @Builder conflicts with @ResponseBody
        // parsing
        assertThat(userService.exists(user.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }
        }));
        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    public void userAccountCreation_DuplicationFailure() {
        var user1 = new User("user1@iainschmitt.com");
        user1.setPassword("!A_Minimal_Password_Really");
        assertThat(userService.exists(user1.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user1.getEmail());
                setPassword(user1.getPassword());
            }
        }));
        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user1.getEmail());
                setPassword("!A_Different_Minimal_Password_Really");
            }
        })).isInstanceOf(NotAuthorizedException.class);
        assertThat(userService.exists(user1.getEmail())).isTrue();
    }

    @Test
    public void userAccountCreation_EmailValidationFailure() {
        var user = new User("NotAnEmail");
        user.setPassword("!A_Minimal_Password_Really");

        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }
        })).isInstanceOf(ValidationException.class);
    }

    @Test
    public void userAccountCreation_PasswordValidationFailure() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!pass");

        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }
        })).isInstanceOf(ValidationException.class);
    }

    @Test
    public void logInUserAccount_Success() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!A_Minimal_Password_Really");
        userService.saveUser(user);

        assertThatNoException().isThrownBy(() -> authService.logInUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }
        }));
        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    public void logInUserAccount_UserDoesntExist() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!A_Minimal_Password_Really");

        assertThatThrownBy(() -> authService.logInUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }
        })).isInstanceOf(NotAuthorizedException.class);
    }
}
