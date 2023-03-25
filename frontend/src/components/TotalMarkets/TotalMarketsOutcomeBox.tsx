import { Link } from "react-router-dom";

import Outcome from "../../model/Outcome";
import { priceNumberFormat } from "../../util/Numeric";
import PositionDirection from "../../model/PositionDirection";
import TransactionType from "../../model/TransactionType";
import classes from "../../styles/MarketsBody.module.css";

interface TotalMarketsOutcomeBoxProps {
  outcome: Outcome;
  singleMarketURI: string;
  selectedOutcomeIndex: number;
}

function TotalMarketsOutcomeBox({ outcome, singleMarketURI, selectedOutcomeIndex }: TotalMarketsOutcomeBoxProps) {
  return (
    <div className={classes.outcome}>
      <div> {outcome.claim} </div>
      <div className={classes.prices}>
        <Link
          state={{
            transactionType: TransactionType.Purchase,
            positionDirection: PositionDirection.YES,
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
            positionDirection: PositionDirection.NO,
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

export default TotalMarketsOutcomeBox;
