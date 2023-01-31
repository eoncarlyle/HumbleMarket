package com.iainschmitt.perdiction;


import org.springframework.data.annotation.Id;
import lombok.Getter;

@Getter
public class User {
    @Id
    private String id;
    private final String userName;
    private final String email;
    private String password;

    public User(String userName, String email) {
        this.userName = userName;
        this.email = email;
        this.password = null;
    }

    @Override
    public String toString() {
        return String.format(
            "User[id=%s, displayName='%s']",
            getId(), getUserName());
    }
}