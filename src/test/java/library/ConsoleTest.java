package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Drives the program the way a person does — through the menus — and checks what
 * came out and what ended up in the files.
 *
 * Every case here corresponds to a defect that was reproduced against the original
 * code before being fixed. They are written as symptoms rather than as assertions
 * about internals: "a letter at the menu is rejected, not fatal" rather than
 * "readMenuChoice returns the previous value".
 *
 * Each test gets its own temporary data directory seeded with a copy of the shipped
 * CSVs, so no test can see another's library and none of them can touch the
 * repository's own data — which matters more than usual here, given that one of the
 * defects under test was the program silently rewriting those files.
 */
class ConsoleTest {

	@TempDir
	Path dir;

	private static final String BOOKS = String.join("\n",
			"The Catcher in the Rye,J.D. Salinger,978-0-316-76948-0,true",
			"To Kill a Mockingbird,Harper Lee,978-0-06-112008-4,true",
			"1984,George Orwell,978-0-452-28423-4,true",
			"Pride and Prejudice,Jane Austen,978-0-14-143951-8,true") + "\n";

	private static final String PATRONS = String.join("\n",
			"Alan Turing,1001,0",
			"Grace Hopper,1002,0",
			"Linus Torvalds,1003,0",
			"Ada Lovelace,1004,0") + "\n";

	@BeforeEach
	void seed() throws Exception {
		Files.writeString(dir.resolve("booklist.csv"), BOOKS);
		Files.writeString(dir.resolve("patronList.csv"), PATRONS);
		LibraryManager.dataDir = dir;
		LibraryManager.reset();
	}

	/** Runs the program with the given keystrokes and returns everything it printed. */
	private String run(String keystrokes) {
		LibraryManager.reset();
		LibraryManager.useInput(new ByteArrayInputStream(keystrokes.getBytes(StandardCharsets.UTF_8)));
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		PrintStream original = System.out;
		try {
			System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
			LibraryManager.main(new String[0]);
		} finally {
			System.setOut(original);
		}
		return captured.toString(StandardCharsets.UTF_8);
	}

