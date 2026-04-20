package database;

import domain.BookCopy;
import domain.Loan;
import domain.Member;
import enums.AccountStatus;
import enums.CopyStatus;
import enums.LoanStatus;
import exceptions.AccountSuspendedException;
import exceptions.MaxBorrowLimitException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * LoanDAO handles all database operations for loans.
 */
public class LoanDAO {
    private Connection connection;
    private MemberDAO memberDAO;
    private BookDAO bookDAO;

    public LoanDAO() {
        try {
            this.connection =
                DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.out.println("LoanDAO connection error: "
                    + e.getMessage());
        }
        this.memberDAO = new MemberDAO();
        this.bookDAO   = new BookDAO();
    }

 
    /**
     * Inserts a new Loan record into the loans table after
     * validating member eligibility.
     *
     * @param loan - Loan object to insert into database
     * @throws AccountSuspendedException if member suspended
     * @throws MaxBorrowLimitException   if limit exceeded
     */
    public void issueLoan(Loan loan)
            throws AccountSuspendedException,
                   MaxBorrowLimitException {

        Member member = loan.getBorrower();

        //check account is not suspended
        if (member.getStatus() == AccountStatus.SUSPENDED ||
            member.getStatus() == AccountStatus.CLOSED) {
            throw new AccountSuspendedException(
                "Member account is " + member.getStatus() +
                ". Cannot issue loan to: " +
                member.getName());
        }

        //check borrow limit not exceeded
        int activeLoans = countActiveLoans(
                member.getUserId());

        if (activeLoans >= member.getMaxBorrowLimit()) {
            throw new MaxBorrowLimitException(
                "Member " + member.getName() +
                " has reached their borrow limit of " +
                member.getMaxBorrowLimit() + " books.");
        }

        //Insert loan record into database
        String sql = "INSERT INTO loans " +
                     "(member_id, copy_id, issue_date, " +
                     "due_date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setInt(1, member.getUserId());
            stmt.setInt(2, loan.getBookCopy().getCopyId());
            stmt.setDate(3, loan.getIssueDate() != null
                    ? new Date(loan.getIssueDate().getTime())
                    : null);
            stmt.setDate(4, loan.getDueDate() != null
                    ? new Date(loan.getDueDate().getTime())
                    : null);
            stmt.setString(5, LoanStatus.ACTIVE.name());

            stmt.executeUpdate();

            //Update BookCopy status to BORROWED
            updateCopyStatus(loan.getBookCopy().getCopyId(),
                             CopyStatus.BORROWED);

            System.out.println("Loan issued successfully to: "
                    + member.getName());

        } catch (SQLException e) {
            System.out.println("Error issuing loan: "
                    + e.getMessage());
        }
    }

    
    /**
     * Retrieves a single Loan from the database by loan ID.
     *
     * @param loanId - ID of the loan to retrieve
     * @return       - Loan object, or null if not found
     */
    public Loan getLoanById(int loanId) {
        String sql = "SELECT * FROM loans WHERE loan_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, loanId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToLoan(rs);
            } else {
                System.out.println(
                    "No loan found with ID: " + loanId);
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving loan: "
                    + e.getMessage());
            return null;
        }
    }

    
    /**
     * Retrieves all loan records from the loans table.
     *
     * @return - List of all Loan objects in the database
     */
    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();

        String sql = "SELECT * FROM loans";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving loans: "
                    + e.getMessage());
        }
        return loans;
    }

    
    /**
     * Retrieves all ACTIVE loans belonging to a member.
     *
     * @param memberId - ID of the member
     * @return         - List of active Loan objects
     */
    public List<Loan> getActiveLoansByMember(int memberId) {
        List<Loan> loans = new ArrayList<>();

        String sql = "SELECT * FROM loans " +
                     "WHERE member_id = ? " +
                     "AND status = 'ACTIVE'";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving member loans: "
                    + e.getMessage());
        }
        return loans;
    }

    
    /**
     * Records a book return by updating the loan record
     * with the return date and changing status to RETURNED.
     * Also updates the BookCopy status back to AVAILABLE.
     *
     * @param loanId     - ID of the loan being returned
     * @param returnDate - date the book was returned
     */
    public void returnLoan(int loanId,
                           java.util.Date returnDate) {
        String sql = "UPDATE loans SET "             +
                     "return_date = ?, "             +
                     "status = ? "                   +
                     "WHERE loan_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setDate(1, returnDate != null
                    ? new Date(returnDate.getTime())
                    : null);
            stmt.setString(2, LoanStatus.RETURNED.name());
            stmt.setInt(3, loanId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // retrieve the loan to get copy ID and update the copy status
                Loan loan = getLoanById(loanId);
                if (loan != null && loan.getBookCopy()
                                        != null) {
                    updateCopyStatus(
                        loan.getBookCopy().getCopyId(),
                        CopyStatus.AVAILABLE);
                }
                System.out.println(
                    "Book returned successfully.");
            } else {
                System.out.println(
                    "No loan found with ID: " + loanId);
            }

        } catch (SQLException e) {
            System.out.println("Error returning loan: "
                    + e.getMessage());
        }
    }


    /**
     * Updates all ACTIVE loans past their due date
     * to OVERDUE status. Called periodically to flag
     * loans that need attention.
     */
    public void markOverdueLoans() {
        String sql = "UPDATE loans SET status = 'OVERDUE' " +
                     "WHERE status = 'ACTIVE' "             +
                     "AND due_date < CURDATE()";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            int rowsAffected = stmt.executeUpdate();
            System.out.println(rowsAffected
                    + " loan(s) marked as OVERDUE.");

        } catch (SQLException e) {
            System.out.println("Error marking overdue: "
                    + e.getMessage());
        }
    }

    
    /**
     * Counts how many ACTIVE loans a member currently has.
     * Used to enforce the maxBorrowLimit before issuing
     * a new loan.
     *
     * @param memberId - ID of the member to check
     * @return         - count of active loans
     */
    private int countActiveLoans(int memberId) {
        String sql = "SELECT COUNT(*) FROM loans "  +
                     "WHERE member_id = ? "          +
                     "AND status = 'ACTIVE'";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println(
                "Error counting active loans: "
                    + e.getMessage());
        }
        return 0;
    }

    
    /**
     * Updates the status of a BookCopy
     *
     * @param copyId - ID of the copy to update
     * @param status - new CopyStatus to set
     */
    private void updateCopyStatus(int copyId,
                                   CopyStatus status) {
        String sql = "UPDATE book_copies SET status = ? " +
                     "WHERE copy_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setString(1, status.name());
            stmt.setInt(2, copyId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                "Error updating copy status: "
                    + e.getMessage());
        }
    }

    
    /**
     * Maps a ResultSet row to a Loan object.
     *
     * @param rs - ResultSet row from database query
     * @return   - populated Loan object
     * @throws SQLException if column name is wrong
     */
    private Loan mapResultSetToLoan(ResultSet rs)
            throws SQLException {

        int memberId = rs.getInt("member_id");
        int copyId   = rs.getInt("copy_id");

        // retrieve full Member object for this loan
        Member member = memberDAO.getMemberById(memberId);

        // retrieve all copies and find the matching one
        BookCopy bookCopy = null;
        if (member != null) {
            // fetch copies for whatever ISBN the copy belong to
            bookCopy = getBookCopyById(copyId);
        }

        return new Loan(
            rs.getInt("loan_id"),
            member,
            bookCopy,
            rs.getDate("issue_date"),
            rs.getDate("due_date"),
            rs.getDate("return_date"),
            // convert String from DB back to LoanStatus enum
            LoanStatus.valueOf(rs.getString("status"))
        );
    }

   
    /**
     * Retrieves a single BookCopy by its copy_id.
     *
     * @param copyId - ID of the book copy to retrieve
     * @return       - BookCopy object, or null if not found
     */
    private BookCopy getBookCopyById(int copyId) {
        String sql = "SELECT * FROM book_copies " +
                     "WHERE copy_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, copyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new BookCopy(
                    rs.getInt("copy_id"),
                    rs.getString("barcode"),
                    CopyStatus.valueOf(
                        rs.getString("status")),
                    rs.getString("book_isbn")
                );
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving book copy: "
                    + e.getMessage());
        }
        return null;
    }
}
