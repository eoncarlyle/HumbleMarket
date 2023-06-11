package com.iainschmitt.perdiction.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import com.iainschmitt.perdiction.model.rest.MarketCreationData;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.iainschmitt.perdiction.service.MarketTransactionService.toBigDecimal;

@SpringBootTest
public class MarketTransactionServiceTests {

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
    void clearAndSetupTestDB() {
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

        var marketData = defaultMultiOutcomeMarket(MarketTransactionService.ADMIN_EMAIL);

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
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialPrice = market.getOutcomes().get(0).getPrice();
        var shares = 3;
        var startingCredits = bank.getCredits().add(user.getCredits());
        var initialTotalCredits = totalCredits();

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, shares);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        bank = transactionService.getBankUser();
        user = userService.getUserByEmail(user.getEmail());

        var markets = marketRepository.findAll();
        var transactions = transactionRepository.findAll();
        var positions = positionRepository.findAll();
        var outcome = markets.get(0).getOutcomes().get(0);

        assertThat(bank.getCredits().doubleValue()).isEqualTo(1_000_000d + shares * initialPrice.doubleValue());
        assertThat(user.getCredits().doubleValue()).isEqualTo(100d - shares * initialPrice.doubleValue());
        assertThat(markets.size()).isEqualTo(1);
        assertThat(transactions.size()).isEqualTo(1);
        assertThat(positions.size()).isEqualTo(1);
        assertThat(bank.getCredits().add(user.getCredits()).doubleValue()).isEqualTo(startingCredits.doubleValue());

        assertThat(outcome.getSharesN()).isEqualTo(8);
        assertThat(outcome.getSharesY()).isEqualTo(12);

        assertThat(transactions.get(0).getDirection()).isEqualTo(PositionDirection.NO);
        assertThat(transactions.get(0).getTransactionType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(transactions.get(0).getCredits().doubleValue()).isEqualTo(1.5d);

        assertThat(positions.get(0).getShares()).isEqualTo(3);
        assertThat(positions.get(0).getDirection()).isEqualTo(PositionDirection.NO);
    }

