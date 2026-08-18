/**
 * A library member, and the count of books they currently hold.
 *
 * @author Seedorf Obeng-Mireku
 */
public class Patron {

	
	private String name;
	private String patronID;
	private int booksBorrowed;
	
	
	public Patron(String name, String patronID, int booksBorrowed) {
		this.name = name;
		this.patronID = patronID;
		this.booksBorrowed = booksBorrowed;
	}
	
	//All getters and setters 
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPatronID() {
		return patronID;
	}


	public void setPatronID(String patronID) {
		this.patronID = patronID;
	}


	public int getBooksBorrowed() {
		return booksBorrowed;
	}


	public void setBooksBorrowed(int booksBorrowed) {
		this.booksBorrowed = booksBorrowed;
	}
	
	/** Credits the patron with one more borrowed book. */
	public void borrowBook() {
		booksBorrowed++; //number of books borrowed increases when the method is called.
	}


	/**
	 * Discharges one borrowed book. Returns false, changing nothing, if this patron
	 * has none out.
	 *
	 * That guard is the whole point of the method existing. LibraryManager used to
	 * decrement the count inline without it, so a patron who had borrowed nothing
	 * could be recorded as having returned a book and end on -1 -- which was then
	 * written to patronList.csv. The rule now lives in one place, and the caller is
	 * told whether it applied instead of being told nothing.
	 */
	public boolean returnBook() {
		if (booksBorrowed <= 0) return false; //nothing to return
		booksBorrowed--; //when book is returned, the number of books borrowed is reduced by 1.
		return true;
	}
}
