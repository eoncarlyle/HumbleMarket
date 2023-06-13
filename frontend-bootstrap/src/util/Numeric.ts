export function priceNumberFormat(val: number): string {
  if (Number.isNaN(val)) {
    return "N/A"
  } else {
    return Number(val).toFixed(2);
  }
}
