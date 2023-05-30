package com.iainschmitt.perdiction.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import org.springframework.data.annotation.Id;
import lombok.Getter;

@Getter
public class User {
    @Id
    private String id;
    private final String email;
    @Setter
    private String passwordHash;
    @Setter
    
    @Getter
    private BigDecimal credits;
    // TODO: Overall just fix the notifications
    private Map<String, Notification> notifications;

    // TODO: Change this constructor to inlcude both email and password
    public User(String email) {
        this.email = email;
        this.passwordHash = null;
        notifications = new HashMap<>();
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, displayName='%s']", getId(), getEmail());
    }

    public void depositCredits(BigDecimal credits) {
        this.credits = this.credits.add(credits);
    }

    public void withdrawCredits(BigDecimal credits) {
        this.credits = this.credits.subtract(credits);
    }

    public void addNotification(String marketId, String message) {
        notifications.put(marketId, new Notification(message));
    }

    public void removeNotification(String marketId) {
        notifications.remove(marketId);
    }
}