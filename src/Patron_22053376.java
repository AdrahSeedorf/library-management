/**
 * 
 */

/**
 * {Student ID: 22053376
Name: Seedorf Obeng-Mireku
Campus: Parramatta South
Tutor Name: Albany Asher
Class Day: Tuesday
Class Time: 1900 - 2100}
 */
public class Patron_22053376 {

	/**
	 * @param args
	 */
	
	private String name;
	private String patronID;
	private int booksBorrowed;
	
	
	public Patron_22053376(String name, String patronID, int booksBorrowed) {
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
	
	//Method to borrow book
	public void borrowBook() {
		booksBorrowed++; //number of books borrowed increases when the method is called by patron class.
		System.out.println(name + " has borrowed a book.");
		System.out.println("Number of books borrowed: " + booksBorrowed);
	}
	
	
	//Method to return book 
	public void returnBook() {
		if (booksBorrowed > 0) {
			booksBorrowed--; //when book is returned, the number of books borrowed is reduced by 1.
			System.out.println(name + " has returned a book.");
			System.out.println("Number of books borrowed: " + booksBorrowed);
		}
		else {
			System.out.println(name + " has no books borrowed.");//if number of books less than or equal to 0, the patron has no books to return.
		}
	}
}
