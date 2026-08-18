package library;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The rules, tested directly rather than through the menus.
 *
 * These are the cases the console tests cannot reach cheaply: what a method returns
 * when it refuses, and what the CSV helpers do with awkward values. The guards here
 * are the ones whose absence caused real defects — Patron.returnBook refusing to go
 * below zero is the rule the manager used to re-implement without it, which is how
 * borrow counts reached -1.
 */
class ModelTest {

	@Test
	@DisplayName("a book cannot be checked out twice")
	void checkOutIsIdempotentlyRefused() {
		Book book = new Book("T", "A", "1", true);
		assertTrue(book.checkBookOut());
		assertFalse(book.getAvailability());
		assertFalse(book.checkBookOut(), "a second check-out must be refused");
	}

	@Test
	@DisplayName("a book that was never out cannot be returned")
	void returnOfShelvedBookIsRefused() {
		Book book = new Book("T", "A", "1", true);
		assertFalse(book.returnBook());
		assertTrue(book.getAvailability());
	}

	@Test
	@DisplayName("a patron's borrow count never goes below zero")
	void borrowCountNeverGoesNegative() {
		Patron patron = new Patron("P", "1001", 0);
		assertFalse(patron.returnBook(), "returning with nothing out must be refused");
		assertEquals(0, patron.getBooksBorrowed());

		patron.borrowBook();
		assertEquals(1, patron.getBooksBorrowed());
		assertTrue(patron.returnBook());
		assertEquals(0, patron.getBooksBorrowed());
	}

	@Test
	@DisplayName("a loan is due 21 days out and open until it is returned")
	void loanDatesAndStatus() {
		Borrows loan = new Borrows(new Book("T", "A", "1", false), new Patron("P", "1001", 1));
		assertEquals(loan.getBorrowingDate().plusDays(21), loan.getDueDate());
		assertTrue(loan.isOnLoan());

		loan.setReturnDate(LocalDate.of(2026, 1, 1));
		assertFalse(loan.isOnLoan());
		assertTrue(loan.toCSVFormat().endsWith("2026-01-01"));
	}

	@Test
	@DisplayName("an open loan writes an empty fifth field rather than omitting it")
	void openLoanKeepsTheTrailingField() {
		Borrows loan = new Borrows(new Book("T", "A", "1", false), new Patron("P", "1001", 1));
		String csv = loan.toCSVFormat();
		assertEquals(5, Csv.split(csv).length, csv);
		assertTrue(Csv.split(csv)[4].isEmpty(), csv);
	}

	@Test
	@DisplayName("plain fields are split on commas")
	void splitsPlainFields() {
		assertArrayEquals(new String[] {"a", "b", "c"}, Csv.split("a,b,c"));
	}

	@Test
	@DisplayName("a comma inside quotes is part of the value")
	void splitsQuotedFields() {
		assertArrayEquals(new String[] {"Dune, Part Two", "Frank Herbert"},
				Csv.split("\"Dune, Part Two\",Frank Herbert"));
	}

	@Test
	@DisplayName("a doubled quote inside quotes is one literal quote")
	void splitsEscapedQuotes() {
		assertArrayEquals(new String[] {"He said \"hi\"", "x"},
				Csv.split("\"He said \"\"hi\"\"\",x"));
	}

	@Test
	@DisplayName("a trailing empty field is kept, not dropped")
	void keepsTrailingEmptyField() {
		assertEquals(3, Csv.split("a,b,").length);
		assertEquals("", Csv.split("a,b,")[2]);
	}

	@Test
	@DisplayName("only values that need quoting get quoted")
	void quotesOnlyWhenNecessary() {
		assertEquals("plain", Csv.field("plain"));
		assertEquals("\"has, comma\"", Csv.field("has, comma"));
		assertEquals("\"has \"\"quote\"\"\"", Csv.field("has \"quote\""));
	}

	@Test
	@DisplayName("quoting and splitting are inverses")
	void quoteThenSplitRoundTrips() {
		String awkward = "A title, with \"quotes\" and, commas";
		assertEquals(awkward, Csv.split(Csv.field(awkward) + ",x")[0]);
	}
}
