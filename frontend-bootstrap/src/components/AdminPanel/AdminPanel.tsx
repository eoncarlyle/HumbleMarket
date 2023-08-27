import { isAdmin } from "../../util/Auth";
import MarketProposalForm from "./MarketProposalForm";
import MarketProposalReview from "./MarketProposalReview";

// TODO: Build out this admin panel
function AdminPanel() {
  return <>{isAdmin() ? <>
    <MarketProposalForm /> 
    <MarketProposalReview />
  </> : <></>}</>;
}

export default AdminPanel;
