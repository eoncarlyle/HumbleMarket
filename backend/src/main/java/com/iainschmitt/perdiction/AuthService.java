package com.iainschmitt.perdiction;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    //TODO: Remove and change this before putting this into production!
    public static String secret = "e1e354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370";
    public static Key KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    @Autowired
    private UserService userService;

    public String createToken(User user) {
        // secondsUntilExpiration is 5 days
        return createToken(user, 432000L);
    }

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
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwsString);
            return true;
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown during JWT authentication", e);
        }
    }

    public SignUpReturnData createUserAccount(AuthData authData) {
        authData.validate();

        if (userService.exists(authData.getEmail())) {
            throw new NotAuthorizedException(String.format(
                "User with email '%s' already exists",
                authData.getEmail()
            ));
        }
        var newUser = new User(authData.getEmail());
        newUser.setPassword(authData.getPassword());
        userService.createUser(newUser);

        // secondsUntilExpiration is 5 days
        var token = createToken(newUser);

        return SignUpReturnData.builder()
            .message("User account creation successful")
            .email(authData.getEmail())
            .token(token)
            .build();
    }

    public LogInReturnData logInUserAccount(AuthData authData) {
        authData.validate();
        var email = authData.getEmail();
        var password = authData.getPassword();
        if (!userService.exists(email) || !userService.getUserByEmail(email).getPassword().equals(password)) {
        //if (!userService.exists(email)) {
            throw new NotAuthorizedException("Failed authentication: username or password is incorrect");
        }

        return LogInReturnData.builder()
            .message("Log in successful")
            .token(createToken(userService.getUserByEmail(email)))
            .build();
    }
}
