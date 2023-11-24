import React from "react"

import TransactionValidation from "../model/TransactionValidation";
import Order from "../model/Order";

type SetTransactionValidation = React.Dispatch<React.SetStateAction<TransactionValidation>>

export const submitHandlerFactory = (setTransactionValidation: SetTransactionValidation, order: Order) => {
  return () => {
    setTransactionValidation({
      valid: true,
      showModal: true,
      message: "",
      order: order,
    });
  };
}

export const closeHandlerFactory = (transactionValidation: TransactionValidation,
  setTransactionValidation: SetTransactionValidation,
  order: Order) => {

  return () => {
    setTransactionValidation({
      valid: transactionValidation.valid,
      showModal: false,
      message: transactionValidation.message,
      order: order,
    });
  }
}

export const shareButtonHandlerFactory = (setTransactionValidation: SetTransactionValidation, order: Order) => {
  return () => {
    setTransactionValidation({
      valid: true,
      showModal: false,
      message: "",
      order: order,
    })
  }
}
