import { useState } from "react";
import { isAdmin } from "../../util/Auth";
import MarketProposalForm from "./MarketProposalForm";
import MarketProposalReview from "./MarketProposalReview";
import AdminPanelState, { neutralAdminPanelState } from "../../model/AdminPanelState";
// TODO: Build out this admin panel
function AdminPanel() {
  const [adminPanelState, setAdminPanelState] = useState<AdminPanelState>(neutralAdminPanelState);

  return <>{isAdmin() ? <>
    <MarketProposalForm adminPanelState={adminPanelState} setAdminPanelState={setAdminPanelState} /> 
    <MarketProposalReview adminPanelState={adminPanelState} setAdminPanelState={setAdminPanelState} />
  </> : <></>}</>;
}

export default AdminPanel;
