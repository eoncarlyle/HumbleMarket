import PositionDirection from "./PositionDirection";

interface PositionData {
  marketQuestion: String;
  outcomeClaim: String;
  direction: PositionDirection;
  shares: number;
  priceAtBuy: number;
}

export default PositionData;
