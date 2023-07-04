import PositionDirection from "./PositionDirection";

interface PositionData {
  marketQuestion: String;
  marketSeqId: number;
  outcomeClaim: String;
  direction: PositionDirection;
  shares: number;
  priceAtBuy: number;
}

export default PositionData;
