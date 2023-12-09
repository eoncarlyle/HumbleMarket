import { useState } from "react";
import { useFile } from "../util/Rest";
import { Container } from "react-bootstrap";

import "../style/Article.module.css";

export default function About() {
  const [articleText, setArticleText] = useState<string | null>(null);
  const path = "/src/assets/About.md";
  useFile(path, setArticleText);
  if (articleText)
    return (
      <>
        <h1>About</h1>
        <Container>
          <p>{articleText}</p>
        </Container>
      </>
    );
  else return <></>;
}
