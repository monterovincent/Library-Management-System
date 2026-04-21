// ============================================================
// Description: Data Access Object for all reservation related
//              database operations in the Library Management
//              System. Handles CREATE, READ, UPDATE operations
//              against the reservations table in MariaDB.
//              All methods use PreparedStatements to prevent
//              SQL injection attacks.
// Inputs:      Reservation objects, member IDs, book ISBNs
// Processing:  Executes SQL queries through the singleton
//              DatabaseConnection. Maps ResultSet rows back
//              to Reservation Java objects.
// Outputs:     Reservation objects and Lists of Reservations
//              returned to ReservationMenu
// ============================================================

package database;

import domain.Book;
import domain.Member;
import domain.Notification;
import domain.Reservation;
import enums.ReservationStatus;
import exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ReservationDAO handles all database operations for
 * book reservations.
 *
 * DAO Pattern:
 *   Domain class (Reservation) stays clean
 *   All SQL lives here in the DAO
 *
 * Used by:
 *   - ReservationMenu.java  for CLI driven operations
 */
public class ReservationDAO {

    // --------------------------------------------------------
    // Attributes
    // --------------------------------------------------------

    // Single shared connection from Singleton
    private Connection connection;

    // Helper DAOs to reconstruct Member and Book objects
    private MemberDAO memberDAO;
    private BookDAO   bookDAO;

    // Number of days a reservation stays ACTIVE before expiring
    private static final int RESERVATION_EXPIRY_DAYS = 7;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Constructor - retrieves the singleton database
     * connection when ReservationDAO is instantiated.
     */
    public ReservationDAO() {
        try {
            // get the single shared connection
            this.connection =
                DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.out.println(
                "ReservationDAO connection error: "
                    + e.getMessage());
        }
        this.memberDAO = new MemberDAO();
        this.bookDAO   = new BookDAO();
    }

    // --------------------------------------------------------
    // CREATE - Place a new reservation
    // --------------------------------------------------------

    /**
     * Inserts a new Reservation record into the reservations
     * table. Sets expiry date to 7 days from today.
     * Sends a confirmation notification to the member.
     *
     * @param reservation - Reservation object to insert
     * @throws DatabaseException if the insert fails
     */
    public void createReservation(Reservation reservation)
            throws DatabaseException {

        String sql = "INSERT INTO reservations "          +
                     "(member_id, book_isbn, "            +
                     "reservation_date, expiry_date, "    +
                     "status) "                           +
                     "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setInt(1,
                reservation.getMember().getUserId());
            stmt.setString(2,
                reservation.getBook().getIsbn());
            stmt.setDate(3,
                reservation.getReservationDate() != null
                    ? new Date(reservation
                               .getReservationDate()
                               .getTime())
                    : null);
            stmt.setDate(4,
                reservation.getExpiryDate() != null
                    ? new Date(reservation
                               .getExpiryDate()
                               .getTime())
                    : null);
            stmt.setString(5,
                ReservationStatus.ACTIVE.name());

            stmt.executeUpdate();

            System.out.println(
                "Reservation created for: "            +
                reservation.getMember().getName()      +
                " - Book: "                            +
                reservation.getBook().getTitle());

