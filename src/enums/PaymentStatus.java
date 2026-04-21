package enums;

/**
 * PaymentStatus defines the states of a fine payment
 * transaction made by a library member.
 *
 * Used by:
 *   - Payment.java    as the status field type
 *   - FineDAO.java    for filtering and updates
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
