package exceptions;

/**
 * DatabaseException is thrown when a critical database
 * error occurs that cannot be recovered from gracefully.
 *
 * Wraps SQLException to provide a cleaner API to callers
 * that do not need to handle SQL-specific exceptions.
 *
 * Used by:
 *   - ReservationDAO.java  for unrecoverable query failures
 *   - FineDAO.java         for unrecoverable query failures
 *   - MainMenu.java        to catch and display DB errors
 */
public class DatabaseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new DatabaseException with a message
     * explaining the database error that occurred.
     *
     * @param message - description of the database error
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * Creates a new DatabaseException wrapping the original
     * cause so the full stack trace is preserved for debugging.
     *
     * @param message - description of the database error
     * @param cause   - original exception that triggered this
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
