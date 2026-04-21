package cli;

import database.FineDAO;
import database.MemberDAO;
import database.BookDAO;
import domain.Book;
import domain.Fine;
import domain.Member;
import domain.Payment;
import domain.Reservation;
import enums.FineStatus;
import enums.PaymentStatus;
import enums.ReservationStatus;
import exceptions.DatabaseException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * ReservationMenu handles all CLI interactions for
 * reservation and fine payment operations.
 *
 * Menu options:
 *   1. Make a reservation
 *   2. Cancel a reservation
 *   3. Fulfill a reservation
 *   4. View all reservations
 *   5. View reservations for a member
 *   6. Mark expired reservations
 *   7. View unpaid fines for a member
 *   8. Pay a fine
 *   9. Waive a fine
 *   0. Back to main menu
 *
 * Used by:
 *   - MainMenu.java which calls reservationMenu.display()
 */
public class ReservationMenu {

    // --------------------------------------------------------
    // Attributes
    // --------------------------------------------------------

    // Scanner reads user input from the terminal
    private Scanner scanner;

    // DAOs for database operations
    private database.ReservationDAO reservationDAO;
    private FineDAO                 fineDAO;
    private MemberDAO               memberDAO;
    private BookDAO                 bookDAO;

    // Number of days before a reservation expires
    private static final int EXPIRY_DAYS = 7;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Creates a ReservationMenu with a shared Scanner.
     * Instantiates all required DAOs.
     *
     * @param scanner - shared Scanner for user input
     */
    public ReservationMenu(Scanner scanner) {
        this.scanner         = scanner;
        this.reservationDAO  = new database.ReservationDAO();
        this.fineDAO         = new FineDAO();
        this.memberDAO       = new MemberDAO();
        this.bookDAO         = new BookDAO();
    }

    // --------------------------------------------------------
    // display - main reservation menu loop
    // --------------------------------------------------------

