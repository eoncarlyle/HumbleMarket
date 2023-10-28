export interface SingleMarketState {
  marketProposal: MarketProposal;
  marketReviewed: boolean;
}

export type AdminPanelState = Array<SingleMarketState>

export const neutralAdminPanelState: AdminPanelState = [
];

export default AdminPanelState;