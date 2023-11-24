import MarketReturnData from "../model/MarketReturnData";
import { getAuthToken, getAuthenticatedResponse } from "./Auth";

// TODO: Fix the typing on this
export default async function getMarket(marketId: string) {
  //TODO: catch exceptions like these in a way that they clear data and log out
  if (!getAuthToken()) throw new Error("Invalid authentication token")
  const response = await getAuthenticatedResponse(`/market/${marketId}`, "GET")

  //TODO: Flesh out the non-happy path better
  const responseData = await response.json();
  return responseData as MarketReturnData;
}