    /**
     * Displays the reservation menu and handles user choices.
     * Loops until user selects 0 to go back to main menu.
     * Called by MainMenu when user selects Reservations.
     */
    public void display() {
        int choice = -1;

        while (choice != 0) {
            printMenu();
            choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    makeReservation();
                    break;
                case 2:
                    cancelReservation();
                    break;
                case 3:
                    fulfillReservation();
                    break;
                case 4:
                    viewAllReservations();
                    break;
                case 5:
                    viewMemberReservations();
                    break;
                case 6:
                    markExpiredReservations();
                    break;
                case 7:
                    viewUnpaidFines();
                    break;
                case 8:
                    payFine();
                    break;
                case 9:
                    waiveFine();
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

    // --------------------------------------------------------
    // private menu helper
    // --------------------------------------------------------

    /**
     * Prints the reservation menu options to the terminal.
     */
    private void printMenu() {
        System.out.println("\n=============================");
        System.out.println("   RESERVATION MANAGEMENT   ");
        System.out.println("=============================");
        System.out.println(" 1. Make a reservation");
        System.out.println(" 2. Cancel a reservation");
        System.out.println(" 3. Fulfill a reservation");
        System.out.println(" 4. View all reservations");
        System.out.println(" 5. View reservations for member");
        System.out.println(" 6. Mark expired reservations");
        System.out.println(" 7. View unpaid fines for member");
        System.out.println(" 8. Pay a fine");
        System.out.println(" 9. Waive a fine");
        System.out.println(" 0. Back to main menu");
        System.out.println("=============================");
    }

    // --------------------------------------------------------
    // MAKE RESERVATION
    // --------------------------------------------------------

    /**
     * Prompts for member ID and book ISBN then creates a
     * new ACTIVE reservation expiring in 7 days.
     */
    private void makeReservation() {
        System.out.println("\n--- Make a Reservation ---");

        int memberId = getIntInput("Enter Member ID: ");

        // validate member exists
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            System.out.println(
                "Error: No member found with ID: "
                    + memberId);
            return;
        }

        String isbn = getStringInput(
            "Enter Book ISBN to reserve: ");
        if (isbn.isEmpty()) {
            System.out.println(
                "Error: ISBN cannot be empty.");
            return;
        }

        // validate book exists
        Book book = null;
        try {
            book = bookDAO.getBookByIsbn(isbn);
        } catch (exceptions.BookNotFoundException e) {
            System.out.println(
                "Error: " + e.getMessage());
            return;
        }

        // set reservation date to today, expiry in 7 days
        Date today    = new Date();
        Date expiry   = addDays(today, EXPIRY_DAYS);

        Reservation reservation = new Reservation(
            0,
            member,
            book,
            today,
            expiry,
            ReservationStatus.ACTIVE
        );

        try {
            reservationDAO.createReservation(reservation);
            System.out.println(
                "Reservation expires on: " + expiry);
        } catch (DatabaseException e) {
            System.out.println(
                "Reservation Error: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // CANCEL RESERVATION
    // --------------------------------------------------------

    /**
     * Prompts for reservation ID and cancels the hold.
     */
    private void cancelReservation() {
        System.out.println("\n--- Cancel Reservation ---");

        int reservationId = getIntInput(
            "Enter Reservation ID to cancel: ");

        // verify reservation exists before cancelling
        Reservation existing =
            reservationDAO.getReservationById(
                reservationId);

        if (existing == null) {
            System.out.println(
                "Error: No reservation found with ID: "
                    + reservationId);
            return;
        }

        if (existing.getStatus()
                != ReservationStatus.ACTIVE) {
            System.out.println(
                "Error: Reservation is already "
                    + existing.getStatus()
                    + ". Cannot cancel.");
            return;
        }

        reservationDAO.cancelReservation(reservationId);
    }

    // --------------------------------------------------------
    // FULFILL RESERVATION
    // --------------------------------------------------------

    /**
     * Prompts for reservation ID and marks it as FULFILLED.
     * Called when the member collects their reserved book.
     */
    private void fulfillReservation() {
        System.out.println("\n--- Fulfill Reservation ---");

        int reservationId = getIntInput(
            "Enter Reservation ID to fulfill: ");

        // verify reservation exists and is ACTIVE
        Reservation existing =
            reservationDAO.getReservationById(
                reservationId);

        if (existing == null) {
            System.out.println(
                "Error: No reservation found with ID: "
                    + reservationId);
            return;
        }

        if (existing.getStatus()
                != ReservationStatus.ACTIVE) {
            System.out.println(
                "Error: Reservation is not ACTIVE. "
                    + "Current status: "
                    + existing.getStatus());
            return;
        }

        reservationDAO.fulfillReservation(reservationId);
    }

    // --------------------------------------------------------
    // VIEW ALL RESERVATIONS
    // --------------------------------------------------------

    /**
     * Retrieves and displays all reservations.
     */
    private void viewAllReservations() {
        System.out.println("\n--- All Reservations ---");

        List<Reservation> reservations =
            reservationDAO.getAllReservations();

        if (reservations.isEmpty()) {
            System.out.println(
                "No reservations found.");
            return;
        }

        for (Reservation r : reservations) {
            System.out.println(r.toString());
        }
        System.out.println(
            "Total reservations: "
                + reservations.size());
    }

    // --------------------------------------------------------
    // VIEW MEMBER RESERVATIONS
    // --------------------------------------------------------

    /**
     * Prompts for member ID and displays all their
     * reservations.
     */
    private void viewMemberReservations() {
        System.out.println(
            "\n--- Reservations for Member ---");

        int memberId = getIntInput("Enter Member ID: ");

        List<Reservation> reservations =
            reservationDAO.getReservationsByMember(
                memberId);

        if (reservations.isEmpty()) {
            System.out.println(
                "No reservations found for member ID: "
                    + memberId);
        } else {
            for (Reservation r : reservations) {
                System.out.println(r.toString());
            }
            System.out.println(
                "Total: " + reservations.size());
        }
    }

    // --------------------------------------------------------
    // MARK EXPIRED RESERVATIONS
    // --------------------------------------------------------

    /**
     * Triggers the expiry update in the database.
     * Marks all ACTIVE reservations past their expiry date.
     */
    private void markExpiredReservations() {
        System.out.println(
            "\n--- Mark Expired Reservations ---");
        reservationDAO.markExpiredReservations();
    }

    // --------------------------------------------------------
    // VIEW UNPAID FINES
    // --------------------------------------------------------

    /**
     * Prompts for member ID and displays all their
     * outstanding (UNPAID) fines.
     */
    private void viewUnpaidFines() {
        System.out.println(
            "\n--- Unpaid Fines for Member ---");

        int memberId = getIntInput("Enter Member ID: ");

        List<Fine> fines =
            fineDAO.getUnpaidFinesByMember(memberId);

        if (fines.isEmpty()) {
            System.out.println(
                "No unpaid fines for member ID: "
                    + memberId);
        } else {
            for (Fine fine : fines) {
                System.out.println(fine.toString());
            }
            System.out.println(
                "Total unpaid fines: " + fines.size());
        }
    }

    // --------------------------------------------------------
    // PAY FINE
    // --------------------------------------------------------

    /**
     * Prompts for fine ID and payment details then records
     * a payment and marks the fine as PAID.
     */
    private void payFine() {
        System.out.println("\n--- Pay a Fine ---");

        int fineId = getIntInput("Enter Fine ID to pay: ");

        // verify fine exists
        Fine fine = fineDAO.getFineById(fineId);
        if (fine == null) {
            System.out.println(
                "Error: No fine found with ID: "
                    + fineId);
            return;
        }

        // check fine is not already paid
        if (fine.getStatus() == FineStatus.PAID
                || fine.getStatus()
                   == FineStatus.WAIVED) {
            System.out.println(
                "Error: This fine is already "
                    + fine.getStatus() + ".");
            return;
        }

        System.out.println(
            "Fine amount: $" + fine.getAmount());

        String method = getStringInput(
            "Enter payment method "
                + "(Cash / Card / Online): ");
        if (method.isEmpty()) {
            System.out.println(
                "Error: Payment method cannot be empty.");
            return;
        }

        Payment payment = new Payment(
            0,
            fine.getAmount(),
            new Date(),
            method,
            PaymentStatus.COMPLETED,
            fine
        );

        fineDAO.recordPayment(payment);
    }

    // --------------------------------------------------------
    // WAIVE FINE
    // --------------------------------------------------------

    /**
     * Prompts for fine ID and waives the fine without
     * requiring payment. Librarian-only operation.
     */
    private void waiveFine() {
        System.out.println("\n--- Waive a Fine ---");

        int fineId = getIntInput(
            "Enter Fine ID to waive: ");

        // verify fine exists
        Fine fine = fineDAO.getFineById(fineId);
        if (fine == null) {
            System.out.println(
                "Error: No fine found with ID: "
                    + fineId);
            return;
        }

        if (fine.getStatus() != FineStatus.UNPAID) {
            System.out.println(
                "Error: Fine is already "
                    + fine.getStatus()
                    + ". Cannot waive.");
            return;
        }

        // confirm before waiving
        String confirm = getStringInput(
            "Are you sure you want to waive fine "
                + fineId + " ($"
                + fine.getAmount()
                + ")? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            fineDAO.updateFineStatus(
                fineId, FineStatus.WAIVED);
        } else {
            System.out.println("Waive cancelled.");
        }
    }

    // --------------------------------------------------------
    // HELPER - add days to a date
    // --------------------------------------------------------

    /**
     * Adds a number of days to a given date.
     * Used to calculate the expiry date from the
     * reservation date.
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

