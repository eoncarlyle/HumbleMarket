import { Dispatch, SetStateAction, useState } from "react";

import Order from "../../model/Order";
import Market from "../../model/Market";
import TransactionType from "../../model/TransactionType";
import BuyForm from "./BuyForm";
import SellForm from "./SellForm";

import classes from "../../styles/SingleMarketBody.module.css";

interface TransactionFormProps {
  market: Market;
  order: Order;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function TransactionForm({ market, order, setOrder }: TransactionFormProps) {
  var outcome = market.outcomes[order.outcomeIndex];
  //TODO: Probably bad idea to have separate state for this now: either the entire order should be populated in advance
  //TODO: ...And that is the state that should be used or _only_ the order state that could be set from the
  //TODO: ...main body should be populated
  const [transactionType, setTransactionType] = useState<TransactionType>(TransactionType.Purchase);
  const isPurchase = transactionType === "Purchase";
  return (
    <>
      <div>
        <button
          className={isPurchase ? classes.activeTransaction : classes.inactiveTransaction}
          onClick={() => {
            setTransactionType(TransactionType.Purchase);
          }}
          type="button"
        >
          Buy
        </button>
        <button
          className={!isPurchase ? classes.activeTransaction : classes.inactiveTransaction}
          onClick={() => {
            setTransactionType(TransactionType.Sale);
          }}
          type="button"
        >
          Sell
        </button>
      </div>
      {isPurchase ? (
        <BuyForm market={market} order={order} outcome={outcome} setOrder={setOrder} />
      ) : (
        <SellForm market={market} order={order} outcome={outcome} setOrder={setOrder} />
      )}
    </>
  );
}

export default TransactionForm;
