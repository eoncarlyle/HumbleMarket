interface MarketProposal {
  id: string;
  question: string;
  marketMakerK: number;
  closeDate: number;
  outcomeClaims: string[];
  isPublic: boolean;
}