import { useState, Dispatch, SetStateAction } from "react";
import { Button, Form, Row, Col } from "react-bootstrap";

import MarketProposal from "../../model/MarketProposal";
import MarketProposalValidationData from "../../model/MarketProposalValidationData";
import processMarketProposalForm from "../../util/ProcessMarketProposalForm";

import styles from "../../style/MarketProposalForm.module.css";

function MarketProposalForm() {
  const [marketProposal, setMarketProposal] = useState<MarketProposal>({
    question: "",
    closeDate: null,
    outcomeClaims: [""],
  });

  const neutralMarketProposalValidationData: MarketProposalValidationData = {
    question: { valid: true, message: "" },
    closeDate: { valid: true, message: "" },
    outcomeClaims: { valid: true, message: "" },
    isCreated: false,
  };

  const [marketProposalValidationData, setMarketProposalValidationData] = useState<MarketProposalValidationData>(
    neutralMarketProposalValidationData
  );

  const outcomeChangeHandler = (index: number, value: String) => {
    const newOutcomes = marketProposal.outcomeClaims.toSpliced(index, 1, value);
    setMarketProposal({
      ...marketProposal,
      outcomeClaims: newOutcomes,
    });
    setMarketProposalValidationData(neutralMarketProposalValidationData);
  };

  var outcomesList: JSX.Element[] = [];
  var outcomeIndex = 0;
  marketProposal.outcomeClaims.forEach(() => {
    outcomesList.push(
      <Form.Control
        type="text"
        isInvalid={!marketProposalValidationData.outcomeClaims.valid}
        onChange={(event) => outcomeChangeHandler(outcomeIndex, event.target.value)}
      />
    );
  });

  const addOutcome = () => {
    setMarketProposal({
      ...marketProposal,
      outcomeClaims: marketProposal.outcomeClaims.concat(""),
    });
  };

  const removeOutcome = () => {
    if (marketProposal.outcomeClaims.length > 1) {
      setMarketProposal({
        ...marketProposal,
        outcomeClaims: marketProposal.outcomeClaims.slice(0, -1),
      });
      setMarketProposalValidationData(neutralMarketProposalValidationData);
    }
  };

  return (
    <Col className={styles.marketProposalForm}>
      <h3> Market Proposal Form </h3>
      <Form.Group className="mb-3">
        <Form.Label>Market Question</Form.Label>
        <Form.Control
          type="text"
          isInvalid={!marketProposalValidationData.question.valid}
          onChange={(event) => {
            setMarketProposal({ ...marketProposal, question: event.target.value });
            setMarketProposalValidationData(neutralMarketProposalValidationData);
          }}
        />
        <Form.Control.Feedback type="invalid">{marketProposalValidationData?.question?.message}</Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Market Close Date (Millisecond Timestamp)</Form.Label>
        <Form.Control
          type="number"
          isInvalid={!marketProposalValidationData.closeDate.valid}
          onChange={(event) => {
            setMarketProposal({ ...marketProposal, closeDate: Number(event.target.value) });
            setMarketProposalValidationData(neutralMarketProposalValidationData);
          }}
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
            variant={marketProposal.outcomeClaims.length <= 1 ? "outline-danger" : "danger"}
            onClick={removeOutcome}
            disabled={marketProposal.outcomeClaims.length <= 1}
          >
            Delete Outcome
          </Button>
        </Col>
      </Row>
      <Row>
        <Button
          variant="primary"
          onClick={processMarketProposalForm(
            marketProposal,
            marketProposalValidationData,
            setMarketProposalValidationData
          )}
        >
          Create Market Proposal
        </Button>
      </Row>
    </Col>
  );
}

export default MarketProposalForm;
