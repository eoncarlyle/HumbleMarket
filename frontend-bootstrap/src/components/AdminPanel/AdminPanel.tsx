import { isAdmin } from "../../util/Auth";
import MarketProposalForm from "./MarketProposalForm";

// TODO: Build out this admin panel
function AdminPanel() {
  return <>{isAdmin() ? <MarketProposalForm /> : <></>}</>;
}

export default AdminPanel;
