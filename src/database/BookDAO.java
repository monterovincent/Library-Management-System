// ============================================================
// Description: Data Access Object for all book and book copy
//              related database operations in the Library
//              Management System. Handles all SQL queries
//              for CREATE, READ, UPDATE, DELETE operations
//              against the books and book_copies tables in
//              MariaDB. All methods use PreparedStatements
//              to prevent SQL injection attacks.
// Inputs:      Book objects, BookCopy objects, isbn strings,
//              search query strings
// Processing:  Executes SQL queries through the singleton
//              DatabaseConnection. Maps ResultSet rows back
//              to Book and BookCopy Java objects.
// Outputs:     Book objects, BookCopy objects, Lists of Books
//              and BookCopies returned to LibraryCatalog
//              and BookMenu
// ============================================================

package database;

import domain.Book;
import domain.BookCopy;
import enums.CopyStatus;
import exceptions.BookNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDAO handles all database operations for books
 * and book copies.
 *
 * DAO Pattern:
 *   Domain classes (Book, BookCopy) stay clean
 *   All SQL lives here in the DAO
 *
 * Used by:
 *   - LibraryCatalog.java  for search operations
 *   - BookMenu.java        for CLI driven CRUD
 */
public class BookDAO {

    // Attribute - database connection
    

    // Single shared connection from Singleton
    private Connection connection;

    
    // Constructor
   

    /**
     * Constructor - retrieves the singleton database
     * connection when BookDAO is instantiated.
     * Called by LibraryCatalog when it creates BookDAO.
     */
    public BookDAO() {
        try {
            // get the single shared connection
            this.connection =
                DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.out.println("BookDAO connection error: "
                    + e.getMessage());
        }
    }

    
    // CREATE - Add a new book to the database
    

    /**
     * Inserts a new Book record into the books table.
     * Called by LibraryCatalog.addBook() from BookMenu.
     *
     * @param book - Book object to insert into database
     */
    public void addBook(Book book) {
        // SQL query with place-holders to prevent
        // SQL injection attacks
        String sql = "INSERT INTO books " +
                     "(isbn, title, author, subject, publisher) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try {
            // PreparedStatement safely fills in the ?
            // placeholders with actual values
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            // fill in each placeholder in order
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getSubject());
            stmt.setString(5, book.getPublisher());

