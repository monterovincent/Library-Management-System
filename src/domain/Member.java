package domain;

import enums.AccountStatus;
import java.util.Date;

/**
 * Member represents a library member who can borrow books.
 *
 * Inheritance:
 *   Extends User
 *
 * Additional attributes:
 *   membershipDate - date member joined the library
 *   maxBorrowLimit - maximum number of books allowed at once
 */
public class Member extends User {
	
    // Date the member registered with the library
    private Date membershipDate;

    // Maximum number of books this member can borrow at once
    private int maxBorrowLimit;


    public Member(Integer userId, String name, String email,
                  String phone, String address,
                  AccountStatus status,
                  Date membershipDate, int maxBorrowLimit) {
        // call User constructor
        super(userId, name, email, phone, address, status);
        this.membershipDate = membershipDate;
        this.maxBorrowLimit = maxBorrowLimit;
    }

    // Getters
    public Date getMembershipDate() {
        return membershipDate;
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    // Setters
    public void setMembershipDate(Date membershipDate) {
        this.membershipDate = membershipDate;
    }

    public void setMaxBorrowLimit(int maxBorrowLimit) {
        this.maxBorrowLimit = maxBorrowLimit;
    }

    /**
     * Returns formatted member details.
     *
     * @return formatted string of all member fields
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                super.toString()                  +
                "\nMembership : " + membershipDate +
                "\nBorrow Limit: " + maxBorrowLimit +
                "\n=============================";
    }
}
