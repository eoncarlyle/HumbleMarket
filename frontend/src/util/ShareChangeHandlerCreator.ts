import Order from "../model/Order";
import { BaseSyntheticEvent, Dispatch, SetStateAction } from "react";

function shareChangeHandlerCreator(order: Order, setOrder: Dispatch<SetStateAction<Order>>) {
  return (event: BaseSyntheticEvent) => {
    const newOrder: Order = {
      positionDirection: order.positionDirection,
      outcomeIndex: order.outcomeIndex,
      shares: Number(event.target.value),
      submitted: false
    };
    setOrder(newOrder);
  };
}
export default shareChangeHandlerCreator;