    @Test
    public void purchaseMultiOutcome_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
        var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
        var shares = 3;
        var initialTotalCredits = totalCredits();

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, shares);

        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy())
                .isEqualTo(toBigDecimal(1d - initialOutcomeZeroPrice.doubleValue()));

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.YES, shares);
        assertThat(positionRepository.findAll().get(1).getPriceAtBuy()).isEqualTo(initialOutcomeOnePrice);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void purchased_ClosedMarketFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
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
        user.setCredits(toBigDecimal(1d));
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
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        assertThatThrownBy(
                () -> transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.NO, 9))
                        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Too many shares requested");
    }

    // TODO: Deprecate test
    @Test
    public void old_sale_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
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
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var secondPrice = outcome.get().getPrice();
        transactionService.purchase(user.getId(), marketId, 0, PositionDirection.YES, shares);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var positions = positionRepository.findByUserIdAndMarketIdOrderByPriceAtBuyDesc(user.getId(), marketId);
        assertThat(positions.size()).isEqualTo(2);
        assertThat(positions.get(0).getPriceAtBuy()).isEqualTo(secondPrice);
        assertThat(positions.get(1).getPriceAtBuy()).isEqualTo(firstPrice);

        transactionService.sale(user.getId(), marketId, 0, PositionDirection.YES, shares + 1);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void sale_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);

        Supplier<Market> market = () -> marketRepository.findAll().get(0);
        Supplier<Outcome> outcome = () -> market.get().getOutcomes().get(0);
        var shares = 3;
        var initialTotalCredits = totalCredits();

        var firstPrice = outcome.get().getPrice();
        transactionService.purchase(user, market.get(), 0, PositionDirection.YES, shares);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var secondPrice = outcome.get().getPrice();
        transactionService.purchase(user, market.get(), 0, PositionDirection.YES, shares);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var positions = positionRepository.findByUserIdAndMarketIdOrderByPriceAtBuyDesc(user.getId(),
                market.get().getId());
        assertThat(positions.size()).isEqualTo(2);
        assertThat(positions.get(0).getPriceAtBuy()).isEqualTo(secondPrice);
        assertThat(positions.get(0).getDirection()).isEqualTo(PositionDirection.YES);
        assertThat(positions.get(1).getPriceAtBuy()).isEqualTo(firstPrice);
        assertThat(positions.get(1).getDirection()).isEqualTo(PositionDirection.YES);

        // Currently held YES shares by user: 2 * `shares`, will sell one more than
        // `shares` resulting one less than `shares`
        // at the end of the transaction
        var newSharesY = outcome.get().getSharesY() + shares + 1;
        var newSharesN = outcome.get().getSharesN();
        var proposedPrice = MarketTransactionService.price(newSharesY, newSharesN);
        transactionService.sale(user, market.get(), 0, PositionDirection.YES, shares + 1, proposedPrice);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void sale_InvalidSalePrice() {
        var user = new User(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);

        Supplier<Market> market = () -> marketRepository.findAll().get(0);
        Supplier<Outcome> outcome = () -> market.get().getOutcomes().get(0);
        var shares = 3;
        var initialTotalCredits = totalCredits();

        transactionService.purchase(user, market.get(), 0, PositionDirection.YES, shares);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var newSharesY = outcome.get().getSharesY() + shares + 1;
        var newSharesN = outcome.get().getSharesN();
        var incorrectProposedPrice = MarketTransactionService.price(newSharesY + 1, newSharesN);
        assertThatThrownBy(
                () -> transactionService.sale(user, market.get(), 0, PositionDirection.YES, 1, incorrectProposedPrice))
                        .isInstanceOf(IllegalArgumentException.class);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void salesPriceList() {
        var user0 = new User(DEFAULT_USER_EMAIL);
        var startingCredits = toBigDecimal(100d);
        user0.setCredits(startingCredits);
        userService.saveUser(user0);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var selectedOutcomeIndex = 0;
        var tradedShares = 3;
        var shareCost = toBigDecimal(0.5d);

        var position0 = new Position(user0.getId(), market.getId(), selectedOutcomeIndex, PositionDirection.YES,
                tradedShares, shareCost);
        var position1 = new Position(user0.getId(), market.getId(), selectedOutcomeIndex, PositionDirection.NO,
                tradedShares, shareCost);

        positionRepository.saveAll(List.of(position0, position1));

        List<BigDecimal> yesSalePriceList = new ArrayList<>();
        List<BigDecimal> noSalePriceList = new ArrayList<>();
        var startingYesShares = market.getOutcomes().get(selectedOutcomeIndex).getSharesY();
        var startingNoShares = market.getOutcomes().get(selectedOutcomeIndex).getSharesN();
        for (int sharesToSell = 1; sharesToSell <= tradedShares; sharesToSell++) {
            yesSalePriceList.add(MarketTransactionService.price(startingYesShares + sharesToSell, startingNoShares));
            noSalePriceList.add(MarketTransactionService.price(startingYesShares, startingNoShares + sharesToSell));
        }

        assertThat(transactionService.getSalePriceList(market, user0)).isEqualTo(List.of(List.of(yesSalePriceList, noSalePriceList)));
    }

    @Test
    public void old_saleMultiOutcome_Success() {
        var user = new User(DEFAULT_USER_EMAIL);
        var bank = transactionService.getBankUser();
        user.setCredits(toBigDecimal(100d));
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
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.NO, shares);
        assertThat(positionRepository.findAll().get(1).getPriceAtBuy())
                .isEqualTo(toBigDecimal(1d - initialOutcomeOnePrice.doubleValue()));
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.YES, 2);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
        transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.NO, 2);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    // TODO: Deprecate test
    @Test
    public void old_sale_InsufficientSharesFailure() {
        var user = new User(DEFAULT_USER_EMAIL);
        var bank = transactionService.getBankUser();
        user.setCredits(toBigDecimal(100d));
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
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        // Valid sale of 2 shares of 0/Yes
        transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 0, PositionDirection.YES, 2);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        // Invalid sale of 1 share of 1/No
        assertThatThrownBy(
                () -> transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 1, PositionDirection.NO, 1))
                        .isInstanceOf(IllegalArgumentException.class);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    // TODO: Deprecate test
    @Test
    public void old_sale_ClosedMarketFailure() {
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
        var user0 = new User(DEFAULT_USER_EMAIL);
        var user1 = new User("user2@iainschmitt.com");
        var startingCredits = toBigDecimal(98.5d);
        user0.setCredits(startingCredits);
        user1.setCredits(startingCredits);
        userService.saveUser(user0);
        userService.saveUser(user1);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        transactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var selectedOutcomeIndex = 0;
        var tradedShares = 3;
        var shareCost = toBigDecimal(0.5d);

        var position0 = new Position(user0.getId(), market.getId(), selectedOutcomeIndex, PositionDirection.YES,
                tradedShares, shareCost);
        var position1 = new Position(user1.getId(), market.getId(), selectedOutcomeIndex, PositionDirection.NO,
                tradedShares, shareCost);

        positionRepository.save(position0);
        positionRepository.save(position1);

        transactionService.close(market, selectedOutcomeIndex);

        assertThatThrownBy(() -> transactionService.sale(user0, market, selectedOutcomeIndex, PositionDirection.YES, 3))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Cannot transact on closed market");
        assertThatThrownBy(
                () -> transactionService.purchase(user0, market, selectedOutcomeIndex, PositionDirection.YES, 3))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Cannot transact on closed market");

        transactionService.resolve(market, selectedOutcomeIndex, PositionDirection.YES);

        assertThat(userService.getUserById(user0.getId()).getCredits().doubleValue()).isEqualTo(101.5d);
        assertThat(userService.getUserById(user1.getId()).getCredits().doubleValue()).isEqualTo(98.5d);
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
        marketData = MarketCreationData.builder().question("What will the temperature in Minneapolis be in 1 hour?")
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
        return userService.getUserByEmail(MarketTransactionService.ADMIN_EMAIL).getId();
    }

    private String getBankId() {
        return userService.getUserByEmail(MarketTransactionService.BANK_EMAIL).getId();
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

    private BigDecimal totalCredits() {
        return totalCredits(MarketTransactionService.BANK_EMAIL, DEFAULT_USER_EMAIL);
    }

    private BigDecimal totalCredits(String bankEmail, String userEmail) {
        return userService.getUserByEmail(DEFAULT_USER_EMAIL).getCredits()
                .add(userService.getUserByEmail(MarketTransactionService.BANK_EMAIL).getCredits());
    }

}
