import { BaseSyntheticEvent, Dispatch, SetStateAction } from "react";

import Market from "../model/Market";
import Order from "../model/Order";
import TransactionValidation from "../model/TransactionValidation";
import { getAuthenticatedResponse } from "./Auth";
import PositionDirection from "../model/PositionDirection";

type SetTransactionValidation = React.Dispatch<React.SetStateAction<TransactionValidation>>

export function processBuyForm(market: Market,
  order: Order,
  sharePrice: number,
  setValid: Dispatch<SetStateAction<TransactionValidation>>,
  setOrder: Dispatch<SetStateAction<Order>>) {
  return async () => {
    const [outcomeIndex, positionDirection, shares] = [
      order.outcomeIndex,
      order.positionDirection,
      order.shares,
    ];

    const response = await getAuthenticatedResponse("/market/purchase", "POST", {
      id: market.id,
      outcomeIndex: outcomeIndex,
      positionDirection: positionDirection,
      shares: shares,
      sharePrice: sharePrice
    });

    if (!response.ok) {
      const responseBody = await response.json() as { message: string; };
      let feedbackMessage: string;

      if (response.status === 422) {
        feedbackMessage = responseBody.message;
      } else {
        feedbackMessage = "Purchase unsuccessful, likely due to problems on our end!";
      }

      setValid({
        valid: false,
        showModal: false,
        message: feedbackMessage,
        order: order
      });
    } else {
      const updatedOrder: Order = {
        positionDirection: positionDirection,
        outcomeIndex: outcomeIndex,
        shares: shares,
        submitted: true
      };
      setOrder(updatedOrder);
      setValid({
        valid: true,
        showModal: false,
        message: "Purchase successful!",
        order: updatedOrder
      });
    }
  };
}//TODO: Rename TransactionValidation to TransactionValidationData

export function processSellForm(market: Market,
  order: Order,
  sharePrice: number,
  setValid: Dispatch<SetStateAction<TransactionValidation>>,
  setOrder: Dispatch<SetStateAction<Order>>) {

  return async () => {
    const [outcomeIndex, positionDirection, shares] = [
      order.outcomeIndex,
      order.positionDirection,
      order.shares,
    ];

    const response = await getAuthenticatedResponse("/market/sale", "POST", {
      id: market.id,
      outcomeIndex: outcomeIndex,
      positionDirection: positionDirection,
      shares: shares,
      sharePrice: sharePrice
    });

    if (!response.ok) {
      const responseBody = await response.json() as { message: string; };
      let feedbackMessage;

      if (response.status === 422) {
        feedbackMessage = responseBody.message;
      } else {
        feedbackMessage = "Sale unsuccessful, likely due to problems on our end!";
      }
      setValid({
        valid: false,
        showModal: false,
        message: feedbackMessage,
        order: order
      });

    } else {
      const updatedOrder: Order = {
        positionDirection: positionDirection,
        outcomeIndex: outcomeIndex,
        shares: shares,
        submitted: true
      };
      setOrder(updatedOrder);
      setValid({
        valid: true,
        showModal: false,
        message: "Sale successful!",
        order: updatedOrder
      });
    }
  };
}

export function shareChangeHandlerCreator(order: Order, setOrder: Dispatch<SetStateAction<Order>>) {
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

export function submitHandlerFactory(setTransactionValidation: SetTransactionValidation, order: Order) {
  return () => {
    setTransactionValidation({
      valid: true,
      showModal: true,
      message: "",
      order: order,
    });
  };
}

export function shareButtonHandlerFactory(setTransactionValidation: SetTransactionValidation, order: Order) {
  return () => {
    setTransactionValidation({
      valid: true,
      showModal: false,
      message: "",
      order: order,
    });
  };
}
export function closeHandlerFactory(transactionValidation: TransactionValidation,
  setTransactionValidation: SetTransactionValidation,
  order: Order) {

  return () => {
    setTransactionValidation({
      valid: transactionValidation.valid,
      showModal: false,
      message: transactionValidation.message,
      order: order,
    });
  };
}
export function isYes(positionDirection: PositionDirection) {
  return positionDirection === PositionDirection.YES;
}
export function rawPrice(shares: number, outcomePriceList: number[]) {
  if (shares > outcomePriceList.length)
    return outcomePriceList.at(-1);

  else
    return outcomePriceList.at(shares - 1);
}
export function directionCost(positionDirection: PositionDirection, shares: number, outcomePriceList: number[]) {
  if (isYes(positionDirection))
    return rawPrice(shares, outcomePriceList);

  else
    return 1 - rawPrice(shares, outcomePriceList);
}

