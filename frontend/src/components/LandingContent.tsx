import { Button, Row } from "react-bootstrap";
import { LinkContainer } from "react-router-bootstrap";
import styles from "../style/LandingContent.module.css";

export default function LandingContent() {
  return (
    <>
      <h1>Think you have what it takes?</h1>
      <p className={styles.landingContent}>Create your own markets, compete against your friends and the public</p>
      <LinkContainer to="/auth/signup" className={styles.landingLink}>
        <Button variant="success">Sign Up</Button>
      </LinkContainer>
      <LinkContainer to="/auth/login" className={styles.landingLink}>
        <Button variant="primary">Log In</Button>
      </LinkContainer>
      <Row className={styles.landingRow}>
        <LinkContainer to="/about" className={styles.landingLink}>
          <a>More about this project</a>
        </LinkContainer>
      </Row>
    </>
  );
}
