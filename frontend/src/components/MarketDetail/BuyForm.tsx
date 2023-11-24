import { useState, useContext } from "react";
import { Button, Col, Container, Form, Row } from "react-bootstrap";
import { Form as RRForm } from "react-router-dom";

import PositionDirection from "../../model/PositionDirection";
import TransactionValidation from "../../model/TransactionValidation";
import { priceNumberFormat } from "../../util/Numeric";
import processBuyForm from "../../util/ProcessBuyForm";
import shareChangeHandlerCreator from "../../util/ShareChangeHandlerCreator";
import MarketDetailContext from "../../util/MarketDetailContext";
import MarketDetailContextValue from "../../model/MarketDetailContextValue";
import TransactionType from "../../model/TransactionType";
import OrderInformation from "./OrderInformation";
import MarketTransactionModal from "./MarketTransactionModal";
import {
  submitHandlerFactory,
  closeHandlerFactory,
  shareButtonHandlerFactory,
} from "../../util/TransactionValidationStateManagement";

import styles from "../../style/TransactionForm.module.css";

export default function BuyForm() {
  const marketDetailContextValue = useContext(MarketDetailContext) as MarketDetailContextValue;
  const { marketReturnData, order, setOrder } = marketDetailContextValue;
  const { market, userCredits } = marketReturnData;

  const transactionType = TransactionType.Purchase;
  const outcome = market.outcomes[order.outcomeIndex];
  const directionShares = order.positionDirection === PositionDirection.YES ? outcome.sharesY : outcome.sharesN;

  const directionCost = order.positionDirection === PositionDirection.YES ? outcome.price : 1 - outcome.price;
  const availableShares = Math.min(directionShares - 1, Math.floor(userCredits / directionCost));

  const [transactionValidation, setTransactionValidation] = useState<TransactionValidation>({
    valid: true,
    showModal: false,
    message: "",
    order: order,
  });

  const submitHandler = submitHandlerFactory(setTransactionValidation, order);
  const closeHandler = closeHandlerFactory(transactionValidation, setTransactionValidation, order);
  const shareButtonClickHandler = shareButtonHandlerFactory(setTransactionValidation, order);

  if (transactionValidation.order !== order) {
    setTransactionValidation({
      valid: transactionValidation.valid,
      showModal: false,
      message: "",
      order: order,
    });
  }

  return (
    <>
      <RRForm className={styles.transactionForm} onSubmit={submitHandler}>
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
        handleClose={closeHandler}
        handleSubmit={processBuyForm(market, order, setTransactionValidation, setOrder)}
      />
    </>
  );
}
