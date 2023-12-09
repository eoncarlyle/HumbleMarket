import { useState } from "react";
import Markdown from "react-markdown";
import { useFile } from "../util/Rest";

export default function About() {
  const [file, setFile] = useState<object | null>(null);
  const path = "../assets/About.md";
  useFile(path, file, setFile);
  if (file) return <Markdown>file</Markdown>;
  else return <></>;
}
