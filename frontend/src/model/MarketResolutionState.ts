import Market from "./Market";

export default interface MarketResolutionState {
  markets: Market[];
  code: number;
  isError: boolean;
  message: string;
}