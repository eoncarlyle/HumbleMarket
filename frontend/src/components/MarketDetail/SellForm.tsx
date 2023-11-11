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
import OrderInformation from "./OrderInformation";

import styles from "../../style/TransactionForm.module.css";
import MarketTransactionModal from "./MarketTransactionModal";
import TransactionType from "../../model/TransactionType";

interface BuyFormProps {
  market: Market;
  order: Order;
  salePriceList: number[][][];
  setOrder: Dispatch<SetStateAction<Order>>;
}

export default function SellForm({ market, salePriceList, order, setOrder }: BuyFormProps) {
  const transactionType = TransactionType.Sale;
  const outcome = market.outcomes[order.outcomeIndex];
  const outcomeSalePriceList =
    salePriceList[order.outcomeIndex][order.positionDirection === PositionDirection.YES ? 0 : 1];
  const sharePrice =
    order.shares > outcomeSalePriceList.length ? outcomeSalePriceList[-1] : outcomeSalePriceList[order.shares - 1];
  const directionCost = order.positionDirection === PositionDirection.YES ? sharePrice : 1 - sharePrice;
  const inputDisabled = outcomeSalePriceList.length === 0;

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
            availableShares={outcomeSalePriceList.length}
          />
          <Row>
            <Col>Shares to Sell</Col>
            <Col>
              <Form.Control
                name="shares"
                type="number"
                step="1"
                min="1"
                max={outcomeSalePriceList.length}
                placeholder={String(order.shares)}
                onChange={shareChangeHandlerCreator(order, setOrder)}
                onClick={shareButtonClickHandler}
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

      <MarketTransactionModal
        transactionType={TransactionType.Sale}
        showModal={transactionValidation.showModal}
        order={order}
        outcomeClaim={outcome.claim}
        directionCost={directionCost}
        handleClose={handleClose}
        handleSubmit={processSellForm(market, order, sharePrice, setTransactionValidation)}
      />
    </>
  );
}
