import Market from "./Market";
import PositionDirection from "./PositionDirection";

export default interface MarketResolutionState {
  resolvedMarkets: Market[];
  market: Market;
  outcomeIndex: number;
  code: number;
  isError: boolean;
  message: string;
  showModal: boolean;
  direction?: PositionDirection;
}