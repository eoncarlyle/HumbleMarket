import { useRouteLoaderData } from "react-router-dom";

import Market from "../model/Market";
import MarketOverview from "./MarketOverview/MarketOverview";

function MarketsBody() {
  const markets = useRouteLoaderData("home") as Array<Market>;
  return (
    //TODO: show markets on desktop as a 2 column grid
    <>
        {markets.map((market: Market) => (
          <MarketOverview market={market} />
        ))}
    </>
  );
}

export default MarketsBody;
