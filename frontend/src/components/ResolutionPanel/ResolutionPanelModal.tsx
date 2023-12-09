import { Modal, Button } from "react-bootstrap";

import MarketResolutionState from "../../model/MarketResolutionState";
import { processMarketResolution } from "../../util/MarketLifecycle";

interface ResolutionPanelModalProps {
  marketResolutionState: MarketResolutionState;
  setMarketResolutionState: React.Dispatch<React.SetStateAction<MarketResolutionState>>;
}

export default function ResolutionPanelModal({
  marketResolutionState,
  setMarketResolutionState,
}: ResolutionPanelModalProps) {
  let cardBody: string;

  const outcome = marketResolutionState.market?.outcomes?.at(marketResolutionState.outcomeIndex);

  const closeHandler = () => {
    setMarketResolutionState({
      resolvedMarkets: marketResolutionState.resolvedMarkets,
      market: marketResolutionState.market,
      outcomeIndex: marketResolutionState.outcomeIndex,
      code: marketResolutionState.code,
      isError: marketResolutionState.isError,
      message: marketResolutionState.message,
      showModal: false,
    });
  };

  const submitHandler = () => {
    processMarketResolution(marketResolutionState, setMarketResolutionState);
  };

  if (!marketResolutionState.market) {
    cardBody = "";
  } else if (marketResolutionState.market.outcomes.length > 1) {
    cardBody = `Resolve ${marketResolutionState.market?.question} market with outcome ${outcome.claim}?`;
  } else {
    cardBody = `Resolve ${marketResolutionState.market?.question} to ${outcome.claim}: ${marketResolutionState.direction}?`;
  }

  return (
    <Modal show={marketResolutionState.showModal} onHide={closeHandler}>
      <Modal.Header>
        <Modal.Title> Confirm Market Resolution </Modal.Title>
      </Modal.Header>
      <Modal.Body>{cardBody}</Modal.Body>
      <Modal.Footer>
        <Button variant="danger" onClick={closeHandler}>
          Cancel
        </Button>
        <Button variant="success" onClick={submitHandler}>
          Submit
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
