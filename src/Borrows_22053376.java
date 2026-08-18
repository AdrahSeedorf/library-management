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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Borrows_22053376 {
	private Book_22053376 book;
	private Patron_22053376 patron;
	private LocalDate borrowingDate;
	private LocalDate dueDate;
	/** When the book came back, or null while it is still out. */
	private LocalDate returnDate;


	public Borrows_22053376(Book_22053376 book, Patron_22053376 patron) {
		this.book = book;
		this.patron = patron;
		this.borrowingDate = LocalDate.now();
		this.dueDate = borrowingDate.plusDays(21); // The due date is set to 21 days after the borrowing date
		this.returnDate = null; // still out
	}

	/**
	 * Rebuilds a loan read back from borrowedBooks.csv, dates and all.
	 *
	 * The other constructor stamps today's date, which is right for a new loan and
	 * wrong for one being loaded from the file -- using it would silently reset every
	 * historic borrowing date to the day the program was last started.
	 */
	public Borrows_22053376(Book_22053376 book, Patron_22053376 patron,
			LocalDate borrowingDate, LocalDate dueDate, LocalDate returnDate) {
		this.book = book;
		this.patron = patron;
		this.borrowingDate = borrowingDate;
		this.dueDate = dueDate;
		this.returnDate = returnDate;
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(LocalDate returnDate) {
		this.returnDate = returnDate;
	}

	/** True while the book is still out. */
	public boolean isOnLoan() {
		return returnDate == null;
	}


	public Book_22053376 getBook() {
		return book;
	}


	public void setBook(Book_22053376 book) {
		this.book = book;
	}


	public Patron_22053376 getPatron() {
		return patron;
	}


	public void setPatron(Patron_22053376 patron) {
		this.patron = patron;
	}


	public LocalDate getBorrowingDate() {
		return borrowingDate;
	}


	public void setBorrowingDate(LocalDate borrowingDate) {
		this.borrowingDate = borrowingDate;
	}


	public LocalDate getDueDate() {
		return dueDate;
	}


	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	
	// This method converts the borrow details into CSV format for writing to the borrowedBooks.csv file
    //
    // A fifth field carries the return date, empty while the book is still out. The
    // file previously stopped at the due date, which left it unable to express the
    // difference between a loan and a completed loan -- so returning a book recorded
    // nothing and every book ever borrowed stayed outstanding forever. A four-field
    // line read back from an older file is treated as still on loan.
    public String toCSVFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return patron.getPatronID() + "," + book.getISBN() + ","
                + borrowingDate.format(formatter) + "," + dueDate.format(formatter) + ","
                + (returnDate == null ? "" : returnDate.format(formatter));
    }

}
