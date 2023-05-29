import { useRouteLoaderData } from "react-router-dom";

import MarketsBody from "./MarketsBody";
import LandingContent from "./LandingContent";
import { tokenExpired } from "../util/Auth";

function HomeContent() {
  const token = useRouteLoaderData("root") as string;
  return <>{token && !tokenExpired() ? <MarketsBody /> : <LandingContent />}</>;
}

export default HomeContent;
