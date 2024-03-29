package com.iainschmitt.prediction.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import com.iainschmitt.prediction.model.rest.AuthData;
import com.iainschmitt.prediction.model.User;
import com.iainschmitt.prediction.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public class AuthControllerIntegrationTests {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserService userService;

    // TODO: Remove the `returnResult` invocation on this and other test classes
    private static final String AUTH_URI_PATH = "/auth";

    private String testUserEmail = "user1@iainschmitt.com";

    @BeforeEach
    void clearTestUserDB() {
        userService.deleteAll();
    }

    @Test
    void signUpUser_Success() {
        var user = User.of(testUserEmail);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        webTestClient.post().uri(AUTH_URI_PATH + "/signup").bodyValue(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(201)).expectBody().returnResult();

        assertThat(userService.exists(user.getEmail())).isTrue();
    }

    @Test
    void singUpUser_AuthValidationFailure() {
        var user = User.of(testUserEmail);
        user.setPasswordHash(" ");
        userService.saveUser(user);
        webTestClient.post().uri(AUTH_URI_PATH + "/signup").bodyValue(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(422)).expectBody().returnResult();
    }

    @Test
    void singUpUser_AuthFailureDuplicate() {
        var user = User.of(testUserEmail);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        userService.saveUser(user);
        webTestClient.post().uri(AUTH_URI_PATH + "/signup").bodyValue(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(403)).expectBody().returnResult();
    }

    @Test
    void singUpUser_BadJson() {
        webTestClient.post().uri(AUTH_URI_PATH + "/signup").bodyValue("{\"email\":\"user1@iainschmitt.com\"}")
                .exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(415)).expectBody().returnResult();
    }

    @Test
    void logInUser_Success() {
        var user = User.of(testUserEmail);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        userService.saveUser(user);

        webTestClient.post().uri(AUTH_URI_PATH + "/login").bodyValue(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(200)).expectBody().returnResult();
    }

    @Test
    void logInUser_UserDoesntExist() {
        var user = User.of(testUserEmail);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));

        var response = webTestClient.post().uri(AUTH_URI_PATH + "/login").bodyValue(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(user.getPasswordHash());
            }
        }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(403)).expectBody().returnResult();

    }

    @Test
    void singUpUser_WrongPassword() {
        var user = User.of(testUserEmail);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));

        webTestClient.post().uri(AUTH_URI_PATH + "/login").bodyValue(new AuthData() {
            {
                setEmail(user.getEmail());
                setPasswordHash(sha256Hex("!A_Different_Password_Really"));
            }
        }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(403)).expectBody().returnResult();
    }

    @Test
    void logInUser_BadJson() {
        var user = User.of(testUserEmail);
        user.setPasswordHash("!A_Minimal_Password_Really");

        webTestClient.post().uri(AUTH_URI_PATH + "/login").bodyValue(String.format("{\"email\":\"%s\"}", testUserEmail))
                .exchange()
                .expectStatus().isEqualTo(HttpStatusCode.valueOf(415)).expectBody().returnResult();
    }
}
