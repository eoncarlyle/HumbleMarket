import { useState } from "react";
import { useLoaderData, useLocation, Link } from "react-router-dom";

import Order from "../../model/Order";
import Market from "../../model/Market";
import TransactionType from "../../model/TransactionType";
import PositionDirection from "../../model/PositionDirection";
import MarketDetailCard from "./MarketDetailCard";
//import TransactionForm from "./SingleMarket/TransactionForm";
//import SingleMarketDetail from "./SingleMarket/SingleMarketDetail";
//import classes from "../styles/SingleMarketBody.module.css";

function MarketDetail() {
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
    <MarketDetailCard market={market} order={order} setOrder={setOrder} />
  ) 
}

export default MarketDetail