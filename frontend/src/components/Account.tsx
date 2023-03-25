import { useLoaderData } from "react-router-dom";

import classes from "../styles/Account.module.css";

import { priceNumberFormat } from "../util/Numeric";
import PositionDetail from "./PositionDetail";
import AccountData from "../model/AccountData";

function Account() {
  const returnData = useLoaderData() as AccountData;
  return (
    <div className={classes.body}>
      <div className={classes.sectionBox}>
        <div>Username: {returnData.email}</div>
        <div>Credits Available: {priceNumberFormat(returnData.credits)}</div>
        <div>
          Active Positions:
          <ul>
            {returnData.positionsReturnData.map((position) => (
              <li>
                {" "}
                <PositionDetail position={position} />{" "}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Account;
