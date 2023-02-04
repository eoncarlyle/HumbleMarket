package com.iainschmitt.perdiction;
import java.util.Date;
import java.time.Instant;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class AuthService {
    //TODO: Remove before putting this into production!
    public static String secret = "e1e354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370";
    public static Key KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    @Autowired
    private UserService userService;

    public String createToken(User user, Long secondsUntilExpiration) {

        return Jwts.builder()
            .setExpiration(
                Date.from(Instant.now().plusSeconds(secondsUntilExpiration))
            )
            .claim("email", user.getEmail())
            .signWith(KEY)
            .compact();
    }

    public boolean authenticateToken(String jwsString, Key key) {
        try {
            var jws = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwsString);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public SignUpReturnData createUserAccount(SignUpData signUpData) {
        validateSignUpData(signUpData);
        if (userService.exists(signUpData.getEmail())) {
            throw new IllegalArgumentException(String.format(
                "User with email '%s' already exists",
                signUpData.getEmail()
            ));
        }
        var newUser = new User(signUpData.getEmail());
        newUser.setPassword(signUpData.getPassword());
        userService.createUser(newUser);

        // secondsUntilExpiration is 5 days
        var token = createToken(newUser, 432000L);

        return SignUpReturnData.builder()
            .message("User account creation successful")
            .email(signUpData.getEmail())
            .token(token)
            .build();
    }

    public void validateSignUpData(SignUpData signUpData) {
        if (!Validation
            .buildDefaultValidatorFactory()
            .getValidator()
            .validate(signUpData)
            .isEmpty()) {
            throw new ValidationException(
                String.format("SignUpData object '%s' did not meet SignUpData validation requirements", signUpData)
            );
        }
    }
}
