package com.iainschmitt.perdiction;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthServiceTest {

    @Test
    public void test() {
        var user = new User("user1", "user1@iainschmitt.com");
        var jwsString = AuthService.createToken(user, 60L);
        assertThat(AuthService.authenticateToken(jwsString, AuthService.KEY)).isTrue();
    }
    @Test
    public void test1() {
        var user = new User("user1", "user1@iainschmitt.com");
        var jwsString = AuthService.createToken(user, 60L);
        var badKey = Keys.hmacShaKeyFor(
            "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
                .getBytes(StandardCharsets.UTF_8)
        );
        assertThat(AuthService.authenticateToken(jwsString, badKey)).isFalse();
    }


}
