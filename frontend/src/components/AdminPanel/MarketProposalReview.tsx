import { useState } from "react";
import { useLoaderData } from "react-router-dom";
import { Card, Button, Row, Col } from "react-bootstrap";

import MarketProposalReviewState from "../../model/MarketProposalReviewState";
import processMarketProposalReview from "../../util/ProcessMarketProposalReview";
import AdminPanelState, { SingleMarketState } from "../../model/AdminPanelState";

import styles from "../../style/MarketProposalForm.module.css";
import MarketProposalReviewCardBody from "./MarketProposalReviewCardBody";

interface MarketProposalReviewProps {
  adminPanelState: AdminPanelState;
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>;
}

export default function MarketProposalReview({ adminPanelState, setAdminPanelState }: MarketProposalReviewProps) {
  let marketProposalReturnData = useLoaderData() as MarketProposal[];

  const [, setMarketProposalReviewState] = useState<MarketProposalReviewState>();

  const marketProposalReviewHandler = (marketProposal: MarketProposal, marketReviewAccepted: boolean) => {
    processMarketProposalReview(
      marketProposal,
      marketReviewAccepted,
      setMarketProposalReviewState,
      adminPanelState,
      setAdminPanelState
    );
  };

  // Removes markets that have been reviewed, adds markets created by form but not yet present
  adminPanelState.forEach((singleMarketState: SingleMarketState) => {
    if (singleMarketState.marketReviewed) {
      marketProposalReturnData = marketProposalReturnData.filter(
        (marketProposal: MarketProposal) => marketProposal.id !== singleMarketState.marketProposal.id
      );
    } else {
      if (!marketProposalReturnData.includes(singleMarketState.marketProposal)) {
        marketProposalReturnData.push(singleMarketState.marketProposal);
      }
    }
  });
  //TODO: Fix minor CSS validation bug

  const marketProposalCards = marketProposalReturnData.map((marketProposal: MarketProposal) => (
    <Card>
      <Card.Title>{marketProposal.question}</Card.Title>
      <MarketProposalReviewCardBody marketProposal={marketProposal} />
      <Row className={styles.buttonRow}>
        <Col>
          <Button variant="success" onClick={() => marketProposalReviewHandler(marketProposal, true)}>
            Accept
          </Button>
        </Col>
        <Col>
          <Button variant="danger" onClick={() => marketProposalReviewHandler(marketProposal, false)}>
            Reject
          </Button>
        </Col>
      </Row>
    </Card>
  ));

  return (
    <>
      <Col className={styles.marketProposalForm}>
        <h3> Market Proposal Review </h3>
        {marketProposalCards}
      </Col>
    </>
  );
}
