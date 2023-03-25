import PositionData from "../model/PositionData";

function PositionDetail({ position }: { position: PositionData }) {
  return (
    <>
      Market: {position.marketQuestion}
      <ul>
        <li>
          {position.shares} {position.direction} shares for {position.outcomeClaim}
        </li>
        <li>Bought at {position.priceAtBuy}</li>
      </ul>
    </>
  );
}

export default PositionDetail;
