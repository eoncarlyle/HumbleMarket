import { useState } from "react";
import { useFile } from "../util/Rest";
import Markdown from "react-markdown";

export default function WrappedMarkdown({ path, className }: { path: string; className?: string }) {
  const [markdownText, setMarkdownText] = useState<string | null>(null);
  useFile(path, setMarkdownText);
  if (markdownText) {
    return <Markdown className={className}>{markdownText}</Markdown>;
  } else return <></>;
}
