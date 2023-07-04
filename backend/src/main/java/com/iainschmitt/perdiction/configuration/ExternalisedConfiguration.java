package com.iainschmitt.perdiction.configuration;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;


@Configuration
@EnableScheduling
public class ExternalisedConfiguration {
    @Value("${secret}")
    private String secret;

    @Value("${marketCloseIntervalMinutes}")
    private String marketCloseIntervalMinutes;

    public Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public long getMarketCloseIntervalMinutes() {
        return Long.valueOf(marketCloseIntervalMinutes);
    }
}