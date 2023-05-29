import { useRouteLoaderData } from "react-router-dom";

import Market from "../model/Market";
import SingleMarket from "./TotalMarkets/SingleMarket";
import classes from "../styles/MarketsBody.module.css";

function MarketsBody() {
  const markets = useRouteLoaderData("home") as Array<Market>;
  return (
    //TODO: show markets on desktop as a 2 column grid
    <>
      <div className={classes.body}>
        {markets.map((market: Market) => (
          <SingleMarket market={market} />
        ))}
      </div>
    </>
  );
}

export default MarketsBody;
