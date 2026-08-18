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
public class Book_22053376 {

	/**
	 * @param args
	 */
	
	private String title;
	private String author;
	private String ISBN;
	private boolean availability;
	
	
	public Book_22053376 (String title, String author, String ISBN, boolean availability) {
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
	
	
	//method to check book out
	public void checkBookOut() {
		if(availability) {
			availability = false; //After the book is checked out, it is no longer available so the availability is changed to false.
			System.out.println("The book " + title + "has been checked out.");
		}
		else { // If the book is not available, it means it had already been checked out.
			System.out.println("The book " + title + " has already been checked out");
		}
	}
	
	
	
	//method to return book
	public void returnBook() {
		if (!availability) { //the book must have already been borrowed to be able to return. Therefore, it must not be available.
			availability = true;//if the book is returned, the book now becomes available.
			System.out.println("The book " + title + " has been returned.");
		}
		else { //If the book was already available, it cannot be returned because it was already in  the library.
			System.out.println("The book " + title + " was already in stock.");
		}
	}

}
