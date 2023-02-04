package com.iainschmitt.perdiction;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerIntegrationTests {
    private static final String AUTH_URI_PATH = "/auth";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserService userService;

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    void singupUser_Success() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword("!A_Minimal_Password_Really");
        webTestClient.post()
            .uri(AUTH_URI_PATH + "/signup")
            .bodyValue(
                new SignUpData(){{
                    setEmail(user.getEmail());
                    setPassword(user.getPassword());
                }}
            )
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatusCode.valueOf(201))
            .expectBody()
            .returnResult();

        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    void singupUser_AuthFailure() {
        var user = new User("user1@iainschmitt.com");
        user.setPassword(" ");
        webTestClient.post()
            .uri(AUTH_URI_PATH + "/signup")
            .bodyValue(
                new SignUpData(){{
                    setEmail(user.getEmail());
                    setPassword(user.getPassword());
                }}
            )
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatusCode.valueOf(422))
            .expectBody()
            .returnResult();
    }


    @Test
    void singupUser_BadJson() {
        webTestClient.post()
            .uri(AUTH_URI_PATH + "/echo")
            .bodyValue(
                "{\"email\":\"user1@iainschmitt.com\"}"
            )
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatusCode.valueOf(415))
            .expectBody()
            .returnResult();

    }
}
