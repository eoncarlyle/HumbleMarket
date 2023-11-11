import { Card, Button } from "react-bootstrap";

import Market from "../../model/Market";
import Outcome from "../../model/Outcome";
import MarketResolutionState from "../../model/MarketResolutionState";
import processMarketResolution from "../../util/ProcessMarketResolution";

import styles from "../../style/MarketCard.module.css";

interface MarketResolutionCardProps {
  market: Market;
  marketResolutionState: MarketResolutionState;
  setMarketResolutionState: React.Dispatch<React.SetStateAction<MarketResolutionState>>;
}

export default function MarketResolutionCard({
  market,
  marketResolutionState,
  setMarketResolutionState,
}: MarketResolutionCardProps) {
  //const outcomeButtons = market.outcomes.map((outcome: Outcome) => <Button>{outcome.claim}</Button>);

  const outcomeButtons: JSX.Element[] = [];

  for (let index = 0; index < market.outcomes.length; index++) {
    let handler = () => {
      processMarketResolution(market.id, index, marketResolutionState, setMarketResolutionState);
    };
    outcomeButtons.push(<Button onClick={handler}>{market.outcomes.at(index).claim}</Button>);
  }

  return (
    <Card border="dark" className={styles.marketCard}>
      <Card.Title className={styles.centerTitle}>{market.question}</Card.Title>
      <Card.Body>
        Which outcome took place?
        {outcomeButtons}
      </Card.Body>
    </Card>
  );
}