	private List<String> lines(String file) throws Exception {
		return Files.readAllLines(dir.resolve(file));
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Input handling")
	class InputHandling {

		@Test
		@DisplayName("a letter at the menu is rejected, not fatal")
		void letterIsRejected() {
			String out = run("x\n4\n");
			assertTrue(out.contains("'x' is not a number"), out);
			assertFalse(out.contains("InputMismatchException"), out);
		}

		@Test
		@DisplayName("a clean exit says so")
		void cleanExitIsAnnounced() {
			assertTrue(run("4\n").contains("End of Program"));
		}

		@Test
		@DisplayName("an out-of-range option is rejected")
		void outOfRangeIsRejected() {
			assertTrue(run("99\n4\n").contains("choose a number between 1 and 4"));
		}

		@Test
		@DisplayName("end of input exits instead of throwing")
		void endOfInputExits() {
			assertFalse(run("").contains("NoSuchElementException"));
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Data integrity")
	class DataIntegrity {

		@Test
		@DisplayName("starting and quitting loses nothing")
		void roundTripLosesNothing() throws Exception {
			int booksBefore = lines("booklist.csv").size();
			int patronsBefore = lines("patronList.csv").size();
			run("4\n");
			assertEquals(booksBefore, lines("booklist.csv").size());
			assertEquals(patronsBefore, lines("patronList.csv").size());
		}

		@Test
		@DisplayName("the patron file is read under its real name")
		void patronsActuallyLoad() {
			assertTrue(run("1\n1\n1001\n4\n4\n").contains("Alan Turing"));
		}

		@Test
		@DisplayName("a missing catalogue is not replaced by an empty one")
		void missingFileIsNotOverwritten() throws Exception {
			Files.delete(dir.resolve("booklist.csv"));
			String out = run("4\n");
			assertTrue(out.contains("Not writing booklist.csv"), out);
			assertFalse(Files.exists(dir.resolve("booklist.csv")));
		}

		@Test
		@DisplayName("but a genuinely emptied catalogue is still saved")
		void deliberateEmptyIsSaved() throws Exception {
			Files.writeString(dir.resolve("booklist.csv"), "Only Book,Some Author,111,true\n");
			run("2\n4\nOnly Book\nSome Author\n5\n4\n");
			assertTrue(Files.exists(dir.resolve("booklist.csv")));
			assertEquals(0, Files.size(dir.resolve("booklist.csv")));
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Book removal")
	class BookRemoval {

		@Test
		@DisplayName("removing the last book does not crash")
		void removingLastBookDoesNotCrash() {
			String out = run("2\n4\nPride and Prejudice\nJane Austen\n5\n4\n");
			assertFalse(out.contains("IndexOutOfBoundsException"), out);
		}

		@Test
		@DisplayName("removing a book that is not there says so")
		void missingBookIsReported() {
			assertTrue(run("2\n4\nNo Such Book\nNobody\n5\n4\n")
					.contains("was not found in the catalogue"));
		}

		@Test
		@DisplayName("removal reports the book that was actually removed")
		void removalReportsTheRightBook() {
			String out = run("2\n4\n1984\nGeorge Orwell\n5\n4\n");
			assertTrue(out.contains("1984 by George Orwell has been removed"), out);
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Listings")
	class Listings {

		@Test
		@DisplayName("the catalogue lists every book")
		void catalogueListsEverything() {
			String out = run("2\n2\n5\n4\n");
			for (String line : BOOKS.strip().split("\n")) {
				String title = line.split(",")[0];
				assertTrue(out.contains(title), "missing from the catalogue: " + title);
			}
		}

		@Test
		@DisplayName("the register lists every patron")
		void registerListsEveryone() {
			String out = run("1\n2\n4\n4\n");
			for (String line : PATRONS.strip().split("\n")) {
				String name = line.split(",")[0];
				assertTrue(out.contains(name), "missing from the register: " + name);
			}
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Loans")
	class Loans {

		private static final String LOAN_1984_TO_1002 = "3\n1\n1984\nGeorge Orwell\n1002\n";

		@Test
		@DisplayName("the wrong patron cannot return a book")
		void wrongPatronCannotReturn() throws Exception {
			String out = run(LOAN_1984_TO_1002 + "2\n1984\nGeorge Orwell\n1001\n4\n4\n");
			assertTrue(out.contains("is on loan to Grace Hopper, not to Alan Turing"), out);
			assertTrue(lines("patronList.csv").contains("Alan Turing,1001,0"),
					"a refused return must not drive the count negative");
		}

		@Test
		@DisplayName("a book already out cannot be loaned again")
		void doubleLoanIsRefused() {
			String out = run(LOAN_1984_TO_1002 + "1\n1984\nGeorge Orwell\n1003\n\n4\n4\n");
			assertTrue(out.contains("has already been loaned out"), out);
		}

		@Test
		@DisplayName("a correct return clears the borrow count and is recorded")
		void correctReturnIsRecorded() throws Exception {
			run(LOAN_1984_TO_1002 + "2\n1984\nGeorge Orwell\n1002\n4\n4\n");
			assertTrue(lines("patronList.csv").contains("Grace Hopper,1002,0"));
			String loan = lines("borrowedBooks.csv").get(0);
			assertEquals(5, Csv.split(loan).length, loan);
			assertFalse(Csv.split(loan)[4].isEmpty(), "the return date must be filled in: " + loan);
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Reservations")
	class Reservations {

		/** 1002 borrows 1984; 1003 then 1004 are refused and join the queue. */
		private static final String QUEUE =
				"3\n1\n1984\nGeorge Orwell\n1002\n"
				+ "1\n1984\nGeorge Orwell\n1003\n"
				+ "1\n1984\nGeorge Orwell\n1004\n";

		@Test
		@DisplayName("a refused loan offers a place in the queue, first come first served")
		void queueIsOrdered() {
			String out = run(QUEUE + "4\n4\n");
			assertTrue(out.contains("Linus Torvalds is number 1 in the queue"), out);
			assertTrue(out.contains("Ada Lovelace is number 2 in the queue"), out);
		}

		@Test
		@DisplayName("a patron cannot join the same queue twice")
		void noDoubleJoin() {
			String out = run(QUEUE + "1\n1984\nGeorge Orwell\n1003\n4\n4\n");
			assertTrue(out.contains("is already in the queue"), out);
		}

		@Test
		@DisplayName("returning the book serves the front of the queue")
		void returnServesTheQueue() throws Exception {
			String out = run(QUEUE + "2\n1984\nGeorge Orwell\n1002\n4\n4\n");
			assertTrue(out.contains("RESERVED: set '1984' aside for Linus Torvalds"), out);
			assertTrue(out.contains("1 other patron(s) still waiting"), out);
			assertTrue(lines("reservations.csv").stream().anyMatch(l -> l.startsWith("1003,") && l.endsWith("FULFILLED")),
					lines("reservations.csv").toString());
		}

		@Test
		@DisplayName("an empty queue says so rather than printing nothing")
		void emptyQueueIsReported() {
			assertTrue(run("3\n3\n4\n4\n").contains("Nobody is waiting for anything"));
		}

		@Test
		@DisplayName("the queue survives a restart, minus whoever was served")
		void queueIsReloaded() {
			run(QUEUE + "2\n1984\nGeorge Orwell\n1002\n4\n4\n");
			String out = run("3\n3\n4\n4\n");
			assertTrue(out.contains("Ada Lovelace"), out);
			assertFalse(out.contains("Linus Torvalds"), out);
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Input validation")
	class Validation {

		@Test
		@DisplayName("a non-numeric patron ID does not break Add Patron")
		void legacyPatronIdIsSkipped() throws Exception {
			Files.writeString(dir.resolve("patronList.csv"), PATRONS + "Legacy Member,ABC123,0\n");
			String out = run("1\n3\nNew Person\n4\n4\n");
			assertFalse(out.contains("NumberFormatException"), out);
			assertTrue(out.contains("New Person has been successfully added"), out);
		}

		@Test
		@DisplayName("a blank patron name is refused, not stored")
		void blankNameIsRefused() throws Exception {
			String out = run("1\n3\n\nReal Name\n4\n4\n");
			assertTrue(out.contains("cannot be blank"), out);
			assertTrue(lines("patronList.csv").stream().noneMatch(l -> l.startsWith(",")));
		}

		@Test
		@DisplayName("a duplicate ISBN is refused")
		void duplicateIsbnIsRefused() {
			String out = run("2\n3\nDup\nSomebody\n978-0-452-28423-4\n5\n4\n");
			assertTrue(out.contains("already in the catalogue"), out);
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("Malformed input files")
	class Malformed {

		@Test
		@DisplayName("a short book line is reported by number, not fatal")
		void shortLineIsSkipped() throws Exception {
			Files.writeString(dir.resolve("booklist.csv"), BOOKS + "Broken Line\n");
			String out = run("4\n");
			assertTrue(out.contains("Skipping line 5 of booklist.csv"), out);
			assertFalse(out.contains("Exception"), out);
		}

		@Test
		@DisplayName("a non-numeric borrow count is reported, not fatal")
		void badCountIsSkipped() throws Exception {
			Files.writeString(dir.resolve("patronList.csv"), PATRONS + "Bad,2001,not-a-number\n");
			assertTrue(run("4\n").contains("is not a whole number"));
		}
	}

	// -----------------------------------------------------------------------
	@Nested
	@DisplayName("CSV round trip")
	class RoundTrip {

		@Test
		@DisplayName("a title containing a comma survives being written and read back")
		void commaInTitleSurvives() throws Exception {
			run("2\n3\nDune, Part Two\nFrank Herbert\n978-0-441-01359-3\n5\n4\n");
			assertTrue(lines("booklist.csv").stream()
							.anyMatch(l -> l.startsWith("\"Dune, Part Two\",Frank Herbert,")),
					lines("booklist.csv").toString());
			assertTrue(run("2\n2\n5\n4\n").contains("Dune, Part Two"));
		}
	}
}
