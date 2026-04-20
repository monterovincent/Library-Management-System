package exceptions;

/**
 * MaxBorrowLimitException is thrown when a member tries
 * to borrow more books than their allowed limit.
 */
public class MaxBorrowLimitException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new MaxBorrowLimitException with a message
     * explaining that the borrow limit has been reached.
     *
     * @param message - description of the limit violation
     */
    public MaxBorrowLimitException(String message) {
        super(message);
    }
}