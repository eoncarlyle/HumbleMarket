import { isAdmin } from "../../util/Auth";

import ResolutionSubPanel from "./ResolutionSubPanel";

export default function ResolutionPanel() {
  return <>{isAdmin() ? <ResolutionSubPanel /> : <></>}</>;
}

