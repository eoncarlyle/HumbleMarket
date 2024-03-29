package com.iainschmitt.prediction.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iainschmitt.prediction.exceptions.NotFoundException;
import com.iainschmitt.prediction.model.Position;
import com.iainschmitt.prediction.model.User;
import com.iainschmitt.prediction.model.rest.AccountReturnData;
import com.iainschmitt.prediction.model.rest.PositionReturnData;
import com.iainschmitt.prediction.repository.MarketRepository;
import com.iainschmitt.prediction.repository.PositionRepository;
import com.iainschmitt.prediction.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private MarketRepository marketRepository;

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        var value = Optional.ofNullable(userRepository.findByEmail(email));
        return value.orElseThrow(() -> new NotFoundException("User with email '%s' does not exist", email));
    }

    public User getUserById(String id) {
        var value = userRepository.findById(id);
        return value.orElseThrow(() -> new NotFoundException("User with id '%s' does not exist", id));
    }

    public Boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }

    public AccountReturnData getAccountReturnData(User user) {
        var positionsReturnData = new ArrayList<PositionReturnData>();
        var positions = positionRepository.findByUserId(user.getId());

        for (final Position position : positions) {
            var market = marketRepository.findById(position.getMarketId()).get();
            positionsReturnData.add(PositionReturnData.builder().marketQuestion(market.getQuestion())
                    .outcomeClaim(market.getOutcomes().get(position.getOutcomeIndex()).getClaim())
                    .direction(position.getDirection()).shares(position.getShares())
                    .priceAtBuy(position.getPriceAtBuy()).build());
        }

        return AccountReturnData.builder().email(user.getEmail()).credits(user.getCredits())
                .positionsReturnData(positionsReturnData).build();
    }
}
