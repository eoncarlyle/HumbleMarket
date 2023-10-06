import { Dispatch, SetStateAction } from "react";

import MarketProposalValidationData from "../model/MarketProposalValidationData";
import MarketProposalInputs, { neutralMarketProposalInputs } from "../model/MarketProposalInputs";
import { getAuthenticatedResponse } from "./Auth";
import ValidationField from "../model/ValidationField";

export default function processMarketProposalForm(
  marketProposal: MarketProposalInputs,
  marketProposalValidationData: MarketProposalValidationData,
  setMarketProposal: Dispatch<SetStateAction<MarketProposalInputs>>,
  setMarketProposalValidationData: Dispatch<SetStateAction<MarketProposalValidationData>>
) {
  const blankValidationField: ValidationField = { valid: false, message: "" };

  return async () => {
    //Pre-request validation
    //TODO: Close date in future
    if (marketProposal.closeDate < Date.now()) {
      marketProposalValidationData.closeDate = { valid: false, message: "Market close date must be in the future" };
    }
    
    let totalOutcomeClaims = new Set();
    
    marketProposal.outcomeClaims.forEach((outcome) => {
      if (totalOutcomeClaims.has(outcome)) {
        marketProposalValidationData.outcomeClaims = { valid: false, message: "Must have unique outcome claims" };
      } else totalOutcomeClaims.add(outcome);
    });

    if (marketProposalValidationData.closeDate.valid && marketProposalValidationData.outcomeClaims) {
      //TODO: centralise defaults like `marketMakerK`
      const response = await getAuthenticatedResponse("/market/market_proposal", "POST", {
        question: marketProposal.question,
        creatorId: "",
        marketMakerK: 100,
        closeDate: marketProposal.closeDate,
        outcomeClaims: marketProposal.outcomeClaims,
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
      } else {
        marketProposalValidationData.isCreated = true;
      }
    }

    //TODO: I am pretty sure that `setMarketProposalValidationDat(marketProposalValidationData)` did ...
    //TODO: ...not work due to shallow copy issues, investigate further
    setMarketProposalValidationData({
      question: marketProposalValidationData.question,
      closeDate: marketProposalValidationData.closeDate,
      outcomeClaims: marketProposalValidationData.outcomeClaims,
      isCreated: marketProposalValidationData.isCreated,
    });

    //! This didn't work when called as a conditional - why?
    setMarketProposal(marketProposalValidationData.isCreated ? { question: "", closeDate: null, outcomeClaims: [""] } : marketProposal);
  };
}