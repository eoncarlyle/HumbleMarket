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

interface BuyFormProps {
  market: Market;
  order: Order;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function BuyForm({ market, order, setOrder }: BuyFormProps) {
  const outcome = market.outcomes[order.outcomeIndex];

  const directionShares =
    order.positionDirection === PositionDirection.YES
      ? outcome.sharesY
      : outcome.sharesN;
  const availableShares = directionShares - 1;

  const directionCost =
    order.positionDirection === PositionDirection.YES
      ? outcome.price
      : 1 - outcome.price;

  const [transactionValidation, setTransactionValidation] =
    useState<TransactionValidation>({
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

  const handleSubmit = async () => {
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

  return (
    <>
      <RRForm className={styles.transactionForm} onSubmit={handleSubmit}>
        <Container className={styles.transactionFormContainer}>
          <Row>
            <Col>Outcome</Col>
            <Col>{outcome.claim}</Col>
          </Row>
          <Row>
            <Col>Direction</Col>
            <Col>{order.positionDirection}</Col>
          </Row>
          <Row>
            <Col>Available Shares to Buy</Col>
            <Col>{availableShares}</Col>
          </Row>
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
                onClick={() =>
                  setTransactionValidation({
                    valid: true,
                    showModal: false,
                    message: "",
                    order: order,
                  })
                }
                isInvalid={!transactionValidation.valid}
                isValid={
                  transactionValidation.valid &&
                  transactionValidation.message !== ""
                }
              ></Form.Control>
              <Form.Control.Feedback type="invalid">
                {transactionValidation.message}
              </Form.Control.Feedback>
              <Form.Control.Feedback type="valid">
                {transactionValidation.message}
              </Form.Control.Feedback>
            </Col>
          </Row>
          <Button
            variant="primary"
            type="submit"
            className={styles.marketButton}
          >
            Buy
          </Button>
          <Row>
            <Col>Cost</Col>
            <Col>{priceNumberFormat(order.shares * directionCost)} CR</Col>
          </Row>
        </Container>
      </RRForm>

      <Modal show={transactionValidation.showModal} on>
        <Modal.Header>
          <Modal.Title>Confirm Buy</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Are you sure that you want to buy {order.shares}{" "}
          {order.positionDirection} shares "{outcome.claim}" for{" "}
          {priceNumberFormat(order.shares * directionCost)} CR?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="danger" onClick={handleClose}>
            Cancel
          </Button>
          <Button
            variant="success"
            onClick={processBuyForm(market, order, setTransactionValidation)}
          >
            Submit Purchase
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default BuyForm;
