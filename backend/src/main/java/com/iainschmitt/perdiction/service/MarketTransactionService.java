package com.iainschmitt.perdiction.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.MarketProposal;
import com.iainschmitt.perdiction.model.MarketProposalBasis;
import com.iainschmitt.perdiction.model.MarketTransaction;
import com.iainschmitt.perdiction.model.MarketTransactionType;
import com.iainschmitt.perdiction.model.Outcome;
import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.model.rest.MarketProposalData;
import com.iainschmitt.perdiction.model.rest.MarketTransactionRequestData;
import com.iainschmitt.perdiction.model.rest.MarketTransactionReturnData;
import com.iainschmitt.perdiction.repository.MarketProposalRepository;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MarketTransactionService {
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ExternalisedConfiguration externalConfig;
    @Autowired
    private MarketProposalRepository marketProposalRepository;

    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public final static RoundingMode ROUNDING_RULE = RoundingMode.HALF_UP;
    public final static int BUFFER_POSITIONS = 1;

    // Not @Transactional, as the only time this is used by itself is for
    public Market createMarket(MarketProposalBasis marketData) {
        // if (!validMarketCreationData(marketData)) {

        // }

        // TODO: consider how we want to accomodate different market making styles in
        // the future: a lot of this math should be moved to the market class

        // For 1 or 2 outcomes, the starting price should be 0.5 for YES outcomes. For 3
        // or more outcomes, \sum P = 1 = P_1 + P_2 + ... + P_3
        var startingPrice = marketData.getOutcomeClaims().size() > 2
                ? toBigDecimal(1d / (marketData.getOutcomeClaims().size()))
                : toBigDecimal(0.5d);
        var startingSharesY = (int) Math.round(unroundedSharesY(startingPrice, marketData.getMarketMakerK()));
        var startingSharesN = (int) Math.round(unroundedSharesN(startingPrice, marketData.getMarketMakerK()));
        var outcomes = new ArrayList<Outcome>() {
            {
                marketData.getOutcomeClaims().forEach(outcomeClaim -> add(
                        new Outcome(outcomeClaim, startingPrice, startingSharesY, startingSharesN)));
            }
        };
        var market = new Market(marketData.getQuestion(), marketData.getCreatorId(), marketData.getMarketMakerK(),
                marketData.getCloseDate(), outcomes, marketData.isPublic(), false, false);

        // TODO: Include non-happy path expcetion handling
        return marketRepository.save(market);
    }

    // TODO: Test
    @Transactional
    public MarketProposal acceptMarketProposal(String marketProposalId) {
        var marketProposal = marketProposalRepository.findById(marketProposalId).get();
        createMarket(marketProposal);
        marketProposalRepository.deleteById(marketProposalId);
        return marketProposal;
    }

    @Transactional
    public MarketProposal rejectMarketProposal(String marketProposalId) {
        var marketProposal = marketProposalRepository.findById(marketProposalId).get();
        marketProposalRepository.deleteById(marketProposalId);
        return marketProposal;
    }

    // TODO: Logging aspects for purchase, sale methods
    public MarketTransaction purchase(String userEmail, MarketTransactionRequestData marketTransactionRequestData) {
        return purchase(
                userService.getUserByEmail(userEmail),
                marketRepository.findById(marketTransactionRequestData.getId()).get(),
                marketTransactionRequestData.getOutcomeIndex(),
                marketTransactionRequestData.getPositionDirection(),
                marketTransactionRequestData.getShares(),
                marketTransactionRequestData.getSharePrice());
    }

    @Transactional
    public MarketTransaction purchase(User user, Market market, int outcomeIndex, PositionDirection direction,
            int shares, BigDecimal sharePrice) {
        // TODO: Package up the logic that is oft-repeated between the other transaction
        // types into methods

        purchaseAndSaleTransactionValidation(market, shares);

        var outcome = market.getOutcome(outcomeIndex);
        var positionPrice = direction.equals(PositionDirection.YES) ? outcome.getPrice()
                : toBigDecimal(1d - outcome.getPrice().doubleValue());
        var tradeValue = positionPrice.multiply(toBigDecimal(shares));
        var bank = getBankUser();

        // TODO pm-15: Move these validations to new method
        purchaseTransactionValidation(user, direction, shares, outcome, sharePrice, tradeValue);

        // Transaction, Position Handling
        var transaction = new MarketTransaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
                MarketTransactionType.PURCHASE, tradeValue);
        var position = new Position(user.getId(), market.getId(), outcomeIndex, direction, shares, positionPrice);

        bank.depositCredits(transaction.getCredits());
        user.withdrawCredits(transaction.getCredits());

        // Outcome pricing changes
        outcome.setPrice(sharePrice);
        outcome.setSharesY(getPurchaseNewSharesY(direction, shares, outcome));
        outcome.setSharesN(getPurchaseNewSharesN(direction, shares, outcome));

        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        positionRepository.save(position);
        transactionRepository.save(transaction);

        return transaction;
    }

    public int getPurchaseNewSharesY(PositionDirection direction, int shares, Outcome outcome) {
        return direction.equals(PositionDirection.YES) ? outcome.getSharesY() - shares : outcome.getSharesY();
    }

    public int getPurchaseNewSharesN(PositionDirection direction, int shares, Outcome outcome) {
        return direction.equals(PositionDirection.NO) ? outcome.getSharesN() - shares : outcome.getSharesN();
    }

    public void purchaseTransactionValidation(User user, PositionDirection direction, int shares,
            Outcome outcome, BigDecimal sharePrice, BigDecimal tradeValue) {
        if (user.getCredits().compareTo(tradeValue) == -1) {
            throw new IllegalArgumentException("Insufficient Funds");
        }

        var yesOverflow = (isYes(direction) && (outcome.getSharesY() - BUFFER_POSITIONS < shares));
        var noOverflow = (isNo(direction) && (outcome.getSharesN() - BUFFER_POSITIONS < shares));
        if (yesOverflow || noOverflow) {
            throw new IllegalArgumentException("Too many shares requested");
        }

        if (!sharePrice.equals(price(getPurchaseNewSharesY(direction, shares, outcome),
                getPurchaseNewSharesN(direction, shares, outcome)))) {
            throw new IllegalArgumentException("Invalid or out-of-date share sale price");
        }

    }

    public MarketTransaction sale(String userEmail, MarketTransactionRequestData marketTransactionRequestData) {
        return sale(
                userService.getUserByEmail(userEmail),
                marketRepository.findById(marketTransactionRequestData.getId()).get(),
                marketTransactionRequestData.getOutcomeIndex(),
                marketTransactionRequestData.getPositionDirection(),
                marketTransactionRequestData.getShares(),
                marketTransactionRequestData.getSharePrice());
    }

    @Transactional
    public MarketTransaction sale(User user, Market market, int outcomeIndex, PositionDirection direction, int shares,
            BigDecimal sharePrice) {

        purchaseAndSaleTransactionValidation(market, shares);

        var outcome = market.getOutcomes().get(outcomeIndex);
        var validPositions = positionRepository.findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(
                user.getId(), market.getId(), outcomeIndex, direction);
        var validUserShares = validPositions.stream().map(position -> position.getShares()).reduce(0,
                (acc, element) -> acc + element);
        var newSharesY = direction.equals(PositionDirection.YES) ? outcome.getSharesY() + shares : outcome.getSharesY();
        var newSharesN = direction.equals(PositionDirection.NO) ? outcome.getSharesN() + shares : outcome.getSharesN();
        var bank = getBankUser();

        saleTransactionValidation(shares, sharePrice, validUserShares, newSharesY, newSharesN);

        var tradeValue = sharePrice.multiply(toBigDecimal(shares));
        var transaction = new MarketTransaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
                MarketTransactionType.SALE, tradeValue);
        bank.withdrawCredits(transaction.getCredits());
        user.depositCredits(transaction.getCredits());

        // TODO pm-15 Extact into two functions: one that returns deleted positions,
        // TODO pm-15 ...another that calculates the modified position
        var shareCount = 0;
        var deletedPositions = new ArrayList<Position>();
        Optional<Position> modifiedPosition = Optional.empty();
        for (Position position : validPositions) {
            if (position.getShares() > (shares - shareCount)) {
                // Current position has more shares than needed to fill sale
                var sharesRemaining = position.getShares() - (shares - shareCount);
                // Any shares not taken by the transaction are left over
                modifiedPosition = Optional.ofNullable(new Position(user.getId(), market.getId(), outcomeIndex,
                        direction, sharesRemaining, position.getPriceAtBuy()));
                break;
            } else if (position.getShares() == (shares - shareCount)) {
                // Current position has exactly enough shares to fill sale
                deletedPositions.add(position);
                break;
            } else {
                // Current position doesn't have enough shares to fill sale
                deletedPositions.add(position);
                shareCount = shareCount + position.getShares();
            }
        }

        // Outcome pricing changes
        outcome.setPrice(sharePrice);
        outcome.setSharesY(newSharesY);
        outcome.setSharesN(newSharesN);

        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        deletedPositions.forEach(position -> positionRepository.delete(position));
        if (modifiedPosition.isPresent()) {
            positionRepository.save(modifiedPosition.get());
        }
        transactionRepository.save(transaction);

        return transaction;
    }

    public void saleTransactionValidation(int shares, BigDecimal sharePrice, int validUserShares, int newSharesY,
            int newSharesN) {
        if (shares > validUserShares) {
            throw new IllegalArgumentException("Insufficient Shares");
        }
        if (!sharePrice.equals(price(newSharesY, newSharesN))) {
            throw new IllegalArgumentException("Invalid or out-of-date share sale price");
        }
    }

    public static boolean priceValidSale(Market market, int outcomeIndex, PositionDirection direction, int shares,
            BigDecimal sharePrice) {
        var outcome = market.getOutcomes().get(outcomeIndex);
        return sharePrice.equals(price(outcome.getSharesY(), outcome.getSharesN()));
    }

    @Scheduled(fixedRateString = "${marketCloseIntervalMinutes}", timeUnit = TimeUnit.MINUTES)
    public void findMarketsPendingClose() {
        var pendingCloseIntervalStart = Instant.now();
        var pendingCloseIntervalEnd = pendingCloseIntervalStart
                .plus(Duration.ofMinutes(Long.valueOf(externalConfig.getMarketCloseIntervalMinutes()))).toEpochMilli();

        marketRepository.findByIsClosedFalseAndCloseDateLessThan(pendingCloseIntervalEnd).forEach(market -> {
            scheduler.schedule(() -> close(market), market.getCloseDate() - pendingCloseIntervalStart.toEpochMilli(),
                    TimeUnit.MILLISECONDS);
        });
    }

    @Transactional
    public MarketTransactionReturnData close(Market market) {
        market.setClosed(true);
        marketRepository.save(market);
        var creator = userService.getUserById(market.getCreatorId());

        // TODO: Fill out notifications
        var message = String.format("Market '%s' has closed, please resolve it by providing the correct answer",
                market.getQuestion());
        creator.addNotification(market.getId(), message);
        userService.saveUser(creator);
        return new MarketTransactionReturnData("");
    }

    @Transactional
    public Market resolve(Market market, int outcomeIndex, PositionDirection direction) {
        if (market.getOutcomes().size() > 1 && direction.equals(PositionDirection.NO)) {
            throw new IllegalArgumentException("Underdefined resolution criteria");
        }
        if (!market.isClosed()) {
            throw new IllegalArgumentException("Attempt to resolve a still open market");
        }
        if (market.getOutcomes().size() <= outcomeIndex) {
            throw new IllegalArgumentException(
                    String.format("Outcome index of '%d' outside of the '%d' possible indexes", outcomeIndex,
                            market.getOutcomes().size()));
        }

        // TODO pm-15: Extract out `transaction` creation
        var transactions = new ArrayList<MarketTransaction>();
        boolean correct;

        for (var position : positionRepository.findByMarketId(market.getId())) {
            if (position.getOutcomeIndex() == outcomeIndex) {
                correct = position.getDirection() == direction;
            } else {
                correct = position.getDirection() != direction;
            }
            if (correct) {
                // Each share redeems at a value of 1
                transactions.add(new MarketTransaction(getBankUserId(), position.getUserId(), market.getId(),
                        outcomeIndex, position.getDirection(), MarketTransactionType.RESOLUTION,
                        toBigDecimal(1d * position.getShares())));
            } else {
                transactions.add(new MarketTransaction(getBankUserId(), position.getUserId(), market.getId(),
                        outcomeIndex, position.getDirection(), MarketTransactionType.RESOLUTION, toBigDecimal(0d)));
            }
        }

        var bank = getBankUser();
        for (var transaction : transactions) {
            var clientUser = userService.getUserById(transaction.getDstUserId());
            clientUser.setCredits(clientUser.getCredits().add(transaction.getCredits()));
            bank.setCredits(bank.getCredits().subtract(transaction.getCredits()));
            userService.saveUser(clientUser);
            transactionRepository.save(transaction);
        }

        userService.saveUser(getBankUser());
        market.setResolved(true);

        return marketRepository.save(market);
    }

    // TODO: think about a more permanent home for this, like a new MarketService
    // class
    public boolean validMarketCreationData(MarketProposalData marketCreationData) {
        if (marketRepository.findByQuestion(marketCreationData.getQuestion()) != null) {
            return false;
        }
        var outcomesSet = new HashSet<String>();
        for (String claim : marketCreationData.getOutcomeClaims()) {
            if (outcomesSet.contains(claim))
                return false;
            outcomesSet.add(claim);
        }
        return true;
    }

    public static BigDecimal toBigDecimal(double val) {
        return new BigDecimal(val).setScale(2, ROUNDING_RULE);
    }

    public static BigDecimal toBigDecimal(int val) {
        return toBigDecimal((double) val);
    }

    public static BigDecimal subtractBigDecimal(BigDecimal minuend, BigDecimal subtrahend) {
        return minuend.subtract(subtrahend);
    }

    public static BigDecimal price(int sharesY, int sharesN) {
        var Y = (double) sharesY;
        var N = (double) sharesN;
        return toBigDecimal(N / (N + Y));
    }

    public List<List<List<BigDecimal>>> getSalePriceList(Market market, User user) {
        List<List<List<BigDecimal>>> returnList = new ArrayList<>();

        for (int outcomeIndex = 0; outcomeIndex < market.getOutcomes().size(); outcomeIndex++) {
            var yesPositions = positionRepository
                    .findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(user.getId(),
                            market.getId(), outcomeIndex, PositionDirection.YES);
            var noPositions = positionRepository
                    .findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(user.getId(),
                            market.getId(), outcomeIndex, PositionDirection.NO);

            var outcome = market.getOutcomes().get(outcomeIndex);
            var yesShares = yesPositions.stream().map(position -> position.getShares()).reduce(0, (a, b) -> a + b);
            List<BigDecimal> yesSalePriceList = new ArrayList<>();
            for (int sharesToSell = 1; sharesToSell <= yesShares; sharesToSell++) {
                yesSalePriceList.add(price(outcome.getSharesY() + sharesToSell, outcome.getSharesN()));
            }

            var noShares = noPositions.stream().map(position -> position.getShares()).reduce(0, (a, b) -> a + b);
            List<BigDecimal> noSalePriceList = new ArrayList<>();
            for (int sharesToSell = 1; sharesToSell <= noShares; sharesToSell++) {
                noSalePriceList.add(price(outcome.getSharesY(), outcome.getSharesN() + sharesToSell));
            }

            returnList.add(List.of(yesSalePriceList, noSalePriceList));
        }
        return returnList;
    }

    public List<List<List<BigDecimal>>> getBuyPriceList(Market market, User user) {
        List<List<List<BigDecimal>>> returnList = new ArrayList<>();

        for (int outcomeIndex = 0; outcomeIndex < market.getOutcomes().size(); outcomeIndex++) {
            var outcome = market.getOutcomes().get(outcomeIndex);
            List<BigDecimal> yesSalePriceList = new ArrayList<>();
            for (int sharesToBuy = 1; sharesToBuy <= outcome.getSharesY() - BUFFER_POSITIONS; sharesToBuy++) {
                yesSalePriceList.add(price(outcome.getSharesY() - sharesToBuy, outcome.getSharesN()));
            }

            List<BigDecimal> noSalePriceList = new ArrayList<>();
            for (int sharesToBuy = 1; sharesToBuy <= outcome.getSharesN() - BUFFER_POSITIONS; sharesToBuy++) {
                noSalePriceList.add(price(outcome.getSharesY(), outcome.getSharesN() - sharesToBuy));
            }

            returnList.add(List.of(yesSalePriceList, noSalePriceList));
        }
        return returnList;
    }

    public MarketProposal processMarketProposal(MarketProposalData marketProposalData) {
        if (marketProposalRepository.existsByQuestion(marketProposalData.getQuestion())) {
            throw new IllegalArgumentException(
                    String.format("Duplicate market question '%s'", marketProposalData.getQuestion()));
        }
        return marketProposalRepository.save(MarketProposal.of(marketProposalData));
    }

    public static double unroundedSharesN(BigDecimal price, int marketMakerK) {
        return Math.sqrt((price.doubleValue() * marketMakerK) / (1d - price.doubleValue()));
    }

    public static double unroundedSharesY(BigDecimal price, int marketMakerK) {
        return marketMakerK / unroundedSharesN(price, marketMakerK);
    }

    public User getAdminUser() {
        return userService.getUserByEmail(externalConfig.getAdminEmail());
    }

    public User getBankUser() {
        return userService.getUserByEmail(externalConfig.getBankEmail());
    }

    public String getBankUserId() {
        return getBankUser().getId();
    }

    public boolean isYes(PositionDirection positionDirection) {
        return positionDirection.equals(PositionDirection.YES);
    }

    public boolean isNo(PositionDirection positionDirection) {
        return positionDirection.equals(PositionDirection.NO);
    }

    public void purchaseAndSaleTransactionValidation(Market market, int shares) {
        if (market.isClosed()) {
            throw new IllegalArgumentException("Cannot transact on closed market");
        } else if (shares < 1) {
            throw new IllegalArgumentException(String.format("Shares to be transacted '%d' must be positive", shares));
        }
    }

    public static BigDecimal purchasePriceCalculator(Market market, int outcomeIndex, PositionDirection direction,
            int shares) {
        var outcome = market.getOutcome(outcomeIndex);
        if (direction.equals(PositionDirection.YES)) {
            return price(outcome.getSharesY() - shares, outcome.getSharesN());
        } else {
            return price(outcome.getSharesY(), outcome.getSharesN() - shares);
        }
    }
}
