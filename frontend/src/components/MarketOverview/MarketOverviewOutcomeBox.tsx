import { LinkContainer } from "react-router-bootstrap";
import { Button, Container, Row, Col } from "react-bootstrap";

import Outcome from "../../model/Outcome";
import { priceNumberFormat } from "../../util/Numeric";
import PositionDirection from "../../model/PositionDirection";
import TransactionType from "../../model/TransactionType";

import styles from "../../style/MarketDetailOutcomeBox.module.css";

interface MarketOverviewOutcomeBoxProps {
  outcome: Outcome;
  singleMarketURI: string;
  outcomeIndex: number;
}

export default function MarketOverviewOutcomeBox({ outcome, singleMarketURI, outcomeIndex }: MarketOverviewOutcomeBoxProps) {
  return (
    <Container>
      <Row className={styles.outcomeRow}>
        <Col sm={4}>{outcome.claim}</Col>
        <Col sm={8}>
          <LinkContainer
            state={{
              transactionType: TransactionType.Purchase,
              positionDirection: PositionDirection.YES,
              outcomeIndex: outcomeIndex,
              shares: 1,
            }}
            to={singleMarketURI}
          >
            <Button variant="success" className={styles.marketButton}>
              Yes: {priceNumberFormat(outcome.price)} CR
            </Button>
          </LinkContainer>
          <LinkContainer
            state={{
              transactionType: TransactionType.Purchase,
              positionDirection: PositionDirection.NO,
              outcomeIndex: outcomeIndex,
              shares: 1,
            }}
            to={singleMarketURI}
          >
            <Button variant="danger" className={styles.marketButton}>
              No: {priceNumberFormat(1 - outcome.price)} CR{" "}
            </Button>
          </LinkContainer>
        </Col>
      </Row>
    </Container>
  );
}

