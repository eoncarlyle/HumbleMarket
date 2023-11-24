import PositionDirection from "./PositionDirection";

interface Order {
  positionDirection: PositionDirection;
  outcomeIndex: number;
  shares: number;
  submitted: boolean;
}

export default Order;
