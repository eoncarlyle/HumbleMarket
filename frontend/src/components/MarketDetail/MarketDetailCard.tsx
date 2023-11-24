import { useContext } from "react";
import { Card, Tab, Tabs, Container, Button } from "react-bootstrap";
import { LinkContainer } from "react-router-bootstrap";

import MarketDetailOutcomeBox from "./MarketDetailOutcomeBox";
import BuyForm from "./BuyForm";
import SellForm from "./SellForm";
import MarketDetailContext from "../../util/MarketDetailContext";
import MarketDetailContextValue from "../../model/MarketDetailContextValue";

import styles from "../../style/MarketCard.module.css";

export default function MarketDetailCard() {
  const marketDetailContextValue = useContext(MarketDetailContext) as MarketDetailContextValue;
  const { marketReturnData } = marketDetailContextValue;
  const { market } = marketReturnData;

  const closeDate = new Date(market.closeDate);
  let outcomeIndex = 0;
  const outcomesList: JSX.Element[] = [];

  market.outcomes.forEach((outcome) => {
    outcomesList.push(<MarketDetailOutcomeBox outcome={outcome} outcomeIndex={outcomeIndex} />);
    outcomeIndex++;
  });

  return (
    <Card border="dark" className={styles.marketCard}>
      <Card.Body>
        <Card.Title className={styles.marketCardTitle}>{market.question}</Card.Title>
        <Card.Text className={styles.marketCardText}>
          Close Date: {closeDate.toDateString()} at {closeDate.toLocaleTimeString()}
        </Card.Text>
        {outcomesList}
        <Tabs className={styles.marketTabs}>
          <Tab eventKey="purchase" title="Buy">
            <Container className={styles.marketTabContent}>
              <BuyForm />
            </Container>
          </Tab>
          <Tab eventKey="sale" title="Sale">
            <Container className={styles.marketTabContent}>
              <SellForm />
            </Container>
          </Tab>
        </Tabs>
        <Container>
          <LinkContainer to={"/"} className={styles.homeMarketLink}>
            <Button variant="secondary">Back to other markets</Button>
          </LinkContainer>
        </Container>
      </Card.Body>
    </Card>
  );
}
