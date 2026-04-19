//Represents a physical copy of a book in the library management system.
//while book.java  holds the general information about a title
//Book copy represents an actual physical copy sitting on the shelf.

package domain;

import enums.CopyStatus;

public class BookCopy {
	
	//Attributes
	
	private int copyId;
	
	private String barcode;
	
	private CopyStatus status;
	
	private String bookIsbn;
	
	//Constructors 
	
	public BookCopy(int copyId, String barcode,
			        CopyStatus status, String bookIsbn) {
		    this.copyId = copyId;
		    this.barcode = barcode;
		    this.status = status;
		    this.bookIsbn = bookIsbn;
	}
	
	//getters - retrieve private attribute values 
	
	public int getCopyId() {
		return copyId;
	}
	
	public String getBarcode() {
		return barcode;
	}
	
	public CopyStatus getStatus() {
		return status;
	}
	
	public String getBookIsbn() {
		return bookIsbn;
	}
	
	//setters -update private attribute values 
	
	public void setCopyId(int copyId) {
		this.copyId = copyId;
	}
	
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
	public void setStatus(CopyStatus status) {
		this.status = status;
	}
	
	public void setBookIsbn(String bookIsbn) {
		this.bookIsbn = bookIsbn;
	}
	
	//to-string displays copy details in readable  format
	
	@Override
    public String toString() {
        return  "\n=============================" +
                "\nCopy ID   : " + copyId        +
                "\nBarcode   : " + barcode        +
                "\nStatus    : " + status         +
                "\nBook ISBN : " + bookIsbn       +
                "\n=============================";
    }

}
