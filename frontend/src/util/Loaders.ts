import { getAuthToken, getAuthenticatedResponse } from "./Auth";

// TODO: Fix the typing on this

export async function marketLoader({ params }: any) {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/market/" + params.marketId, "GET");

    //TODO: Flesh out the non-happy path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}// TODO: Fix the typing on this

export async function marketProposalsLoader(): Promise<MarketProposal[] | null> {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/market/market_proposal", "GET");

    //TODO: Flesh out the non-happy path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
export async function homeLoader() {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/market", "GET");

    //TODO: Flesh out the non-happy path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
// TODO: Fix the typing on this

export async function marketsReadyForResolutionLoader(): Promise<MarketProposal[] | null> {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/market/resolved", "GET");

    //TODO: Flesh out the non-happy path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
// TODO: Fix the typing on this

export async function accountLoader() {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/user/data", "GET");

    //TODO: Flesh out non-happy-path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}

