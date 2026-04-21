// ============================================================
// Description: Data Access Object for all fine and payment
//              related database operations in the Library
//              Management System. Handles CRUD operations
//              against the fines and payments tables in
//              MariaDB. All methods use PreparedStatements
//              to prevent SQL injection attacks.
// Inputs:      Fine objects, Payment objects, loan IDs,
//              member IDs
// Processing:  Executes SQL queries through the singleton
//              DatabaseConnection. Maps ResultSet rows back
//              to Fine and Payment Java objects.
// Outputs:     Fine objects and Lists of Fines returned to
//              ReservationMenu for display and management
// ============================================================

package database;

import domain.Fine;
import domain.Loan;
import domain.Member;
import domain.Notification;
import domain.Payment;
import enums.FineStatus;
import enums.PaymentStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * FineDAO handles all database operations for fines
 * and fine payments.
 *
 * DAO Pattern:
 *   Domain classes (Fine, Payment) stay clean
 *   All SQL lives here in the DAO
 *
 * Used by:
 *   - ReservationMenu.java  for CLI fine payment flow
 */
public class FineDAO {

    // --------------------------------------------------------
    // Attributes
    // --------------------------------------------------------

    // Single shared connection from Singleton
    private Connection connection;

    // Helper DAO to reconstruct Loan objects
    private LoanDAO loanDAO;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Constructor - retrieves the singleton database
     * connection when FineDAO is instantiated.
     */
    public FineDAO() {
        try {
            // get the single shared connection
            this.connection =
                DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.out.println(
                "FineDAO connection error: "
                    + e.getMessage());
        }
        this.loanDAO = new LoanDAO();
    }

    // --------------------------------------------------------
    // CREATE - Issue a new fine
    // --------------------------------------------------------

    /**
     * Inserts a new Fine record into the fines table.
     * Called by LoanDAO or a librarian when a loan is overdue.
     * Sends a notification to the member about the fine.
     *
     * @param fine - Fine object to insert into database
     */
    public void addFine(Fine fine) {
        String sql = "INSERT INTO fines "               +
                     "(loan_id, reason, amount, "       +
                     "issued_date, status) "            +
                     "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setInt(1,
                fine.getLoan() != null
                    ? fine.getLoan().getLoanId()
                    : 0);
            stmt.setString(2, fine.getReason());
            stmt.setFloat(3, fine.getAmount());
            stmt.setDate(4,
                fine.getIssuedDate() != null
                    ? new Date(fine.getIssuedDate()
                                   .getTime())
                    : null);
            stmt.setString(5, FineStatus.UNPAID.name());

            stmt.executeUpdate();

            System.out.println(
                "Fine issued: $" + fine.getAmount()
                + " - " + fine.getReason());

