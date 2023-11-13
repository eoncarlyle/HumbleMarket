import { Card, ListGroup, ListGroupItem } from "react-bootstrap";

export default function MarketProposalReviewCardBody({ marketProposal }: { marketProposal: MarketProposal }) {
  return (
    <Card.Body>
      <ListGroup>
        <ListGroupItem>Close Date: {marketProposal.closeDate}</ListGroupItem>
        <ListGroupItem>
          Outcomes
          <ListGroup>
            {marketProposal.outcomeClaims.map((claim: string) => (
              <ListGroupItem>{claim}</ListGroupItem>
            ))}
          </ListGroup>
        </ListGroupItem>
      </ListGroup>
    </Card.Body>
  );
}
