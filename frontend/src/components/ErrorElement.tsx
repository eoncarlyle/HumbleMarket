import { useRouteError } from "react-router-dom";
import HomeNavbar from "./HomeNavBar";
import { Button } from "react-bootstrap";

import styles from "../style/MarketCard.module.css";
import { LinkContainer } from "react-router-bootstrap";

export default function ErrorElement() {
  const error = useRouteError() as any;
  let message: string;
  if (error?.message) {
    message = error.message;
  } else {
    message = "No message provided by error";
  }
  return (
    <>
      {error ? <HomeNavbar /> : <> </>}
      <div className={styles.marketCard}>
        <h1>You have encountered an error!</h1>
        <p>Error message provided: {message}</p>
        <p>Please let Iain know about the issue!</p>
        <LinkContainer to="/">
          <Button>Return Home</Button>
        </LinkContainer>
      </div>
    </>
  );
}
