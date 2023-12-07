import PositionDirection from "../model/PositionDirection";

export function isYes(positionDirection: PositionDirection) {
  return positionDirection === PositionDirection.YES
}

export function rawPrice(shares: number, outcomePriceList: number[]) {
  if (shares > outcomePriceList.length)
    return outcomePriceList.at(-1)
  else
    return outcomePriceList.at(shares - 1)
}

export function directionCost(positionDirection: PositionDirection, shares: number, outcomePriceList: number[]) {
  if (isYes(positionDirection))
    return rawPrice(shares, outcomePriceList)
  else
    return 1 - rawPrice(shares, outcomePriceList)
}