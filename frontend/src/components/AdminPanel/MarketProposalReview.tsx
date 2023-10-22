import { useState } from "react";
import { useLoaderData } from "react-router-dom";
import {
  Card,
  Button,
  ListGroup,
  ListGroupItem,
  Row,
  Col,
} from "react-bootstrap";

import MarketProposalReviewState, {
  neturalMarketProposalState,
} from "../../model/MarketProposalReviewState";
import processMarketProposalReview from "../../util/ProcessMarketProposalReview";

import MarketProposalReviewModal from "./MarketProposalReivewModal";

function MarketProposalReview() {
  //TODO: Stop doing this with a handler function and instead find a way to
  //TODO... accomplish this in way where on state changes it can be re-run
  let marketProposalReturnData = useLoaderData() as MarketProposal[];

  const [marketProposalReviewState, setMarketProposalReviewState] =
    useState<MarketProposalReviewState>(neturalMarketProposalState);

  return (
    <>
      {marketProposalReviewState.completed ? (
        <MarketProposalReviewModal
          marketProposalReviewState={marketProposalReviewState}
          setMarketProposalReviewState={setMarketProposalReviewState}
        />
      ) : (
        <></>
      )}
      {marketProposalReturnData.map((marketProposal: MarketProposal) => (
        <Card>
          <Card.Title>{marketProposal.question}</Card.Title>
          <Card.Body>
            <ListGroup>
              <ListGroupItem>
                Close Date: {marketProposal.closeDate}
              </ListGroupItem>
            </ListGroup>
            <ListGroup>
              <ListGroupItem>Outcomes</ListGroupItem>
              {marketProposal.outcomeClaims.map((claim: string) => (
                <ListGroupItem>{claim}</ListGroupItem>
              ))}
            </ListGroup>
          </Card.Body>
          <Row>
            <Col>
              <Button
                variant="success"
                onClick={() => {
                  processMarketProposalReview(
                    {
                      id: marketProposal.id,
                      reviewAccepted: true,
                      completed: false,
                      isError: false,
                      message: null,
                    },
                    setMarketProposalReviewState
                  );
                }}
              >
                Accept
              </Button>
            </Col>
            <Col>
              <Button
                variant="danger"
                onClick={() => {
                  processMarketProposalReview(
                    {
                      id: marketProposal.id,
                      reviewAccepted: false,
                      completed: false,
                      isError: false,
                      message: null,
                    },
                    setMarketProposalReviewState
                  );
                }}
              >
                Reject
              </Button>
            </Col>
          </Row>
        </Card>
      ))}
    </>
  );
}

export default MarketProposalReview;
