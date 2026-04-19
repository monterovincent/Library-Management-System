


// Description: Command Line Interface menu for all book
//              related operations in the Library Management
//              System. Handles user input and output for
//              adding, removing, updating, searching and
//              displaying books and book copies. Delegates
//              all business logic to LibraryCatalog which
//              in turn delegates to BookDAO for database
//              operations.


package cli;

import domain.Book;
import domain.BookCopy;
import domain.LibraryCatalog;
import enums.CopyStatus;
import exceptions.BookNotFoundException;

import java.util.List;
import java.util.Scanner;

/**
 * BookMenu handles all CLI interactions for book operations.
 *
 * Menu options:
 *   1. Add a new book
 *   2. Remove a book
 *   3. Update a book
 *   4. Display all books
 *   5. Search by title
 *   6. Search by author
 *   7. Add a book copy
 *   8. Display copies of a book
 *   0. Back to main menu
 *
 * Used by:
 *   - MainMenu.java which calls bookMenu.display()
 */
public class BookMenu {

    // --------------------------------------------------------
    // Attributes
    // --------------------------------------------------------

    // Scanner reads user input from the terminal
    private Scanner scanner;

    // LibraryCatalog is the bridge to all book operations
    // BookMenu never talks to BookDAO directly
    private LibraryCatalog catalog;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Creates a BookMenu with a shared Scanner and catalog.
     * Scanner and LibraryCatalog are passed in from
     * MainMenu to avoid creating multiple instances.
     *
     * @param scanner - shared Scanner for user input
     * @param catalog - shared LibraryCatalog instance
     */
    public BookMenu(Scanner scanner, LibraryCatalog catalog) {
        this.scanner = scanner;
        this.catalog = catalog;
    }

    // --------------------------------------------------------
    // display - main book menu loop
    // --------------------------------------------------------

