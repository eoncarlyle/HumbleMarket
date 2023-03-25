import { useState } from "react";
import { useLoaderData, useLocation, Link } from "react-router-dom";

import Order from "../model/Order";
import Market from "../model/Market";
import TransactionType from "../model/TransactionType";
import PositionDirection from "../model/PositionDirection";
import TransactionForm from "./SingleMarket/TransactionForm";
import SingleMarketDetail from "./SingleMarket/SingleMarketDetail";
import classes from "../styles/SingleMarketBody.module.css";
/*

- If the user has clicked an outcome/direction button, they have populated the `location` object with
  `state={{ selectedOutcomeIndex: selectedOutcomeIndex, selectedDirection: PositionDirection.YES }}`

*/

function SingleMarketBody() {
  const market = useLoaderData() as Market;
  const startingSelection = useLocation().state;
  const [order, setOrder] = useState<Order>(
    startingSelection
      ? startingSelection
      : {
          transactionType: TransactionType.Purchase,
          positionDirection: PositionDirection.YES,
          outcomeIndex: 0,
          shares: 1,
        }
  );
  return (
    <>
      <div className={classes.body}>
        <SingleMarketDetail market={market} setOrder={setOrder} />
        <Link to="/" className={classes.bottomLink}>
          Back to other markets
        </Link>
        <TransactionForm market={market} order={order} setOrder={setOrder} />
      </div>
    </>
  );
}

export default SingleMarketBody;
