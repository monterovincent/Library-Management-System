package cli;

import database.LoanDAO;
import database.MemberDAO;
import database.BookDAO;
import domain.BookCopy;
import domain.Loan;
import domain.Member;
import enums.CopyStatus;
import enums.LoanStatus;
import exceptions.AccountSuspendedException;
import exceptions.MaxBorrowLimitException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * LoanMenu handles all CLI interactions for loan operations.
 *
 * Menu options:
 *   1. Issue a loan (borrow a book)
 *   2. Return a book
 *   3. View all loans
 *   4. View active loans for a member
 *   5. Mark overdue loans
 *   0. Back to main menu
 */
public class LoanMenu {

    
    private Scanner scanner;
    private LoanDAO loanDAO;
    private MemberDAO memberDAO;
    private BookDAO bookDAO;
    private static final int LOAN_PERIOD_DAYS = 14;

    
    public LoanMenu(Scanner scanner) {
        this.scanner   = scanner;
        this.loanDAO   = new LoanDAO();
        this.memberDAO = new MemberDAO();
        this.bookDAO   = new BookDAO();
    }

    
    /**
     * Displays the loan menu and handles user choices.
     */
    public void display() {
        int choice = -1;

        while (choice != 0) {
            printMenu();
            choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    issueLoan();
                    break;
                case 2:
                    returnBook();
                    break;
                case 3:
                    viewAllLoans();
                    break;
                case 4:
                    viewMemberLoans();
                    break;
                case 5:
                    markOverdueLoans();
                    break;
                case 0:
                    System.out.println(
                        "Returning to main menu...");
                    break;
                default:
                    System.out.println(
                        "Invalid option. Please try again.");
            }
        }
    }

    
    /**
     * Prints the loan menu options to the terminal.
     */
    private void printMenu() {
        System.out.println("\n=============================");
        System.out.println("      LOAN MANAGEMENT        ");
        System.out.println("=============================");
        System.out.println(" 1. Issue a loan");
        System.out.println(" 2. Return a book");
        System.out.println(" 3. View all loans");
        System.out.println(" 4. View loans for a member");
        System.out.println(" 5. Mark overdue loans");
        System.out.println(" 0. Back to main menu");
        System.out.println("=============================");
    }

    
    /**
     * Prompts for member ID and copy barcode then creates
     * a new loan.
     */
    private void issueLoan() {
        System.out.println("\n--- Issue Loan ---");

        int memberId = getIntInput("Enter Member ID: ");

        // retrieve and validate member exists
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            System.out.println(
                "Error: No member found with ID: "
                + memberId);
            return;
        }

        String barcode = getStringInput(
            "Enter book copy barcode: ");
        if (barcode.isEmpty()) {
            System.out.println(
                "Error: Barcode cannot be empty.");
            return;
        }

        // retrieve the BookCopy by barcode
        BookCopy copy = bookDAO.getBookCopyByBarcode(barcode);
        if (copy == null) {
            System.out.println(
                "Error: No book copy found with barcode: "
                + barcode);
            return;
        }

        // check copy is available to borrow
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            System.out.println(
                "Error: This copy is not available. " +
                "Current status: " + copy.getStatus());
            return;
        }

        // set issue date to today, due date to +14 days
        Date issueDate = new Date();
        Date dueDate   = addDays(issueDate, LOAN_PERIOD_DAYS);

        Loan loan = new Loan(
            0,
            member,
            copy,
            issueDate,
            dueDate,
            null,
            LoanStatus.ACTIVE
        );

        try {
            loanDAO.issueLoan(loan);
            System.out.println("Due date: " + dueDate);

        } catch (AccountSuspendedException e) {
            System.out.println(
                "\nLoan Error: " + e.getMessage());
        } catch (MaxBorrowLimitException e) {
            System.out.println(
                "\nLoan Error: " + e.getMessage());
        }
    }

    
    /**
     * Prompts for loan ID and records the book return.
     */
    private void returnBook() {
        System.out.println("\n--- Return Book ---");

        int loanId = getIntInput("Enter Loan ID to return: ");

        // verify loan exists before returning
        Loan existing = loanDAO.getLoanById(loanId);
        if (existing == null) {
            System.out.println(
                "Error: No loan found with ID: " + loanId);
            return;
        }

        if (existing.getStatus() == LoanStatus.RETURNED) {
            System.out.println(
                "Error: This loan has already been returned.");
            return;
        }

        // record return as today's date
        loanDAO.returnLoan(loanId, new Date());
    }

    
    /**
     * Retrieves and displays all loan records.
     */
    private void viewAllLoans() {
        System.out.println("\n--- All Loans ---");

        List<Loan> loans = loanDAO.getAllLoans();

        if (loans.isEmpty()) {
            System.out.println("No loans found.");
            return;
        }

        for (Loan loan : loans) {
            System.out.println(loan.toString());
        }
        System.out.println("Total loans: " + loans.size());
    }

    
    /**
     * Prompts for member ID and displays their active loans.
     */
    private void viewMemberLoans() {
        System.out.println("\n--- Member Active Loans ---");

        int memberId = getIntInput("Enter Member ID: ");

        List<Loan> loans =
            loanDAO.getActiveLoansByMember(memberId);

        if (loans.isEmpty()) {
            System.out.println(
                "No active loans for member ID: " + memberId);
        } else {
            for (Loan loan : loans) {
                System.out.println(loan.toString());
            }
            System.out.println(
                "Active loans: " + loans.size());
        }
    }

    /**
     * Triggers the overdue loan update in the database.
     * Marks all ACTIVE loans past their due date as OVERDUE.
     * Librarian can run this to keep loan statuses current.
     */
    private void markOverdueLoans() {
        System.out.println("\n--- Mark Overdue Loans ---");
        loanDAO.markOverdueLoans();
    }

    
   

    // --------------------------------------------------------
    // HELPER - add days to a date
    // --------------------------------------------------------

    /**
     * Adds a number of days to a given date.
     * Used to calculate the due date from the issue date.
     *
     * @param date - starting date
     * @param days - number of days to add
     * @return     - new Date with days added
     */
    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    // --------------------------------------------------------
    // INPUT HELPER METHODS
    // Keeps input reading consistent across all menu methods
    // --------------------------------------------------------

    /**
     * Reads a String input from the user.
     * Trims whitespace from both ends of input.
     *
     * @param prompt - message displayed to user
     * @return       - trimmed String input
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Reads an integer input from the user.
     * Validates input is a valid integer.
     * Loops until valid integer is entered.
     *
     * @param prompt - message displayed to user
     * @return       - valid integer input
     */
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(
                        scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                // input validation - catches non-numeric input
                System.out.println(
                    "Invalid input. Please enter a number.");
            }
        }
    }
}
