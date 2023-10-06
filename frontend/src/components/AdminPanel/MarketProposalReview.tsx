import { useLoaderData } from "react-router-dom";

import { Card, Button, ListGroup, ListGroupItem, Row, Col } from "react-bootstrap";

function MarketProposalReview() {
  const marketProposalReturnData = useLoaderData() as MarketProposal[];

  return (
    <>
      {marketProposalReturnData.map((marketProposal: MarketProposal) => (
        <Card>
          <Card.Title>{marketProposal.question}</Card.Title>

          <Card.Body>
            <ListGroup>
              <ListGroupItem>Close Date: {marketProposal.closeDate}</ListGroupItem>
            </ListGroup>

            <ListGroup>
              <ListGroupItem>Outcomes</ListGroupItem>
              {marketProposal.outcomeClaims.map((claim: string) => (
                <ListGroupItem>claim</ListGroupItem>
              ))}
            </ListGroup>
          </Card.Body>
          {/* !! Figure out how to return ids from spring */}
          <Row>
            <Col>
              <Button variant="success">Accept</Button>
            </Col>
            <Col>
              <Button variant="danger">Reject</Button>
            </Col>
          </Row>
        </Card>
      ))}
    </>
  );
}

export default MarketProposalReview;
