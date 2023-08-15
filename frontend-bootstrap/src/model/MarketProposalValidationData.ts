import ValidationField from "./ValidationField"

interface MarketProposalValidationData {
  question: ValidationField;
  closeDate: ValidationField;
  outcomeClaims: ValidationField;
  isCreated: boolean;
}

export default MarketProposalValidationData