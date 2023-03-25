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

interface BuyFormProps {
  market: Market;
  order: Order;
  outcome: Outcome;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function BuyForm({ market, order, outcome, setOrder }: BuyFormProps) {
  const directionShares = order.positionDirection === PositionDirection.YES ? outcome.sharesY : outcome.sharesN;
  const directionCost = order.positionDirection === PositionDirection.YES ? outcome.price : 1 - outcome.price;

  const [validation, setValidation] = useState<TransactionValidation>();

  // Rather than use react-router-dom actions, relying on directionShares and directionCost in this component means that I
  // either need to modify the serialized form input and add both variables before it is passed to the action or to do it as I have done here
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
    const requestUrl = `http://${hostname}:8080/market/${market.seqId}/outcome/${outcomeIndex}/${positionDirection}/purchase/${shares}`;

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
          message: "Insufficient credits for purchase!",
        });
      } else {
        setValidation({
          valid: false,
          message: "Purchase unsuccesful, likely due to server-side issues!",
        });
      }
    } else {
      setValidation({
        valid: true,
        message: "Successful purchase!",
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
          <div>Cost:</div>
        </div>
        <div className={classes.orderSubSection}>
          <div>{outcome.claim}</div>
          <div>{order.positionDirection}</div>
          <input
            name="shares"
            type="number"
            min="1"
            step="1"
            max={directionShares} //TODO: Set ceiling lower according to user's funds
            placeholder={String(order.shares)}
            onChange={shareChangeHandlerCreator(order, setOrder)}
          />
          <div>{priceNumberFormat(order.shares * directionCost)}</div>
        </div>
      </div>
      {message}
      <button type="submit" className={classes.orderButton}>
        Submit Buy Order
      </button>
    </Form> //TODO: Grey out according to the user's funds
  );
}

export default BuyForm;
