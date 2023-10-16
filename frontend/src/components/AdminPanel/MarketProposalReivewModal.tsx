import { Modal, Button } from "react-bootstrap"
import MarketProposalReviewState, { neturalMarketProposalState } from "../../model/MarketProposalReviewState";

interface MarketProposalReviewModalProps {
  marketProposalReviewState: MarketProposalReviewState;
  setMarketProposalReviewState:  React.Dispatch<React.SetStateAction<MarketProposalReviewState>>;
}

function MarketProposalReviewModal({ marketProposalReviewState, setMarketProposalReviewState}: MarketProposalReviewModalProps) {
  
  const handleClose = () => setMarketProposalReviewState(neturalMarketProposalState) 
  const reviewAcceptedString = marketProposalReviewState.reviewAccepted ? "Accepted" : "Rejected" 
  let modalTitle, modalBody : string

  if (marketProposalReviewState.isError) {
    modalTitle = `Market proposal review error for ${reviewAcceptedString}`
    modalBody = `Error ${marketProposalReviewState.message}, market proposal id: ${marketProposalReviewState.id}`
  } else {
    modalTitle = `Market Proposal ${reviewAcceptedString}`
    modalBody = `Market proposal id: ${marketProposalReviewState.id}`
  }  

  return (
    <Modal show={true} onHide={handleClose}>
      <Modal.Header> 
        <Modal.Title>{ modalTitle }</Modal.Title>
      </Modal.Header>
      <Modal.Body>{modalBody}</Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
         Ok 
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

export default MarketProposalReviewModal