import { Dispatch, SetStateAction } from "react";
import Market from "../model/Market";
import Order from "../model/Order";
import TransactionValidation from "../model/TransactionValidation";
import { getAuthenticatedResponse } from "./Auth";

export default function processSellForm(market: Market, order: Order, setValid: Dispatch<SetStateAction<TransactionValidation>>) {
  return async () => {
    const [outcomeIndex, positionDirection, shares] = [
      order.outcomeIndex,
      order.positionDirection,
      order.shares,
    ];

    const requestSubpath = `/market/${market.seqId}/outcome/${outcomeIndex}/${positionDirection}/sale/${shares}`;
    const response = await getAuthenticatedResponse(requestSubpath, "POST")

    if (!response.ok) {
      const responseBody = await response.json() as { message: string };
      if (response.status === 422) {
        var feedbackMessage = responseBody.message;
      } else {
        var feedbackMessage = "Sale unsuccesful, likely due to problems on our end!"
      }
      setValid({
        valid: false, 
        showModal: false,
        message: feedbackMessage
      })
    } else {
      setValid({
        valid: true, 
        showModal: false,
        message: "Sale succesful!" 
      })
    }
  }
}