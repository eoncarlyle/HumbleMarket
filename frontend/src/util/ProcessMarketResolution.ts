import { Dispatch, SetStateAction } from "react";
import Market from "../model/Market";

import { getAuthenticatedResponse } from "./Auth";
import MarketResolutionState from "../model/MarketResolutionState";

export default async function processMarketResolution(
  marketId: string,
  outcomeIndex: number,
  marketResolutionState: MarketResolutionState,
  setMarketResolutionState: React.Dispatch<React.SetStateAction<MarketResolutionState>>
) {
  const response = await getAuthenticatedResponse(`/market/resolve_market/${marketId}/${outcomeIndex}`, "POST");

  if (!response.ok) {
    setMarketResolutionState({
      markets: marketResolutionState.markets, 
      code: response.status,
      isError: true,
      message: `HTTP Error ${response.status} encountered during submission!`,
    });
  } else {
    const market = (await response.json()) as Market;
    setMarketResolutionState({
      markets: marketResolutionState.markets.concat(market),
      code: response.status,
      isError: true,
      message: "Market resolution successful!",
    });
  }
}
