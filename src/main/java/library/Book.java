package library;

/**
 * A single book in the catalogue.
 *
 * Availability is the book's own business: checkBookOut and returnBook apply the
 * rule and report whether it applied, rather than printing. See the note on
 * checkBookOut for why that matters.
 *
 * @author Seedorf Obeng-Mireku
 */
public class Book {

	
	private String title;
	private String author;
	private String ISBN;
	private boolean availability;
	
	
	public Book (String title, String author, String ISBN, boolean availability) {
		this.title = title;
		this.author = author;
		this.ISBN = ISBN;
		this.availability = availability; 
	}
	
	
	//All getters and setters
	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getAuthor() {
		return author;
	}



	public void setAuthor(String author) {
		this.author = author;
	}



	public String getISBN() {
		return ISBN;
	}



	public void setISBN(String iSBN) {
		ISBN = iSBN;
	}



	public boolean getAvailability() {
		return availability;
	}



	public void setAvailability(boolean availability) {
		this.availability = availability;
	}
	
	
	/**
	 * Marks the book as checked out. Returns false, changing nothing, if it was
	 * already out.
	 *
	 * Reports the outcome rather than printing it. A model class that writes to
	 * System.out can only be used by a console program, cannot be tested without
	 * capturing output, and prints at moments the caller may not want -- which is
	 * exactly why LibraryManager ignored these methods and re-implemented the same
	 * state changes inline. Two copies of a rule is one copy too many: the copy in
	 * Patron.returnBook guarded against a negative borrow count and the manager's
	 * copy did not, so borrow counts went negative in the shipped program.
	 */
	public boolean checkBookOut() {
		if (!availability) return false; // already out
		availability = false; //After the book is checked out, it is no longer available.
		return true;
	}



	/** Marks the book as back on the shelf. Returns false if it was never out. */
	public boolean returnBook() {
		if (availability) return false; //it was already in stock, so there is nothing to return
		availability = true;//if the book is returned, the book now becomes available.
		return true;
	}

}
