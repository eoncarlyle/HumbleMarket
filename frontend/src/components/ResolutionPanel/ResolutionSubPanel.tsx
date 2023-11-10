import { useLoaderData } from "react-router-dom";

import { isAdmin } from "../../util/Auth";
import Market from "../../model/Market";

function ResolutionSubPanel() {
  const markets = useLoaderData() as Array<Market>;
  return (
    <>
      {markets.map((market: Market) => (
        <div>{market.closeDate} </div>
      ))}
    </>
  );
}

export default ResolutionSubPanel;
