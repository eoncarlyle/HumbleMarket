import { useLoaderData } from "react-router-dom"

function MarketProposalReview() {
  const marketProposalReturnData = useLoaderData() as MarketProposal[];

  return <>
    {marketProposalReturnData.map((marketProposal: MarketProposal) => (
      marketProposal.question
    ))}
  </>
}

export default MarketProposalReview;