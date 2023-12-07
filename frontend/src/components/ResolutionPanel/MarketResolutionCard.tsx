import { Card, Button } from "react-bootstrap";

import Market from "../../model/Market";
import MarketResolutionState from "../../model/MarketResolutionState";
import PositionDirection from "../../model/PositionDirection";
import { isYes } from "../../util/TradeMarketTransaction";

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
  const outcomeButtons: JSX.Element[] = [];
  const outcomeCount = market.outcomes.length;
  let handler: () => void;

  const createSingleOutcomeButton = (positionDirection: PositionDirection) => {
    handler = () => {
      setMarketResolutionState({
        resolvedMarkets: marketResolutionState.resolvedMarkets,
        market: market,
        outcomeIndex: 0,
        code: marketResolutionState.code,
        isError: marketResolutionState.isError,
        message: marketResolutionState.message,
        showModal: true,
        direction: positionDirection,
      });
    };
    return (
      <Button variant={isYes(positionDirection) ? "success" : "danger"} onClick={handler}>
        {market.outcomes.at(0).claim}: {positionDirection}
      </Button>
    );
  };

  //TODO: Provide different logic that sets a position direction to state for single-outcome markets
  if (outcomeCount > 1) {
    for (let index = 0; index < outcomeCount; index++) {
      handler = () => {
        setMarketResolutionState({
          resolvedMarkets: marketResolutionState.resolvedMarkets,
          market: market,
          outcomeIndex: index,
          code: marketResolutionState.code,
          isError: marketResolutionState.isError,
          message: marketResolutionState.message,
          showModal: true,
        });
      };
      outcomeButtons.push(<Button onClick={handler}>{market.outcomes.at(index).claim}</Button>);
    }
  } else {
    outcomeButtons.push(createSingleOutcomeButton(PositionDirection.YES));
    outcomeButtons.push(createSingleOutcomeButton(PositionDirection.NO));
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
