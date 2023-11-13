import { useState } from "react";
import { useLoaderData, useLocation } from "react-router-dom";

import Order from "../../model/Order";
import TransactionType from "../../model/TransactionType";
import PositionDirection from "../../model/PositionDirection";
import MarketDetailCard from "./MarketDetailCard";
import MarketReturnData from "../../model/MarketReturnData";

export default function MarketDetail() {
  const marketReturnData = useLoaderData() as MarketReturnData;
  const [market, salePriceList] = [
    marketReturnData.market,
    marketReturnData.salePriceList,
  ];
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
    <MarketDetailCard
      market={market}
      salePriceList={salePriceList}
      order={order}
      setOrder={setOrder}
    />
  );
}
