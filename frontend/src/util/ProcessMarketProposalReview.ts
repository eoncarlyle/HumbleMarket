import { Dispatch, SetStateAction } from "react";
import MarketProposalReviewState from "../model/MarketProposalReviewState";

import { getBaseUrl, getAuthenticatedResponse } from "./Auth";

export default async function processMarketProposalReview(
  marketProposalReviewState: MarketProposalReviewState,
  setMarketProposalReviewState: Dispatch<SetStateAction<MarketProposalReviewState>>
) {
  const reviewResult = marketProposalReviewState.reviewAccepted ? "accept_market_proposal" : "reject_market_proposal";

  const response = await getAuthenticatedResponse(`/market/${reviewResult}/${marketProposalReviewState.id}`, "POST", {
    marketProposalId: marketProposalReviewState.id,
  });

  // TODO: Only admin sees this, so improving this UX is a low priority item
  if (!response.ok) {
    setMarketProposalReviewState({
      id: marketProposalReviewState.id,
      reviewAccepted: marketProposalReviewState.reviewAccepted,
      completed: true,
      isError: true,
      message: `HTTP error ${response.status}`,
    });
  } else {
    setMarketProposalReviewState({
      id: marketProposalReviewState.id,
      reviewAccepted: marketProposalReviewState.reviewAccepted,
      completed: true,
      isError: false,
      message: null,
    });
  }
}
