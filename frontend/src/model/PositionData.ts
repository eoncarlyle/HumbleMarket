import PositionDirection from "./PositionDirection";

interface PositionData {
  marketQuestion: string;
  marketmarketId: number;
  outcomeClaim: string;
  direction: PositionDirection;
  shares: number;
  priceAtBuy: number;
}

export default PositionData;
