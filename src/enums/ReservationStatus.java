package enums;

/**
 * ReservationStatus defines the lifecycle states
 * of a book reservation placed by a member.
 *
 * Used by:
 *   - Reservation.java      as the status field type
 *   - ReservationDAO.java   for filtering and updates
 *   - ReservationMenu.java  for CLI display
 */
public enum ReservationStatus {
    ACTIVE,
    CANCELLED,
    FULFILLED,
    EXPIRED
}
