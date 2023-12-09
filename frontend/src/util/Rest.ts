import { useEffect } from "react";
import MarketReturnData from "../model/MarketReturnData";
import { getAuthToken, getAuthenticatedResponse } from "./Auth";


// TODO: Fix the typing on this
export async function getMarket(marketId: string) {
  //TODO: catch exceptions like these in a way that they clear data and log out
  if (!getAuthToken()) throw new Error("Invalid authentication token");
  const response = await getAuthenticatedResponse(`/market/${marketId}`, "GET");

  //TODO: Flesh out the non-happy path better
  const responseData = await response.json();
  return responseData as MarketReturnData;
}

export function useFile(path: string, setArticleText: React.Dispatch<React.SetStateAction<string | null>>) {
  useEffect(() => {
    fetch(path).then((response) => response.text()).then((text) => setArticleText(text))
  }, [path, setArticleText])
}
