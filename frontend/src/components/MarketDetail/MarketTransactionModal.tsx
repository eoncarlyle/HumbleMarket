import { Modal, Button } from "react-bootstrap";

import Order from "../../model/Order";
import { priceNumberFormat } from "../../util/Numeric";
import TransactionType from "../../model/TransactionType";


interface MarketTransactionProps {
  transactionType: TransactionType, 
  showModal: boolean;
  order: Order;
  outcomeClaim: string;
  directionCost: number;
  handleClose: () => void;
  handleSubmit: () => Promise<void>;
}

export default function MarketTransactionModal({
  transactionType,
  showModal,
  order,
  outcomeClaim,
  directionCost,
  handleClose,
  handleSubmit,
}: MarketTransactionProps) {
  return (
    <Modal show={showModal} on>
      <Modal.Header>
        <Modal.Title>Confirm { transactionType }</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Are you sure that you want to { transactionType.toLowerCase() } {order.shares} {order.positionDirection} shares "{outcomeClaim}" for{" "}
        {priceNumberFormat(order.shares * directionCost)} CR?
      </Modal.Body>
      <Modal.Footer>
        <Button variant="danger" onClick={handleClose}>
          Cancel
        </Button>
        <Button variant="success" onClick={handleSubmit}>
          Submit { transactionType.toLowerCase() } 
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
