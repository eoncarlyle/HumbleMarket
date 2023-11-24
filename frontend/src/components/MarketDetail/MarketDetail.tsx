import { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";

import Order from "../../model/Order";
import TransactionType from "../../model/TransactionType";
import PositionDirection from "../../model/PositionDirection";
import MarketDetailCard from "./MarketDetailCard";
import MarketReturnData from "../../model/MarketReturnData";
import getMarket from "../../util/GetMarket";
import MarketDetailContextValue from "../../model/MarketDetailContextValue";
import MarketDetailContext from "../../util/MarketDetailContext";

export default function MarketDetail() {
  const [marketReturnData, setMarketReturnData] = useState<MarketReturnData | null>(null);
  const startingSelection = useLocation().state;
  const { marketId } = useParams();

  const [order, setOrder] = useState<Order>(
    startingSelection
      ? startingSelection
      : {
          transactionType: TransactionType.Purchase,
          positionDirection: PositionDirection.YES,
          outcomeIndex: 0,
          shares: 1,
          submitted: false,
        }
  );

  const useMarket = () => {
    useEffect(() => {
      getMarket(marketId).then((result) => setMarketReturnData(result));
    }, [order]);
  };

  useMarket();

  const marketDetailContextValue: MarketDetailContextValue = {
    marketReturnData: marketReturnData,
    setMarketReturnData: setMarketReturnData,
    order: order,
    setOrder: setOrder,
    useMarket: useMarket,
  };

  if (marketReturnData) {
    return (
      <MarketDetailContext.Provider value={marketDetailContextValue}>
        <MarketDetailCard />
      </MarketDetailContext.Provider>
    );
  } else {
    return <></>;
  }
}
