package com.iainschmitt.perdiction;

import java.nio.charset.StandardCharsets;


import io.jsonwebtoken.security.Keys;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
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
    public void decodeJwt_SignatureVerificationFailure() {
        var user = new User("user1@iainschmitt.com");
        var jwsString = authService.createToken(user, 60L);
        var badKey = Keys.hmacShaKeyFor(
            "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
                .getBytes(StandardCharsets.UTF_8)
        );
        assertThat(authService.authenticateToken(jwsString, badKey)).isFalse();
    }

    @Test
    public void userAccountCreation_Success() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!A_Minimal_Password_Really");

        // Anoynmous class done here because @Builder conflicts with @ResponseBody parsing
        assertThat(userService.exists(user.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(
            new SignUpData(){{
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }}
        ));
        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    public void userAccountCreation_DuplicationFailure() {
        var user1 = new User("user1@iainschmitt.com");
        user1.setPassword("!A_Minimal_Password_Really");
        assertThat(userService.exists(user1.getEmail())).isFalse();
        assertThatNoException().isThrownBy(() -> authService.createUserAccount(
            new SignUpData(){{
                setEmail(user1.getEmail());
                setPassword(user1.getPassword());
            }}
        ));
        assertThatThrownBy(() ->  authService.createUserAccount(
            new SignUpData(){{
                setEmail(user1.getEmail());
                setPassword("!A_Different_Minimal_Password_Really");
            }}
        )).isInstanceOf(IllegalArgumentException.class);
        assertThat(userService.exists(user1.getEmail())).isTrue();
    }

    @Test
    public void userAccountCreation_EmailValidationFailure() {
        var user = new User("NotAnEmail");
        user.setPassword("!A_Minimal_Password_Really");

        assertThatThrownBy(() -> authService.createUserAccount(
            new SignUpData(){{
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }}
        )).isInstanceOf(ValidationException.class);
    }

    @Test
    public void userAccountCreation_PasswordValidationFailure() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!pass");

        assertThatThrownBy(() -> authService.createUserAccount(
            new SignUpData(){{
                setEmail(user.getEmail());
                setPassword(user.getPassword());
            }}
        )).isInstanceOf(ValidationException.class);
    }
}
