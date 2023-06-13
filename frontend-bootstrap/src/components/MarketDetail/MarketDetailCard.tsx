import { Dispatch, SetStateAction } from "react";
import { Card, Tab, Tabs, Container, Button } from "react-bootstrap";
import { LinkContainer } from "react-router-bootstrap";

import Order from "../../model/Order";
import Market from "../../model/Market";
import MarketDetailOutcomeBox from "./MarketDetailOutcomeBox";
import BuyForm from "./BuyForm";
import SellForm from "./SellForm";

import styles from "../../style/MarketCard.module.css";

interface MarketDetailCardProps {
  market: Market;
  salePriceList: number[][][];
  order: Order;
  setOrder: Dispatch<SetStateAction<Order>>;
}

function MarketDetailCard({ market, salePriceList, order, setOrder }: MarketDetailCardProps) {
  var closeDate = new Date(market.closeDate);
  var outcomeIndex = 0;
  var outcomesList: JSX.Element[] = [];

  market.outcomes.forEach((outcome) => {
    outcomesList.push(<MarketDetailOutcomeBox outcome={outcome} setOrder={setOrder} outcomeIndex={outcomeIndex} />);
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

        {/* 
        TODO
        - Buy/Sell form submission and validation
          - Modal to confirm -> loading spinner -> feedback
        
        */}
        <Tabs className={styles.marketTabs}>
          <Tab eventKey="purchase" title="Buy">
            <Container className={styles.marketTabContent}>
              <BuyForm market={market} order={order} setOrder={setOrder} />
            </Container>
          </Tab>
          <Tab eventKey="sale" title="Sale">
            <Container className={styles.marketTabContent}>
              <SellForm market={market} salePriceList={salePriceList} order={order} setOrder={setOrder} />
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

export default MarketDetailCard;
