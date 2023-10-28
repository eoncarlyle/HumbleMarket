package com.iainschmitt.perdiction.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import lombok.SneakyThrows;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import com.iainschmitt.perdiction.model.rest.MarketProposalData;
import com.iainschmitt.perdiction.model.rest.PurchaseRequestData;
import com.iainschmitt.perdiction.model.rest.SaleRequestData;
import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.service.UserService;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.MarketTransactionService;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;
import com.iainschmitt.perdiction.repository.WhitelistEmailRepository;
import com.iainschmitt.perdiction.model.WhitelistEmail;

import static com.iainschmitt.perdiction.service.MarketTransactionService.toBigDecimal;
import static com.iainschmitt.perdiction.service.MarketTransactionService.price;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public class TransactionControllerIntegrationTests {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private AuthService authService;
    @Autowired
    private MarketTransactionService marketTransactionService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ExternalisedConfiguration externalisedConfiguration;
    @Autowired
    private WhitelistEmailRepository whitelistEmailRepository;

    public String DEFAULT_USER_EMAIL = "user1@iainschmitt.com";
    private static final String MARKET_URI_PATH = "/market";

    @BeforeEach
    void clearTestUserDB() {
        marketRepository.deleteAll();
        userService.deleteAll();
        positionRepository.deleteAll();
        transactionRepository.deleteAll();

        var bank = User.of(externalisedConfiguration.getBankEmail());
        bank.setCredits(toBigDecimal(1_000_000d));
        userService.saveUser(bank);

        userService.saveUser(User.of(externalisedConfiguration.getAdminEmail()));
    }

    @Test
    void purchase_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var token = authService.createToken(user);
        
        var market = defaultMultiOutcomeMarket(externalisedConfiguration.getAdminEmail());
        marketTransactionService.createMarket(market);
        var marketId = marketRepository.findAll().get(0).getId();
        
        webTestClient.post().uri(MARKET_URI_PATH + "/purchase").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(new PurchaseRequestData() {
                    {
                        setId(marketId);
                        setOutcomeIndex(1);
                        setPositionDirection(PositionDirection.YES);
                        setShares(1);
                    }
                }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(202));
    }

    @Test
    void purchase_InsufficientFundsFailure() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(0d));
        userService.saveUser(user);
        var token = authService.createToken(user);

        var market = defaultMultiOutcomeMarket(externalisedConfiguration.getAdminEmail());
        marketTransactionService.createMarket(market);
        var marketId = marketRepository.findAll().get(0).getId();
        var response = webTestClient.post().uri(MARKET_URI_PATH + "/purchase").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(new PurchaseRequestData() {
                    {
                        setId(marketId);
                        setOutcomeIndex(1);
                        setPositionDirection(PositionDirection.YES);
                        setShares(1);
                    }
                }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(422)).expectBody().returnResult();

        assertThat(new String(response.getResponseBody()))
                .isEqualTo("{\"status\":422,\"message\":\"Insufficient Funds\"}");
    }

    @Test
    void sale_Success() {
        // TODO
        // var user = User.of(DEFAULT_USER_EMAIL);
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
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var token = authService.createToken(user);
        var market = defaultMultiOutcomeMarket(externalisedConfiguration.getAdminEmail());
        marketTransactionService.createMarket(market);
        
        var marketId = marketRepository.findAll().get(0).getId();
        var sharesY = marketRepository.findById(marketId).get().getOutcomes().get(1).getSharesY();
        var sharesN = marketRepository.findById(marketId).get().getOutcomes().get(1).getSharesN();
        var sharesTraded = 1;

        var response = webTestClient.post().uri(MARKET_URI_PATH + "/sale").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(new SaleRequestData() {
                    {
                        setId(marketId);
                        setOutcomeIndex(1);
                        setPositionDirection(PositionDirection.YES);
                        setShares(sharesTraded);
                        setSharePrice(price(sharesY + sharesTraded, sharesN));
                    }
                }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(422)).expectBody().returnResult();

        assertThat(new String(response.getResponseBody()))
                .isEqualTo("{\"status\":422,\"message\":\"Insufficient Shares\"}");
    }

    @Test
    @SneakyThrows
    void sale_AuthFailure() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setPasswordHash(sha256Hex("!A_Minimal_Password_Really"));
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var token = authService.createToken(user, Duration.ofSeconds(0));
        Thread.sleep(1001L);
        whitelistEmailRepository.save(new WhitelistEmail(DEFAULT_USER_EMAIL));

        var market = defaultMultiOutcomeMarket(externalisedConfiguration.getAdminEmail());
        marketTransactionService.createMarket(market);
        var marketId = marketRepository.findAll().get(0).getId();
        
        var sharesY = marketRepository.findById(marketId).get().getOutcomes().get(1).getSharesY();
        var sharesN = marketRepository.findById(marketId).get().getOutcomes().get(1).getSharesN();
        var sharesTraded = 1;

        var response = webTestClient.post().uri(MARKET_URI_PATH + "/sale").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(new SaleRequestData() {
                    {
                        setId(marketId);
                        setOutcomeIndex(1);
                        setPositionDirection(PositionDirection.YES);
                        setShares(sharesTraded);
                        setSharePrice(price(sharesY + sharesTraded, sharesN));
                    }
                }).exchange().expectStatus().isEqualTo(HttpStatusCode.valueOf(403)).expectBody().returnResult();

        assertThat(new String(response.getResponseBody()))
                .isEqualTo("{\"status\":403,\"message\":\"Failed authentication: invalid token\"}");
        
        whitelistEmailRepository.save(new WhitelistEmail(DEFAULT_USER_EMAIL));
    }

    
    private MarketProposalData defaultMultiOutcomeMarket(String creatorId) {

        return MarketProposalData.of("What will the temperature in Minneapolis be in 1 hour?", creatorId, 100,
                Instant.now().plus(Duration.ofHours(1L)).toEpochMilli(), (new ArrayList<String>() {
                    {
                        add("Between 40 °F and 50 °F");
                        add("Outside this range");
                    }
                }), true);
    }
}