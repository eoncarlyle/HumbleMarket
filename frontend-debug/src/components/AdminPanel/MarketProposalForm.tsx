import React, { useState } from "react";
import { Button, Form, Row, Col } from "react-bootstrap";

import MarketProposalInputs, { neutralMarketProposalInputs } from "../../model/MarketProposalInputs";
import MarketProposalValidationData, {
  neutralMarketProposalValidationData,
} from "../../model/MarketProposalValidationData";
import processMarketProposalForm from "../../util/ProcessMarketProposalForm";
import AdminPanelState from "../../model/AdminPanelState";

import styles from "../../style/MarketProposalForm.module.css";

interface MarketProposalFormReviewProps {
  adminPanelState: AdminPanelState;
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>;
}

// TODO! Figure out why the outcome stays after the form is sent
export default function MarketProposalForm({ adminPanelState, setAdminPanelState }: MarketProposalFormReviewProps) {
  const [marketProposalInputs, setMarketProposalInputs] = useState<MarketProposalInputs>({
    question: "",
    closeDate: null,
    outcomeClaims: [null],
  });

  const [marketProposalValidationData, setMarketProposalValidationData] = useState<MarketProposalValidationData>(
    neutralMarketProposalValidationData
  );

  const outcomeChangeHandler = (index: number, value: string) => {
    const newOutcomes = marketProposalInputs.outcomeClaims;
    newOutcomes.splice(index, 1, value);
    setMarketProposalInputs({
      ...marketProposalInputs,
      outcomeClaims: newOutcomes,
    });
    setMarketProposalValidationData(neutralMarketProposalValidationData);
  };

  let outcomesList: JSX.Element[] = [];
  for (let outcomeIndex = 0; outcomeIndex < marketProposalInputs.outcomeClaims.length; outcomeIndex++) {
    outcomesList.push(
      <Form.Control
        type="text"
        isInvalid={!marketProposalValidationData.outcomeClaims.valid}
        onChange={(event) => {
          outcomeChangeHandler(outcomeIndex, event.target.value);
        }}
        value={marketProposalInputs.outcomeClaims[outcomeIndex]}
      />
    );
  }

  const addOutcome = () => {
    setMarketProposalInputs({
      ...marketProposalInputs,
      outcomeClaims: marketProposalInputs.outcomeClaims.concat(""),
    });
  };

  const removeOutcome = () => {
    if (marketProposalInputs.outcomeClaims.length > 1) {
      setMarketProposalInputs({
        ...marketProposalInputs,
        outcomeClaims: marketProposalInputs.outcomeClaims.slice(0, -1),
      });
      setMarketProposalValidationData(neutralMarketProposalValidationData);
    }
  };

  //TODO: Validation with redundant names is broken, fix it
  console.log(marketProposalInputs.outcomeClaims)  
  return (
    <Col className={styles.marketProposalForm}>
      <h3> Market Proposal Form </h3>
      <Form.Group className="mb-3">
        <Form.Label>Market Question</Form.Label>
        <Form.Control
          type="text"
          isInvalid={!marketProposalValidationData.question.valid}
          onChange={(event) => {
            setMarketProposalInputs({
              ...marketProposalInputs,
              question: event.target.value,
            });
            setMarketProposalValidationData(neutralMarketProposalValidationData);
          }}
          value={marketProposalInputs.question}
        />
        <Form.Control.Feedback type="invalid">{marketProposalValidationData?.question?.message}</Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Market Close Date (Millisecond Timestamp)</Form.Label>
        <Form.Control
          type="number"
          isInvalid={!marketProposalValidationData.closeDate.valid}
          onChange={(event) => {
            setMarketProposalInputs({
              ...marketProposalInputs,
              closeDate: Number(event.target.value),
            });
            setMarketProposalValidationData(neutralMarketProposalValidationData);
          }}
          //Value is what needs to be updated!
          value={marketProposalInputs.closeDate ? marketProposalInputs.closeDate : ""}
        />
        <Form.Control.Feedback type="invalid">{marketProposalValidationData?.closeDate?.message}</Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Outcomes List</Form.Label>
        {outcomesList}
        <Form.Control.Feedback type="invalid">
          {marketProposalValidationData?.outcomeClaims?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Row>
        <Col>
          <Button variant="success" onClick={addOutcome}>
            Add Outcome
          </Button>
        </Col>
        <Col>
          <Button
            variant={marketProposalInputs.outcomeClaims.length <= 1 ? "outline-danger" : "danger"}
            onClick={removeOutcome}
            disabled={marketProposalInputs.outcomeClaims.length <= 1}
          >
            Delete Outcome
          </Button>
        </Col>
      </Row>
      <Row>
        <Button
          variant="primary"
          onClick={() =>
            processMarketProposalForm(
              marketProposalInputs,
              marketProposalValidationData,
              setMarketProposalInputs,
              setMarketProposalValidationData,
              adminPanelState,
              setAdminPanelState
            )
          }
        >
          Create Market Proposal
        </Button>
      </Row>
    </Col>
  );
}
