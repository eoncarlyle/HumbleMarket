import PositionDirection from "./PositionDirection";

interface Order {
  positionDirection: PositionDirection;
  outcomeIndex: number;
  shares: number;
}

export default Order;