            // notify the member about the fine
            if (fine.getLoan() != null
                    && fine.getLoan().getBorrower()
                       != null) {
                Member member =
                    fine.getLoan().getBorrower();
                Notification note = new Notification(
                    0,
                    "A fine of $" + fine.getAmount()   +
                    " has been issued to your account. " +
                    "Reason: " + fine.getReason(),
                    "FINE",
                    new java.util.Date()
                );
                note.sendEmail(member);
            }

        } catch (SQLException e) {
            System.out.println(
                "Error adding fine: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // READ - Get a single fine by ID
    // --------------------------------------------------------

    /**
     * Retrieves a single Fine from the database by ID.
     *
     * @param fineId - ID of the fine to retrieve
     * @return       - Fine object, or null if not found
     */
    public Fine getFineById(int fineId) {
        String sql = "SELECT * FROM fines " +
                     "WHERE fine_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, fineId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFine(rs);
            } else {
                System.out.println(
                    "No fine found with ID: " + fineId);
                return null;
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving fine: "
                    + e.getMessage());
            return null;
        }
    }

    // --------------------------------------------------------
    // READ - Get all fines
    // --------------------------------------------------------

    /**
     * Retrieves all fine records from the fines table.
     *
     * @return - List of all Fine objects in the database
     */
    public List<Fine> getAllFines() {
        List<Fine> fines = new ArrayList<>();

        String sql = "SELECT * FROM fines";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fines.add(mapResultSetToFine(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving fines: "
                    + e.getMessage());
        }
        return fines;
    }

    // --------------------------------------------------------
    // READ - Get unpaid fines for a member
    // --------------------------------------------------------

    /**
     * Retrieves all UNPAID fines belonging to a member.
     * Joins the fines table with loans to filter by member.
     * Called by ReservationMenu to show outstanding fines.
     *
     * @param memberId - ID of the member to check
     * @return         - List of unpaid Fine objects
     */
    public List<Fine> getUnpaidFinesByMember(int memberId) {
        List<Fine> fines = new ArrayList<>();

        // join with loans to filter by member
        String sql = "SELECT f.* FROM fines f "       +
                     "JOIN loans l "                  +
                     "ON f.loan_id = l.loan_id "      +
                     "WHERE l.member_id = ? "         +
                     "AND f.status = 'UNPAID'";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fines.add(mapResultSetToFine(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving member fines: "
                    + e.getMessage());
        }
        return fines;
    }

    // --------------------------------------------------------
    // READ - Get fines linked to a specific loan
    // --------------------------------------------------------

    /**
     * Retrieves all fines linked to a specific loan.
     * Called when processing a loan return to check
     * outstanding penalties.
     *
     * @param loanId - ID of the loan to check
     * @return       - List of Fine objects for that loan
     */
    public List<Fine> getFinesByLoan(int loanId) {
        List<Fine> fines = new ArrayList<>();

        String sql = "SELECT * FROM fines "    +
                     "WHERE loan_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, loanId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                fines.add(mapResultSetToFine(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving loan fines: "
                    + e.getMessage());
        }
        return fines;
    }

    // --------------------------------------------------------
    // UPDATE - Update fine status
    // --------------------------------------------------------

    /**
     * Updates the status field of an existing fine record.
     * Called after a successful payment to mark fine as PAID,
     * or by a librarian to waive a fine.
     *
     * @param fineId - ID of the fine to update
     * @param status - new FineStatus to apply
     */
    public void updateFineStatus(int fineId,
                                  FineStatus status) {
        String sql = "UPDATE fines SET status = ? " +
                     "WHERE fine_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setString(1, status.name());
            stmt.setInt(2, fineId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(
                    "Fine " + fineId
                        + " status updated to: "
                        + status);
            } else {
                System.out.println(
                    "No fine found with ID: " + fineId);
            }

        } catch (SQLException e) {
            System.out.println(
                "Error updating fine status: "
                    + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // PAYMENT - Record a fine payment
    // --------------------------------------------------------

    /**
     * Inserts a new Payment record into the payments table
     * and updates the associated Fine status to PAID.
     * Called from ReservationMenu when a member pays a fine.
     *
     * @param payment - Payment object to record
     */
    public void recordPayment(Payment payment) {
        String sql = "INSERT INTO payments "          +
                     "(fine_id, amount, "             +
                     "payment_date, method, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setInt(1,
                payment.getFine() != null
                    ? payment.getFine().getFineId()
                    : 0);
            stmt.setFloat(2, payment.getAmount());
            stmt.setDate(3,
                payment.getPaymentDate() != null
                    ? new Date(payment.getPaymentDate()
                                      .getTime())
                    : null);
            stmt.setString(4, payment.getMethod());
            stmt.setString(5,
                PaymentStatus.COMPLETED.name());

            stmt.executeUpdate();

            System.out.println(
                "Payment recorded: $"
                    + payment.getAmount()
                    + " via " + payment.getMethod());

            // mark the fine as paid now that payment
            // has been recorded successfully
            if (payment.getFine() != null) {
                updateFineStatus(
                    payment.getFine().getFineId(),
                    FineStatus.PAID);
            }

        } catch (SQLException e) {
            System.out.println(
                "Error recording payment: "
                    + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // HELPER - map database rows to Java objects
    // --------------------------------------------------------

    /**
     * Maps a ResultSet row to a Fine object.
     * Called internally after every SELECT query.
     * Keeps mapping logic in one place - DRY principle.
     *
     * @param rs - ResultSet row from database query
     * @return   - populated Fine object
     * @throws SQLException if column name is wrong
     */
    private Fine mapResultSetToFine(ResultSet rs)
            throws SQLException {

        int loanId = rs.getInt("loan_id");

        // retrieve full Loan object for this fine
        Loan loan = loanDAO.getLoanById(loanId);

        return new Fine(
            rs.getInt("fine_id"),
            rs.getString("reason"),
            rs.getFloat("amount"),
            rs.getDate("issued_date"),
            loan,
            // convert String from DB back to FineStatus enum
            FineStatus.valueOf(rs.getString("status"))
        );
    }
}
