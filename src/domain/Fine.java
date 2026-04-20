package domain;

import enums.FineStatus;
import java.util.Date;

/**
 * Fine represents an overdue penalty issued to a member.
 */
public class Fine {

    private Integer fineId;
    private String reason;
    private float amount;
    private Date issuedDate;
    private Loan loan;
    private FineStatus status;

    public Fine(Integer fineId, String reason, float amount,
                Date issuedDate, Loan loan,
                FineStatus status) {
        this.fineId     = fineId;
        this.reason     = reason;
        this.amount     = amount;
        this.issuedDate = issuedDate;
        this.loan       = loan;
        this.status     = status;
    }

    // Getters
    public Integer getFineId() {
        return fineId;
    }

    public String getReason() {
        return reason;
    }

    public float getAmount() {
        return amount;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public Loan getLoan() {
        return loan;
    }

    public FineStatus getStatus() {
        return status;
    }

    // Setters
    public void setFineId(Integer fineId) {
        this.fineId = fineId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public void setStatus(FineStatus status) {
        this.status = status;
    }

    /**
     * Returns formatted fine details.
     * @return formatted string of all fine fields
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                "\nFine ID    : " + fineId        +
                "\nReason     : " + reason        +
                "\nAmount     : $" + amount       +
                "\nIssued     : " + issuedDate    +
                "\nLoan ID    : " + (loan != null
                                    ? loan.getLoanId()
                                    : "N/A")      +
                "\nStatus     : " + status        +
                "\n=============================";
    }
}
