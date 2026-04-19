//Represent a book entity in the library management system

//Book represents a title in the library catalog
/*
 *  Used by:
 *   - BookDAO.java      for database CRUD operations
 *   - LibraryCatalog    for search operations
 *   - BookMenu.java     for CLI display
 */

package domain;

public class Book {
	
	//Attributes , mirrors the book table columns 
	private String isbn;
	
	//Title of book
	private String title;
	
	//book authors name 
	private String author;
	
	//genre category of book
	private String subject;
	
	//Name of publishing company
	private String publisher;
	
	//constructor 
	
	public Book(String isbn, String title, String author,
			    String subject, String publisher) {
		     this.isbn = isbn;
		     this.title = title;
		     this.author = author;
		     this.subject = subject;
		     this.publisher = publisher;
	}
	
	//getters retrieve private attribute values
	
	public String getIsbn() {
		return isbn;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	//setters -update private attribute values
	
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	//toString -  display book details in readable  format 
	
	@Override 
	public String toString() {
		return "\n==============================" +
	             "\nISBN   : " + isbn             +
	             "\nTitle  : " + title            +
	             "\nAuthor : " + author           +
	             "\nSubject : " + subject         +
	             "\nPublisher : " + publisher     +
	              "\n=============================";
	}

}
