import Market from "../../model/Market";
import SingleMarketLink from "./SingleMarketLink";
import TotalMarketsOutcomeBox from "./TotalMarketsOutcomeBox";
import classes from "../../styles/MarketsBody.module.css";

interface SingleMarketProps {
  market: Market;
}

function SingleMarket({ market }: SingleMarketProps) {
  var closeDate = new Date(market.closeDate);
  var singleMarketURI = "/market/" + market.seqId;
  return (
    <div className={classes.marketBox}>
      <div>
        <div className={classes.question}>{market.question}</div>
        <div className={classes.closeDate}>
          Close Date: {closeDate.toDateString()} at {closeDate.toLocaleTimeString()}
        </div>
      </div>
      <div className={classes.outcomeContainer}>
        {/* //TODO This neccesitated turning off strict null checks in tsconfig.json, investigate this later */}
        <TotalMarketsOutcomeBox
          outcome={market.outcomes.at(0)}
          singleMarketURI={singleMarketURI}
          selectedOutcomeIndex={0}
        />
        {market.outcomes.length > 2 ? (
          <TotalMarketsOutcomeBox
            outcome={market.outcomes.at(1)}
            singleMarketURI={singleMarketURI}
            selectedOutcomeIndex={1}
          />
        ) : (
          <></>
        )}
      </div>
      <div className={classes.singleMarketLinkContainer}>
        <div className={classes.singleMarketLinkSubContainer}>
          {" "}
          <SingleMarketLink market={market} singleMarketURI={singleMarketURI} />
        </div>
      </div>
    </div>
  );
}

export default SingleMarket;
