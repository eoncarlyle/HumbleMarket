import { Col } from "react-bootstrap";

import WrappedMarkdown from "./WrappedMarkdown";

import styles from "../style/Article.module.css";

export default function About() {
  return (
    <Col className={styles.articleCol}>
      <h2>About</h2>
      <WrappedMarkdown className={styles.articleBody} path={"/About.md"} />
    </Col>
  );
}
