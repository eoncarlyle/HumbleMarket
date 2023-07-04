package com.iainschmitt.perdiction.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.PositionDirection;

@Component
@Repository
public interface PositionRepository extends MongoRepository<Position, String> {
    List<Position> findByUserIdAndMarketIdOrderByPriceAtBuyDesc(String userId, String marketId);

    List<Position> findByUserIdAndMarketIdAndOutcomeIndexAndDirectionOrderByPriceAtBuyDesc(String userId,
            String marketId, int outcomeIndex, PositionDirection direction);

    List<Position> findByUserId(String userId);

    List<Position> findByMarketId(String marketId);

    List<Position> deleteByMarketId(String marketId);
}
