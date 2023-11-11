import { Row, Col } from "react-bootstrap";

interface RowWithColumnsProps {
  columnsInnerElements: Array<JSX.Element>;
}

export default function RowWithColumns({ columnsInnerElements }: RowWithColumnsProps) {
  return (
    <Row>
      {columnsInnerElements.map((element) => (
        <Col>{element}</Col>
      ))}
    </Row>
  );
}
