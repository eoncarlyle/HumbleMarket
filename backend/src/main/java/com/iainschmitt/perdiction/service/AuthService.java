package com.iainschmitt.perdiction.service;

import java.security.Key;
import java.time.Instant;
import java.time.Duration;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.exceptions.NotAuthorizedException;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.model.rest.AuthData;
import com.iainschmitt.perdiction.model.rest.LogInReturnData;
import com.iainschmitt.perdiction.model.rest.SignUpReturnData;


@Getter
@Service
public class AuthService {
    public final Duration TOKEN_LIFESPAN = Duration.ofDays(5);
    
    @Autowired
    private ExternalisedConfiguration externalConfig;

    @Autowired
    private UserService userService;

    public String createToken(User user) {
        // secondsUntilExpiration is 5 days
        return createToken(user, TOKEN_LIFESPAN);
    }

    public String createToken(User user, Duration durationUntilExpiration) {
        return Jwts.builder().setExpiration(Date.from(Instant.now().plus(durationUntilExpiration)))
                .claim("email", user.getEmail()).signWith(externalConfig.getKey()).compact();
    }

    // TODO: Please find a better name for this
    public boolean authenticateToken(String jwsString) {
        return authenticateToken(jwsString, externalConfig.getKey());
    }

    public boolean authenticateToken(String jwsString, Key key) {
        try {
            getClaims(jwsString, key);
            return true;
        } catch (SignatureException e) {
            return false;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown during JWT authentication", e);
        }
    }

    public Jws<Claims> getClaims(String jwsString, Key key) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwsString);
    }

    public Jws<Claims> getClaims(String jwsString) {
        return Jwts.parserBuilder().setSigningKey(externalConfig.getKey()).build().parseClaimsJws(jwsString);
    }

    // TODO: Please find a better name for this
    public void authenticateTokenThrows(String token) {
        if (!authenticateToken(token)) {
            throw new NotAuthorizedException("Failed authentication: invalid token");
        }
    }

    public String getClaim(String jwsString, String claim) {
        try {
            var claims = getClaims(jwsString, externalConfig.getKey());
            return claims.getBody().get(claim, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown during JWT authentication", e);
        }
    }

    public SignUpReturnData createUserAccount(AuthData authData) {
        authData.validate();

        if (userService.exists(authData.getEmail())) {
            throw new NotAuthorizedException(String.format("User with email '%s' already exists", authData.getEmail()));
        }
        var newUser = new User(authData.getEmail());
        newUser.setPasswordHash(authData.getPasswordHash());
        userService.saveUser(newUser);

        return SignUpReturnData.builder().message("User account creation successful").email(authData.getEmail())
                .token(createToken(newUser)).build();
    }

    public LogInReturnData logInUserAccount(AuthData authData) {
        authData.validate();
        var email = authData.getEmail();
        var password = authData.getPasswordHash();
        if (!userService.exists(email) || !userService.getUserByEmail(email).getPasswordHash().equals(password)) {
            // if (!userService.exists(email)) {
            throw new NotAuthorizedException("Failed authentication: username or password is incorrect");
        }

        return LogInReturnData.builder().message("Log in successful")
                .token(createToken(userService.getUserByEmail(email))).build();
    }

}
