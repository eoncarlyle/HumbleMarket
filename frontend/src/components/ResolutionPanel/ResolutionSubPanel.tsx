import { useLoaderData } from "react-router-dom";

import Market from "../../model/Market";
import MarketResolutionCard from "./MarketResolutionCard";
import { useState } from "react";
import MarketResolutionState from "../../model/MarketResolutionState";

export default function ResolutionSubPanel() {
  let resolvableMarkets = useLoaderData() as Array<Market>;
  
  const [marketResolutionState, setMarketResolutionState] = useState<MarketResolutionState>({
    markets: [],
    code: null,
    isError: false,
    message: "",
  });
  //!TODO: Put modal for confirmation
  const resolvedMarketIds = marketResolutionState.markets.map((market: Market) => market.id);

  resolvableMarkets = resolvableMarkets.filter((market: Market) => {
    return !resolvedMarketIds.includes(market.id);
  });

  return (
    <>
      {resolvableMarkets.map((market: Market) => (
        <MarketResolutionCard
          market={market}
          marketResolutionState={marketResolutionState}
          setMarketResolutionState={setMarketResolutionState}
        />
      ))}
    </>
  );
}
