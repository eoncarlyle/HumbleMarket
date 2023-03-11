import { useState } from "react";
import { useRouteLoaderData, Link } from "react-router-dom";

import classes from "../styles/MarketsBody.module.css";
import Market from "../model/Market";
import Outcome from "../model/Outcome";
import { priceNumberFormat } from "../util/Numeric";
import PositionDirection from "../model/PositionDirection";
import TransactionType from "../model/TransactionType";

// TODO: Put prices, outcome claims on different lines for small screens
function OutcomeBox(outcome: Outcome, singleMarketURI: string, selectedOutcomeIndex: number) {
  return (
    <div className={classes.outcome}>
      <div> {outcome.claim} </div>
      <div className={classes.prices}>
        <Link
          state={{
            transactionType: TransactionType.Purchase,
            positionDirection: PositionDirection.Yes,
            outcomeIndex: selectedOutcomeIndex,
            shares: 1,
          }}
          to={singleMarketURI}
          className={classes.priceYes}
        >
          Yes: {priceNumberFormat(outcome.price)} CR
        </Link>
        <Link
          state={{
            transactionType: TransactionType.Purchase,
            positionDirection: PositionDirection.No,
            outcomeIndex: selectedOutcomeIndex,
            shares: 1,
          }}
          to={singleMarketURI}
          className={classes.priceNo}
        >
          No: {priceNumberFormat(1 - outcome.price)} CR
        </Link>
      </div>
    </div>
  );
}

function SingleMarketLink(market: Market, singleMarketURI: string) {
  return (
    <Link to={singleMarketURI} className={classes.singleMarketLink}>
      {market.outcomes.length < 3 ? <>View Market</> : <> View Market: {market.outcomes.length - 2} more outcomes</>}
    </Link>
  );
}

function MarketBox(market: Market) {
  var closeDate = new Date(market.closeDate);
  var singleMarketURI = "/market/" + market.seqId;
  return (
    <div className={classes.marketBox}>
      <div>
        <div className={classes.question}>{market.question}</div>
        <div className={classes.closeDate}>
          Close Date: {closeDate.toDateString()} at {closeDate.toLocaleTimeString()}
        </div>
      </div>
      <div className={classes.outcomeContainer}>
        {/* //TODO This neccesitated turning off strict null checks in tsconfig.json, investigate this later */}
        {OutcomeBox(market.outcomes.at(0), singleMarketURI, 0)}
        {market.outcomes.length > 2 ? OutcomeBox(market.outcomes.at(1), singleMarketURI, 1) : <></>}
      </div>
      <div className={classes.singleMarketLinkContainer}>
        <div className={classes.singleMarketLinkSubContainer}>{SingleMarketLink(market, singleMarketURI)}</div>
      </div>
    </div>
  );
}

function MarketsBody() {
  const markets = useRouteLoaderData("home") as Array<Market>;
  return (
    //TODO: show markets on desktop as a 2 column grid
    <div className={classes.body}>{markets.map((market: Market) => MarketBox(market))}</div>
  );
}

export default MarketsBody;
