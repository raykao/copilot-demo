/**
 * Base class for expected, client-facing errors. The error handler maps
 * `status` and `code` straight onto the HTTP response.
 */
export class AppError extends Error {
  constructor(message, { status = 500, code = 'INTERNAL_ERROR' } = {}) {
    super(message);
    this.name = this.constructor.name;
    this.status = status;
    this.code = code;
  }
}

/** The request body is missing data or has invalid values. */
export class ValidationError extends AppError {
  constructor(message) {
    super(message, { status: 400, code: 'VALIDATION_ERROR' });
  }
}

/** A referenced product, customer or route does not exist. */
export class NotFoundError extends AppError {
  constructor(message) {
    super(message, { status: 404, code: 'NOT_FOUND' });
  }
}

/** A promo code is unknown, expired, or not applicable to the cart. */
export class InvalidPromoError extends AppError {
  constructor(message) {
    super(message, { status: 400, code: 'INVALID_PROMO' });
  }
}

/** There is not enough stock to fulfil an order line. */
export class OutOfStockError extends AppError {
  constructor(message) {
    super(message, { status: 409, code: 'OUT_OF_STOCK' });
  }
}

/** A customer tried to redeem more loyalty points than they have. */
export class InsufficientPointsError extends AppError {
  constructor(message) {
    super(message, { status: 409, code: 'INSUFFICIENT_POINTS' });
  }
}
