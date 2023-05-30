package com.iainschmitt.perdiction.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.reactive.server.WebTestClient;
import lombok.SneakyThrows;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import com.iainschmitt.perdiction.model.rest.AuthData;
import com.iainschmitt.perdiction.model.rest.MarketCreationData;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.service.UserService;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.MarketTransactionService;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;

import static com.iainschmitt.perdiction.service.MarketTransactionService.toBigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public class TransactionControllerIntegrationTests {

    private static final String MARKET_URI_PATH = "/market";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuthService authService;
    @Autowired
    private MarketTransactionService transactionService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public String DEFAULT_USER_EMAIL = "user1@iainschmitt.com";

    @BeforeEach
    void clearTestUserDB() {
        marketRepository.deleteAll();
        userService.deleteAll();
        positionRepository.deleteAll();
        transactionRepository.deleteAll();

        var bank = new User(MarketTransactionService.BANK_EMAIL);
        bank.setCredits(toBigDecimal(1_000_000d));
        userService.saveUser(bank);

        userService.saveUser(new User(MarketTransactionService.ADMIN_EMAIL));
    }

    @Test
    void purchase_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var token = authService.createToken(user);

        var market = defaultMultiOutcomeMarket(MarketTransactionService.ADMIN_EMAIL);
        transactionService.createMarket(market);
        webTestClient.post().uri(MARKET_URI_PATH + "/1/outcome/1/YES/purchase/1").header("Authorization", token)
                .exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(202)).expectBody().returnResult();
    }

    @Test
    void purchase_InsufficientFundsFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(0d));
        userService.saveUser(user);
        var token = authService.createToken(user);

        var market = defaultMultiOutcomeMarket(MarketTransactionService.ADMIN_EMAIL);
        transactionService.createMarket(market);
        var response = webTestClient.post().uri(MARKET_URI_PATH + "/1/outcome/1/YES/purchase/1")
                .header("Authorization", token).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(422))
                .expectBody().returnResult();
        assertThat(new String(response.getResponseBody()))
                .isEqualTo("{\"status\":422,\"message\":\"Insufficient Funds\"}");
    }

    @Test
    void sale_Success() {
        // TODO
        // var user = new User(DEFAULT_USER_EMAIL);
        // user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        // user.setCredits(toBigDecimal(100d));
        // userService.saveUser(user);
        // var token = authService.createToken(user);

        // var market = defaultMultiOutcomeMarket(TransactionService.ADMIN_EMAIL);
        // transactionService.createMarket(market);
        // transactionService.purchase(DEFAULT_USER_EMAIL, 0, 0, PositionDirection.NO,
        // 1);
    }

    @Test
    void sale_InsufficientSharesFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var token = authService.createToken(user);

        var market = defaultMultiOutcomeMarket(MarketTransactionService.ADMIN_EMAIL);
        transactionService.createMarket(market);
        var response = webTestClient.post().uri(MARKET_URI_PATH + "/1/outcome/1/YES/sale/1")
                .header("Authorization", token).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(422))
                .expectBody().returnResult();
        assertThat(new String(response.getResponseBody()))
                .isEqualTo("{\"status\":422,\"message\":\"Insufficient Shares\"}");
    }

    @Test
    @SneakyThrows
    void sale_AuthFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var token = authService.createToken(user, 0l);
        Thread.sleep(1001L);

        var market = defaultMultiOutcomeMarket(MarketTransactionService.ADMIN_EMAIL);
        transactionService.createMarket(market);

        var response = webTestClient.post().uri(MARKET_URI_PATH + "/1/outcome/1/YES/sale/1")
                .header("Authorization", token).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(401))
                .expectBody().returnResult();
        assertThat(new String(response.getResponseBody()))
                .isEqualTo("{\"status\":401,\"message\":\"Failed authentication: invalid token\"}");
    }

    private MarketCreationData defaultSingleOutcomeMarket(String creatorId) {
        return MarketCreationData.builder().question("What will the temperature in Minneapolis be in 1 hour?")
                .creatorId(creatorId).marketMakerK(100)
                .closeDate(Instant.now().plus(Duration.ofHours(1L)).toEpochMilli()).isPublic(true)
                .outcomeClaims(new ArrayList<String>() {
                    {
                        add("Greater than 40 °F");
                    }
                }).build();
    }

    private MarketCreationData defaultMultiOutcomeMarket(String creatorId) {
        return MarketCreationData.builder().question("What will the temperature in Minneapolis be in 1 hour?")
                .creatorId(creatorId).marketMakerK(100)
                .closeDate(Instant.now().plus(Duration.ofHours(1L)).toEpochMilli()).isPublic(true)
                .outcomeClaims(new ArrayList<String>() {
                    {
                        add("Between 40 °F and 50 °F");
                        add("Outside this range");
                    }
                }).build();
    }
}