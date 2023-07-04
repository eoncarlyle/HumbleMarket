package com.iainschmitt.perdiction.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.jsonwebtoken.security.Keys;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.exceptions.NotAuthorizedException;
import com.iainschmitt.perdiction.model.rest.AuthData;
import com.iainschmitt.perdiction.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@SpringBootTest
public class AuthServiceTests {
    @Autowired
    private ExternalisedConfiguration externalConfig;

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
        var jwsString = authService.createToken(user, Duration.ofSeconds(60));
        assertThat(authService.authenticateToken(jwsString, externalConfig.getKey())).isTrue();
    }

    @Test
    public void decodeJwt_SignatureVerificationFailureBadKey() {
        var user = new User("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(60));
        var badKey = Keys.hmacShaKeyFor(
                "___354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370".getBytes(StandardCharsets.UTF_8));
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void decodeJwt_SignatureVerificationFailureExpired() {
        var user = new User("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(-10));
        var badKey = Keys.hmacShaKeyFor(
                "___354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370".getBytes(StandardCharsets.UTF_8));
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void userAccountCreation_Success() {
        var user = new User("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));

        // Anoynmous class done here because @Builder conflicts with @ResponseBody
        // parsing
        assertThat(userService.exists(user.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }));
        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    public void userAccountCreation_DuplicationFailure() {
        var user1 = new User("user1@iainschmitt.com");
        user1.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        assertThat(userService.exists(user1.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user1.getEmail());
                setPasswordHash(user1.getPasswordHash());
            }
        }));
        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user1.getEmail());
                setPasswordHash(sha256Hex("!A_Different_Minimal_Password_Really"));
            }
        })).isInstanceOf(NotAuthorizedException.class);
        assertThat(userService.exists(user1.getEmail())).isTrue();
    }

    @Test
    public void userAccountCreation_EmailValidationFailure() {
        var user = new User("NotAnEmail");
        user.setPasswordHash("!A_Minimal_Password_Really");

        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        })).isInstanceOf(ValidationException.class);
    }

    @Test
    public void userAccountCreation_PasswordValidationFailure() {
        var user = new User("user1@iainschmitt.com");
        user.setPasswordHash("!pass");

        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        })).isInstanceOf(ValidationException.class);
    }

    @Test
    public void logInUserAccount_Success() {
        var user = new User("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        userService.saveUser(user);

        assertThatNoException().isThrownBy(() -> authService.logInUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }));
        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    public void logInUserAccount_UserDoesntExist() {
        var user = new User("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));

        assertThatThrownBy(() -> authService.logInUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        })).isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    public void getClaims_Success() {
        var user = new User("user3@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(60));
        assertThat(authService.authenticateToken(jwsString, externalConfig.getKey())).isTrue();
        assertThat(authService.getClaim(jwsString, "email")).isEqualTo(user.getEmail());
    }
}
