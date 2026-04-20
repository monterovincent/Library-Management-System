package domain;

import enums.LoanStatus;
import java.util.Date;

/**
 * Loan represents a single book borrowing transaction.
 */
public class Loan {

    private Integer loanId;
    private Member borrower;
    private BookCopy bookCopy;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private LoanStatus status;

 
    public Loan(Integer loanId, Member borrower,
                BookCopy bookCopy, Date issueDate,
                Date dueDate, Date returnDate,
                LoanStatus status) {
        this.loanId     = loanId;
        this.borrower   = borrower;
        this.bookCopy   = bookCopy;
        this.issueDate  = issueDate;
        this.dueDate    = dueDate;
        this.returnDate = returnDate;
        this.status     = status;
    }

  
    // Getters
    public Integer getLoanId() {
        return loanId;
    }

    public Member getBorrower() {
        return borrower;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    
    // Setters
    public void setLoanId(Integer loanId) {
        this.loanId = loanId;
    }

    public void setBorrower(Member borrower) {
        this.borrower = borrower;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    /**
     * Returns formatted loan details.
     * @return formatted string of all loan fields
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                "\nLoan ID    : " + loanId        +
                "\nBorrower   : " + (borrower != null
                                    ? borrower.getName()
                                    : "N/A")      +
                "\nBarcode    : " + (bookCopy != null
                                    ? bookCopy.getBarcode()
                                    : "N/A")      +
                "\nIssue Date : " + issueDate     +
                "\nDue Date   : " + dueDate       +
                "\nReturned   : " + (returnDate != null
                                    ? returnDate
                                    : "Not returned") +
                "\nStatus     : " + status        +
                "\n=============================";
    }
}
