package com.iainschmitt.perdiction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureWebTestClient(timeout = "20s")
public class UserApiIntegrationTest {
    private static final String USERS_URI_PATH = "/users";

    private static WebTestClient webTestClient;
    @Autowired
    private UserService userService;

    @BeforeAll
    static void setupWebTestClient() {
        webTestClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:8080")
            .build();
    }

    //TODO
    @Test
    void fetchUser_Success() {

    }

    @Test
    void fetchUser_UserNotFound() {
        var uncreatedUser = "uncreatedUser";
        webTestClient.get()
            .uri(USERS_URI_PATH + "/" + uncreatedUser)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatusCode.valueOf(404))
            .expectBody()
            .jsonPath("$.body")
            .isEqualTo("com.iainschmitt.perdiction.NotFoundException: User with name '" + uncreatedUser + "' does not exist");
    }
}
