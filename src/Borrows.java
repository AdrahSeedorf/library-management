import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * One loan: which book, to which patron, when it went out, when it is due, and
 * when it came back.
 *
 * A null return date means the book is still out. That distinction is what lets
 * borrowedBooks.csv record a completed loan rather than only an opened one.
 *
 * @author Seedorf Obeng-Mireku
 */
public class Borrows {
	private Book book;
	private Patron patron;
	private LocalDate borrowingDate;
	private LocalDate dueDate;
	/** When the book came back, or null while it is still out. */
	private LocalDate returnDate;


	public Borrows(Book book, Patron patron) {
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
	public Borrows(Book book, Patron patron,
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


	public Book getBook() {
		return book;
	}


	public void setBook(Book book) {
		this.book = book;
	}


	public Patron getPatron() {
		return patron;
	}


	public void setPatron(Patron patron) {
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
        return Csv.field(patron.getPatronID()) + "," + Csv.field(book.getISBN()) + ","
                + borrowingDate.format(formatter) + "," + dueDate.format(formatter) + ","
                + (returnDate == null ? "" : returnDate.format(formatter));
    }

}
