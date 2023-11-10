import { isAdmin } from "../../util/Auth";

import ResolutionSubPanel from "./ResolutionSubPanel";

function ResolutionPanel() {
  return <>{isAdmin() ? <ResolutionSubPanel /> : <></>}</>;
}

export default ResolutionPanel;
