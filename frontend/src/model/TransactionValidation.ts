import Order from "./Order";

interface TransactionValidation {
  valid: boolean;
  showModal: boolean;
  message: string;
  order: Order;
}

export default TransactionValidation;
