package domain;

import enums.PaymentStatus;
import java.util.Date;

/**
 * Payment represents a transaction made by a member to
 * settle an outstanding library fine.
 *
 * A Payment is always linked to one Fine. When a payment
 * is COMPLETED the associated Fine status updates to PAID.
 *
 * Used by:
 *   - FineDAO.java          for recording payment transactions
 *   - ReservationMenu.java  for CLI fine payment flow
 */
public class Payment {

    // --------------------------------------------------------
    // Attributes - mirrors the payments table columns
    // --------------------------------------------------------

    // Auto-generated primary key from the database
    private Integer paymentId;

    // Amount paid in dollars
    private float amount;

    // Date the payment was processed
    private Date paymentDate;

    // Payment method e.g. "Cash", "Card", "Online"
    private String method;

    // Current state of the payment transaction
    private PaymentStatus status;

    // The fine this payment is settling
    private Fine fine;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Creates a fully populated Payment object.
     * paymentId of 0 signals the DB will assign the ID.
     *
     * @param paymentId   - database primary key (0 for new)
     * @param amount      - amount paid in dollars
     * @param paymentDate - date the payment was processed
     * @param method      - payment method used
     * @param status      - current payment status
     * @param fine        - the fine being paid
     */
    public Payment(Integer paymentId, float amount,
                   Date paymentDate, String method,
                   PaymentStatus status, Fine fine) {
        this.paymentId   = paymentId;
        this.amount      = amount;
        this.paymentDate = paymentDate;
        this.method      = method;
        this.status      = status;
        this.fine        = fine;
    }

    // --------------------------------------------------------
    // Getters - retrieve private attribute values
    // --------------------------------------------------------

    public Integer getPaymentId() {
        return paymentId;
    }

    public float getAmount() {
        return amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public String getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Fine getFine() {
        return fine;
    }

    // --------------------------------------------------------
    // Setters - update private attribute values
    // --------------------------------------------------------

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setFine(Fine fine) {
        this.fine = fine;
    }

    // --------------------------------------------------------
    // toString - display payment details in readable format
    // --------------------------------------------------------

    /**
     * Returns formatted payment details.
     *
     * @return formatted string of all payment fields
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                "\nPayment ID  : " + paymentId   +
                "\nAmount      : $" + amount      +
                "\nDate        : " + paymentDate  +
                "\nMethod      : " + method       +
                "\nStatus      : " + status       +
                "\nFine ID     : " + (fine != null
                                     ? fine.getFineId()
                                     : "N/A")    +
                "\n=============================";
    }
}
