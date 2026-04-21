package application;

import cli.MainMenu;
import database.DatabaseConnection;

import java.sql.SQLException;

/**
 * Main is the entry point for the Library Management System.
 *
 * Responsibilities:
 *   1. Establish the database connection on startup
 *   2. Launch the MainMenu CLI loop
 *   3. Cleanly close the database connection on exit
 *
 * All sub-menus are managed by MainMenu. Main stays as
 * small as possible - it only wires the top level together.
 */
public class Main {

    /**
     * Application entry point.
     * Starts the database connection then launches the CLI.
     * Ensures the connection is closed when the app exits.
     *
     * @param args - command line arguments (not used)
     */
    public static void main(String[] args) {

        // --------------------------------------------------------
        // Step 1 - Establish database connection
        // --------------------------------------------------------
        try {
            // trigger Singleton creation - connects to MariaDB
            DatabaseConnection.getInstance();

        } catch (SQLException e) {
            // cannot run without a database connection
            System.out.println(
                "Fatal Error: Could not connect to database.");
            System.out.println(e.getMessage());
            System.out.println(
                "Please check that MariaDB is running "
                    + "and the credentials in "
                    + "DatabaseConnection.java are correct.");
            return;
        }

        // --------------------------------------------------------
        // Step 2 - Launch the main menu CLI loop
        // --------------------------------------------------------
        MainMenu mainMenu = new MainMenu();
        mainMenu.display();

        // --------------------------------------------------------
        // Step 3 - Close the database connection on exit
        // --------------------------------------------------------
        // This runs after display() returns (user chose Exit)
        try {
            DatabaseConnection instance =
                DatabaseConnection.getInstance();
            instance.closeConnection();
        } catch (SQLException e) {
            System.out.println(
                "Error closing database connection: "
                    + e.getMessage());
        }
    }
}
