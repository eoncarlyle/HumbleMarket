import { Dispatch, SetStateAction, useState } from "react";
import { Button, Col, Container, Form, Modal, Row } from "react-bootstrap";
import { Form as RRForm } from "react-router-dom";

import Market from "../../model/Market";
import Order from "../../model/Order";
import PositionDirection from "../../model/PositionDirection";
import TransactionValidation from "../../model/TransactionValidation";
import { priceNumberFormat } from "../../util/Numeric";
import processBuyForm from "../../util/ProcessBuyForm";
import shareChangeHandlerCreator from "../../util/ShareChangeHandlerCreator";

import styles from "../../style/TransactionForm.module.css";
import MarketTransactionModal from "./MarketTransactionModal";
import TransactionType from "../../model/TransactionType";
import OrderInformation from "./OrderInformation";

interface BuyFormProps {
  market: Market;
  order: Order;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function BuyForm({ market, order, setOrder }: BuyFormProps) {
  const transactionType = TransactionType.Purchase;
  const outcome = market.outcomes[order.outcomeIndex];
  const directionShares = order.positionDirection === PositionDirection.YES ? outcome.sharesY : outcome.sharesN;
  const availableShares = directionShares - 1;
  const directionCost = order.positionDirection === PositionDirection.YES ? outcome.price : 1 - outcome.price;

  const [transactionValidation, setTransactionValidation] = useState<TransactionValidation>({
    valid: true,
    showModal: false,
    message: "",
    order: order,
  });

  if (transactionValidation.order !== order) {
    setTransactionValidation({
      valid: transactionValidation.valid,
      showModal: false,
      message: "",
      order: order,
    });
  }

  const handleSubmit = () => {
    setTransactionValidation({
      valid: true,
      showModal: true,
      message: "",
      order: order,
    });
  };

  const handleClose = () => {
    setTransactionValidation({
      valid: transactionValidation.valid,
      showModal: false,
      message: transactionValidation.message,
      order: order,
    });
  };

  const shareButtonClickHandler = () =>
    setTransactionValidation({
      valid: true,
      showModal: false,
      message: "",
      order: order,
    });

  return (
    <>
      <RRForm className={styles.transactionForm} onSubmit={handleSubmit}>
        <Container className={styles.transactionFormContainer}>
          <OrderInformation
            transactionType={transactionType}
            claim={outcome.claim}
            direction={order.positionDirection}
            availableShares={availableShares}
          />
          <Row>
            <Col>Shares to Buy</Col>
            <Col>
              <Form.Control
                name="shares"
                type="number"
                step="1"
                min="1"
                max={availableShares}
                placeholder={String(order.shares)}
                onChange={shareChangeHandlerCreator(order, setOrder)}
                onClick={shareButtonClickHandler}
                isInvalid={!transactionValidation.valid}
                isValid={transactionValidation.valid && transactionValidation.message !== ""}
              ></Form.Control>
              <Form.Control.Feedback type="invalid">{transactionValidation.message}</Form.Control.Feedback>
              <Form.Control.Feedback type="valid">{transactionValidation.message}</Form.Control.Feedback>
            </Col>
          </Row>
          <Button variant="primary" type="submit" className={styles.marketButton}>
            Buy
          </Button>
          <Row>
            <Col>Cost</Col>
            <Col>{priceNumberFormat(order.shares * directionCost)} CR</Col>
          </Row>
        </Container>
      </RRForm>

      <MarketTransactionModal
        transactionType={transactionType}
        showModal={transactionValidation.showModal}
        order={order}
        outcomeClaim={outcome.claim}
        directionCost={directionCost}
        handleClose={handleClose}
        handleSubmit={processBuyForm(market, order, setTransactionValidation)}
      />
    </>
  );
}

export default BuyForm;
