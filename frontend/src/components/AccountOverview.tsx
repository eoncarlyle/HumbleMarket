import { useLoaderData } from "react-router-dom";
import { Card, Table } from "react-bootstrap";

import { priceNumberFormat } from "../util/Numeric";
import AccountData from "../model/AccountData";

function AccountOverview() {
  const returnData = useLoaderData() as AccountData;

  return (
    <Card>
      <p>Email: {returnData.email}</p>
      <p>Credit Balance: {priceNumberFormat(returnData.credits)} CR</p>
      <p>Active Positions Table</p>
      <Table>
        <thead>
          <th>Direction</th>
          <th>Outcome</th>
          <th>Number of Shares</th>
          <th>Price at Buy</th>
        </thead>
        <tbody>
          {returnData.positionsReturnData.map((position) => (
            <tr>
              <th>{position.direction}</th>
              <th>{position.outcomeClaim}</th>
              <th>{position.shares}</th>
              <th>{position.priceAtBuy}</th>
            </tr>
          ))}
        </tbody>
      </Table>
    </Card>
  );
}

export default AccountOverview;
