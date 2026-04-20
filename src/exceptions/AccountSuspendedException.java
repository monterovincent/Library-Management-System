package exceptions;

/**
 * AccountSuspendedException is thrown when a suspended
 * member tries to borrow or reserve a book.
 */
public class AccountSuspendedException extends Exception {

   
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new AccountSuspendedException with a message
     * explaining why the account is suspended.
     *
     * @param message - description of the suspension reason
     */
    public AccountSuspendedException(String message) {
        super(message);
    }
}
