import { Dispatch } from "react";

import { priceNumberFormat } from "../../util/Numeric";
import Outcome from "../../model/Outcome";
import PositionDirection from "../../model/PositionDirection";
import TransactionType from "../../model/TransactionType";

import classes from "../../styles/SingleMarketBody.module.css";

interface SingleMarketOutcomeDetailProps {
  outcome: Outcome;
  setOrder: Dispatch<any>;
  outcomeIndex: number;
}

function SingleMarketOutcomeDetail({ outcome, setOrder, outcomeIndex }: SingleMarketOutcomeDetailProps) {
  return (
    <>
      <div className={classes.outcome}>
        <div> {outcome.claim} </div>
        <div className={classes.prices}>
          <div className={classes.priceContainerYes}>
            <button
              onClick={() =>
                setOrder({
                  transactionType: TransactionType.Purchase,
                  positionDirection: PositionDirection.YES,
                  outcomeIndex: outcomeIndex,
                  shares: 1,
                })
              }
              className={classes.priceYes}
            >
              Yes: {priceNumberFormat(outcome.price)} CR
            </button>
            {Math.max(outcome.sharesY, 0)} open shares
            {/* At least 2 unsold shares must be present to put some guardrails up */}
          </div>
          <div className={classes.priceContainerNo}>
            <button
              onClick={() =>
                setOrder({
                  transactionType: TransactionType.Purchase,
                  positionDirection: PositionDirection.NO,
                  outcomeIndex: outcomeIndex,
                  shares: 1,
                })
              }
              className={classes.priceNo}
            >
              No: {priceNumberFormat(1 - outcome.price)} CR
            </button>
            {Math.max(outcome.sharesN - 2, 0)} open shares
            {/* At least 2 unsold shares must be present to put some guardrails up */}
          </div>
        </div>
      </div>
    </>
  );
}

export default SingleMarketOutcomeDetail;
