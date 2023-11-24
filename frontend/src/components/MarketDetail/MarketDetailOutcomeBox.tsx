import { Button, Container, Row, Col } from "react-bootstrap";
import { Dispatch, SetStateAction, useContext } from "react";

import Outcome from "../../model/Outcome";
import { priceNumberFormat } from "../../util/Numeric";
import PositionDirection from "../../model/PositionDirection";
import Order from "../../model/Order";
import MarketDetailContext from "../../util/MarketDetailContext";
import MarketDetailContextValue from "../../model/MarketDetailContextValue";

import styles from "../../style/MarketDetailOutcomeBox.module.css";

interface MarketDetailOutcomeBoxProps {
  outcome: Outcome;
  outcomeIndex: number;
}

//TODO: Go back to CSS modules, placed as the classname on the top level fragment
export default function MarketDetailOutcomeBox({ outcome, outcomeIndex }: MarketDetailOutcomeBoxProps) {
  const marketDetailContextValue = useContext(MarketDetailContext) as MarketDetailContextValue;
  const { setOrder } = marketDetailContextValue;
  return (
    <Container>
      <Row className={styles.outcomeRow}>
        <Col sm={4}>{outcome.claim}</Col>
        <Col sm={8}>
          <Button
            className={styles.marketButton}
            onClick={() =>
              setOrder({
                positionDirection: PositionDirection.YES,
                outcomeIndex: outcomeIndex,
                shares: 1,
                submitted: false,
              })
            }
            variant="success"
          >
            Yes: {priceNumberFormat(outcome.price)} CR
          </Button>
          <Button
            className={styles.marketButton}
            onClick={() =>
              setOrder({
                positionDirection: PositionDirection.NO,
                outcomeIndex: outcomeIndex,
                shares: 1,
                submitted: false,
              })
            }
            variant="danger"
          >
            No: {priceNumberFormat(1 - outcome.price)} CR{" "}
          </Button>
        </Col>
      </Row>
    </Container>
  );
}
