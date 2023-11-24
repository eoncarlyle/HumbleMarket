import { Dispatch, SetStateAction } from "react";
import Market from "../model/Market";
import Order from "../model/Order";
import TransactionValidation from "../model/TransactionValidation";
import { getAuthenticatedResponse } from "./Auth";

//TODO: Rename TransactionValidation to TransactionValidationData
export default function processSellForm(market: Market,
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
    })

    if (!response.ok) {
      const responseBody = await response.json() as { message: string };
      let feedbackMessage;

      if (response.status === 422) {
        feedbackMessage = responseBody.message;
      } else {
        feedbackMessage = "Sale unsuccessful, likely due to problems on our end!"
      }
      setValid({
        valid: false,
        showModal: false,
        message: feedbackMessage,
        order: order
      })

    } else {
      setValid({
        valid: true,
        showModal: false,
        message: "Sale successful!",
        order: order
      })
      setOrder({
        positionDirection: positionDirection,
        outcomeIndex: outcomeIndex,
        shares: shares,
        submitted: true
      })
    }
  }
}