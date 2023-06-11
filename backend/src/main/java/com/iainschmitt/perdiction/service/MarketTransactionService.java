package com.iainschmitt.perdiction.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.model.Outcome;
import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.MarketTransaction;
import com.iainschmitt.perdiction.model.TransactionType;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.rest.MarketCreationData;
import com.iainschmitt.perdiction.model.rest.TransactionReturnData;

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

    public final static String ADMIN_EMAIL = "admin@iainschmitt.com";
    public final static String BANK_EMAIL = "bank@iainschmitt.com";
    public final static RoundingMode ROUNDING_RULE = RoundingMode.HALF_UP;

    public TransactionReturnData createMarket(MarketCreationData marketData) {
        // TODO: Implement validation: unique question names and outputs
        marketData.validate();
        // TODO: Consider a better way of doing this
        var seqId = (int) marketRepository.count() + 1;

        // TODO: consider how we want to accomodate different market making styles in
        // the future: a lot of this math should be moved to the market class
        var startingPrice = toBigDecimal(1d / (marketData.getOutcomeClaims().size() + 1d));
        var startingSharesY = (int) Math.round(unroundedSharesY(startingPrice, marketData.getMarketMakerK()));
        var startingSharesN = (int) Math.round(unroundedSharesN(startingPrice, marketData.getMarketMakerK()));
        var outcomes = new ArrayList<Outcome>() {
            {
                marketData.getOutcomeClaims().forEach(outcomeClaim -> add(
                        new Outcome(outcomeClaim, startingPrice, startingSharesY, startingSharesN)));
            }
        };
        var market = new Market(seqId, marketData.getQuestion(), marketData.getCreatorId(),
                marketData.getMarketMakerK(), marketData.getCloseDate(), outcomes, marketData.isPublic(), false, false);

        // TODO: Include non-happy path expcetion handling
        marketRepository.save(market);
        return new TransactionReturnData(
                String.format("Succesful market creation for question '%s'", marketData.getQuestion()));
    }

    @SneakyThrows
    public TransactionReturnData purchase(String userId, String marketId, int outcomeIndex, PositionDirection direction,
            int shares) {
        var user = userService.getUserById(userId);
        var market = marketRepository.findById(marketId).orElseThrow();
        return purchase(user, market, outcomeIndex, direction, shares);
    }

    @SneakyThrows
    public TransactionReturnData purchase(String userEmail, int marketSeqId, int outcomeIndex,
            PositionDirection direction, int shares) {
        var user = userService.getUserByEmail(userEmail);
        var market = marketRepository.findBySeqId(marketSeqId);
        return purchase(user, market, outcomeIndex, direction, shares);
    }

    public TransactionReturnData purchase(User user, Market market, int outcomeIndex, PositionDirection direction,
            int shares) {
        log.info("Purchase transaction attempted for:");
        log.info(String.format("User: %s", user.getEmail()));
        log.info(String.format("Market: %s", market.getId()));
        log.info(String.format("Outcome index: %d", outcomeIndex));
        log.info(String.format("Direction: %s", direction.toString()));

        // TODO: Package up the logic that is oft-repeated between the other transaction
        // types into methods
        if (market.isClosed()) {
            throw new IllegalArgumentException("Cannot transact on closed market");
        }
        var outcome = market.getOutcomes().get(outcomeIndex);
        var positionPrice = direction.equals(PositionDirection.YES) ? outcome.getPrice()
                : toBigDecimal(1d - outcome.getPrice().doubleValue());
        var tradeValue = positionPrice.multiply(toBigDecimal(shares));

        if (user.getCredits().compareTo(tradeValue) == -1) {
            throw new IllegalArgumentException("Insufficient Funds");
        }
        if (direction.equals(PositionDirection.YES) && (outcome.getSharesY() - 2 < shares)) {
            throw new IllegalArgumentException("Too many shares requested");
        } else if (direction.equals(PositionDirection.NO) && (outcome.getSharesN() - 2 < shares)) {
            throw new IllegalArgumentException("Too many shares requested");
        }

        log.info("Purchase validation checks passed");
        // Transaction, Position Handling
        var bank = getBankUser();

        log.info(String.format("Starting bank credits: %f", bank.getCredits()));
        log.info(String.format("Starting user credits: %f", user.getCredits()));

        var transaction = new MarketTransaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
                TransactionType.PURCHASE, tradeValue);
        var position = new Position(user.getId(), market.getId(), outcomeIndex, direction, shares, positionPrice);
        bank.depositCredits(transaction.getCredits());
        user.withdrawCredits(transaction.getCredits());

        log.info(String.format("Post-transaction bank credits: %f", bank.getCredits()));
        log.info(String.format("Post-transaction user credits: %f", user.getCredits()));

        log.info(String.format("Starting shares for outcome:"));
        log.info(String.format("\tYES: %f CR, %d shares", outcome.getPrice(), outcome.getSharesY()));
        log.info(String.format("\tNO: %d shares", outcome.getSharesN()));

        // Outcome pricing changes
        if (direction.equals(PositionDirection.YES)) {
            log.info(String.format("Purchase: Moved YES shares from %d to %d", outcome.getSharesY(),
                    outcome.getSharesY() - shares));
            outcome.setSharesY(outcome.getSharesY() - shares);
        } else {
            log.info(String.format("Purchase: Moved NO shares from %d to %d", outcome.getSharesN(),
                    outcome.getSharesN() - shares));
            outcome.setSharesN(outcome.getSharesN() - shares);
        }
        var newPrice = priceRecalc(outcome.getSharesY(), outcome.getSharesN());
        outcome.setPrice(newPrice);

        var newSharesY = Math.max((int) Math.round(unroundedSharesY(newPrice, market.getMarketMakerK())), 1);
        var newSharesN = Math.max((int) Math.round(unroundedSharesN(newPrice, market.getMarketMakerK())), 1);

        log.info(String.format("Post-recalc shares for outcome:"));
        log.info(String.format("\tYES: %f CR, %d shares", outcome.getPrice(), outcome.getSharesY()));
        log.info(String.format("\tNO: %d shares", outcome.getSharesN()));

        outcome.setSharesY(newSharesY);
        outcome.setSharesN(newSharesN);

        log.info("Saving changes to database");
        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        positionRepository.save(position);
        transactionRepository.save(transaction);

        log.info("Sale transaction succesful");
        return new TransactionReturnData("");
    }

    public TransactionReturnData sale(String userId, String marketId, int outcomeIndex, PositionDirection direction,
            int shares) {
        // Check that user has sufficient shares (shares*price)
        // Create a position in the position table representing the sale
        // Create a sale transaction 'Bank -> User, shares*price'
        // Append the sale transaction ID to the user object
        var user = userService.getUserById(userId);
        var market = marketRepository.findById(marketId).orElseThrow();
        return sale(user, market, outcomeIndex, direction, shares);
    }

    // TODO: Use aspects for string id/market id/object conversion
    public TransactionReturnData sale(String userEmail, int marketSeqId, int outcomeIndex, PositionDirection direction,
            int shares) {
        var user = userService.getUserByEmail(userEmail);
        var market = marketRepository.findBySeqId(marketSeqId);
        return sale(user, market, outcomeIndex, direction, shares);
    }

    // TODO: Depreciate method
    public TransactionReturnData sale(User user, Market market, int outcomeIndex, PositionDirection direction,
            int shares) {
        // TODO: Create aspects for conversion here
        log.info("Sale transaction attempted for:");
        log.info(String.format("User: %s", user.getEmail()));
        log.info(String.format("Market: %s", market.getId()));
        log.info(String.format("Outcome index: %d", outcomeIndex));
        log.info(String.format("Direction: %s", direction.toString()));
        log.info(String.format("Shares: %d", shares));

        // Validation Checks
        if (market.isClosed()) {
            throw new IllegalArgumentException("Cannot transact on closed market");
        }

        var validPositions = positionRepository.findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(
                user.getId(), market.getId(), outcomeIndex, direction);
        var validUserShares = validPositions.stream().map(position -> position.getShares()).reduce(0,
                (acc, element) -> acc + element);
        if (shares > validUserShares) {
            throw new IllegalArgumentException("Insufficient Shares");
        }

        log.info("Sale validation checks passed");

        var bank = getBankUser();
        var outcome = market.getOutcomes().get(outcomeIndex);
        var tradeValue = outcome.getPrice().multiply(toBigDecimal(shares));

        log.info(String.format("Starting bank credits:", bank.getCredits()));
        log.info(String.format("Starting user credits:", user.getCredits()));

        // Transaction, Position Handling
        var transaction = new MarketTransaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
                TransactionType.SALE, tradeValue);
        bank.withdrawCredits(transaction.getCredits());
        user.depositCredits(transaction.getCredits());

        log.info(String.format("Post-transaction bank credits:", bank.getCredits()));
        log.info(String.format("Post-transaction user credits:", user.getCredits()));

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

        log.info(String.format("Starting shares for outcome:"));
        log.info(String.format("\tYES: %f CR, %d shares", outcome.getPrice(), outcome.getSharesY()));
        log.info(String.format("\tNO: %d shares", outcome.getSharesN()));

        // Outcome pricing changes
        if (direction.equals(PositionDirection.YES)) {
            log.info(String.format("Sale: Moved YES shares from %d to %d", outcome.getSharesY(),
                    outcome.getSharesY() + shares));
            outcome.setSharesY(outcome.getSharesY() + shares);
        } else {
            log.info(String.format("Sale: Moved NO shares from %d to %d", outcome.getSharesN(),
                    outcome.getSharesN() + shares));
            outcome.setSharesN(outcome.getSharesN() + shares);
        }

        var price = priceRecalc(outcome.getSharesY(), outcome.getSharesN());
        outcome.setPrice(price);

        var newSharesY = Math.max((int) Math.round(unroundedSharesY(price, market.getMarketMakerK())), 1);
        var newSharesN = Math.max((int) Math.round(unroundedSharesN(price, market.getMarketMakerK())), 1);
        outcome.setSharesY(newSharesY);
        outcome.setSharesN(newSharesN);

        log.info(String.format("Post-recalc shares for outcome:"));
        log.info(String.format("\tYES: %f CR, %d shares", outcome.getPrice(), outcome.getSharesY()));
        log.info(String.format("\tNO: %d shares", outcome.getSharesN()));

        log.info("Saving changes to database");
        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        deletedPositions.forEach(position -> positionRepository.delete(position));
        if (modifiedPosition.isPresent()) {
            positionRepository.save(modifiedPosition.get());
        }
        transactionRepository.save(transaction);
        log.info("Sale transaction succesful");
        // TODO: Fill out
        return new TransactionReturnData("");
    }

    public TransactionReturnData sale(User user, Market market, int outcomeIndex, PositionDirection direction,
            int shares, BigDecimal sharePrice) {
        if (market.isClosed()) {
            throw new IllegalArgumentException("Cannot transact on closed market");
        }

        var outcome = market.getOutcomes().get(outcomeIndex);
        var validPositions = positionRepository.findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(
                user.getId(), market.getId(), outcomeIndex, direction);
        var validUserShares = validPositions.stream().map(position -> position.getShares()).reduce(0,
                (acc, element) -> acc + element);
        var newSharesY = direction.equals(PositionDirection.YES) ? outcome.getSharesY() + shares : outcome.getSharesY();
        var newSharesN = direction.equals(PositionDirection.NO) ? outcome.getSharesN() + shares : outcome.getSharesN();

        if (shares > validUserShares) {
            throw new IllegalArgumentException("Insufficient Shares");
        }
        if (!sharePrice.equals(price(newSharesY, newSharesN))) {
            throw new IllegalArgumentException("Invalid or out-of-date share sale price");
        }

        log.info("Sale validation checks passed");
        var bank = getBankUser();
        var tradeValue = sharePrice.multiply(toBigDecimal(shares));

        log.info(String.format("Starting bank credits:", bank.getCredits()));
        log.info(String.format("Starting user credits:", user.getCredits()));

        // Transaction, Position Handling
        var transaction = new MarketTransaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
                TransactionType.SALE, tradeValue);
        bank.withdrawCredits(transaction.getCredits());
        user.depositCredits(transaction.getCredits());

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

        log.info(String.format("Starting shares for outcome:"));
        log.info(String.format("\tYES: %f CR, %d shares", outcome.getPrice(), outcome.getSharesY()));
        log.info(String.format("\tNO: %d shares", outcome.getSharesN()));

        // Outcome pricing changes
        if (direction.equals(PositionDirection.YES)) {
            log.info(String.format("Sale: Moved YES shares from %d to %d", outcome.getSharesY(), newSharesY));
            outcome.setSharesY(newSharesY);
        } else {
            log.info(String.format("Sale: Moved NO shares from %d to %d", outcome.getSharesN(), newSharesN));
            outcome.setSharesN(newSharesN);
        }

        outcome.setPrice(sharePrice);
        outcome.setSharesY(newSharesY);
        outcome.setSharesN(newSharesN);

        log.info("Saving changes to database");
        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        deletedPositions.forEach(position -> positionRepository.delete(position));
        if (modifiedPosition.isPresent()) {
            positionRepository.save(modifiedPosition.get());
        }
        transactionRepository.save(transaction);
        log.info("Sale transaction succesful");
        // TODO: Fill out
        return new TransactionReturnData("");
    }

    public static boolean priceValidSale(Market market, int outcomeIndex, PositionDirection direction, int shares,
            BigDecimal sharePrice) {
        var outcome = market.getOutcomes().get(outcomeIndex);
        return sharePrice.equals(price(outcome.getSharesY(), outcome.getSharesN()));
    }

    public TransactionReturnData close(Market market, int correctOutcomeIndex) {
        market.setClosed(true);
        marketRepository.save(market);
        var creator = userService.getUserById(market.getCreatorId());

        // TODO: Fill out notifications
        var message = String.format("Market '%s' has closed, please resolve it by providing the correct answer",
                market.getQuestion());
        creator.addNotification(market.getId(), message);
        userService.saveUser(creator);
        return new TransactionReturnData("");
    }

    public TransactionReturnData resolve(Market market, int outcomeIndex, PositionDirection direction) {

        // TODO: throw exception for markets that aren't closed
        if (market.getOutcomes().size() > 1 && direction.equals(PositionDirection.NO)) {
            throw new IllegalArgumentException("Underdefined resolution criteria");
        }

        var transactions = new ArrayList<MarketTransaction>();
        boolean correct;

        for (var position : positionRepository.findByMarketId(market.getId())) {
            if (position.getOutcomeIndex() == outcomeIndex) {
                correct = position.getDirection() == direction;
            } else {
                correct = position.getDirection() != direction;
            }
            if (correct) {
                // Each share redeems at a credit value of 1
                transactions.add(new MarketTransaction(getBankUserId(), position.getUserId(), market.getId(),
                        outcomeIndex, position.getDirection(), TransactionType.RESOLUTION,
                        toBigDecimal(1d * position.getShares())));
            } else {
                transactions.add(new MarketTransaction(getBankUserId(), position.getUserId(), market.getId(),
                        outcomeIndex, position.getDirection(), TransactionType.RESOLUTION, toBigDecimal(0d)));
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
        positionRepository.deleteByMarketId(market.getId());
        return new TransactionReturnData("");
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

    public static BigDecimal priceRecalc(int sharesY, int sharesN) {
        var Y = (double) sharesY;
        var N = (double) sharesN;
        return toBigDecimal(N / (N + Y));
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
                .findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(user.getId(), market.getId(), outcomeIndex, PositionDirection.YES);
            var noPositions = positionRepository
                .findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(user.getId(), market.getId(), outcomeIndex, PositionDirection.NO);

            var outcome = market.getOutcomes().get(outcomeIndex);
            var yesShares = yesPositions.stream().map(position -> position.getShares()).reduce(0, (a, b) -> a + b);
            List<BigDecimal> yesSalePriceList = new ArrayList<>();
            for (int sharesToSell = 1; sharesToSell <= yesShares; sharesToSell++) {
                yesSalePriceList.add(price(outcome.getSharesY() + sharesToSell, outcome.getSharesN()));
            }
         
            var noShares = noPositions.stream().map(position -> position.getShares()).reduce(0, (a, b) -> a + b); 
            List<BigDecimal> noSalePriceList = new ArrayList<>();
            for (int sharesToSell = 1; sharesToSell <= noShares; sharesToSell++) {
                noSalePriceList.add(price(outcome.getSharesY(), outcome.getSharesN() + sharesToSell ));
            }

            returnList.add(List.of(yesSalePriceList, noSalePriceList));
        }

        return returnList;
    }

    public static double unroundedSharesN(BigDecimal price, int marketMakerK) {
        return Math.sqrt((price.doubleValue() * marketMakerK) / (1d - price.doubleValue()));
    }

    public static double unroundedSharesY(BigDecimal price, int marketMakerK) {
        return marketMakerK / unroundedSharesN(price, marketMakerK);
    }

    public User getBankUser() {
        return userService.getUserByEmail(BANK_EMAIL);
    }

    public String getBankUserId() {
        return getBankUser().getId();
    }

}
