import { Row, Col } from "react-bootstrap";

import PositionDirection from "../../model/PositionDirection";
import TransactionType from "../../model/TransactionType";

interface OrderInformationProps {
  transactionType: TransactionType;
  claim: string;
  direction: PositionDirection;
  availableShares: number;
}

export default function OrderInformation({
  transactionType,
  claim,
  direction,
  availableShares,
}: OrderInformationProps) {
  return (
    <>
      <Row>
        <Col>Outcome</Col>
        <Col>{claim}</Col>
      </Row>
      <Row>
        <Col>Direction</Col>
        <Col>{direction}</Col>
      </Row>
      <Row>
        <Col>Shares Available for {transactionType}</Col>
        <Col>{availableShares}</Col>
      </Row>
    </>
  );
}