            // execute the INSERT query
            stmt.executeUpdate();
            System.out.println("Book added successfully: "
                    + book.getTitle());

        } catch (SQLException e) {
            System.out.println("Error adding book: "
                    + e.getMessage());
        }
    }

    
    // READ - Get a single book by ISBN
    

    /**
     * Retrieves a single Book from the database by ISBN.
     * Called by BookMenu when displaying a specific book.
     *
     * @param isbn - ISBN of the book to retrieve
     * @return     - Book object if found
     * @throws BookNotFoundException if no book matches ISBN
     */
    public Book getBookByIsbn(String isbn)
            throws BookNotFoundException {

        String sql = "SELECT * FROM books WHERE isbn = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setString(1, isbn);

            // execute the SELECT query
            ResultSet rs = stmt.executeQuery();

            // if a row was returned map it to a Book object
            if (rs.next()) {
                return mapResultSetToBook(rs);
            } else {
                // no book found - throw custom exception
                throw new BookNotFoundException(
                    "No book found with ISBN: " + isbn);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving book: "
                    + e.getMessage());
            return null;
        }
    }

    // --------------------------------------------------------
    // READ - Get all books from database
    // --------------------------------------------------------

    /**
     * Retrieves all books from the books table.
     * Called by LibraryCatalog.getAllBooks() from BookMenu
     * when displaying the full catalog.
     *
     * @return - List of all Book objects in the database
     */
    public List<Book> getAllBooks() {
        // list to hold all books retrieved
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM books";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            // loop through every row returned
            // map each row to a Book object
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving books: "
                    + e.getMessage());
        }
        return books;
    }

    // --------------------------------------------------------
    // UPDATE - Update an existing book in the database
    // --------------------------------------------------------

    /**
     * Updates an existing Book record in the books table.
     * Called by LibraryCatalog.updateBook() from BookMenu.
     *
     * @param book - Book object with updated information
     */
    public void updateBook(Book book) {
        String sql = "UPDATE books SET " +
                     "title = ?, " +
                     "author = ?, " +
                     "subject = ?, " +
                     "publisher = ? " +
                     "WHERE isbn = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getSubject());
            stmt.setString(4, book.getPublisher());
            stmt.setString(5, book.getIsbn());

            // executeUpdate returns number of rows affected
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book updated successfully.");
            } else {
                System.out.println("No book found to update.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating book: "
                    + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // DELETE - Remove a book from the database
    // --------------------------------------------------------

    /**
     * Deletes a Book record from the books table by ISBN.
     * Called by LibraryCatalog.removeBook() from BookMenu.
     *
     * @param isbn - ISBN of the book to delete
     */
    public void removeBook(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setString(1, isbn);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book removed successfully.");
            } else {
                System.out.println("No book found with that ISBN.");
            }

        } catch (SQLException e) {
            System.out.println("Error removing book: "
                    + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // SEARCH - Search books by title
    // --------------------------------------------------------

    /**
     * Searches books table for books matching the title.
     * Called by LibraryCatalog.searchByTitle() which
     * fulfills the Searchable interface contract.
     * Uses SQL LIKE for partial title matching.
     *
     * @param title - full or partial title to search for
     * @return      - List of matching Book objects
     */
    public List<Book> searchByTitle(String title) {
        List<Book> books = new ArrayList<>();

        // LIKE with % allows partial matching
        // searching "clean" will find "Clean Code"
        String sql = "SELECT * FROM books " +
                     "WHERE title LIKE ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            // wrap search term with % for partial matching
            stmt.setString(1, "%" + title + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error searching by title: "
                    + e.getMessage());
        }
        return books;
    }

    // --------------------------------------------------------
    // SEARCH - Search books by author
    // --------------------------------------------------------

    /**
     * Searches books table for books matching the author.
     * Called by LibraryCatalog.searchByAuthor() which
     * fulfills the Searchable interface contract.
     * Uses SQL LIKE for partial author name matching.
     *
     * @param author - full or partial author name
     * @return       - List of matching Book objects
     */
    public List<Book> searchByAuthor(String author) {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM books " +
                     "WHERE author LIKE ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, "%" + author + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error searching by author: "
                    + e.getMessage());
        }
        return books;
    }

    
    // BOOK COPY OPERATIONS
    

    /**
     * Inserts a new BookCopy record into book_copies table.
     * Called by BookMenu when adding a physical copy.
     *
     * @param copy - BookCopy object to insert
     */
    public void addBookCopy(BookCopy copy) {
        String sql = "INSERT INTO book_copies " +
                     "(barcode, status, book_isbn) " +
                     "VALUES (?, ?, ?)";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, copy.getBarcode());
            stmt.setString(2, copy.getStatus().name());
            stmt.setString(3, copy.getBookIsbn());

            stmt.executeUpdate();
            System.out.println("Book copy added: "
                    + copy.getBarcode());

        } catch (SQLException e) {
            System.out.println("Error adding book copy: "
                    + e.getMessage());
        }
    }

    /**
     * Retrieves all copies of a specific book by ISBN.
     * Called by BookMenu to display available copies.
     *
     * @param bookIsbn - ISBN of the book
     * @return         - List of BookCopy objects
     */
    public List<BookCopy> getCopiesByIsbn(String bookIsbn) {
        List<BookCopy> copies = new ArrayList<>();

        String sql = "SELECT * FROM book_copies " +
                     "WHERE book_isbn = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setString(1, bookIsbn);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                copies.add(mapResultSetToBookCopy(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving copies: "
                    + e.getMessage());
        }
        return copies;
    }
    
    /**
     * Retrieves a BookCopy from the database by barcode.
     *
     * @param barcode - barcode of the copy to find
     * @return        - matching BookCopy or null
     */
    public BookCopy getBookCopyByBarcode(String barcode) {
        String sql = "SELECT * FROM book_copies " +
                     "WHERE barcode = ?";

        try {
            java.sql.Connection conn =
                database.DatabaseConnection
                    .getInstance().getConnection();
            java.sql.PreparedStatement stmt =
                    conn.prepareStatement(sql);
            stmt.setString(1, barcode);

            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new BookCopy(
                    rs.getInt("copy_id"),
                    rs.getString("barcode"),
                    CopyStatus.valueOf(
                        rs.getString("status")),
                    rs.getString("book_isbn")
                );
            }

        } catch (java.sql.SQLException e) {
            System.out.println(
                "Error looking up barcode: "
                    + e.getMessage());
        }
        return null;
    }

    
    // HELPER METHODS - map database rows to Java objects
    

    /**
     * Maps a ResultSet row to a Book object.
     * Called internally after every SELECT query.
     * Keeps mapping logic in one place - DRY principle.
     *
     * @param rs - ResultSet row from database query
     * @return   - populated Book object
     * @throws SQLException if column name is wrong
     */
    private Book mapResultSetToBook(ResultSet rs)
            throws SQLException {
        return new Book(
            rs.getString("isbn"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("subject"),
            rs.getString("publisher")
        );
    }

    /**
     * Maps a ResultSet row to a BookCopy object.
     * Called internally after every book_copies SELECT.
     *
     * @param rs - ResultSet row from database query
     * @return   - populated BookCopy object
     * @throws SQLException if column name is wrong
     */
    private BookCopy mapResultSetToBookCopy(ResultSet rs)
            throws SQLException {
        return new BookCopy(
            rs.getInt("copy_id"),
            rs.getString("barcode"),
            // convert String from DB back to CopyStatus enum
            CopyStatus.valueOf(rs.getString("status")),
            rs.getString("book_isbn")
        );
    }
}