    /**
     * Displays the book menu and handles user choices.
     * Loops until user selects 0 to go back to main menu.
     * Called by MainMenu when user selects Books option.
     */
    public void display() {
        int choice = -1;

        while (choice != 0) {
            printMenu();
            choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    removeBook();
                    break;
                case 3:
                    updateBook();
                    break;
                case 4:
                    displayAllBooks();
                    break;
                case 5:
                    searchByTitle();
                    break;
                case 6:
                    searchByAuthor();
                    break;
                case 7:
                    addBookCopy();
                    break;
                case 8:
                    displayCopies();
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
     * Prints the book menu options to the terminal.
     */
    private void printMenu() {
        System.out.println("\n=============================");
        System.out.println("       BOOK MANAGEMENT       ");
        System.out.println("=============================");
        System.out.println(" 1. Add a new book");
        System.out.println(" 2. Remove a book");
        System.out.println(" 3. Update a book");
        System.out.println(" 4. Display all books");
        System.out.println(" 5. Search by title");
        System.out.println(" 6. Search by author");
        System.out.println(" 7. Add a book copy");
        System.out.println(" 8. Display copies of a book");
        System.out.println(" 0. Back to main menu");
        System.out.println("=============================");
    }

    // --------------------------------------------------------
    // ADD BOOK
    // --------------------------------------------------------

    /**
     * Prompts user for book details and adds to database.
     * Validates that ISBN and title are not empty before
     * calling catalog.addBook().
     */
    private void addBook() {
        System.out.println("\n--- Add New Book ---");

        // collect book details from user
        String isbn = getStringInput("Enter ISBN: ");
        String title = getStringInput("Enter Title: ");
        String author = getStringInput("Enter Author: ");
        String subject = getStringInput("Enter Subject: ");
        String publisher = getStringInput("Enter Publisher: ");

        // validate ISBN and title are not empty
        if (isbn.isEmpty() || title.isEmpty()) {
            System.out.println(
                "Error: ISBN and Title cannot be empty.");
            return;
        }

        // create Book object and add to database
        Book book = new Book(
                isbn, title, author, subject, publisher);
        catalog.addBook(book);
    }

    // --------------------------------------------------------
    // REMOVE BOOK
    // --------------------------------------------------------

    /**
     * Prompts user for ISBN and removes book from database.
     * Validates ISBN is not empty before removing.
     */
    private void removeBook() {
        System.out.println("\n--- Remove Book ---");

        String isbn = getStringInput("Enter ISBN to remove: ");

        // validate ISBN not empty
        if (isbn.isEmpty()) {
            System.out.println("Error: ISBN cannot be empty.");
            return;
        }

        // confirm before deleting
        String confirm = getStringInput(
            "Are you sure you want to remove this book?" +
            " (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            catalog.removeBook(isbn);
        } else {
            System.out.println("Remove cancelled.");
        }
    }

    // --------------------------------------------------------
    // UPDATE BOOK
    // --------------------------------------------------------

    /**
     * Prompts user for updated book details and updates
     * the database record. First retrieves existing book
     * to confirm it exists before updating.
     */
    private void updateBook() {
        System.out.println("\n--- Update Book ---");

        String isbn = getStringInput("Enter ISBN to update: ");

        try {
            // check book exists first
            Book existing = catalog.getBookByIsbn(isbn);
            System.out.println("Current details: "
                    + existing.toString());

            // collect updated details
            String title = getStringInput(
                "Enter new Title (" +
                existing.getTitle() + "): ");
            String author = getStringInput(
                "Enter new Author (" +
                existing.getAuthor() + "): ");
            String subject = getStringInput(
                "Enter new Subject (" +
                existing.getSubject() + "): ");
            String publisher = getStringInput(
                "Enter new Publisher (" +
                existing.getPublisher() + "): ");

            // use existing value if user enters nothing
            Book updated = new Book(
                isbn,
                title.isEmpty()     ? existing.getTitle()
                                    : title,
                author.isEmpty()    ? existing.getAuthor()
                                    : author,
                subject.isEmpty()   ? existing.getSubject()
                                    : subject,
                publisher.isEmpty() ? existing.getPublisher()
                                    : publisher
            );

            catalog.updateBook(updated);

        } catch (BookNotFoundException e) {
            // custom exception caught and displayed to user
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // DISPLAY ALL BOOKS
    // --------------------------------------------------------

    /**
     * Retrieves and displays all books from the database.
     * Shows message if no books exist yet.
     */
    private void displayAllBooks() {
        System.out.println("\n--- All Books ---");

        List<Book> books = catalog.getAllBooks();

        // check if catalog is empty
        if (books.isEmpty()) {
            System.out.println("No books found in catalog.");
            return;
        }

        // display each book using Book toString()
        for (Book book : books) {
            System.out.println(book.toString());
        }
        System.out.println(
            "Total books: " + books.size());
    }

    // --------------------------------------------------------
    // SEARCH BY TITLE
    // --------------------------------------------------------

    /**
     * Prompts user for title and displays matching books.
     * Uses Searchable interface via LibraryCatalog.
     */
    private void searchByTitle() {
        System.out.println("\n--- Search by Title ---");

        String title = getStringInput("Enter title to search: ");

        if (title.isEmpty()) {
            System.out.println("Error: Title cannot be empty.");
            return;
        }

        List<Book> results = catalog.searchByTitle(title);

        if (results.isEmpty()) {
            System.out.println(
                "No books found matching: " + title);
        } else {
            for (Book book : results) {
                System.out.println(book.toString());
            }
            System.out.println(
                "Results found: " + results.size());
        }
    }

    // --------------------------------------------------------
    // SEARCH BY AUTHOR
    // --------------------------------------------------------

    /**
     * Prompts user for author and displays matching books.
     * Uses Searchable interface via LibraryCatalog.
     */
    private void searchByAuthor() {
        System.out.println("\n--- Search by Author ---");

        String author = getStringInput(
            "Enter author to search: ");

        if (author.isEmpty()) {
            System.out.println("Error: Author cannot be empty.");
            return;
        }

        List<Book> results = catalog.searchByAuthor(author);

        if (results.isEmpty()) {
            System.out.println(
                "No books found matching: " + author);
        } else {
            for (Book book : results) {
                System.out.println(book.toString());
            }
            System.out.println(
                "Results found: " + results.size());
        }
    }

    // --------------------------------------------------------
    // ADD BOOK COPY
    // --------------------------------------------------------

    /**
     * Prompts user for copy details and adds physical
     * copy to the database linked to an existing book.
     */
    private void addBookCopy() {
        System.out.println("\n--- Add Book Copy ---");

        String isbn = getStringInput(
            "Enter ISBN of book to add copy for: ");
        String barcode = getStringInput(
            "Enter barcode for this copy: ");

        if (isbn.isEmpty() || barcode.isEmpty()) {
            System.out.println(
                "Error: ISBN and barcode cannot be empty.");
            return;
        }

        // new copies always start as AVAILABLE
        BookCopy copy = new BookCopy(
                0, barcode, CopyStatus.AVAILABLE, isbn);

        catalog.addBookCopy(copy);
    }

    // --------------------------------------------------------
    // DISPLAY COPIES
    // --------------------------------------------------------

    /**
     * Displays all physical copies of a specific book.
     * Shows each copy's barcode and current status.
     */
    private void displayCopies() {
        System.out.println("\n--- Display Book Copies ---");

        String isbn = getStringInput(
            "Enter ISBN to view copies: ");

        if (isbn.isEmpty()) {
            System.out.println("Error: ISBN cannot be empty.");
            return;
        }

        List<BookCopy> copies = catalog.getCopiesByIsbn(isbn);

        if (copies.isEmpty()) {
            System.out.println(
                "No copies found for ISBN: " + isbn);
        } else {
            for (BookCopy copy : copies) {
                System.out.println(copy.toString());
            }
            System.out.println(
                "Total copies: " + copies.size());
        }
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