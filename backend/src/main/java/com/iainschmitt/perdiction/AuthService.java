package com.iainschmitt.perdiction;
import java.util.Date;
import java.time.Instant;
import java.security.Key;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    //TODO: Remove before putting this into production!
    public static String secret = "e1e354166fd7ebc50dee83388faf4930f3ec409c16c18c641cfd85a953012370";
    public static Key KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    public static String createToken(User user, Long secondsUntilExpiration) {

        return Jwts.builder()
            .setExpiration(
                Date.from(Instant.now().plusSeconds(secondsUntilExpiration))
            )
            .claim("email", user.getEmail())
            .signWith(KEY)
            .compact();
    }

    public static boolean authenticateToken(String jwsString, Key key) {
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
}
