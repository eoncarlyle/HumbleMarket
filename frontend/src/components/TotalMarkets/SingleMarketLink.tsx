import { Link } from "react-router-dom";

import Market from "../../model/Market";
import classes from "../../styles/MarketsBody.module.css";

interface SingleMarketLinkProps {
  market: Market;
  singleMarketURI: string;
}

function SingleMarketLink({ market, singleMarketURI }: SingleMarketLinkProps) {
  return (
    <Link to={singleMarketURI} className={classes.singleMarketLink}>
      {market.outcomes.length < 3 ? <>View Market</> : <> View Market: {market.outcomes.length - 2} more outcomes</>}
    </Link>
  );
}

export default SingleMarketLink;
