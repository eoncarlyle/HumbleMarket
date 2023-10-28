import ValidationField from "./ValidationField"

interface MarketProposalValidationData {
  question: ValidationField;
  closeDate: ValidationField;
  outcomeClaims: ValidationField;
  isCreated: boolean;
}

export const neutralMarketProposalValidationData: MarketProposalValidationData = {
  question: { valid: true, message: "" },
  closeDate: { valid: true, message: "" },
  outcomeClaims: { valid: true, message: "" },
  isCreated: false,
};

export default MarketProposalValidationData