/**
 * Rounds a USD amount to whole cents.
 * @param {number} value
 */
export const roundCurrency = (value) => Math.round(value * 100) / 100;
