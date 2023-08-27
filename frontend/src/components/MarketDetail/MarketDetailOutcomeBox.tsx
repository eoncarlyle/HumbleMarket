import { Button, Container, Row, Col } from "react-bootstrap";
import { Dispatch, SetStateAction } from "react";

import Outcome from "../../model/Outcome";
import { priceNumberFormat } from "../../util/Numeric";
import PositionDirection from "../../model/PositionDirection";
import Order from "../../model/Order";

import styles from "../../style/MarketDetailOutcomeBox.module.css";

interface MarketDetailOutcomeBoxProps {
  outcome: Outcome;
  setOrder: Dispatch<SetStateAction<Order>>;
  outcomeIndex: number;
}

//TODO: Go back to CSS modules, placed as the classname on the top level fragment
function MarketDetailOutcomeBox({ outcome, setOrder, outcomeIndex }: MarketDetailOutcomeBoxProps) {
  return (
    <Container>
      <Row className={styles.outcomeRow}>
        <Col sm={4}>{outcome.claim}</Col>
        <Col sm={8}>
          <Button
            className = {styles.marketButton}
            onClick={() =>
              setOrder({
                positionDirection: PositionDirection.YES,
                outcomeIndex: outcomeIndex,
                shares: 1,
              })
            }
            variant="success"
          >
            Yes: {priceNumberFormat(outcome.price)} CR
          </Button>
          <Button
            className = {styles.marketButton}
            onClick={() =>
              setOrder({
                positionDirection: PositionDirection.NO,
                outcomeIndex: outcomeIndex,
                shares: 1,
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

export default MarketDetailOutcomeBox;
