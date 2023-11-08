import { useLoaderData } from "react-router-dom";
import { Card, ListGroup, Table } from "react-bootstrap";

import { priceNumberFormat } from "../util/Numeric";
import AccountData from "../model/AccountData";

import styles from "../style/AccountOverview.module.css";

function AccountOverview() {
  const returnData = useLoaderData() as AccountData;

  return (
    <>
      <Card className={styles.accountElements}>
        <h4>Account Details</h4>
        <ListGroup variant="flush">
          <ListGroup.Item>Email: {returnData.email}</ListGroup.Item>
          <ListGroup.Item>
            Credit Balance: {priceNumberFormat(returnData.credits)} CR
          </ListGroup.Item>
        </ListGroup>
      </Card>
      <Card className={styles.accountElements}>
        <h4>Active Positions Table</h4>
        <Table>
          <thead>
            <th>Market Question</th>
            <th>Direction</th>
            <th>Outcome</th>
            <th>Number of Shares</th>
            <th>Price at Buy</th>
          </thead>
          <tbody>
            {returnData.positionsReturnData.map((position) => (
              <tr>
                <th>{position.marketQuestion}</th>
                <th>{position.direction}</th>
                <th>{position.outcomeClaim}</th>
                <th>{position.shares}</th>
                <th>{position.priceAtBuy}</th>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>
    </>
  );
}

export default AccountOverview;
