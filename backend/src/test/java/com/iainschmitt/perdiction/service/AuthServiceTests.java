package com.iainschmitt.perdiction.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.validation.ValidationException;

import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.exceptions.NotAuthorizedException;
import com.iainschmitt.perdiction.model.rest.AuthData;
import com.iainschmitt.perdiction.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {
    @Autowired
    private ExternalisedConfiguration externalConfig;
    @Mock
    private UserService userService;
    @InjectMocks
    @Autowired
    private AuthService authService;

    @Test
    public void decodeJwt_SignatureVerificationSuccess() {
        var user = User.of("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(60));
        assertThat(authService.authenticateToken(jwsString, externalConfig.getKey())).isTrue();
    }

    @Test
    public void decodeJwt_SignatureVerificationFailureBadKey() {
        var user = User.of("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(60));
        var badKey = Keys.hmacShaKeyFor(
                "___354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370".getBytes(StandardCharsets.UTF_8));
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void decodeJwt_SignatureVerificationFailureExpired() {
        var user = User.of("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(-10));
        var badKey = Keys.hmacShaKeyFor(
                "___354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370".getBytes(StandardCharsets.UTF_8));
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void userAccountCreation_Success() {
        var user = User.of("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        when(userService.exists(user.getEmail()))
            .thenReturn(false);

        // Anoynmous class done here because @Builder conflicts with @ResponseBody
        // parsing
        assertThat(userService.exists(user.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(AuthData.of(user.getEmail(), user.getPasswordHash())));
    }

    @Test
    public void userAccountCreation_DuplicationFailure() {
        var user = User.of("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        
        when(userService.exists(user.getEmail()))
            .thenReturn(false);
        
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(AuthData.of(user.getEmail(), user.getPasswordHash())));
        
        when(userService.exists(user.getEmail()))
            .thenReturn(true);
        assertThatThrownBy(() -> authService.createUserAccount(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(sha256Hex("!A_Different_Minimal_Password_Really"));
            }
        })).isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    public void userAccountCreation_EmailValidationFailure() {
        var user = User.of("NotAnEmail");
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
        var user = User.of("user1@iainschmitt.com");
        user.setPasswordHash("!pass");

        assertThatThrownBy(() -> authService.createUserAccount(AuthData.of(user.getEmail(), user.getPasswordHash()))).isInstanceOf(ValidationException.class);
    }

    @Test
    public void logInUserAccount_Success() {
        var user = User.of("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        
        when(userService.exists(user.getEmail())).thenReturn(true);
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        assertThatNoException().isThrownBy(() -> authService.logInUserAccount(AuthData.of(user.getEmail(), user.getPasswordHash())));
    }

    @Test
    public void logInUserAccount_UserDoesNotExist() {
        var user = User.of("user1@iainschmitt.com");
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        when(userService.exists(user.getEmail())).thenReturn(false);
        assertThatThrownBy(() -> authService.logInUserAccount(AuthData.of(user.getEmail(), user.getPasswordHash()))).isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    public void getClaims_Success() {
        var user = User.of("user3@iainschmitt.com");
        var jwsString = authService.createToken(user, Duration.ofSeconds(60));
        assertThat(authService.authenticateToken(jwsString, externalConfig.getKey())).isTrue();
        assertThat(authService.getClaim(jwsString, "email")).isEqualTo(user.getEmail());
    }
}
