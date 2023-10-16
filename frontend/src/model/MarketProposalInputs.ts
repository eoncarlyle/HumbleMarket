interface MarketProposalInputs {
  question: string;
  closeDate: number;
  outcomeClaims: string[];
}

export const neutralMarketProposalInputs: MarketProposalInputs = {
  question: "",
  closeDate: null,
  outcomeClaims: [null,]
}

export default MarketProposalInputs;