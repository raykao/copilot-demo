const usd = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

/**
 * Formats a USD amount, or an em dash when there is no amount.
 * @param {number | null | undefined} amount
 */
export const formatMoney = (amount) => (typeof amount === 'number' ? usd.format(amount) : '—');
