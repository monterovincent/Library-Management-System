package domain;

import enums.AccountStatus;

/**
 * Librarian represents a library staff member.
 *
 * Inheritance:
 *   Extends User
 *
 * Additional attributes:
 *   employeeId - unique staff identifier
 *   workShift  - shift the librarian works
 */
public class Librarian extends User {

    // Unique employee identifier for this librarian
    private String employeeId;

    // Work shift assigned to this librarian (e.g. MORNING)
    private String workShift;

    public Librarian(Integer userId, String name, String email,
                     String phone, String address,
                     AccountStatus status,
                     String employeeId, String workShift) {
        // call User constructor
        super(userId, name, email, phone, address, status);
        this.employeeId = employeeId;
        this.workShift  = workShift;
    }

    
    // Getters
    public String getEmployeeId() {
        return employeeId;
    }

    public String getWorkShift() {
        return workShift;
    }


    // Setters
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    /**
     * Returns formatted librarian details.
     * @return formatted string of all librarian fields
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                super.toString()                  +
                "\nEmployee ID : " + employeeId   +
                "\nWork Shift  : " + workShift    +
                "\n=============================";
    }
}
