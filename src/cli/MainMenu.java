package cli;

import domain.LibraryCatalog;

import java.util.Scanner;

/**
 * MainMenu is the entry point for all CLI interactions in
 * the Library Management System.
 *
 * It creates and coordinates all sub-menus, passing the
 * shared Scanner and dependencies so each menu does not
 * create its own Scanner instance.
 *
 * Menu options:
 *   1. Book Management      -> BookMenu
 *   2. Member Management    -> MemberMenu
 *   3. Loan Management      -> LoanMenu
 *   4. Reservation & Fines  -> ReservationMenu
 *   0. Exit application
 *
 * Used by:
 *   - Main.java  which calls mainMenu.display()
 */
public class MainMenu {

    // --------------------------------------------------------
    // Attributes
    // --------------------------------------------------------

    // Single Scanner shared across all sub-menus
    // Only one Scanner should read System.in at a time
    private Scanner scanner;

    // Sub-menus - one per functional area
    private BookMenu        bookMenu;
    private MemberMenu      memberMenu;
    private LoanMenu        loanMenu;
    private ReservationMenu reservationMenu;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Creates a MainMenu and initialises all sub-menus.
     * LibraryCatalog is created here and passed to BookMenu
     * so there is only one instance of the catalog.
     */
    public MainMenu() {
        this.scanner = new Scanner(System.in);

        // LibraryCatalog is shared with BookMenu
        LibraryCatalog catalog = new LibraryCatalog();

        // create all sub-menus with the shared Scanner
        this.bookMenu        = new BookMenu(
                                   scanner, catalog);
        this.memberMenu      = new MemberMenu(scanner);
        this.loanMenu        = new LoanMenu(scanner);
        this.reservationMenu = new ReservationMenu(
                                   scanner);
    }

    // --------------------------------------------------------
    // display - main application loop
    // --------------------------------------------------------

    /**
     * Displays the main menu and handles user choices.
     * Loops until user selects 0 to exit the application.
     * Called once from Main.java to start the application.
     */
    public void display() {
        int choice = -1;

        System.out.println(
            "\nWelcome to the Library Management System");

        while (choice != 0) {
            printMenu();
            choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    bookMenu.display();
                    break;
                case 2:
                    memberMenu.display();
                    break;
                case 3:
                    loanMenu.display();
                    break;
                case 4:
                    reservationMenu.display();
                    break;
                case 0:
                    System.out.println(
                        "\nThank you for using the "
                            + "Library Management System. "
                            + "Goodbye!");
                    break;
                default:
                    System.out.println(
                        "Invalid option. Please try again.");
            }
        }

        // close scanner when application exits
        scanner.close();
    }

    // --------------------------------------------------------
    // private menu helper
    // --------------------------------------------------------

    /**
     * Prints the main menu options to the terminal.
     */
    private void printMenu() {
        System.out.println("\n=============================");
        System.out.println("  LIBRARY MANAGEMENT SYSTEM  ");
        System.out.println("=============================");
        System.out.println(" 1. Book Management");
        System.out.println(" 2. Member Management");
        System.out.println(" 3. Loan Management");
        System.out.println(" 4. Reservations & Fines");
        System.out.println(" 0. Exit");
        System.out.println("=============================");
    }

    // --------------------------------------------------------
    // INPUT HELPER METHOD
    // --------------------------------------------------------

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

