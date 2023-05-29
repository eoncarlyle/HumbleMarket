import { useRouteLoaderData } from "react-router-dom";

import { tokenExpired } from "../util/Auth";
import LandingContent from "./LandingContent"
import TotalMarketOverview from "./TotalMarketOverview";

function HomeContent() {
  const token = useRouteLoaderData("root") as string;
  return <>{token && !tokenExpired() ? <TotalMarketOverview/> : <LandingContent/>}</>
}

export default HomeContent;