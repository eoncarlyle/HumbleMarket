import React, { useState } from "react";
import { Button, Form, Row, Col } from "react-bootstrap";

import MarketProposalInputs from "../../model/MarketProposalInputs";
import MarketProposalValidationData, {
  neutralMarketProposalValidationData,
} from "../../model/MarketProposalValidationData";
import processMarketProposalForm from "../../util/processMarketProposalForm";
import AdminPanelState from "../../model/AdminPanelState";
import RowWithColumns from "../../util/RowWithColumns";

import styles from "../../style/MarketProposalForm.module.css";

interface MarketProposalFormReviewProps {
  adminPanelState: AdminPanelState;
  setAdminPanelState: React.Dispatch<React.SetStateAction<AdminPanelState>>;
}

export default function MarketProposalForm({ adminPanelState, setAdminPanelState }: MarketProposalFormReviewProps) {
  const [marketProposalInputs, setMarketProposalInputs] = useState<MarketProposalInputs>({
    question: "",
    closeDate: null,
    outcomeClaims: [""],
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

  const outcomesList: JSX.Element[] = [];
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

  const questionChangeHandler = (event: React.ChangeEvent<HTMLInputElement>) => {
    setMarketProposalInputs({
      ...marketProposalInputs,
      question: event.target.value,
    });
    setMarketProposalValidationData(neutralMarketProposalValidationData);
  };

  const marketCloseDateChangeHandler = (event: React.ChangeEvent<HTMLInputElement>) => {
    setMarketProposalInputs({
      ...marketProposalInputs,
      closeDate: Number(event.target.value),
    });
    setMarketProposalValidationData(neutralMarketProposalValidationData);
  };

  const submitMarketProposalHandler = () => {
    processMarketProposalForm(
      marketProposalInputs,
      setMarketProposalInputs,
      setMarketProposalValidationData,
      adminPanelState,
      setAdminPanelState
    );
  };

  //TODO: Validation with redundant names is broken, fix it

  return (
    <Col className={styles.marketProposalForm}>
      <h3> Market Proposal Form </h3>
      <Form.Group className="mb-3">
        <Form.Label>Market Question</Form.Label>
        <Form.Control
          type="text"
          isInvalid={!marketProposalValidationData.question.valid}
          onChange={questionChangeHandler}
          value={marketProposalInputs.question}
        />
        <Form.Control.Feedback type="invalid">{marketProposalValidationData?.question?.message}</Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Market Close Date (Millisecond Timestamp)</Form.Label>
        <Form.Control
          type="number"
          isInvalid={!marketProposalValidationData.closeDate.valid}
          onChange={marketCloseDateChangeHandler}
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

      <RowWithColumns
        columnsInnerElements={[
          <Button variant="success" onClick={addOutcome}>
            Add Outcome
          </Button>,
          <Button
            variant={marketProposalInputs.outcomeClaims.length <= 1 ? "outline-danger" : "danger"}
            onClick={removeOutcome}
            disabled={marketProposalInputs.outcomeClaims.length <= 1}
          >
            Delete Outcome
          </Button>,
        ]}
      />

      <Row>
        <Button variant="primary" onClick={submitMarketProposalHandler}>
          Create Market Proposal
        </Button>
      </Row>
    </Col>
  );
}
