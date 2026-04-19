
// Description: Represents the library's catalog of all books
//              in the Library Management System. This class
//              implements the Searchable interface providing
//              concrete search functionality for finding books
//              by title and author. Acts as the central hub
//              connecting Book objects to search operations
//              and database queries through BookDAO.
// Inputs:      Search queries (title or author as String)
//              DatabaseConnection for querying MariaDB
// Processing:  Implements Searchable interface contract by
//              delegating search queries to BookDAO which
//              executes SQL against the MariaDB database
// Outputs:     List of matching Book objects returned to
//              BookMenu for CLI display
// ============================================================

package domain;

import interfaces.Searchable;
import database.BookDAO;
import domain.BookCopy;
import exceptions.BookNotFoundException;
import java.util.List;

/**
 * LibraryCatalog is the central catalog of all books
 * in the library system.
 *
 * Key responsibilities:
 *   1. Implements Searchable interface - provides search
 *      functionality for books by title and author
 *   2. Delegates all database operations to BookDAO
 *   3. Acts as the bridge between CLI menus and database
 *
 * Implements:
 *   Searchable - contract requires searchByTitle()
 *                and searchByAuthor() to be implemented
 *
 * Used by:
 *   - BookMenu.java for all book operations
 *   - MainMenu.java as central catalog reference
 */
public class LibraryCatalog implements Searchable {

    // --------------------------------------------------------
    // Attributes
    // --------------------------------------------------------

    // Unique identifier for this catalog instance
    private int catalogId;

    // Data Access Object for all book related
    // database operations
    private BookDAO bookDAO;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Parameterized constructor - creates a LibraryCatalog
     * instance with a specific catalog ID.
     * Initializes BookDAO for database operations.
     *
     * @param catalogId - unique identifier for this catalog
     */
    public LibraryCatalog(int catalogId) {
        this.catalogId = catalogId;
        this.bookDAO   = new BookDAO();
    }

    // --------------------------------------------------------
    // Searchable Interface Implementation
    // --------------------------------------------------------

    /**
     * Searches for books by title in the database.
     * Implements searchByTitle from Searchable interface.
     *
     * @param title - full or partial title to search for
     * @return      - List of Book objects matching the title
     */
    @Override
    public List<Book> searchByTitle(String title) {
        return bookDAO.searchByTitle(title);
    }

    /**
     * Searches for books by author in the database.
     * Implements searchByAuthor from Searchable interface.
     *
     * @param author - full or partial author name
     * @return       - List of Book objects matching author
     */
    @Override
    public List<Book> searchByAuthor(String author) {
        return bookDAO.searchByAuthor(author);
    }

    // --------------------------------------------------------
    // Book Management Methods
    // --------------------------------------------------------

    /**
     * Adds a new book to the catalog and database.
     * @param book - Book object to add
     */
    public void addBook(Book book) {
        bookDAO.addBook(book);
    }

    /**
     * Removes a book from the catalog and database.
     * @param isbn - ISBN of the book to remove
     */
    public void removeBook(String isbn) {
        bookDAO.removeBook(isbn);
    }

    /**
     * Updates an existing book in the catalog and database.
     * @param book - Book object with updated information
     */
    public void updateBook(Book book) {
        bookDAO.updateBook(book);
    }

    /**
     * Retrieves all books from the catalog and database.
     * @return - List of all Book objects
     */
    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    /**
     * Retrieves a single book by ISBN from the database.
     * @param isbn - ISBN of the book to retrieve
     * @return     - Book object if found
     * @throws BookNotFoundException if no book matches ISBN
     */
    public Book getBookByIsbn(String isbn)
            throws BookNotFoundException {
        return bookDAO.getBookByIsbn(isbn);
    }

    /**
     * Retrieves all physical copies of a book by ISBN.
     * @param isbn - ISBN of the book
     * @return     - List of BookCopy objects
     */
    public List<BookCopy> getCopiesByIsbn(String isbn) {
        return bookDAO.getCopiesByIsbn(isbn);
    }

    /**
     * Adds a physical book copy to the database.
     * @param copy - BookCopy object to add
     */
    public void addBookCopy(BookCopy copy) {
        bookDAO.addBookCopy(copy);
    }

    // --------------------------------------------------------
    // Getter
    // --------------------------------------------------------

    /**
     * Returns the catalog ID.
     * @return catalogId as int
     */
    public int getCatalogId() {
        return catalogId;
    }

    // --------------------------------------------------------
    // toString
    // --------------------------------------------------------

    /**
     * Returns formatted catalog details.
     * @return formatted string
     */
    @Override
    public String toString() {
        return  "\n=============================" +
                "\nCatalog ID : " + catalogId    +
                "\n=============================";
    }
}