import Market from "../model/Market";

import { getAuthenticatedResponse } from "./Auth";
import MarketResolutionState from "../model/MarketResolutionState";

export default async function processMarketResolution(
  marketResolutionState: MarketResolutionState,
  setMarketResolutionState: React.Dispatch<React.SetStateAction<MarketResolutionState>>
) {
  let response: Response;
  if (marketResolutionState.market.outcomes.length > 1) {
    response = await getAuthenticatedResponse(
      `/market/resolve_market/${marketResolutionState.market.id}/${marketResolutionState.outcomeIndex}`,
      "POST"
    );
  } else {
    response = await getAuthenticatedResponse(
      `/market/resolve_market/${marketResolutionState.market.id}/direction/${marketResolutionState.direction}`,
      "POST"
    );
  }

  //TODO: Provide acutal feedback for when this goes wrong
  if (!response.ok) {
    setMarketResolutionState({
      resolvedMarkets: marketResolutionState.resolvedMarkets,
      market: marketResolutionState.market,
      outcomeIndex: marketResolutionState.outcomeIndex,
      code: response.status,
      isError: true,
      message: `HTTP Error ${response.status} encountered during submission!`,
      showModal: false,
    });
  } else {
    const market = (await response.json()) as Market;
    setMarketResolutionState({
      resolvedMarkets: marketResolutionState.resolvedMarkets.concat(market),
      market: market,
      outcomeIndex: marketResolutionState.outcomeIndex,
      code: response.status,
      isError: true,
      message: "Market resolution successful!",
      showModal: false,
    });
  }
}
