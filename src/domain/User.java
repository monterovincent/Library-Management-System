package domain;
import enums.AccountStatus;

/**
 * User is the abstract parent class for all library users.
 *
 * Inheritance hierarchy:
 *   User (abstract)
 *   ├── Member   - library members who borrow books
 *   └── Librarian - staff who manage the library
 *
 * Used by:
 *   - Member.java     extends User
 *   - Librarian.java  extends User
 *   - MemberDAO.java  for database mapping
 *   - MemberMenu.java for CLI display
 */
public abstract class User {
    private Integer userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private AccountStatus status;

    public User(Integer userId, String name, String email,
                String phone, String address,
                AccountStatus status) {
        this.userId  = userId;
        this.name    = name;
        this.email   = email;
        this.phone   = phone;
        this.address = address;
        this.status  = status;
    }

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public AccountStatus getStatus() {
        return status;
    }

    
    // Setters
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /**
     * Returns formatted user details.
     *
     * @return formatted string of common user fields
     */
    @Override
    public String toString() {
        return  "\nUser ID  : " + userId   +
                "\nName     : " + name     +
                "\nEmail    : " + email    +
                "\nPhone    : " + phone    +
                "\nAddress  : " + address  +
                "\nStatus   : " + status;
    }
}
