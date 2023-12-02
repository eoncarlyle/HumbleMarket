package com.iainschmitt.perdiction.service;

import static com.iainschmitt.perdiction.service.MarketTransactionService.price;
import static com.iainschmitt.perdiction.service.MarketTransactionService.priceValidSale;
import static com.iainschmitt.perdiction.service.MarketTransactionService.toBigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.MarketTransaction;
import com.iainschmitt.perdiction.model.MarketTransactionType;
import com.iainschmitt.perdiction.model.Outcome;
import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.model.rest.MarketProposalData;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;

import lombok.SneakyThrows;

@SpringBootTest
public class MarketTransactionServiceTests {
    @InjectMocks
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
    private ExternalisedConfiguration externalConfig;

    public String DEFAULT_USER_EMAIL = "user1@iainschmitt.com";

    @BeforeEach
    @SneakyThrows
    void clearAndSetupTestDB() {
        marketRepository.deleteAll();
        userService.deleteAll();
        positionRepository.deleteAll();
        transactionRepository.deleteAll();

        userService.saveUser(User.of(externalConfig.getAdminEmail()));

        var bank = User.of(externalConfig.getBankEmail());
        bank.setCredits(toBigDecimal(1_000_000d));
        userService.saveUser(bank);
    }

    @Test
    public void createMarketSingleOutcome_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(user.getId());

