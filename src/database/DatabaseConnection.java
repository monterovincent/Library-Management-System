//Manages the single database connection for the library management system using the singleton design pattern
//Ensures only one connection instance exists throughout the entire application life-cycle.


package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/*
 * DatabaseConnection implements the Singleton pattern
 * Singleton means:Only ONE instance of this class can ever exist
 * Every DAO class shares the SAME connection
 * No duplicate connections are opened
 */
public class DatabaseConnection {
	
	//Database configuration constants
	
	private static final String URL = 
			"jdbc:mariadb://localhost:3306/librarydb";
	
	//mariaDB user-name - default root
	private static final String USERNAME = "root";
	
	//mariaDB password 
	private static final String PASSWORD = "password";
	
	//singleton instance 
	
	private static volatile DatabaseConnection instance = null;
	
	//Actual database connection object 
	private Connection connection = null;
	
	//private Constructors- establishes the mariaDB Connection.
	
	private DatabaseConnection() throws SQLException {
		try {
			//Load the mariaDB JDBC driver
			Class.forName("org.mariadb.jdbc.Driver");
			
			//Establish connection to mariaDB
			this.connection = DriverManager.getConnection(
					URL, USERNAME, PASSWORD);
			System.out.println("Database connection established.");
		} catch (ClassNotFoundException e) {
			//Driver JAR not added to build path
			throw new SQLException(
					"MariaDB driver not found. " +
			    "Check your build path for the JAR file."
							+e.getMessage());
		}
	}
	
	//getInstance- the only way to get the connection
	
	//Returns the single instance of the database connection
	
	public static DatabaseConnection getInstance()
	 throws SQLException {
		
		//only create instance if it doesnt exist yet
		if (instance == null) {
			//synchronized block prevents 2 threads 
			//creating 2 instances at the same time 
			synchronized (DatabaseConnection.class) {
				if (instance == null) {
					instance = new DatabaseConnection();
				}
			}
		}
		return instance;
	}
	
	//getConnection - returns the connection object
	//used by all DAO classes to run SQL queries .
	
	public Connection getConnection() {
		return connection;
	}
	
	//Close Connection- cleanly close connection
	//Called when the application exits
	//should be called from the main.java when the app exits
	
	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				System.out.println("Database connection closed.");
			}
		} catch (SQLException e) {
			System.out.println("Error closing connection:"
					+ e.getMessage());
		}
	}
	
	
	

}
