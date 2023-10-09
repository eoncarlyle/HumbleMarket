import { Card } from "react-bootstrap";
import Market from "../../model/Market";
import MarketDetailLink from "./MarketDetailLink";
import MarketOverviewOutcomeBox from "./MarketOverviewOutcomeBox";

import styles from "../../style/MarketCard.module.css";

interface SingleMarketProps {
  market: Market;
}

function SingleMarket({ market }: SingleMarketProps) {
  const closeDate = new Date(market.closeDate);
  const singleMarketURI = "/market/" + market.id;
  return (
    <Card border="dark" className={styles.marketCard}>
      <Card.Body>
        <Card.Title className={styles.marketCardTitle}>{market.question}</Card.Title>
        <Card.Text className={styles.marketCardText}>
          Close Date: {closeDate.toDateString()} at {closeDate.toLocaleTimeString()}
        </Card.Text>
        {/* //TODO This neccesitated turning off strict null checks in tsconfig.json, investigate this later */}
        <MarketOverviewOutcomeBox outcome={market.outcomes.at(0)} singleMarketURI={singleMarketURI} outcomeIndex={0} />
        {market.outcomes.length > 1 ? (
          <MarketOverviewOutcomeBox
            outcome={market.outcomes.at(1)}
            singleMarketURI={singleMarketURI}
            outcomeIndex={1}
          />
        ) : (
          <></>
        )}
        <MarketDetailLink market={market} singleMarketURI={singleMarketURI} />
      </Card.Body>
    </Card>
  );
}

export default SingleMarket;
