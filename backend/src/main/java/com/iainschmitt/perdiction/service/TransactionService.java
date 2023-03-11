package com.iainschmitt.perdiction.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.repository.UserRepository;
import com.iainschmitt.perdiction.repository.TransactionRepository;

import lombok.Setter;
import lombok.SneakyThrows;

import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.model.Outcome;
import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.Transaction;
import com.iainschmitt.perdiction.model.TransactionType;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.model.rest.MarketData;
import com.iainschmitt.perdiction.model.rest.MarketReturnData;

@Service
public class TransactionService {
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
    public final static MathContext MATH_CONTEXT = new MathContext(2, RoundingMode.CEILING);

    public MarketReturnData createMarket(MarketData marketData) {
        // TODO: Implement validation: unique question names and outputs
        marketData.validate();
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
        return new MarketReturnData(
                String.format("Succesful market creation for question '%s'", marketData.getQuestion()));
    }

    @SneakyThrows
    public MarketReturnData purchase(String userId, String marketId, int outcomeIndex, PositionDirection direction,
            int shares) {
        var user = userService.getUserById(userId);
        var market = marketRepository.findById(marketId).orElseThrow();
        return purchase(user, market, outcomeIndex, direction, shares);
    }

    public MarketReturnData purchase(User user, Market market, int outcomeIndex, PositionDirection direction,
            int shares) {
        // TODO: Package up the logic that is oft-repeated between the other transaction
        // types into methods

        // Validation Checks
        if (market.isClosed()) {
            throw new IllegalArgumentException("Cannot transact on closed market");
        }
        // ! Might be playing a dangerous game with respect to the type conversions and
        // rounding, neccesitates later checks
        var outcome = market.getOutcomes().get(outcomeIndex);
        var positionPrice = direction.equals(PositionDirection.YES) ? outcome.getPrice()
                : toBigDecimal(1d - outcome.getPrice().doubleValue());
        var tradeValue = positionPrice.doubleValue() * shares;

        if (tradeValue > user.getCredits()) {
            throw new IllegalArgumentException("Insufficient Funds");
        }
        if (direction.equals(PositionDirection.YES) && (outcome.getSharesY() - 2 < shares)) {
            throw new IllegalArgumentException(
                    "Too many shares requested, at least two remaining shares need to be purchased");
        } else if (direction.equals(PositionDirection.NO) && (outcome.getSharesN() - 2 < shares)) {
            throw new IllegalArgumentException(
                    "Too many shares requested, at least two remaining shares need to be purchased");
        }

        // Transaction, Position Handling
        var bank = getBankUser();
        var transaction = new Transaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
                TransactionType.PURCHASE, tradeValue);
        var position = new Position(user.getId(), market.getId(), outcomeIndex, direction, shares, positionPrice);
        bank.depositCredits(transaction.getCredits());
        user.withdrawCredits(transaction.getCredits());

        // Outcome pricing changes
        if (direction.equals(PositionDirection.YES)) {
            outcome.setSharesY(outcome.getSharesY() - shares);
        } else {
            outcome.setSharesN(outcome.getSharesN() - shares);
        }
        var newPrice = priceRecalc(outcome.getSharesY(), outcome.getSharesN());
        outcome.setPrice(newPrice);
        outcome.setSharesY((int) Math.round(unroundedSharesY(newPrice, market.getMarketMakerK())));
        outcome.setSharesN((int) Math.round(unroundedSharesN(newPrice, market.getMarketMakerK())));

        // Saving Changes
        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        positionRepository.save(position);
        transactionRepository.save(transaction);

