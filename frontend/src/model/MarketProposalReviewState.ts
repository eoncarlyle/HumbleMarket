interface MarketProposalReviewState {
  id: string;
  reviewAccepted: boolean; 
  completed: boolean;
  isError: boolean;
  message: string;
}

export const neturalMarketProposalState: MarketProposalReviewState = {
  id: null,
  reviewAccepted: null,
  completed: false,
  isError: false,
  message: null
} 

export default MarketProposalReviewState