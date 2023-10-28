import { Dispatch, SetStateAction } from "react";

import MarketProposalValidationData, {
  neutralMarketProposalValidationData,
} from "../model/MarketProposalValidationData";
import MarketProposalInputs, { neutralMarketProposalInputs } from "../model/MarketProposalInputs";
import { getAuthenticatedResponse } from "./Auth";
import ValidationField from "../model/ValidationField";
import AdminPanelState from "../model/AdminPanelState";
import { neturalMarketProposalState } from "../model/MarketProposalReviewState";

export default async function processMarketProposalForm(
  marketProposalInputs: MarketProposalInputs,
  marketProposalValidationData: MarketProposalValidationData,
  setMarketProposalInputs: Dispatch<SetStateAction<MarketProposalInputs>>,
  setMarketProposalValidationData: Dispatch<SetStateAction<MarketProposalValidationData>>,
  adminPanelState: AdminPanelState,
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>
) {
  const blankValidationField: ValidationField = { valid: false, message: "" };

  //Pre-request validation
  //TODO: Close date in future
  if (marketProposalInputs.closeDate < Date.now()) {
    marketProposalValidationData.closeDate = { valid: false, message: "Market close date must be in the future" };
  }

  let totalOutcomeClaims = new Set();

  marketProposalInputs.outcomeClaims.forEach((outcome) => {
    if (totalOutcomeClaims.has(outcome)) {
      marketProposalValidationData.outcomeClaims = { valid: false, message: "Must have unique outcome claims" };
    } else totalOutcomeClaims.add(outcome);
  });

  if (marketProposalValidationData.closeDate.valid && marketProposalValidationData.outcomeClaims) {
    //TODO: centralise defaults like `marketMakerK`
    const response = await getAuthenticatedResponse("/market/market_proposal", "POST", {
      question: marketProposalInputs.question,
      creatorId: "",
      marketMakerK: 100,
      closeDate: marketProposalInputs.closeDate,
      outcomeClaims: marketProposalInputs.outcomeClaims,
      isPublic: true,
    });

    //TODO: This makes the assumption that only the market question is wrong at this point, eventually this might not be the case
    if (!response.ok) {
      if (response.status === 422) {
        marketProposalValidationData.question = {
          valid: false,
          message: "A market for this question already exists",
        };
      } else if (response.status == 500) {
        marketProposalValidationData.question = blankValidationField;
        marketProposalValidationData.closeDate = blankValidationField;
        marketProposalValidationData.outcomeClaims = {
          valid: false,
          message: "Server error during submission - please try again in a few minutes",
        };
      } else {
        marketProposalValidationData.question = blankValidationField;
        marketProposalValidationData.closeDate = blankValidationField;
        marketProposalValidationData.outcomeClaims = {
          valid: false,
          message: `HTTP error response ${response.status} on submission - please report!`,
        };
      }

      setMarketProposalValidationData({
        question: marketProposalValidationData.question,
        closeDate: marketProposalValidationData.closeDate,
        outcomeClaims: marketProposalValidationData.outcomeClaims,
        isCreated: marketProposalValidationData.isCreated,
      });
    } else {
      marketProposalValidationData.isCreated = true;
      var marketProposal = (await response.json()) as MarketProposal;
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
  }
}
