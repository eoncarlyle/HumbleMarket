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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(outcome.getPrice()).isEqualTo(0.5f);
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
        assertThat(firstOutcome.getPrice()).isEqualTo(1f / 3f);
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

        transactionService.purchase(user.getId(), market.getId(), 0, PositionDirection.NO, shares);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        bank = transactionService.getBankUser();
        user = userService.getUserByEmail(user.getEmail());

        var markets = marketRepository.findAll();
        var transactions = transactionRepository.findAll();
        var positions = positionRepository.findAll();
        var outcome = markets.get(0).getOutcomes().get(0);

        assertThat(bank.getCredits()).isEqualTo(1_000_000f + shares * initialPrice);
        assertThat(user.getCredits()).isEqualTo(100f - shares * initialPrice);
        assertThat(markets.size()).isEqualTo(1);
        assertThat(transactions.size()).isEqualTo(1);
        assertThat(positions.size()).isEqualTo(1);
        assertThat(bank.getCredits() + user.getCredits()).isEqualTo(startingCredits);

        assertThat(outcome.getSharesN()).isEqualTo(8);
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

        transactionService.purchase(user.getId(), market.getId(), 0, PositionDirection.NO, shares);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy()).isEqualTo(1 - initialOutcomeZeroPrice);

        transactionService.purchase(user.getId(), market.getId(), 1, PositionDirection.YES, shares);
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

        assertThatThrownBy(() -> transactionService.purchase(user.getId(), market.getId(), 0, PositionDirection.NO, 3))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Cannot transact on closed market");
    }

    @Test
    public void purchase_InsufficientFundsFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(1f);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        assertThatThrownBy(() -> transactionService.purchase(user.getId(), market.getId(), 0, PositionDirection.NO, 3))
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
        assertThatThrownBy(() -> transactionService.purchase(user.getId(), market.getId(), 0, PositionDirection.NO, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many shares requested, at least two remaining shares need to be purchased");
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

        transactionService.purchase(user.getId(), market.getId(), 0, PositionDirection.YES, shares);
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy()).isEqualTo(initialOutcomeZeroPrice);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);

        transactionService.purchase(user.getId(), market.getId(), 1, PositionDirection.NO, shares);
        assertThat(positionRepository.findAll().get(1).getPriceAtBuy()).isEqualTo(1 - initialOutcomeOnePrice);
        assertThat(totalCredits()).isEqualTo(initialTotalCredits);
    }

    @Test
    public void sale_ClosedMarketFailure() {
        // TODO
    }

    @Test
    public void sale_InsufficientSharesFailure() {
        // TODO: Test with multiple positions so that the validUserShares is tested
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

    private float totalCredits() {
        return totalCredits(TransactionService.BANK_EMAIL, DEFAULT_USER_EMAIL);
    }

    private float totalCredits(String bankEmail, String userEmail) {
        return userService.getUserByEmail(DEFAULT_USER_EMAIL).getCredits()
                + userService.getUserByEmail(TransactionService.BANK_EMAIL).getCredits();
    }
}