        marketTransactionService.createMarket(marketData);
        assertThat(marketRepository.existsByQuestion(marketData.getQuestion())).isTrue();
        var outcome = marketRepository.findByQuestion(marketData.getQuestion()).getOutcomes().get(0);
        assertThat(outcome.getPrice()).isEqualTo(toBigDecimal(0.5d));
        assertThat(outcome.getSharesN()).isEqualTo(10);
        assertThat(outcome.getSharesY()).isEqualTo(10);
    }

    @Test
    public void createMarketMultiOutcome_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(externalConfig.getAdminEmail());

        marketTransactionService.createMarket(marketData);
        assertThat(marketRepository.existsByQuestion(marketData.getQuestion())).isTrue();
        var firstOutcome = marketRepository.findByQuestion(marketData.getQuestion()).getOutcomes().get(0);
        assertThat(firstOutcome.getPrice()).isEqualTo(toBigDecimal(1d / 2d));
        assertThat(firstOutcome.getSharesN()).isEqualTo(10);
        assertThat(firstOutcome.getSharesY()).isEqualTo(10);
    }

    @Test
    public void createMarketThreeOutcome_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        userService.saveUser(user);

        final MarketProposalData marketData = threeOutcomeMarket(getAdminId());

        marketTransactionService.createMarket(marketData);
        assertThat(marketRepository.existsByQuestion(marketData.getQuestion())).isTrue();
        var firstOutcome = marketRepository.findByQuestion(marketData.getQuestion()).getOutcomes().get(0);
        assertThat(firstOutcome.getPrice()).isEqualTo(toBigDecimal(1d / 3d));
        assertThat(firstOutcome.getSharesN()).isEqualTo(7);
        assertThat(firstOutcome.getSharesY()).isEqualTo(14);
    }

    @Test
    public void purchase_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        var bank = marketTransactionService.getBankUser();
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var outcomeIndex = 0;
        var outcome = market.getOutcome(outcomeIndex);
        var initialPrice = market.getOutcome(outcomeIndex).getPrice();
        var shares = 3;
        var startingCredits = bank.getCredits().add(user.getCredits());
        var initialTotalCredits = totalCredits();

        marketTransactionService.purchase(user, market, outcomeIndex, PositionDirection.NO, shares,
                MarketTransactionService.purchasePriceCalculator(market, outcomeIndex, PositionDirection.NO, shares));
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        bank = marketTransactionService.getBankUser();
        user = userService.getUserByEmail(user.getEmail());

        var markets = marketRepository.findAll();
        var transactions = transactionRepository.findAll();
        var positions = positionRepository.findAll();
        outcome = markets.get(0).getOutcome(outcomeIndex);

        assertThat(bank.getCredits().doubleValue()).isEqualTo(1_000_000d + shares * initialPrice.doubleValue());
        assertThat(user.getCredits().doubleValue()).isEqualTo(100d - shares * initialPrice.doubleValue());
        assertThat(markets.size()).isEqualTo(1);
        assertThat(transactions.size()).isEqualTo(1);
        assertThat(positions.size()).isEqualTo(1);
        assertThat(bank.getCredits().add(user.getCredits()).doubleValue()).isEqualTo(startingCredits.doubleValue());

        assertThat(outcome.getSharesN()).isEqualTo(7);
        assertThat(outcome.getSharesY()).isEqualTo(10);

        var marketTransaction = transactions.get(0);
        assertThat(marketTransaction.getDirection()).isEqualTo(PositionDirection.NO);
        assertThat(marketTransaction.getTransactionType()).isEqualTo(MarketTransactionType.PURCHASE);
        assertThat(marketTransaction.getCredits().doubleValue()).isEqualTo(1.5d);

        var position = positions.get(0);
        assertThat(position.getShares()).isEqualTo(3);
        assertThat(positions.get(0).getDirection()).isEqualTo(PositionDirection.NO);
    }

    // TODO pm-15: sale_InvalidSalePrice method needed

    @Test
    public void purchaseMultiOutcome_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultMultiOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
        var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
        var shares = 3;
        var initialTotalCredits = totalCredits();

        marketTransactionService.purchase(user, market, 0, PositionDirection.NO, shares,
                MarketTransactionService.purchasePriceCalculator(market, 0, PositionDirection.NO, shares));

        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
        assertThat(positionRepository.findAll().get(0).getPriceAtBuy())
                .isEqualTo(toBigDecimal(1d - initialOutcomeZeroPrice.doubleValue()));

        marketTransactionService.purchase(user, market, 1, PositionDirection.YES, shares,
                MarketTransactionService.purchasePriceCalculator(market, 1, PositionDirection.YES, shares));
        assertThat(positionRepository.findAll().get(1).getPriceAtBuy()).isEqualTo(initialOutcomeOnePrice);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void purchased_ClosedMarketFailure() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        market.setClosed(true);
        marketRepository.save(market);
        assertThatThrownBy(
                () -> marketTransactionService.purchase(user, market, 0, PositionDirection.NO, 3, toBigDecimal(0.5d)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Cannot transact on closed market");
    }

    @Test
    public void purchase_InsufficientFundsFailure() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(1d));
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        assertThatThrownBy(() -> marketTransactionService.purchase(user, market, 0, PositionDirection.NO, 3,
                MarketTransactionService.purchasePriceCalculator(market, 0, PositionDirection.NO, 3)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Insufficient Funds");
    }

    @Test
    public void purchase_TooManySharesFailure() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        assertThatThrownBy(
                () -> marketTransactionService.purchase(user, market, 0, PositionDirection.NO, 10,
                        MarketTransactionService.purchasePriceCalculator(market, 0, PositionDirection.NO, 10)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Too many shares requested");
    }

    @Test
    public void sale_Success() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var outcomeIndex = 0;

        Supplier<Market> market = () -> marketRepository.findAll().get(0);
        Supplier<Outcome> outcome = () -> market.get().getOutcome(outcomeIndex);
        var shares = 3;
        var initialTotalCredits = totalCredits();

        var firstPrice = outcome.get().getPrice();
        marketTransactionService.purchase(user, market.get(), outcomeIndex, PositionDirection.YES, shares,
                MarketTransactionService.purchasePriceCalculator(market.get(), outcomeIndex, PositionDirection.YES,
                        shares));
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var secondPrice = outcome.get().getPrice();
        marketTransactionService.purchase(user, market.get(), outcomeIndex, PositionDirection.YES, shares,
                MarketTransactionService.purchasePriceCalculator(market.get(), outcomeIndex, PositionDirection.YES,
                        shares));
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
        marketTransactionService.sale(user, market.get(), outcomeIndex, PositionDirection.YES, shares + 1,
                proposedPrice);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void sale_InvalidSalePrice() {
        var user = User.of(DEFAULT_USER_EMAIL);
        user.setCredits(toBigDecimal(100d));
        userService.saveUser(user);
        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);

        Supplier<Market> market = () -> marketRepository.findAll().get(0);
        Supplier<Outcome> outcome = () -> market.get().getOutcomes().get(0);
        var shares = 3;
        var initialTotalCredits = totalCredits();

        marketTransactionService.purchase(user, market.get(), 0, PositionDirection.YES, shares,
                MarketTransactionService.purchasePriceCalculator(market.get(), 0, PositionDirection.YES, shares));
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

        var newSharesY = outcome.get().getSharesY() + shares + 1;
        var newSharesN = outcome.get().getSharesN();
        var incorrectProposedPrice = MarketTransactionService.price(newSharesY + 1, newSharesN);
        assertThatThrownBy(() -> marketTransactionService.sale(user, market.get(), 0, PositionDirection.YES, 1,
                incorrectProposedPrice)).isInstanceOf(IllegalArgumentException.class);
        assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    }

    @Test
    public void salePriceList() {
        var user0 = User.of(DEFAULT_USER_EMAIL);
        var startingCredits = toBigDecimal(100d);
        user0.setCredits(startingCredits);
        userService.saveUser(user0);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
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

        assertThat(marketTransactionService.getSalePriceList(market, user0))
                .isEqualTo(List.of(List.of(yesSalePriceList, noSalePriceList)));
    }

    @Test
    public void buyPriceList() {
        var user0 = User.of(DEFAULT_USER_EMAIL);
        var startingCredits = toBigDecimal(100d);
        user0.setCredits(startingCredits);
        userService.saveUser(user0);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
        var market = marketRepository.findAll().get(0);
        var selectedOutcomeIndex = 0;

        List<BigDecimal> yesBuyPriceList = new ArrayList<>();
        List<BigDecimal> noBuyPriceList = new ArrayList<>();
        var availableYesShares = market.getOutcomes().get(selectedOutcomeIndex).getSharesY();
        var availableNoShares = market.getOutcomes().get(selectedOutcomeIndex).getSharesN();
        assertThat(availableYesShares).isEqualTo(availableNoShares);

        for (int sharesToBuy = 1; sharesToBuy <= availableYesShares
                - MarketTransactionService.BUFFER_POSITIONS; sharesToBuy++) {
            yesBuyPriceList.add(MarketTransactionService.price(availableYesShares - sharesToBuy, availableYesShares));
            noBuyPriceList.add(MarketTransactionService.price(availableYesShares, availableNoShares - sharesToBuy));
        }

        assertThat(marketTransactionService.getBuyPriceList(market, user0))
                .isEqualTo(List.of(List.of(yesBuyPriceList, noBuyPriceList)));
    }

    @Test
    public void saleMultiOutcome_Success() {
    }

    // @Test
    // public void old_saleMultiOutcome_Success() {
    // var user = User.of(DEFAULT_USER_EMAIL);
    // var bank = transactionService.getBankUser();
    // user.setCredits(toBigDecimal(100d));
    // userService.saveUser(user);

    // var marketData = defaultMultiOutcomeMarket(getAdminId());
    // transactionService.createMarket(marketData);
    // var market = marketRepository.findAll().get(0);
    // var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
    // var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
    // var shares = 3;
    // var initialTotalCredits = totalCredits();

    // transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0,
    // PositionDirection.YES, shares);
    // assertThat(positionRepository.findAll().get(0).getPriceAtBuy()).isEqualTo(initialOutcomeZeroPrice);
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

    // transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 1,
    // PositionDirection.NO, shares);
    // assertThat(positionRepository.findAll().get(1).getPriceAtBuy())
    // .isEqualTo(toBigDecimal(1d - initialOutcomeOnePrice.doubleValue()));
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

    // transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 0,
    // PositionDirection.YES, 2);
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    // transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 1,
    // PositionDirection.NO, 2);
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    // }

    @Test
    public void sale_InsufficientSharesFailure() {
    }

    // // TODO: Deprecate test
    // @Test
    // public void old_sale_InsufficientSharesFailure() {
    // var user = User.of(DEFAULT_USER_EMAIL);
    // var bank = transactionService.getBankUser();
    // user.setCredits(toBigDecimal(100d));
    // userService.saveUser(user);

    // var marketData = defaultMultiOutcomeMarket(getAdminId());
    // transactionService.createMarket(marketData);
    // var market = marketRepository.findAll().get(0);
    // var initialOutcomeZeroPrice = market.getOutcomes().get(0).getPrice();
    // var initialOutcomeOnePrice = market.getOutcomes().get(1).getPrice();
    // var shares = 3;
    // var initialTotalCredits = totalCredits();

    // // Only buy transaction
    // transactionService.purchase(DEFAULT_USER_EMAIL, market.getSeqId(), 0,
    // PositionDirection.YES, shares);
    // assertThat(positionRepository.findAll().get(0).getPriceAtBuy()).isEqualTo(initialOutcomeZeroPrice);
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

    // // Valid sale of 2 shares of 0/Yes
    // transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 0,
    // PositionDirection.YES, 2);
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());

    // // Invalid sale of 1 share of 1/No
    // assertThatThrownBy(
    // () -> transactionService.sale(DEFAULT_USER_EMAIL, market.getSeqId(), 1,
    // PositionDirection.NO, 1))
    // .isInstanceOf(IllegalArgumentException.class);
    // assertThat(totalCredits().doubleValue()).isEqualTo(initialTotalCredits.doubleValue());
    // }

    @Test
    @SneakyThrows
    public void findMarketsPendingClose_Success() {
        var firstMarket = shortTermSingleOutcomeMarket("First question", externalConfig.getAdminEmail(), 3L);
        var secondMarket = shortTermSingleOutcomeMarket("Second question", externalConfig.getAdminEmail(), 4L);
        marketTransactionService.createMarket(firstMarket);
        marketTransactionService.createMarket(secondMarket);

        marketTransactionService.findMarketsPendingClose();
        assertThat(marketRepository.findByQuestion(firstMarket.getQuestion()).isClosed()).isEqualTo(false);
        assertThat(marketRepository.findByQuestion(secondMarket.getQuestion()).isClosed()).isEqualTo(false);
        Thread.sleep(Duration.ofSeconds(5L).toMillis());

        assertThat(marketRepository.findByQuestion(firstMarket.getQuestion()).isClosed()).isEqualTo(true);
        assertThat(marketRepository.findByQuestion(secondMarket.getQuestion()).isClosed()).isEqualTo(true);
    }

    @Test
    public void sale_ClosedMarketFailure() {
        // TODO: Test with multiple positions so that all validUserShares code paths are
    }

    @Test
    public void close_SingleOutomce() {

    }

    @Test
    public void resolve_Success() {
        var user0 = User.of(DEFAULT_USER_EMAIL);
        var user1 = User.of("user2@iainschmitt.com");
        var startingCredits = toBigDecimal(98.5d);
        user0.setCredits(startingCredits);
        user1.setCredits(startingCredits);
        userService.saveUser(user0);
        userService.saveUser(user1);

        var marketData = defaultSingleOutcomeMarket(getAdminId());
        marketTransactionService.createMarket(marketData);
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
        marketTransactionService.close(market);

        var price = MarketTransactionService.price(
                market.getOutcomes().get(selectedOutcomeIndex).getSharesY() + tradedShares,
                market.getOutcomes().get(selectedOutcomeIndex).getSharesN());

        assertThatThrownBy(() -> marketTransactionService.sale(user0, market, selectedOutcomeIndex,
                PositionDirection.YES, tradedShares, price)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transact on closed market");
        assertThatThrownBy(
                () -> marketTransactionService.purchase(user0, market, selectedOutcomeIndex, PositionDirection.YES, 3,
                        toBigDecimal(0.5d)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transact on closed market");

        marketTransactionService.resolve(market, selectedOutcomeIndex, PositionDirection.YES);

        assertThat(userService.getUserById(user0.getId()).getCredits().doubleValue()).isEqualTo(101.5d);
        assertThat(userService.getUserById(user1.getId()).getCredits().doubleValue()).isEqualTo(98.5d);
    }

    @Test
    public void resolve_UnderdefinedFailure() {
        // TODO
    }

    @Test
    public void validMarketCreationData_Valid() {
        marketTransactionService.validMarketCreationData(defaultMultiOutcomeMarket(externalConfig.getAdminEmail()));
    }

    @Test
    public void validMarketCreationData_DuplicateQuestion() {
        marketTransactionService.createMarket(defaultSingleOutcomeMarket(DEFAULT_USER_EMAIL));
        assertThat(marketTransactionService.validMarketCreationData(defaultSingleOutcomeMarket(DEFAULT_USER_EMAIL)))
                .isEqualTo(false);
    }

    @Test
    public void validMarketCreationData_DuplicateClaims() {
        var invalidSingleOutcomeMarket = MarketProposalData.of("What will the temperature in Minneapolis be in 1 hour?",
                getAdminId(), 100,
                Instant.now().plus(Duration.ofHours(1L)).toEpochMilli(),
                outcomeClaimsList("Between 40 °F and 50 °F", "Between 40 °F and 50 °F"), true);
        assertThat(marketTransactionService.validMarketCreationData(invalidSingleOutcomeMarket)).isEqualTo(false);
    }

    public String getAdminId() {
        return userService.getUserByEmail(externalConfig.getAdminEmail()).getId();
    }

    public String getBankId() {
        return userService.getUserByEmail(externalConfig.getBankEmail()).getId();
    }

    private static MarketProposalData defaultSingleOutcomeMarket(String creatorId) {
        return MarketProposalData.of("What will the temperature in Minneapolis be in 1 hour?", creatorId, 100,
                Instant.now().plus(Duration.ofHours(1L)).toEpochMilli(), outcomeClaimsList("Greater than 40 °F"), true);
    }

    private MarketProposalData defaultMultiOutcomeMarket(String creatorId) {
        return MarketProposalData.of("What will the temperature in Minneapolis be in 1 hour?", creatorId, 100,
                Instant.now().plus(Duration.ofHours(1L)).toEpochMilli(),
                outcomeClaimsList("Between 40 °F and 50 °F", "Outside this range"), true);
    }

    private MarketProposalData shortTermSingleOutcomeMarket(String question, String creatorId, long secondDuration) {
        return MarketProposalData.of(question, creatorId, 100,
                Instant.now().plus(Duration.ofSeconds(secondDuration)).toEpochMilli(),
                outcomeClaimsList("Greater than 40 °F"), true);
    }

    private MarketProposalData threeOutcomeMarket(String creatorId) {
        return MarketProposalData.of("What will the temperature in Minneapolis be in 1 hour?", creatorId, 100,
                Instant.now().plus(Duration.ofHours(1L)).toEpochMilli(),
                outcomeClaimsList("Less than 40 °F", "Between 40 °F and 50 °F", "Greater than 50 °F"), true);
    }

    private BigDecimal totalCredits() {
        return totalCredits(externalConfig.getBankEmail(), DEFAULT_USER_EMAIL);
    }

    private BigDecimal totalCredits(String bankEmail, String userEmail) {
        return userService.getUserByEmail(DEFAULT_USER_EMAIL).getCredits()
                .add(userService.getUserByEmail(externalConfig.getBankEmail()).getCredits());
    }

    private static ArrayList<String> outcomeClaimsList(String... args) {
        var outcomeClaimsList = new ArrayList<String>();
        for (String arg : args)
            outcomeClaimsList.add(arg);
        return outcomeClaimsList;
    }

    // public MarketTransaction purchase(User user, Market market, int outcomeIndex,
    // PositionDirection direction,
    // int shares, BigDecimal sharePrice)

    // marketTransactionService.purchase(user, market, 0, PositionDirection.NO,
    // shares,
    // toBigDecimal(1d - secondPrice.doubleValue()));
}
