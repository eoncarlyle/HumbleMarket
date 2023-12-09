import { useState, useContext } from "react";
import { Button, Col, Container, Form, Row } from "react-bootstrap";
import { Form as RRForm } from "react-router-dom";

import TransactionValidation from "../../model/TransactionValidation";
import { priceNumberFormat } from "../../util/Numeric";
import MarketDetailContext from "../../util/MarketDetailContext";
import MarketDetailContextValue from "../../model/MarketDetailContextValue";
import TransactionType from "../../model/TransactionType";
import OrderInformation from "./OrderInformation";
import MarketTransactionModal from "./MarketTransactionModal";
import {
  closeHandlerFactory,
  shareButtonHandlerFactory,
  submitHandlerFactory,
  processBuyForm,
  shareChangeHandlerCreator,
  directionCost,
  rawPrice,
  isYes,
} from "../../util/MarketTransaction";

import styles from "../../style/TransactionForm.module.css";

export default function BuyForm() {
  const marketDetailContextValue = useContext(MarketDetailContext) as MarketDetailContextValue;
  const { marketReturnData, order, setOrder } = marketDetailContextValue;
  const { market, purchasePriceList, userCredits } = marketReturnData;

  const outcome = market.outcomes[order.outcomeIndex];
  const outcomePurchasePriceList = purchasePriceList[order.outcomeIndex][isYes(order.positionDirection) ? 0 : 1];
  const directionShares = isYes(order.positionDirection) ? outcome.sharesY : outcome.sharesN;
  const orderDirectionCost = directionCost(order.positionDirection, order.shares, outcomePurchasePriceList);
  const availableShares = Math.min(directionShares - 1, Math.floor(userCredits / orderDirectionCost));

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

  // TODO pm-15: Need to change BuyForm to use purchasePriceData

  return (
    <>
      <RRForm className={styles.transactionForm} onSubmit={submitHandler}>
        <Container className={styles.transactionFormContainer}>
          <OrderInformation
            transactionType={TransactionType.Purchase}
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
            <Col>{priceNumberFormat(order.shares * orderDirectionCost)} CR</Col>
          </Row>
        </Container>
      </RRForm>

      <MarketTransactionModal
        transactionType={TransactionType.Purchase}
        showModal={transactionValidation.showModal}
        order={order}
        outcomeClaim={outcome.claim}
        directionCost={orderDirectionCost}
        handleClose={closeHandler}
        handleSubmit={processBuyForm(
          market,
          order,
          rawPrice(order.shares, outcomePurchasePriceList),
          setTransactionValidation,
          setOrder
        )}
      />
    </>
  );
}
