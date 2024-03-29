import Outcome from "../model/Outcome";

interface Market {
  id: string;
  question: string;
  creatorId: string;
  marketMakerK: number;
  closeDate: number;
  outcomes: Outcome[];
  isPublic: boolean;
  isClosed: boolean;
  isResolved: boolean;
  salesPriceList: number[][][];
}
export default Market;
