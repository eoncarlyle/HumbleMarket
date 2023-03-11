import { useLoaderData, useLocation } from "react-router-dom";

import classes from "../styles/SingleMarketBody.module.css";
import { priceNumberFormat } from "../util/Numeric";
import Order from "../model/Order";
import Outcome from "../model/Outcome";
import Market from "../model/Market";
import TransactionType from "../model/TransactionType";
import PositionDirection from "../model/PositionDirection";
import { BaseSyntheticEvent, Dispatch, SetStateAction, SyntheticEvent, useState } from "react";

/*

- If the user has clicked an outcome/direction button, they have populated the `location` object with
  `state={{ selectedOutcomeIndex: selectedOutcomeIndex, selectedDirection: PositionDirection.Yes }}`

*/

function OutcomeBox(outcome: Outcome, setSelection: React.Dispatch<any>, outcomeIndex: number) {
  //TODO: Place the number of available shares underneath the price buttons with a flexbox
  return (
    <>
      <div className={classes.outcome}>
        <div> {outcome.claim} </div>
        <div className={classes.prices}>
          <button
            onClick={() =>
              setSelection({
                transactionType: TransactionType.Purchase,
                positionDirection: PositionDirection.Yes,
                outcomeIndex: outcomeIndex,
                shares: 1,
              })
            }
            className={classes.priceYes}
          >
            Yes: {priceNumberFormat(outcome.price)} CR
          </button>
          <button
            onClick={() =>
              setSelection({
                transactionType: TransactionType.Purchase,
                positionDirection: PositionDirection.No,
                outcomeIndex: outcomeIndex,
                shares: 1,
              })
            }
            className={classes.priceNo}
          >
            No: {priceNumberFormat(1 - outcome.price)} CR
          </button>
        </div>
      </div>
    </>
  );
}

function MarketBox(market: Market, setSelection: React.Dispatch<any>) {
  var closeDate = new Date(market.closeDate);

  var outcomeIndex = 0;
  var outcomesList: JSX.Element[] = [];
  market.outcomes.forEach((outcome) => {
    outcomesList.push(OutcomeBox(outcome, setSelection, outcomeIndex));
    outcomeIndex++;
  });
  return (
    <div className={classes.marketBox}>
      <div>
        <div className={classes.question}>{market.question}</div>
        <div className={classes.closeDate}>
          Close Date: {closeDate.toDateString()} at {closeDate.toLocaleTimeString()}
        </div>
      </div>
      <div className={classes.outcomeContainer}>{outcomesList}</div>
    </div>
  );
}

//TODO: What do you call a function that creates a handler function?
function shareChangeHandlerCreator(order: Order, setOrder: Dispatch<SetStateAction<Order>>) {
  return (event: BaseSyntheticEvent) => {
    var newOrder: Order = {
      positionDirection: order.positionDirection,
      outcomeIndex: order.outcomeIndex,
      shares: Number(event.target.value),
    };
    setOrder(newOrder);
  };
}

function SellFormSection(market: Market, order: Order, outcome: Outcome, setOrder: Dispatch<SetStateAction<Order>>) {
  var directionShares = order.positionDirection === PositionDirection.Yes ? outcome.sharesY : outcome.sharesN;
  var directionProceeds = order.positionDirection === PositionDirection.Yes ? outcome.price : 1 - outcome.price;

  return (
    <form className={classes.transactionForm}>
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
            type="number"
            min="1"
            step="1"
            max={directionShares}
            placeholder={String(order.shares)}
            onChange={shareChangeHandlerCreator(order, setOrder)}
          />
          <div>{order.shares * directionProceeds}</div>
        </div>
      </div>
      <button className={classes.orderButton}>Submit Sell Order</button>
    </form>
  );
}

function BuyFormSection(market: Market, order: Order, outcome: Outcome, setOrder: Dispatch<SetStateAction<Order>>) {
  var directionShares = order.positionDirection === PositionDirection.Yes ? outcome.sharesY : outcome.sharesN;
  var directionCost = order.positionDirection === PositionDirection.Yes ? outcome.price : 1 - outcome.price;
  return (
    <form className={classes.transactionForm}>
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
            type="number"
            min="1"
            step="1"
            max={directionShares}
            placeholder={String(order.shares)}
            onChange={shareChangeHandlerCreator(order, setOrder)}
          />
          <div>{order.shares * directionCost}</div>
        </div>
      </div>
      <button className={classes.orderButton}>Submit Buy Order</button>
    </form>
  );
}

function TransactionForm(market: Market, order: Order, setOrder: Dispatch<SetStateAction<Order>>) {
  var outcome = market.outcomes[order.outcomeIndex];
  //TODO: Probably bad idea to have separate state for this now: either the entire order should be populated in advance
  //TODO: ...And that is the state that should be used or _only_ the order state that could be set from the
  //TODO: ...main body should be populated
  const [transactionType, setTransactionType] = useState<TransactionType>(TransactionType.Purchase);
  const isPurchase = transactionType === "Purchase";
  return (
    <>
      <div>
        <button
          className={isPurchase ? classes.activeTransaction : classes.inactiveTransaction}
          onClick={() => {
            setTransactionType(TransactionType.Purchase);
          }}
          type="button"
        >
          Buy
        </button>
        <button
          className={!isPurchase ? classes.activeTransaction : classes.inactiveTransaction}
          onClick={() => {
            setTransactionType(TransactionType.Sale);
          }}
          type="button"
        >
          Sell
        </button>
      </div>
      {isPurchase
        ? BuyFormSection(market, order, outcome, setOrder)
        : SellFormSection(market, order, outcome, setOrder)}
    </>
  );
}

function SingleMarketBody() {
  const market = useLoaderData() as Market;
  const startingSelection = useLocation().state;
  const [order, setOrder] = useState<Order>(
    startingSelection
      ? startingSelection
      : {
          transactionType: TransactionType.Purchase,
          positionDirection: PositionDirection.Yes,
          outcomeIndex: 0,
          shares: 1,
        }
  );
  return (
    <>
      <div className={classes.body}>
        {MarketBox(market, setOrder)}
        {TransactionForm(market, order, setOrder)}
      </div>
    </>
  );
}

export default SingleMarketBody;
