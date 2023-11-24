import MarketReturnData from "./MarketReturnData";
import Order from "./Order";

export default interface MarketDetailContextValue {
  marketReturnData: MarketReturnData;
  setMarketReturnData: React.Dispatch<React.SetStateAction<MarketReturnData>>;
  order: Order;
  setOrder: React.Dispatch<React.SetStateAction<Order>>;
  useMarket: () => void;
}