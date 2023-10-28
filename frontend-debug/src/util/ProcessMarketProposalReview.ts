import { Dispatch, SetStateAction } from "react";
import MarketProposalReviewState from "../model/MarketProposalReviewState";

import AdminPanelState, { SingleMarketState } from "../model/AdminPanelState";

export default async function processMarketProposalReview(
  marketProposal: MarketProposal,
  marketReviewAccepted: boolean,
  marketProposalReviewState: MarketProposalReviewState,
  setMarketProposalReviewState: Dispatch<SetStateAction<MarketProposalReviewState>>,
  adminPanelState: AdminPanelState,
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>
) {
  var marketProposalId = marketProposal.id;
  if (
    adminPanelState.map((singleMarketState) => singleMarketState.marketProposal.id === marketProposalId).length === 0
  ) {
    var newAdminPanelState = adminPanelState.concat([{ marketProposal: marketProposal, marketReviewed: true }]);
  } else {
    // Records the completed review in the AdminPanelState object
    var newAdminPanelState = adminPanelState.map((singleMarketState: SingleMarketState) => {
      if (singleMarketState.marketProposal.id === marketProposalId) {
        return { marketProposal: singleMarketState.marketProposal, marketReviewed: true };
      } else return singleMarketState;
    });
  }

  setAdminPanelState(newAdminPanelState);
}
