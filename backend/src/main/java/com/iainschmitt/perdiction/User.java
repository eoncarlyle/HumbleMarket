package com.iainschmitt.perdiction;


import lombok.Setter;
import org.springframework.data.annotation.Id;
import lombok.Getter;

@Getter
public class User {
    @Id
    private String id;
    private final String email;
    @Setter
    private String password;

    //TODO: Change this constructor to inlcude both email and password
    //TODO: Don't even accept passwords, accept password hashes
    public User(String email) {
        this.email = email;
        this.password = null;
    }

    @Override
    public String toString() {
        return String.format(
            "User[id=%s, displayName='%s']",
            getId(), getEmail());
    }
}