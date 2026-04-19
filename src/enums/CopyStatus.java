package enums;

/**
 * CopyStatus defines the lifecycle states of a physical book copy.
 * Used in BookCopy.java to track the current status of each copy.
 *
 * Inputs:   None - enum constants are predefined
 * Process:  Assigned to BookCopy objects and updated as copies
 *           change state (e.g. borrowed, returned, lost)
 * Outputs:  Status value used in conditional logic across
 *           BookDAO, LoanDAO, and BookMenu
 */
public enum CopyStatus {

    // Copy is on the shelf and available to borrow
    AVAILABLE,

    // Copy has been borrowed by a member and is not on the shelf
    BORROWED,

    // Copy has been reserved by a member and held for pickup
    RESERVED,

    // Copy has been reported lost and cannot be borrowed
    LOST,

    // Copy is damaged and taken out of circulation
    DAMAGED
}
