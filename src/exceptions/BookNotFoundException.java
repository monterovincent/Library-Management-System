//custom exception thrown when book cannot be found in in the 
//Library management system database.

package exceptions;

public class BookNotFoundException extends Exception {
	
	//serial version UID
	//Required for all exception subclasses
	//used by java for object serialization
	
	private static final long serialVersionUID = 1L;
	
	//Constructor
	//creates a new bookNotFoundException
	
	public BookNotFoundException(String message) {
		super(message);
	}

}
