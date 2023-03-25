import { useRouteLoaderData } from "react-router-dom";

import MarketsBody from "./MarketsBody";
import LandingContent from "./LandingContent";

function HomeContent() {
  const token = useRouteLoaderData("root") as string;
  return <>{token ? <MarketsBody /> : <LandingContent />}</>;
}

export default HomeContent;
