package domain;

import enums.ReservationStatus;
import java.util.Date;

/**
 * Reservation represents a hold placed by a member on a book
 * title when no copies are currently available.
 *
 * When a copy becomes available the reservation status moves
 * from ACTIVE to FULFILLED and the member is notified.
 * Reservations that are not collected expire automatically.
 *
 * Used by:
 *   - ReservationDAO.java   for database CRUD operations
 *   - ReservationMenu.java  for CLI display and management
 */
public class Reservation {

    // --------------------------------------------------------
    // Attributes - mirrors the reservations table columns
    // --------------------------------------------------------

    // Auto-generated primary key from the database
    private Integer reservationId;

    // Member who placed the hold
    private Member member;

    // Book title being reserved (not a specific physical copy)
    private Book book;

    // Date the reservation was created
    private Date reservationDate;

    // Date the reservation expires if not collected
    private Date expiryDate;

    // Current lifecycle state of the reservation
    private ReservationStatus status;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Creates a fully populated Reservation object.
     * reservationId of 0 signals the DB will assign the ID.
     *
     * @param reservationId   - database primary key (0 for new)
     * @param member          - member who placed the hold
     * @param book            - book title being reserved
     * @param reservationDate - date the reservation was made
     * @param expiryDate      - date the reservation expires
     * @param status          - current reservation status
     */
    public Reservation(Integer reservationId, Member member,
                       Book book, Date reservationDate,
                       Date expiryDate,
                       ReservationStatus status) {
        this.reservationId   = reservationId;
        this.member          = member;
        this.book            = book;
        this.reservationDate = reservationDate;
        this.expiryDate      = expiryDate;
        this.status          = status;
    }

    // --------------------------------------------------------
    // Getters - retrieve private attribute values
    // --------------------------------------------------------

    public Integer getReservationId() {
        return reservationId;
    }

    public Member getMember() {
        return member;
    }

    public Book getBook() {
        return book;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    // --------------------------------------------------------
    // Setters - update private attribute values
    // --------------------------------------------------------

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    // --------------------------------------------------------
    // toString - display reservation details in readable format
    // --------------------------------------------------------

    /**
     * Returns formatted reservation details.
     *
     * @return formatted string of all reservation fields
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                "\nReservation ID : " + reservationId  +
                "\nMember         : " + (member != null
                                        ? member.getName()
                                        : "N/A")       +
                "\nBook           : " + (book != null
                                        ? book.getTitle()
                                        : "N/A")       +
                "\nBook ISBN      : " + (book != null
                                        ? book.getIsbn()
                                        : "N/A")       +
                "\nReserved On    : " + reservationDate +
                "\nExpiry Date    : " + expiryDate      +
                "\nStatus         : " + status          +
                "\n=============================";
    }
}
