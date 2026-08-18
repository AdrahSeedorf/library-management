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
	
	
	public Borrows_22053376(Book_22053376 book, Patron_22053376 patron) {
		this.book = book;
		this.patron = patron;
		this.borrowingDate = LocalDate.now();
		this.dueDate = borrowingDate.plusDays(21); // The due date is set to 21 days after the borrowing date 
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
    public String toCSVFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return patron.getPatronID() + "," + book.getISBN() + "," + borrowingDate.format(formatter) + "," + dueDate.format(formatter);
    }

}
