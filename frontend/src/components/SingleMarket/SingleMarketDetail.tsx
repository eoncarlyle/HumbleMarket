import { Dispatch, SetStateAction } from "react";

import Order from "../../model/Order";
import Market from "../../model/Market";
import SingleMarketOutcomeDetail from "./SingleMarketOutcomeDetail";
import classes from "../../styles/SingleMarketBody.module.css";

interface SingleMarketDetailProps {
  market: Market;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function SingleMarketDetail({ market, setOrder }: SingleMarketDetailProps) {
  var closeDate = new Date(market.closeDate);

  var outcomeIndex = 0;
  var outcomesList: JSX.Element[] = [];
  market.outcomes.forEach((outcome) => {
    outcomesList.push(<SingleMarketOutcomeDetail outcome={outcome} setOrder={setOrder} outcomeIndex={outcomeIndex} />);
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

export default SingleMarketDetail;
