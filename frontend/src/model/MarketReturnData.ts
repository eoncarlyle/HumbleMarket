import Market from "./Market";

//TODO: Remove this class and place it into the Market DetailContext Instead
//TODO pm-22: Add max credit attribute here and to the endpoint this comes from
export default interface MarketReturnData {
  market: Market;
  salePriceList: number[][][];
}

export const neutralMarketReturnData: MarketReturnData = {
  market: null,
  salePriceList: [[[]]]
}