            // notify the member of their reservation
            Notification note = new Notification(
                0,
                "Your reservation for \""             +
                reservation.getBook().getTitle()      +
                "\" is confirmed. It expires on "     +
                reservation.getExpiryDate() + ".",
                "RESERVATION",
                new java.util.Date()
            );
            note.sendEmail(reservation.getMember());

        } catch (SQLException e) {
            throw new DatabaseException(
                "Error creating reservation: "
                    + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------
    // READ - Get a single reservation by ID
    // --------------------------------------------------------

    /**
     * Retrieves a single Reservation from the database by ID.
     *
     * @param reservationId - ID of the reservation to retrieve
     * @return              - Reservation object, or null if
     *                        not found
     */
    public Reservation getReservationById(int reservationId) {
        String sql = "SELECT * FROM reservations "  +
                     "WHERE reservation_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, reservationId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReservation(rs);
            } else {
                System.out.println(
                    "No reservation found with ID: "
                        + reservationId);
                return null;
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving reservation: "
                    + e.getMessage());
            return null;
        }
    }

    // --------------------------------------------------------
    // READ - Get all reservations
    // --------------------------------------------------------

    /**
     * Retrieves all reservation records from the database.
     *
     * @return - List of all Reservation objects
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();

        String sql = "SELECT * FROM reservations";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reservations.add(
                    mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving reservations: "
                    + e.getMessage());
        }
        return reservations;
    }

    // --------------------------------------------------------
    // READ - Get all reservations for a specific member
    // --------------------------------------------------------

    /**
     * Retrieves all reservations belonging to a member.
     * Used by ReservationMenu to show a member their holds.
     *
     * @param memberId - ID of the member
     * @return         - List of Reservation objects
     */
    public List<Reservation> getReservationsByMember(
            int memberId) {
        List<Reservation> reservations = new ArrayList<>();

        String sql = "SELECT * FROM reservations "  +
                     "WHERE member_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reservations.add(
                    mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                "Error retrieving member reservations: "
                    + e.getMessage());
        }
        return reservations;
    }

    // --------------------------------------------------------
    // UPDATE - Cancel a reservation
    // --------------------------------------------------------

    /**
     * Sets the status of a reservation to CANCELLED.
     * Called when a member no longer wants the hold.
     *
     * @param reservationId - ID of the reservation to cancel
     */
    public void cancelReservation(int reservationId) {
        updateStatus(reservationId,
                     ReservationStatus.CANCELLED);
        System.out.println(
            "Reservation " + reservationId
                + " has been cancelled.");
    }

    // --------------------------------------------------------
    // UPDATE - Fulfil a reservation
    // --------------------------------------------------------

    /**
     * Sets the status of a reservation to FULFILLED.
     * Called when the reserved book copy has been issued
     * to the member as a loan.
     * Sends a notification to the member.
     *
     * @param reservationId - ID of the reservation fulfilled
     */
    public void fulfillReservation(int reservationId) {
        updateStatus(reservationId,
                     ReservationStatus.FULFILLED);

        // retrieve reservation to build the notification
        Reservation res =
            getReservationById(reservationId);

        if (res != null && res.getMember() != null
                       && res.getBook()   != null) {
            Notification note = new Notification(
                0,
                "Your reservation for \""           +
                res.getBook().getTitle()             +
                "\" is ready for pickup!",
                "RESERVATION",
                new java.util.Date()
            );
            note.sendEmail(res.getMember());
        }

        System.out.println(
            "Reservation " + reservationId
                + " marked as FULFILLED.");
    }

    // --------------------------------------------------------
    // UPDATE - Expire overdue reservations
    // --------------------------------------------------------

    /**
     * Sets all ACTIVE reservations past their expiry date
     * to EXPIRED. Should be called periodically to keep
     * reservation statuses current.
     */
    public void markExpiredReservations() {
        String sql = "UPDATE reservations "          +
                     "SET status = 'EXPIRED' "       +
                     "WHERE status = 'ACTIVE' "      +
                     "AND expiry_date < CURDATE()";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            int rowsAffected = stmt.executeUpdate();
            System.out.println(rowsAffected
                + " reservation(s) marked as EXPIRED.");

        } catch (SQLException e) {
            System.out.println(
                "Error marking expired reservations: "
                    + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // HELPER - shared status updater
    // --------------------------------------------------------

    /**
     * Updates the status column of a single reservation row.
     * Keeps status update logic in one place - DRY principle.
     *
     * @param reservationId - ID of the reservation to update
     * @param status        - new status to apply
     */
    private void updateStatus(int reservationId,
                               ReservationStatus status) {
        String sql = "UPDATE reservations "          +
                     "SET status = ? "               +
                     "WHERE reservation_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setString(1, status.name());
            stmt.setInt(2, reservationId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println(
                    "No reservation found with ID: "
                        + reservationId);
            }

        } catch (SQLException e) {
            System.out.println(
                "Error updating reservation status: "
                    + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // HELPER - map database rows to Java objects
    // --------------------------------------------------------

    /**
     * Maps a ResultSet row to a Reservation object.
     * Called internally after every SELECT query.
     * Keeps mapping logic in one place - DRY principle.
     *
     * @param rs - ResultSet row from database query
     * @return   - populated Reservation object
     * @throws SQLException if column name is wrong
     */
    private Reservation mapResultSetToReservation(ResultSet rs)
            throws SQLException {

        int    memberId = rs.getInt("member_id");
        String bookIsbn = rs.getString("book_isbn");

        // retrieve full Member and Book objects
        Member member = memberDAO.getMemberById(memberId);
        Book   book   = null;

        try {
            book = bookDAO.getBookByIsbn(bookIsbn);
        } catch (exceptions.BookNotFoundException e) {
            // book may have been deleted - leave as null
            System.out.println(
                "Warning: book not found for ISBN: "
                    + bookIsbn);
        }

        return new Reservation(
            rs.getInt("reservation_id"),
            member,
            book,
            rs.getDate("reservation_date"),
            rs.getDate("expiry_date"),
            // convert String from DB back to enum
            ReservationStatus.valueOf(
                rs.getString("status"))
        );
    }
}
