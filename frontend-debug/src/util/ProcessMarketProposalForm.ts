import { Dispatch, SetStateAction } from "react";

import MarketProposalValidationData, {
  neutralMarketProposalValidationData,
} from "../model/MarketProposalValidationData";
import MarketProposalInputs from "../model/MarketProposalInputs";
import AdminPanelState from "../model/AdminPanelState";

export default async function processMarketProposalForm(
  marketProposalInputs: MarketProposalInputs,
  marketProposalValidationData: MarketProposalValidationData,
  setMarketProposalInputs: Dispatch<SetStateAction<MarketProposalInputs>>,
  setMarketProposalValidationData: Dispatch<SetStateAction<MarketProposalValidationData>>,
  adminPanelState: AdminPanelState,
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>
) {

  marketProposalValidationData.isCreated = true;
  var marketProposal: MarketProposal = {
    id: "myId",
    question: marketProposalInputs.question,
    marketMakerK: 100,
    closeDate: marketProposalInputs.closeDate,
    isPublic: false,
    outcomeClaims: marketProposalInputs.outcomeClaims 
  }
  setAdminPanelState(
    adminPanelState.concat([
      {
        marketProposal: marketProposal,
        marketReviewed: false,
      },
    ])
  );
  setMarketProposalValidationData(neutralMarketProposalValidationData);
  setMarketProposalInputs({
    question: "",
    closeDate: null,
    outcomeClaims: [""],
  });
}
