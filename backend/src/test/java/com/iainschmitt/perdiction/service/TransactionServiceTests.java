package com.iainschmitt.perdiction.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.Outcome;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.TransactionType;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.model.rest.MarketData;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;
import com.iainschmitt.perdiction.repository.UserRepository;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.iainschmitt.perdiction.service.TransactionService.toBigDecimal;

@SpringBootTest
public class TransactionServiceTests {

    @Autowired
    private TransactionService transactionService;
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
    void clearAndSetupTestDB() {
        marketRepository.deleteAll();
        userService.deleteAll();
        positionRepository.deleteAll();
        transactionRepository.deleteAll();

        var bank = new User(TransactionService.BANK_EMAIL);
        bank.setCredits(1_000_000f);
        userService.saveUser(bank);

        userService.saveUser(new User(TransactionService.ADMIN_EMAIL));
    }

    @Test
    public void createMarketSingleOutcome_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(user.getId());

        assertThatNoException().isThrownBy(() -> transactionService.createMarket(marketData));
        assertThat(marketRepository.existsByQuestion(marketData.getQuestion())).isTrue();
        var outcome = marketRepository.findByQuestion(marketData.getQuestion()).getOutcomes().get(0);
        assertThat(outcome.getPrice()).isEqualTo(toBigDecimal(0.5d));
        assertThat(outcome.getSharesN()).isEqualTo(10);
        assertThat(outcome.getSharesY()).isEqualTo(10);
    }

    @Test
    public void createMarketMultiOutcome_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(TransactionService.ADMIN_EMAIL);

        assertThatNoException().isThrownBy(() -> transactionService.createMarket(marketData));
        assertThat(marketRepository.existsByQuestion(marketData.getQuestion())).isTrue();
        var firstOutcome = marketRepository.findByQuestion(marketData.getQuestion()).getOutcomes().get(0);
        assertThat(firstOutcome.getPrice()).isEqualTo(toBigDecimal(1d / 3d));
        assertThat(firstOutcome.getSharesN()).isEqualTo(7);
        assertThat(firstOutcome.getSharesY()).isEqualTo(14);
    }

    @Test
    public void purchase_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        var bank = transactionService.getBankUser();
        user.setCredits(100f);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialPrice = market.getOutcomes().get(0).getPrice();
        var shares = 3;
        var startingCredits = bank.getCredits() + user.getCredits();
        var initialTotalCredits = totalCredits();

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, shares);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        bank = transactionService.getBankUser();
        user = userService.getUserByEmail(user.getEmail());

        var markets = marketRepository.findAll();
        var transactions = transactionRepository.findAll();
        var positions = positionRepository.findAll();
        var outcome = markets.get(0).getOutcomes().get(0);

        assertThat(bank.getCredits()).isEqualTo(1_000_000d + shares * initialPrice.doubleValue());
        assertThat(user.getCredits()).isEqualTo(100d - shares * initialPrice.doubleValue());
        assertThat(markets.size()).isEqualTo(1);
        assertThat(transactions.size()).isEqualTo(1);
        assertThat(positions.size()).isEqualTo(1);
        assertThat(bank.getCredits() + user.getCredits()).isEqualTo(startingCredits);

        assertThat(outcome.getSharesN()).isEqualTo(9);
        assertThat(outcome.getSharesY()).isEqualTo(12);

        assertThat(transactions.get(0).getDirection()).isEqualTo(PositionDirection.NO);
        assertThat(transactions.get(0).getTransactionType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(transactions.get(0).getCredits()).isEqualTo(1.5f);

        assertThat(positions.get(0).getShares()).isEqualTo(3);
        assertThat(positions.get(0).getDirection()).isEqualTo(PositionDirection.NO);
    }

    @Test
    public void purchaseMultiOutcome_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(100f);
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
        var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
        var shares = 3;
        var initialTotalCredits = totalCredits();

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, shares);

        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy())
                .isEqualTo(toBigDecimal(1d - initialOutcomeZeroPrice.doubleValue()));

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.YES, shares);
        assertThat(positionRepository.findAll().get(1).getPriceAtBuy()).isEqualTo(initialOutcomeOnePrice);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
    }

    @Test
    public void purchased_ClosedMarketFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(1f);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        market.setClosed(true);
        marketRepository.save(market);
        assertThatThrownBy(
                () -> transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, 3))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Cannot transact on closed market");
    }

    @Test
    public void purchase_InsufficientFundsFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(1f);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        assertThatThrownBy(
                () -> transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, 3))
                        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Insufficient Funds");
    }

    @Test
    public void purchase_TooManySharesFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(100f);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        assertThatThrownBy(
                () -> transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, 9))
                        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
                                "Too many shares requested, at least two remaining shares need to be purchased");
    }

    @Test
    public void sale_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(100f);
        userService.saveUser(user);
        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);

        Supplier<Market> market = () -> marketRepository.findAll().get(0);
        Supplier<Outcome> outcome = () -> market.get().getOutcomes().get(0);
        var shares = 3;
        var marketId = market.get().getId();
        var initialTotalCredits = totalCredits();

        var firstPrice = outcome.get().getPrice();
        transactionService.purchase(user.getId(), marketId, 0, PositionDirection.YES, shares);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        var secondPrice = outcome.get().getPrice();
        transactionService.purchase(user.getId(), marketId, 0, PositionDirection.YES, shares);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        var positions = positionRepository.findByUserIdAndMarketIdOrderByPriceAtBuyDesc(user.getId(), marketId);
        assertThat(positions.size()).isEqualTo(2);
        assertThat(positions.get(0).getPriceAtBuy()).isEqualTo(secondPrice);
        assertThat(positions.get(1).getPriceAtBuy()).isEqualTo(firstPrice);

        transactionService.sale(user.getId(), marketId, 0, PositionDirection.YES, shares + 1);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
    }

    @Test
    public void saleMultiOutcome_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        var bank = transactionService.getBankUser();
        user.setCredits(100f);
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
        var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
        var shares = 3;
        var initialTotalCredits = totalCredits();

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.YES, shares);
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy()).isEqualTo(initialOutcomeZeroPrice);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.NO, shares);
        assertThat(positionRepository.findAll().get(1).getPriceAtBuy())
                .isEqualTo(toBigDecimal(1d - initialOutcomeOnePrice.doubleValue()));
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.YES, 2);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
        transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.NO, 2);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
    }

    @Test
    public void sale_InsufficientSharesFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        var bank = transactionService.getBankUser();
        user.setCredits(100f);
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
        var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
        var shares = 3;
        var initialTotalCredits = totalCredits();

        // Only buy transaction
        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.YES, shares);
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy()).isEqualTo(initialOutcomeZeroPrice);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        // Valid sale of 2 shares of 0/Yes
        transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.YES, 2);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        // Invalid sale of 1 share of 1/No
        assertThatThrownBy(
                () -> transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.NO, 1))
                        .isInstanceOf(IllegalArgumentException.class);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
    }

    @Test
    public void sale_ClosedMarketFailure() {
        // TODO: Test with multiple positions so that all validUserShares code paths are
    }

    @Test
    public void close_SingleOutomce() {
        // TODO: Include user notification
    }

    @Test
    public void close_MultiOutomce() {
        // TODO: Include user notification
    }

    @Test
    public void resolve_Success() {
        // TODO
    }

    @Test
    public void resolve_UnderdefinedFailure() {
        // TODO
    }

    // Creates documents for doing frontend UI testing
    @Test
    public void frontendMarketTest() {
        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        transactionService.createMarket(marketData);
        marketData = defaultMultiOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        marketData = MarketData.builder().question("What will the temperature in Minneapolis be in 1 hour?")
                .creatorId(getAdminId()).marketMakerK(100)
                .closeDate(Instant.now().plus(Duration.ofHours(1L)).toEpochMilli()).isPublic(true)
                .outcomeClaims(new ArrayList<String>() {
                    {
                        add("Less than 40 °F");
                        add("Between 40 °F and 50 °F");
                        add("Greather than 50 °F");
                    }
                }).build();
        transactionService.createMarket(marketData);

        var user = new User("user1@mail.com");
        user.setPasswordHash(sha256Hex("password"));
        userService.saveUser(user);
    }

    private String getAdminId() {
        return userService.getUserByEmail(TransactionService.ADMIN_EMAIL).getId();
    }

    private MarketData defaultSingleOutcomeMarket(String creatorId) {
        return MarketData.builder().question("What will the temperature in Minneapolis be in 1 hour?")
                .creatorId(creatorId).marketMakerK(100)
                .closeDate(Instant.now().plus(Duration.ofHours(1L)).toEpochMilli()).isPublic(true)
                .outcomeClaims(new ArrayList<String>() {
                    {
                        add("Greater than 40 °F");
                    }
                }).build();
    }

    private MarketData defaultMultiOutcomeMarket(String creatorId) {
        return MarketData.builder().question("What will the temperature in Minneapolis be in 1 hour?")
                .creatorId(creatorId).marketMakerK(100)
                .closeDate(Instant.now().plus(Duration.ofHours(1L)).toEpochMilli()).isPublic(true)
                .outcomeClaims(new ArrayList<String>() {
                    {
                        add("Between 40 °F and 50 °F");
                        add("Outside this range");
                    }
                }).build();
    }

    private double totalCredits() {
        return totalCredits(TransactionService.BANK_EMAIL, DEFAULT_USER_EMAIL);
    }

    private double totalCredits(String bankEmail, String userEmail) {
        return userService.getUserByEmail(DEFAULT_USER_EMAIL).getCredits()
                + userService.getUserByEmail(TransactionService.BANK_EMAIL).getCredits();
    }

}
