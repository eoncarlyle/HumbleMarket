import { useState, useContext } from "react";
import { Form as RRForm } from "react-router-dom";
import { Container, Row, Col, Form, Button } from "react-bootstrap";

import { priceNumberFormat } from "../../util/Numeric";
import shareChangeHandlerCreator from "../../util/ShareChangeHandlerCreator";
import TransactionValidation from "../../model/TransactionValidation";
import processSellForm from "../../util/ProcessSellForm";
import OrderInformation from "./OrderInformation";
import MarketDetailContext from "../../util/MarketDetailContext";
import MarketDetailContextValue from "../../model/MarketDetailContextValue";
import {
  submitHandlerFactory,
  closeHandlerFactory,
  shareButtonHandlerFactory,
} from "../../util/TransactionValidationStateManagement";
import MarketTransactionModal from "./MarketTransactionModal";
import TransactionType from "../../model/TransactionType";
import { directionCost, isYes } from "../../util/TradeMarketTransaction";

import styles from "../../style/TransactionForm.module.css";

export default function SellForm() {
  const marketDetailContextValue = useContext(MarketDetailContext) as MarketDetailContextValue;
  const { marketReturnData, order, setOrder } = marketDetailContextValue;
  const { market, salePriceList } = marketReturnData;

  const outcome = market.outcomes[order.outcomeIndex];
  const outcomeSalePriceList = salePriceList[order.outcomeIndex][isYes(order.positionDirection) ? 0 : 1];
  const orderDirectionCost = directionCost(order.positionDirection, order.shares, outcomeSalePriceList);

  const [transactionValidation, setTransactionValidation] = useState<TransactionValidation>({
    valid: true,
    showModal: false,
    message: "",
    order: order,
  });

  const submitHandler = submitHandlerFactory(setTransactionValidation, order);
  const closeHandler = closeHandlerFactory(transactionValidation, setTransactionValidation, order);
  const shareButtonHandler = shareButtonHandlerFactory(setTransactionValidation, order);

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
            transactionType={TransactionType.Sale}
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
                onClick={shareButtonHandler}
                isInvalid={!transactionValidation.valid}
                isValid={transactionValidation.valid && transactionValidation.message !== ""}
                disabled={outcomeSalePriceList.length === 0}
              ></Form.Control>
              <Form.Control.Feedback type="invalid">{transactionValidation.message}</Form.Control.Feedback>
              <Form.Control.Feedback type="valid">{transactionValidation.message}</Form.Control.Feedback>
            </Col>
          </Row>
          <Button
            variant="primary"
            type="submit"
            className={styles.marketButton}
            disabled={outcomeSalePriceList.length === 0}
          >
            Sell
          </Button>
          <Row>
            <Col>Proceeds</Col>
            <Col>{priceNumberFormat(order.shares * orderDirectionCost)} CR</Col>
          </Row>
        </Container>
      </RRForm>

      <MarketTransactionModal
        transactionType={TransactionType.Sale}
        showModal={transactionValidation.showModal}
        order={order}
        outcomeClaim={outcome.claim}
        directionCost={orderDirectionCost}
        handleClose={closeHandler}
        handleSubmit={processSellForm(market, order, orderDirectionCost, setTransactionValidation, setOrder)}
      />
    </>
  );
}
