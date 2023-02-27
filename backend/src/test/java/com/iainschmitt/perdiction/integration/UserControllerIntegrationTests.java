package com.iainschmitt.perdiction.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public class UserControllerIntegrationTests {
    private static final String USERS_URI_PATH = "/users";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserService userService;

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    void fetchUser_Success() {
        var user = new User("user1@iainschmitt.com");
        userService.saveUser(user);
        webTestClient.get().uri(USERS_URI_PATH + "/" + user.getEmail()).exchange().expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(200)).expectBody().jsonPath("$.email").isEqualTo(user.getEmail());
    }

    @Test
    void fetchUser_UserNotFound() {
        var uncreatedUser = "uncreatedUser";
        webTestClient.get().uri(USERS_URI_PATH + "/" + uncreatedUser).exchange().expectStatus()
                .isEqualTo(HttpStatusCode.valueOf(404)).expectBody().jsonPath("$.message")
                .isEqualTo("User with email '" + uncreatedUser + "' does not exist");
    }
}
