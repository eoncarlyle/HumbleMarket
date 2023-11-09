import { useRouteLoaderData } from "react-router-dom";

import Market from "../model/Market";
import MarketOverview from "./MarketOverview/MarketOverview";

function MarketsBody() {
  const markets = useRouteLoaderData("home") as Array<Market>;
  const marketOverviewList = (
    <>
      {markets.map((market: Market) => (
        <MarketOverview market={market} />
      ))}
    </>
  );

  const noMarketsMessage = (
    <>
      <h2>
        No current markets to transact with, make sure to check back later!
      </h2>
    </>
  );

  return markets.length > 0 ? marketOverviewList : noMarketsMessage;
}

export default MarketsBody;