        // TODO: Fill out
        return new MarketReturnData("");
    }

    public MarketReturnData sale(String userId, String marketId, int outcomeIndex, PositionDirection direction,
            int shares) {
        // Check that user has sufficient shares (shares*price)
        // Create a position in the position table representing the sale
        // Create a sale transaction 'Bank -> User, shares*price'
        // Append the sale transaction ID to the user object
        var user = userService.getUserById(userId);
        var market = marketRepository.findById(marketId).orElseThrow();
        return sale(user, market, outcomeIndex, direction, shares);
    }

    public MarketReturnData sale(User user, Market market, int outcomeIndex, PositionDirection direction, int shares) {

        // Validation Checks
        if (market.isClosed()) {
            throw new IllegalArgumentException("Cannot transact on closed market");
        }
        var bank = getBankUser();
        var outcome = market.getOutcomes().get(outcomeIndex);
        var tradeValue = outcome.getPrice().doubleValue() * shares;

        var validPositions = positionRepository.findByUserIdAndMarketIdOrderByPriceAtBuyDesc(user.getId(),
                market.getId());
        var validUserShares = validPositions.stream().map(position -> position.getShares()).reduce(0,
                (acc, element) -> acc + element);
        if (shares > validUserShares) {
            throw new IllegalArgumentException("Insufficient Shares");
        }

        // Transaction, Position Handling
        var transaction = new Transaction(user.getId(), getBankUserId(), market.getId(), outcomeIndex, direction,
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

        // Outcome pricing changes
        if (direction.equals(PositionDirection.YES)) {
            outcome.setSharesY(outcome.getSharesY() + shares);
        } else {
            outcome.setSharesN(outcome.getSharesN() + shares);
        }

        var price = priceRecalc(outcome.getSharesY(), outcome.getSharesN());
        outcome.setPrice(price);
        outcome.setSharesY((int) Math.round(unroundedSharesY(price, market.getMarketMakerK())));
        outcome.setSharesN((int) Math.round(unroundedSharesN(price, market.getMarketMakerK())));

        // Saving Changes
        marketRepository.save(market);
        userService.saveUser(user);
        userService.saveUser(bank);
        if (modifiedPosition.isPresent()) {
            positionRepository.save(modifiedPosition.get());
        }
        transactionRepository.save(transaction);
        // TODO: Fill out
        return new MarketReturnData("");
    }

    public MarketReturnData close(Market market, int correctOutcomeIndex) {
        market.setClosed(true);
        marketRepository.save(market);
        var creator = userService.getUserById(market.getCreatorId());
        var message = String.format("Market '%s' has closed, please resolve it by providing the correct answer",
                market.getQuestion());
        // TODO: Figure out where link should go
        creator.addNotification(market.getId(), message, "");
        return new MarketReturnData("");
    }

    public MarketReturnData resolve(Market market, int outcomeIndex, PositionDirection direction) {
        if (market.getOutcomes().size() > 1 && direction.equals(PositionDirection.NO)) {
            throw new IllegalArgumentException("Underdefined resolution criteria");
        }

        var transactions = new ArrayList<Transaction>();
        Boolean correct;

        for (var position : positionRepository.findByMarketId(market.getId())) {
            if (position.getOutcomeIndex() == outcomeIndex) {
                correct = position.getDirection() == direction;
            } else {
                correct = position.getDirection() != direction;
            }
            if (correct) {
                // Each share redeems at a credit value of 1
                transactions.add(new Transaction(getBankUserId(), position.getUserId(), market.getId(), outcomeIndex,
                        position.getDirection(), TransactionType.RESOLUTION, (float) position.getShares()));
            } else {
                transactions.add(new Transaction(getBankUserId(), position.getUserId(), market.getId(), outcomeIndex,
                        position.getDirection(), TransactionType.RESOLUTION, 0));
            }
        }

        var bank = getBankUser();
        for (var transaction : transactions) {
            var clientUser = userService.getUserById(transaction.getDstUserId());
            clientUser.setCredits(clientUser.getCredits() + transaction.getCredits());
            bank.setCredits(bank.getCredits() - transaction.getCredits());
            userService.saveUser(clientUser);
        }
        userService.saveUser(getBankUser());
        positionRepository.deleteByMarketId(market.getId());

        // TODO: Fill out
        return new MarketReturnData("");
    }

    public static BigDecimal toBigDecimal(double val) {
        return new BigDecimal(val, MATH_CONTEXT);
    }

    public static BigDecimal priceRecalc(int sharesY, int sharesN) {
        var Y = (double) sharesY;
        var N = (double) sharesN;
        return toBigDecimal(N / (N + Y));
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
        // TODO: Provide startup instruction for creating bank user if it does not exist
        return getBankUser().getId();
    }

}
