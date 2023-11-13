import { useLoaderData } from "react-router-dom";

import Market from "../../model/Market";
import MarketResolutionCard from "./MarketResolutionCard";
import { useState } from "react";
import MarketResolutionState from "../../model/MarketResolutionState";
import ResolutionPanelModal from "./ResolutionPanelModal";

export default function ResolutionSubPanel() {
  let resolvableMarkets = useLoaderData() as Array<Market>;

  const [marketResolutionState, setMarketResolutionState] = useState<MarketResolutionState>({
    resolvedMarkets: [],
    market: null,
    outcomeIndex: null,
    code: null,
    isError: false,
    message: "",
    showModal: false
  });
  
  const resolvedMarketIds = marketResolutionState.resolvedMarkets.map((market: Market) => market.id);
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
      <ResolutionPanelModal
        marketResolutionState={marketResolutionState}
        setMarketResolutionState={setMarketResolutionState}
      />
    </>
  );
}
