import { Dispatch, SetStateAction } from "react";

import TransactionValidation from "../model/TransactionValidation";
import Order from "../model/Order";
import Market from "../model/Market";
import { getAuthenticatedResponse } from "./Auth";


export default function processBuyForm(market: Market, order: Order, setValid: Dispatch<SetStateAction<TransactionValidation>>) {
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
      shares: shares
    })

    if (!response.ok) {
      const responseBody = await response.json() as { message: string };
      let feedbackMessage: string;

      if (response.status === 422) {
        feedbackMessage = responseBody.message;
      } else {
        feedbackMessage = "Purchase unsuccesful, likely due to problems on our end!"
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
        message: "Purchase succesful!",
        order: order
      })
    }
  };
}
