import { Dispatch, SetStateAction } from "react";
import MarketProposalInputs from "../model/MarketProposalInputs";
import MarketProposalValidationData, { neutralMarketProposalValidationData } from "../model/MarketProposalValidationData";
import { getAuthenticatedResponse } from "./Auth";
import AdminPanelState, { SingleMarketState } from "../model/AdminPanelState";
import MarketProposalReviewState from "../model/MarketProposalReviewState";
import Market from "../model/Market";
import MarketResolutionState from "../model/MarketResolutionState";

export async function processMarketProposalForm(
  marketProposalInputs: MarketProposalInputs,
  setMarketProposalInputs: Dispatch<SetStateAction<MarketProposalInputs>>,
  setMarketProposalValidationData: Dispatch<SetStateAction<MarketProposalValidationData>>,
  adminPanelState: AdminPanelState,
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>
) {
  let marketProposalValidationData: MarketProposalValidationData = {
    question: { valid: true, message: "" },
    closeDate: { valid: true, message: "" },
    outcomeClaims: { valid: true, message: "" },
    isCreated: false,
  };

  //Pre-request validation
  //TODO: Close date in future
  if (marketProposalInputs.closeDate < Date.now()) {
    marketProposalValidationData.closeDate = { valid: false, message: "Market close date must be in the future" };
  }

  const totalOutcomeClaims = new Set();

  marketProposalInputs.outcomeClaims.forEach((outcome) => {
    if (totalOutcomeClaims.has(outcome)) {
      marketProposalValidationData.outcomeClaims = { valid: false, message: "Must have unique outcome claims" };
    } else totalOutcomeClaims.add(outcome);
  });

  if (marketProposalValidationData.closeDate.valid && marketProposalValidationData.outcomeClaims.valid) {
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
        marketProposalValidationData.outcomeClaims = {
          valid: false,
          message: "Server error during submission - please try again in a few minutes",
        };
      } else {
        marketProposalValidationData.outcomeClaims = {
          valid: false,
          message: `HTTP error response ${response.status} on submission - please report!`,
        };
      }
    } else {
      marketProposalValidationData.isCreated = true;
      const marketProposal = (await response.json()) as MarketProposal;
      setAdminPanelState(
        adminPanelState.concat([
          {
            marketProposal: marketProposal,
            marketReviewed: false,
          },
        ])
      );

      marketProposalValidationData = neutralMarketProposalValidationData;
      setMarketProposalInputs({
        question: "",
        closeDate: null,
        outcomeClaims: [""],
      });
    }
  }

  setMarketProposalValidationData({
    question: marketProposalValidationData.question,
    closeDate: marketProposalValidationData.closeDate,
    outcomeClaims: marketProposalValidationData.outcomeClaims,
    isCreated: marketProposalValidationData.isCreated,
  });
}

export async function processMarketProposalReview(
  marketProposal: MarketProposal,
  marketReviewAccepted: boolean,
  setMarketProposalReviewState: Dispatch<SetStateAction<MarketProposalReviewState>>,
  adminPanelState: AdminPanelState,
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>
) {
  const reviewResult = marketReviewAccepted ? "accept_market_proposal" : "reject_market_proposal";
  const marketProposalId = marketProposal.id;

  const response = await getAuthenticatedResponse(`/market/${reviewResult}/${marketProposalId}`, "POST", {
    marketProposalId: marketProposalId,
  });

  let newAdminPanelState: AdminPanelState;

  if (!response.ok) {
    setMarketProposalReviewState({
      isError: true,
      message: `HTTP error ${response.status}`,
    });
  } else {
    setMarketProposalReviewState({
      isError: false,
      message: null,
    });

    // If market isn't present in AdminPanel state, have to provide it
    if (adminPanelState.map((singleMarketState) => singleMarketState.marketProposal.id === marketProposalId).length === 0) {
      newAdminPanelState = adminPanelState.concat([{ marketProposal: marketProposal, marketReviewed: true }]);
    } else {
      // Records the completed review in the AdminPanelState object
      newAdminPanelState = adminPanelState.map((singleMarketState: SingleMarketState) => {
        if (singleMarketState.marketProposal.id === marketProposalId) {
          return { marketProposal: singleMarketState.marketProposal, marketReviewed: true };
        } else return singleMarketState;
      });
    }

    setAdminPanelState(newAdminPanelState);
  }
}

export async function processMarketResolution(
  marketResolutionState: MarketResolutionState,
  setMarketResolutionState: React.Dispatch<React.SetStateAction<MarketResolutionState>>
) {
  let response: Response;
  if (marketResolutionState.market.outcomes.length > 1) {
    response = await getAuthenticatedResponse(
      `/market/resolve_market/${marketResolutionState.market.id}/${marketResolutionState.outcomeIndex}`,
      "POST"
    );
  } else {
    response = await getAuthenticatedResponse(
      `/market/resolve_market/${marketResolutionState.market.id}/direction/${marketResolutionState.direction}`,
      "POST"
    );
  }

  //TODO: Provide acutal feedback for when this goes wrong
  if (!response.ok) {
    setMarketResolutionState({
      resolvedMarkets: marketResolutionState.resolvedMarkets,
      market: marketResolutionState.market,
      outcomeIndex: marketResolutionState.outcomeIndex,
      code: response.status,
      isError: true,
      message: `HTTP Error ${response.status} encountered during submission!`,
      showModal: false,
    });
  } else {
    const market = (await response.json()) as Market;
    setMarketResolutionState({
      resolvedMarkets: marketResolutionState.resolvedMarkets.concat(market),
      market: market,
      outcomeIndex: marketResolutionState.outcomeIndex,
      code: response.status,
      isError: true,
      message: "Market resolution successful!",
      showModal: false,
    });
  }
}

