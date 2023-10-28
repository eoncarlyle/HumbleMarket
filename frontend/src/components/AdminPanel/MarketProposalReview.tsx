import { useState } from "react";
import { useLoaderData } from "react-router-dom";
import { Card, Button, ListGroup, ListGroupItem, Row, Col } from "react-bootstrap";

import MarketProposalReviewState, { neturalMarketProposalState } from "../../model/MarketProposalReviewState";
import processMarketProposalReview from "../../util/ProcessMarketProposalReview";
import AdminPanelState, { SingleMarketState } from "../../model/AdminPanelState";

import styles from "../../style/MarketProposalForm.module.css";

interface MarketProposalReviewProps {
  adminPanelState: AdminPanelState;
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>;
}

function MarketProposalReview({ adminPanelState, setAdminPanelState }: MarketProposalReviewProps) {
  let marketProposalReturnData = useLoaderData() as MarketProposal[];

  const [marketProposalReviewState, setMarketProposalReviewState] =
    useState<MarketProposalReviewState>(neturalMarketProposalState);

  const marketProposalReviewHandler = (marketProposal: MarketProposal, marketReviewAccepted: boolean) => {
    processMarketProposalReview(
      marketProposal,
      marketReviewAccepted,
      {
        isError: false,
        message: null,
      },
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
        marketProposalReturnData.push(singleMarketState.marketProposal)
      } 
    }
  });
  //TODO: Fix minor CSS validation bug

  return (
    <>
      <Col className={styles.marketProposalForm}>
        <h3> Market Proposal Review </h3>
        {marketProposalReturnData.map((marketProposal: MarketProposal) => (
          <Card>
            <Card.Title>{marketProposal.question}</Card.Title>
            <Card.Body>
              <ListGroup>
                <ListGroupItem >Close Date: {marketProposal.closeDate}</ListGroupItem>
                <ListGroupItem>
                  Outcomes  
                  <ListGroup>{marketProposal.outcomeClaims.map((claim: string) => (
                      <ListGroupItem>{claim}</ListGroupItem>
                    ))}</ListGroup>
                  </ListGroupItem>
              </ListGroup>
            </Card.Body>
            <Row className={ styles.buttonRow }>
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
        ))}
      </Col>
    </>
  );
}

export default MarketProposalReview;
