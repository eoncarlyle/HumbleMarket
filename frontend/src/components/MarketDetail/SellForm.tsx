import { Dispatch, SetStateAction, useState } from "react";
import { Form as RRForm } from "react-router-dom";
import { Container, Row, Col, Form, Button, Modal } from "react-bootstrap";

import { priceNumberFormat } from "../../util/Numeric";
import shareChangeHandlerCreator from "../../util/ShareChangeHandlerCreator";
import Order from "../../model/Order";
import Market from "../../model/Market";
import PositionDirection from "../../model/PositionDirection";
import TransactionValidation from "../../model/TransactionValidation";
import processSellForm from "../../util/ProcessSellForm";

import styles from "../../style/TransactionForm.module.css";

interface BuyFormProps {
  market: Market;
  order: Order;
  salePriceList: number[][][];
  setOrder: Dispatch<SetStateAction<Order>>;
}

function SellForm({ market, salePriceList, order, setOrder }: BuyFormProps) {
  const outcome = market.outcomes[order.outcomeIndex];
  const outcomeSalePriceList = salePriceList[order.outcomeIndex][order.positionDirection === PositionDirection.YES ? 0 : 1];
  const sharePrice = order.shares > outcomeSalePriceList.length ? outcomeSalePriceList[-1] : outcomeSalePriceList[order.shares - 1];
  const directionCost = order.positionDirection === PositionDirection.YES ? sharePrice : 1 - sharePrice;
  const inputDisabled = outcomeSalePriceList.length === 0;
  const [transactionValidation, setTransactionValidation] = useState<TransactionValidation>({
    valid: true,
    showModal: false,
    message: "",
  });

  const handleSubmit = async () => {
    setTransactionValidation({ valid: true, showModal: true, message: "" });
  };

  const handleClose = () => {
    setTransactionValidation({
      valid: transactionValidation.valid,
      showModal: false,
      message: transactionValidation.message,
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
            <Col>Shares</Col>
            <Col>
              <Form.Control
                name="shares"
                type="number"
                step="1"
                min="1"
                max={outcomeSalePriceList.length}
                placeholder={String(order.shares)}
                onChange={shareChangeHandlerCreator(order, setOrder)}
                onClick={() => setTransactionValidation({ valid: true, showModal: false, message: "" })}
                isInvalid={!transactionValidation.valid}
                isValid={transactionValidation.valid && transactionValidation.message !== ""}
                disabled={inputDisabled}
              ></Form.Control>
              <Form.Control.Feedback type="invalid">{transactionValidation.message}</Form.Control.Feedback>
              <Form.Control.Feedback type="valid">{transactionValidation.message}</Form.Control.Feedback>
            </Col>
          </Row>
          <Button variant="primary" type="submit" className={styles.marketButton} disabled={inputDisabled}>
            Sell
          </Button>
          <Row>
            <Col>Proceeds</Col>
            <Col>{priceNumberFormat(order.shares * directionCost)} CR</Col>
          </Row>
        </Container>
      </RRForm>
      
      <Modal show={transactionValidation.showModal} on>
        <Modal.Header>
          <Modal.Title>Confirm Sale</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Are you sure that you want to sell {order.shares} {order.positionDirection} shares "{outcome.claim}" for 
          {" "}{priceNumberFormat(order.shares * directionCost)} CR?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="danger" onClick={handleClose}>
            Cancel
          </Button>
          <Button variant="success" onClick={processSellForm(market, order, sharePrice, setTransactionValidation)}>
            Submit Sale
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default SellForm;