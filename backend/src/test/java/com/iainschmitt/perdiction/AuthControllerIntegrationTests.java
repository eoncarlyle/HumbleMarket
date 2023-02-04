package com.iainschmitt.perdiction;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

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
  void signUpUser_Success() {
    var user = new User("user1@iainschmitt.com");
    user.setPassword("!A_Minimal_Password_Really");
    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/signup")
      .bodyValue(
        new AuthData() {
          {
            setEmail(user.getEmail());
            setPassword(user.getPassword());
          }
        }
      )
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(201))
      .expectBody()
      .returnResult();

    assertThat(userService.exists(user.getEmail())).isTrue();
  }

  @Test
  void singUpUser_AuthFailure() {
    var user = new User("user1@iainschmitt.com");
    user.setPassword(" ");
    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/signup")
      .bodyValue(
        new AuthData() {
          {
            setEmail(user.getEmail());
            setPassword(user.getPassword());
          }
        }
      )
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(422))
      .expectBody()
      .returnResult();
  }

  @Test
  void singUpUser_BadJson() {
    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/signup")
      .bodyValue("{\"email\":\"user1@iainschmitt.com\"}")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(415))
      .expectBody()
      .returnResult();
  }

  @Test
  void logInUser_Success() {
    var user = new User("user1@iainschmitt.com");
    user.setPassword("!A_Minimal_Password_Really");
    userService.createUser(user);

    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/login")
      .bodyValue(
        new AuthData() {
          {
            setEmail(user.getEmail());
            setPassword(user.getPassword());
          }
        }
      )
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(200))
      .expectBody()
      .returnResult();
  }

  @Test
  void logInUser_UserDoesntExist() {
    var user = new User("user1@iainschmitt.com");
    user.setPassword("!A_Minimal_Password_Really");

    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/login")
      .bodyValue(
        new AuthData() {
          {
            setEmail(user.getEmail());
            setPassword(user.getPassword());
          }
        }
      )
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(401))
      .expectBody()
      .returnResult();
  }

  @Test
  void singUpUser_WrongPassword() {
    var user = new User("user1@iainschmitt.com");
    user.setPassword("!A_Minimal_Password_Really");

    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/login")
      .bodyValue(
        new AuthData() {
          {
            setEmail(user.getEmail());
            setPassword("!A_Different_Password_Really");
          }
        }
      )
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(401))
      .expectBody()
      .returnResult();
  }

  @Test
  void logInUser_BadJson() {
    var user = new User("user1@iainschmitt.com");
    user.setPassword("!A_Minimal_Password_Really");

    webTestClient
      .post()
      .uri(AUTH_URI_PATH + "/login")
      .bodyValue("{\"email\":\"user1@iainschmitt.com\"}")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatusCode.valueOf(415))
      .expectBody()
      .returnResult();
  }
}
