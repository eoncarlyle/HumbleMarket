package com.iainschmitt.perdiction.model;

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
    private float credits;
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

    public void depositCredits(float credits) {
        this.credits += credits;
    }

    public void withdrawCredits(float credits) {
        this.credits -= credits;
    }

    public void addNotification(String marketId, String message, String link) {
        notifications.put(marketId, new Notification(message, link));
    }

    public void removeNotification(String marketId) {
        notifications.remove(marketId);
    }
}