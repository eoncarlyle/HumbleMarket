import { Dispatch, FormEvent, SetStateAction, useState } from "react";
import { Form } from "react-router-dom";

import { getAuthToken } from "../../util/Auth";
import { priceNumberFormat } from "../../util/Numeric";
import shareChangeHandlerCreator from "../../util/ShareChangeHandlerCreator";
import Order from "../../model/Order";
import Outcome from "../../model/Outcome";
import Market from "../../model/Market";
import PositionDirection from "../../model/PositionDirection";
import TransactionValidation from "../../model/TransactionValidation";

import classes from "../../styles/SingleMarketBody.module.css";

interface SellFormProps {
  market: Market;
  order: Order;
  outcome: Outcome;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function SellForm({ market, order, outcome, setOrder }: SellFormProps) {
  var directionShares = order.positionDirection === PositionDirection.YES ? outcome.sharesY : outcome.sharesN;
  var directionProceeds = order.positionDirection === PositionDirection.YES ? outcome.price : 1 - outcome.price;

  const [validation, setValidation] = useState<TransactionValidation>();

  const formSubmissionHandler = async (event: FormEvent) => {
    event.preventDefault();
    const target = event.target as typeof event.target & {
      shares: { value: number };
    };

    //TODO: Centralize API FQDN
    const [hostname, outcomeIndex, positionDirection, shares] = [
      new URL(window.location.href).hostname,
      order.outcomeIndex,
      order.positionDirection,
      target.shares.value,
    ];

    const requestUrl = `http://${hostname}:8080/market/${market.seqId}/outcome/${outcomeIndex}/${positionDirection}/sale/${shares}`;

    const response = await fetch(requestUrl, {
      method: "POST",
      headers: {
        Authorization: String(getAuthToken()),
      },
    });
    //TODO: This is bad and you know it
    if (!response.ok) {
      if (response.status === 422) {
        setValidation({
          valid: false,
          message: "Insufficient shares to sell!",
        });
      } else {
        setValidation({
          valid: false,
          message: "Transaction unsuccesful, likely due to server-side issues!",
        });
      }
    } else {
      setValidation({
        valid: true,
        message: "Successful sale!",
      });
    }
  };

  var message = <></>;

  if (validation !== undefined) {
    validation.valid
      ? (message = <div className={classes.valid}>{validation.message}</div>)
      : (message = <div className={classes.simpleInvalid}>{validation.message}</div>);
  }

  return (
    <Form onSubmit={formSubmissionHandler} className={classes.transactionForm}>
      <div className={classes.orderSection}>
        <div className={classes.orderSubSection}>
          <div>Outcome:</div>
          <div>Direction:</div>
          <div>Shares:</div>
          <div>Proceeds:</div>
        </div>
        <div className={classes.orderSubSection}>
          <div>{outcome.claim}</div>
          <div>{order.positionDirection}</div>
          <input
            name="shares"
            type="number"
            min="1"
            step="1"
            //max={directionShares} //TODO: Set ceiling lower according to user's shares
            placeholder={String(order.shares)}
            onChange={shareChangeHandlerCreator(order, setOrder)}
          />
          <div>{priceNumberFormat(order.shares * directionProceeds)}</div>
        </div>
      </div>
      {message}
      <button type="submit" className={classes.orderButton}>
        Submit Sell Order
      </button>
    </Form> //TODO: Grey out according to the user's shares
  );
}

export default SellForm;
