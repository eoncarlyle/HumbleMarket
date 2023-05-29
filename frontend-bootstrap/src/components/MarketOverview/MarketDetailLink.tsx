import { LinkContainer } from "react-router-bootstrap";

import Market from "../../model/Market";
import { Button } from "react-bootstrap";

interface MarketDetailLink {
  market: Market;
  singleMarketURI: string;
}

function MarketDetailLink({ market, singleMarketURI }: MarketDetailLink) {
  return (
    <LinkContainer to={singleMarketURI}>
      <Button variant="secondary">
        {market.outcomes.length < 3 ? <>View Market</> : <> View Market: {market.outcomes.length - 2} more outcomes</>}
      </Button>
    </LinkContainer>
  );
}

export default MarketDetailLink;
