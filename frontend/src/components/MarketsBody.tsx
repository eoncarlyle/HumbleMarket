import { useLoaderData } from "react-router-dom";

import classes from "../styles/MarketsBody.module.css";

function MarketsBody() {
  const markets = useLoaderData() as any;
  return (
    //TODO: Use `BigDecimal` for prices, doubles for credits, integers for share numbers
    //TODO: show markets on desktop as a 2 column grid
    <div className={classes.body}>
      {markets.map((market: any) => (
        <div className={classes.marketBox}>
          <div>
            <div className={classes.question}>{market.question}</div>
            <div className={classes.closeDate}> Close Date: {market.closeDate} </div>
          </div>
          <div className={classes.outcomeContainer}>
            {market.outcomes.slice(0, 2).map((outcome: any) => (
              <div className={classes.outcome}>
                <div> {outcome.claim} </div>
                <div className={classes.prices}>
                  <div className={classes.priceYes}>Yes: {0.39} CR</div>
                  <div className={classes.priceNo}>No: {0.61} CR</div>
                </div>
              </div>
            ))}
          </div>
          <div className={classes.furtherInfoContainer}>
            <div className={classes.furtherInfoSubContainer}>
              {/* I am not proud of this but I don't know a better way to do this*/}
              <div className={classes.furtherInfo}>
                {market.outcomes.length < 3 ? (
                  <>View Market</>
                ) : (
                  <> View Market: {market.outcomes.length - 2} more outcomes</>
                )}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default MarketsBody;
