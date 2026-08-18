package library;

import java.time.LocalDate;

/**
 * One patron's place in the queue for one book.
 *
 * A reservation exists because the book was already out when it was wanted. It is
 * fulfilled the moment that book comes back and this patron is at the front of the
 * queue; it is cancelled if the patron gives up first. Fulfilled and cancelled
 * reservations are kept rather than deleted, so the file is a record of demand
 * rather than only of demand that is still outstanding -- which book people wait
 * for is the interesting half.
 *
 * Order is by the date the hold was placed, and ties are broken by the order the
 * records appear in the file. That makes the queue first-come-first-served and
 * reproducible, which matters: a queue whose order depends on a HashMap's iteration
 * would hand the same book to different people on different runs.
 *
 * @author Seedorf Obeng-Mireku
 */
public class Reservation {

	/** Where a reservation is in its life. */
	public enum Status { WAITING, FULFILLED, CANCELLED }

	private Book book;
	private Patron patron;
	private LocalDate placedOn;
	private Status status;

	public Reservation(Book book, Patron patron) {
		this(book, patron, LocalDate.now(), Status.WAITING);
	}

	/** Rebuilds a reservation read back from file, keeping its original date. */
	public Reservation(Book book, Patron patron, LocalDate placedOn, Status status) {
		this.book = book;
		this.patron = patron;
		this.placedOn = placedOn;
		this.status = status;
	}

	public Book getBook() {
		return book;
	}

	public Patron getPatron() {
		return patron;
	}

	public LocalDate getPlacedOn() {
		return placedOn;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	/** True while this patron is still waiting for the book. */
	public boolean isWaiting() {
		return status == Status.WAITING;
	}

	/** patronID,ISBN,placedOn,status */
	public String toCSVFormat() {
		return Csv.field(patron.getPatronID()) + "," + Csv.field(book.getISBN()) + ","
				+ placedOn + "," + status;
	}
}
