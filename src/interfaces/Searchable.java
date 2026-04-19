
//Interface that defines the search contract for
//the Library Management System. Any class that
//implements this interface MUST provide concrete
//implementations for searching books by title
//and by author. Currently implemented by
//LibraryCatalog.java
package interfaces;

import java.util.List;
import domain.Book;

//Searchable is a contract interface 
//Any class implementing searchable must provide a way 
// to search books by title and by author 
//This ensures consistent search behaviour 
public interface Searchable {
	
	//search for books by their title 
	
	List<Book> searchByTitle(String title);
	List<Book> searchByAuthor(String author);

}
