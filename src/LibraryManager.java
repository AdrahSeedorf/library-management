import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * The console front end: menus, CSV persistence, and the library operations.
 *
 * Reads booklist.csv, patronList.csv and borrowedBooks.csv from the working
 * directory on startup, and writes the first two back on a clean exit. The three
 * are cross-checked against each other as they load, and disagreements are
 * reported rather than believed.
 *
 * @author Seedorf Obeng-Mireku
 */
public class LibraryManager {

	
	/**
	 * 
	 */

	
		static Scanner keyboard = new Scanner(System.in);

		/**
		 * Reads a menu choice between min and max, re-prompting until it gets one.
		 *
		 * Every menu previously called keyboard.nextInt() directly, which throws
		 * InputMismatchException on anything that is not a number and kills the whole
		 * program with a stack trace. Typing a letter by accident -- or pressing Ctrl-D,
		 * or piping input that runs out -- ended the session and lost the work in it.
		 * That is the first thing anyone trying the program is likely to do.
		 *
		 * Reading a whole line and parsing it also fixes the leftover-newline problem
		 * that nextInt() creates, which is why every caller used to need a matching
		 * nextLine() to clean up after it.
		 */
		public static int readMenuChoice(int min, int max) {
			while (true) {
				System.out.print("Select an option (" + min + " - " + max + "): ");
				if (!keyboard.hasNextLine()) {
					// Input ended: treat as a request to leave rather than an error. The
					// caller's exit option is always the largest one.
					System.out.println();
					return max;
				}
				String line = keyboard.nextLine().trim();
				try {
					int choice = Integer.parseInt(line);
					if (choice >= min && choice <= max) {
						return choice;
					}
					System.out.println("Please choose a number between " + min + " and " + max + ".");
				} catch (NumberFormatException e) {
					System.out.println("'" + line + "' is not a number. Please choose between "
							+ min + " and " + max + ".");
				}
			}
		}

		/** Reads a line of text, returning "" rather than throwing if input has ended. */
		public static String readLine() {
			return keyboard.hasNextLine() ? keyboard.nextLine().trim() : "";
		}

		/**
		 * Reads a value that must not be blank, re-prompting until it is not.
		 *
		 * Pressing Enter at a prompt used to be accepted, so a patron could be added
		 * with no name at all -- printing " has been successfully added." and writing
		 * ",1016,0" into patronList.csv. A record with an empty key is worse than no
		 * record, because nothing can ever look it up again.
		 *
		 * Returns "" only when input has ended, which the caller must treat as a
		 * cancellation rather than as a value.
		 */
		public static String readRequired(String what) {
			while (true) {
				if (!keyboard.hasNextLine()) return "";
				String value = keyboard.nextLine().trim();
				if (!value.isEmpty()) return value;
				System.out.println("The " + what + " cannot be blank. Enter a value, please: ");
			}
		}

		
		
		/**
		 * The data files, named once so the read and the write cannot disagree.
		 *
		 * They previously did: main() read "patronlist.csv" while the file on disk is
		 * "patronList.csv". macOS filesystems are case-insensitive by default so it
		 * worked on the machine it was written on, and failed completely on Linux --
		 * "File not found: patronlist.csv", zero patrons loaded, and then an EMPTY
		 * patronlist.csv written out on exit beside the real one.
		 */
		public static final String BOOKS_FILE = "booklist.csv";
		public static final String PATRONS_FILE = "patronList.csv";
		public static final String LOANS_FILE = "borrowedBooks.csv";

		public static ArrayList<Book> books = new ArrayList<>(); //An arrayList named books is created to store books
	    public static ArrayList<Patron> patrons = new ArrayList<>(); //An arrayList named patrons is created to store patrons
	    /**
	     * Every loan, open and closed. borrowedBooks.csv used to be written and never
	     * read, so the program had no idea who was holding which book -- which is why
	     * returns went unchecked.
	     */
	    public static ArrayList<Borrows> loans = new ArrayList<>();

	    /**
	     * Whether each data file was readable at startup.
	     *
	     * The exit path rewrites both files from memory. If a file was missing, memory
	     * holds nothing for it, and the write turned "the file is not here" into "the
	     * file is here and empty" -- destroying the difference between a misplaced file
	     * and an empty library. Recording what was found lets the exit refuse.
	     */
	    public static boolean booksFileLoaded = false;
	    public static boolean patronsFileLoaded = false;

		
		
		public static int displayMainMenu() {
				boolean closeProgram = false;
				int option = 0;
				int menu = 0;
			   while(!closeProgram) {
		        	//A list of options is printed on the screen
			     	System.out.println("\n==========================================");
		        	System.out.println("Programming Techniques Library Management");
			     	System.out.println("==========================================");
		        	System.out.println("1. Patron Management");
		        	System.out.println("2. Book Management");
		        	System.out.println("3. Loans Management");
		        	System.out.println("4. Exit Program");
		        	option = readMenuChoice(1, 4); //The input from the user is obtained to perform the corresponding function
		        	
		        	 if (option == 1) {
		 	        	menu = patronMenu();
		 	        }
		        	 else if(option == 2) {
		        		 menu = bookMenu();
		        		 
		        	 }
		        	 else if (option == 3) {
		        		 menu = loanMenu();
		        	 }
		        	 else if (option == 4) {
		        		 saveOnExit(BOOKS_FILE, booksFileLoaded, books.isEmpty(),
		        				 () -> writeBooksToFile(BOOKS_FILE, books));
		        		 saveOnExit(PATRONS_FILE, patronsFileLoaded, patrons.isEmpty(),
		        				 () -> writePatronsToFile(PATRONS_FILE, patrons));
		        		 System.out.println("End of Program");
		        		 closeProgram = true;
		        	 }
			   }
		        return option;	   
		}
		
		public static int patronMenu() {
			int option = 0;
			while (!(option == 4)) {
		     	System.out.println("\n---------------------");
				System.out.println("Patron Management");
		     	System.out.println("---------------------");
		     	System.out.println("1. Search Patron");
		     	System.out.println("2. List All Patrons");
		     	System.out.println("3. Add Patron");
		     	System.out.println("4. Exit sub-menu");
		     	option = readMenuChoice(1, 4);
		     	if (option == 1) {
		     		searchPatron();
		     	}
		     	else if (option == 2) {
		     		outputListOfPatrons(patrons);
		     	}
		     	else if (option == 3) {
		     		addPatron();
		     	}
		     	else if (option == 4) {
		     		System.out.println("Patron Menu Exited");
		     		break;
		     	}
				
			}
			
			return option;
		}
		
		public static int bookMenu() {
			int option = 0; 
			
			while (!(option == 5)) {
		     	System.out.println("\n---------------------");
				System.out.println("Book Management");
		     	System.out.println("---------------------");
				System.out.println("1. Check Availability");
				System.out.println("2. List All Books");
				System.out.println("3. Add Book");
				System.out.println("4. Remove Book");
				System.out.println("5. Exit sub-menu");
				option = readMenuChoice(1, 5);
				if (option == 1) {
					checkAvailability();
				}
				else if (option == 2) {
					outputBookCatalogue(books);
				}
				else if (option == 3) {
					addBook();
				}
				else if (option == 4) {
					removeBook();
				}
				else if (option == 5) {
					System.out.println("Book Menu Exited");
					break;
				}
			}
			return option;	
		}
		
		public static int loanMenu() {
			int option = 0;
			
			while (!(option == 3)) {
		     	System.out.println("\n---------------------");
				System.out.println("Loans Management");
		     	System.out.println("---------------------");
				System.out.println("1. Loan Book");
				System.out.println("2. Return Book");
				System.out.println("3. Exit sub-menu");
				option = readMenuChoice(1, 3);
				if (option == 1) {
					loanBook();
				}
				else if (option == 2) {
					returnBorrowedBook();
				}
				else if (option == 3) {
					System.out.println("Loan Menu Exited");
					break;
				}
			}
			return option;
		}
		
		
		//The method below reads data about boos from booksFile and stores it in the arrayList called bookList
		 public static void readBooksFromFile(String booksFile, ArrayList<Book> books) {
			 
			 String str;
			 String title;
			 String author;
			 String isbn;
			 boolean availability;
		        // No cap on how many books are read. There used to be one -- position < 20
		        // -- and because the exit path REWRITES this file from whatever is in
		        // memory, every run silently deleted every book past the twentieth.
		        int lineNumber = 0;
		        try (Scanner bk = new Scanner(new File(booksFile))) {
		            while (bk.hasNextLine()) {
		                str = bk.nextLine();
		                lineNumber++;
		                if (str.trim().isEmpty()) continue; // blank line, including a trailing one
		                String[] item = Csv.split(str); //quote-aware, so a comma inside a title survives
		                // Say which line is wrong and carry on, rather than dying on it. A
		                // short line used to throw ArrayIndexOutOfBoundsException out of
		                // main with no indication of which line caused it.
		                if (item.length < 4) {
		                    System.out.println("Skipping line " + lineNumber + " of " + booksFile
		                            + ": expected 4 fields, found " + item.length);
		                    continue;
		                }
		                title = item[0].trim(); //The first item on the line is stored as the title of the book
		                author = item[1].trim(); //The second item on the line is stored as the author
		                isbn = item[2].trim(); //The third item is stored as the ISBN of the book
		                availability = Boolean.parseBoolean(item[3].trim()); //data[3] is read from the file as string so it is converted to boolean to match the type of availability.
		                Book book = new Book(title, author, isbn, availability);

		                //The new book object created is added to the arraylist of books
		                books.add(book);
		            }
		            booksFileLoaded = true;
		        } catch (FileNotFoundException e) {
		            System.out.println("File not found: " + booksFile);
		        }
		    }
		 
		 
		 //The method below reads data about patrons from patronsFile and stores it in the arrayList called patronList
		 public static void readPatronsFromFile(String patronsFile, ArrayList<Patron> patrons) {
			 String str;
			 String name;
			 String patronID;
			 int booksBorrowed;

		        // No cap, for the same reason as readBooksFromFile: the old limit of 10
		        // silently deleted every patron past the tenth on exit.
		        try (Scanner pt = new Scanner(new File(patronsFile))) {
		            int lineNumber = 0;
		            while (pt.hasNextLine()) {
		                str = pt.nextLine();
		                lineNumber++;
		                if (str.trim().isEmpty()) continue;
		                String[] item = Csv.split(str); //quote-aware, so a comma inside a title survives
		                if (item.length < 3) {
		                    System.out.println("Skipping line " + lineNumber + " of " + patronsFile
		                            + ": expected 3 fields, found " + item.length);
		                    continue;
		                }
		                name = item[0].trim(); //The first item on the line is stored as name 
		                patronID = item[1].trim(); //The second item on the line is stored as patronID
		                try {
		                    booksBorrowed = Integer.parseInt(item[2].trim()); //converted to int to match the type of booksBorrowed
		                } catch (NumberFormatException e) {
		                    System.out.println("Skipping line " + lineNumber + " of " + patronsFile
		                            + ": '" + item[2].trim() + "' is not a whole number");
		                    continue;
		                }
		                
		                
		                Patron patron = new Patron(name, patronID, booksBorrowed);// A new patron object is created and given name, patronID and booksBorrowed as parameters

		                //The new patron object created is added to the arraylist of patrons
		                patrons.add(patron);
		            }
		            patronsFileLoaded = true;
		        } catch (FileNotFoundException e) {
		            System.out.println("File not found: " + patronsFile);
		        }
		    }
		 
		 
		 
		 
		 //This method displays the books owned by the library with their titles, authors,ISBN, and their availability.
		 public static void outputBookCatalogue(ArrayList<Book> books) {
		        System.out.println("\n===== Book Catalogue in the Library =====");
		        System.out.println("-------------------------------------------");
		        System.out.printf("\n%-40s %-30s %-20s %-10s%n","Title", "Author", "ISBN", "Available?");
		        for (Book book : books) {
		            System.out.printf("%-40s %-30s %-20s %-10s%n", book.getTitle(), book.getAuthor(), book.getISBN(), book.getAvailability());
		        }
		    }
		 
		 
		 
		 
		 
		 //This method displays the names of patrons, their patronID and the number of books each patron has borrowed
		 public static void outputListOfPatrons(ArrayList<Patron> patrons) {
		        System.out.println("\n===== List of Patrons =====");
		        System.out.println("-----------------------------");
	           System.out.printf("\n%-30s %-20s %-5s%n", "Name", "Patron ID", "Books borrowed");
		        for (Patron patron : patrons) {
		            System.out.printf("%-30s %-20s %-5d%n", patron.getName(), patron.getPatronID(), patron.getBooksBorrowed());
		        }
		    }
		
		 
		//Method to add books to the catalogue 
		 public static void addBook() {
			 String title, author, isbn;
			 boolean availability;
			 
		     System.out.println("\n---------------------");
		     System.out.println("*ADD BOOK*");
		     System.out.println("---------------------");

			 System.out.println("\nEnter the title of the book: ");
			 title = readRequired("title");
			 if (title.isEmpty()) return;
			 System.out.println("Enter the author of the book: ");
			 author = readRequired("author");
			 if (author.isEmpty()) return;
			 System.out.println("Enter the ISBN of the book: ");
			 isbn = readRequired("ISBN");
			 if (isbn.isEmpty()) return;

			 // An ISBN identifies an edition, and the loan file records loans BY isbn,
			 // so two books sharing one makes every loan and return ambiguous --
			 // findBookByISBN can only ever return the first of them.
			 Book existing = findBookByISBN(isbn);
			 if (existing != null) {
				 System.out.println("A book with ISBN " + isbn + " is already in the catalogue: '"
						 + existing.getTitle() + "' by " + existing.getAuthor() + ". Nothing was added.");
				 return;
			 }

			 availability = true;
			 Book newBook = new Book(title, author, isbn, availability);
			 books.add(newBook);
			 System.out.println(newBook.getTitle() + " by " + newBook.getAuthor() + " has been successfully added.");
		 }
		 

		 //Method to remove Book
		 public static void removeBook() {
			 String title, author;
			 
			 System.out.println("\n---------------------");
		     System.out.println("*REMOVE BOOK*");
		     System.out.println("---------------------");
		     
			 System.out.println("\nEnter the title of the book: ");
			 title = readLine();
			 System.out.println("Enter the author of the book: ");
			 author = readLine();
			 boolean removed = false;
			 for (int i = 0; i < books.size(); i++) {
				 if (title.equals(books.get(i).getTitle()) && author.equals(books.get(i).getAuthor())) {
					 // Hold the book BEFORE removing it. Reading books.get(i) afterwards
					 // reads whatever shuffled into that slot -- the next book along, or
					 // nothing at all if this was the last one.
					 Book gone = books.remove(i);
					 System.out.println(gone.getTitle() + " by " + gone.getAuthor() + " has been removed successfully.");
					 removed = true;
					 break;
				 }
			 }
			 if (!removed) {
				 System.out.println(title + " by " + author + " was not found in the catalogue.");
			 }
			 
		 }
		 
		 
		 //Method to check if book is available or loaned out
		 public static void checkAvailability() {
			 boolean found = false;
			 String title, author;
			 
			 System.out.println("\n---------------------");
		     System.out.println("*CHECK AVAILABILITY*");
		     System.out.println("---------------------");
			 System.out.println("\nEnter the title of the book: ");
			 title = readLine();
			 System.out.println("Enter the author of the book: ");
			 author = readLine();
			 for (int i = 0; i < books.size(); i++) {
				 if (title.equals(books.get(i).getTitle()) && author.equals(books.get(i).getAuthor())) {
				     System.out.printf("\n%-40s %-30s %-40s %-10s%n","Title", "Author", "ISBN", "Available?");
					 System.out.printf("%-40s %-30s %-40s %-10s%n", books.get(i).getTitle(), books.get(i).getAuthor(), books.get(i).getISBN(), books.get(i).getAvailability());
					 found = true;
					 break;
				 }
			 }
			 if (!found) {
				 System.out.println(title + " by " + author + " was not found");
			 }
		 }
		 
		 
		 //PATRON MANAGEMENT SUBMENU
		 
		 //Method to add a patron to the patron list
		 public static void addPatron() {
			 String name;
			 int id, lastID, booksBorrowed;
			 System.out.println("\n---------------------");
		     System.out.println("*ADD PATRON*");
		     System.out.println("---------------------");
			 System.out.println("\nEnter the name of the patron: ");
			 name = readRequired("patron's name");
			 if (name.isEmpty()) return; // input ended; nothing to add
			 // Skip IDs that are not numbers rather than parsing every one blindly.
			 // Patron IDs are Strings and the loader accepts any text, so a single
			 // non-numeric ID in patronList.csv -- a legacy membership number, say --
			 // made this method throw NumberFormatException out of main and kill the
			 // program. Adding a patron should not depend on every OTHER patron's ID
			 // being well formed.
			 lastID = 1000;
			 for (int i = 0; i < patrons.size(); i++) {
				 try {
					 int currentId = Integer.parseInt(patrons.get(i).getPatronID());
					 if (currentId > lastID) {
						 lastID = currentId;
					 }
				 } catch (NumberFormatException e) {
					 continue; // not a sequential number; it cannot be the highest one
				 }
			 }
			 id = lastID + 1; //The new patron is given the next sequential id after the highest numeric one.
			 booksBorrowed = 0; //New Patrons have no books borrowed
			 Patron newPatron = new Patron(name, String.valueOf(id), booksBorrowed);
			 patrons.add(newPatron); //The new patron is added to the list of patrons
			 System.out.println( newPatron.getName() + " has been successfully added.");
		 }
		 
		 
		 //Method to search for an existing patron
		 public static void searchPatron() {
			 String patronID;
			 boolean found = false;
			 System.out.println("\n---------------------");
		     System.out.println("*SEARCH PATRON*");
		     System.out.println("---------------------");
			 System.out.println("\nEnter the patron ID: ");
			 patronID = readLine();
			 for (int i = 0; i < patrons.size(); i++) {
				 if (patronID.equals(patrons.get(i).getPatronID())) {
			         System.out.printf("\n%-30s %-30s %-5s%n", "Name", "Patron ID", "Books borrowed");
			         System.out.printf("\n%-30s %-30s %-5s%n", patrons.get(i).getName(), patrons.get(i).getPatronID(), patrons.get(i).getBooksBorrowed());
					 found = true;
					 break;
				 }
			 }
			 if (!found) {
				 System.out.println("The patron with ID " + patronID + " was not found.");
			 }
			 
		 }
		 
		 //LOANS MANAGEMENT SUBMENU
		 
		 //Method to loan  a book to a patron
		 public static void loanBook() {
			 boolean foundBook = false;
			 boolean foundPatron = false;
			 String title, author;
			 
			 System.out.println("\n---------------------");
		     System.out.println("*LOAN BOOK*");
		     System.out.println("---------------------");
			 System.out.println("Enter the title of the book: ");
		     title = readLine();
		     System.out.println("Enter the author of the book: ");
		     author = readLine();
		     
		     Book loanBook = null;
		     for (int i = 0; i < books.size(); i++) {
				 if (title.equals(books.get(i).getTitle()) && author.equals(books.get(i).getAuthor())) {
					 foundBook = true;
					 loanBook = books.get(i);
					 break;
				 }
			 }
		     if(!foundBook) {
		    	 System.out.println("The book '" + title + "' by '" + author + "' was not found." );
		    	 return;
		     }
		     if(!loanBook.getAvailability()) { //If the availability of the book is false, it means it has already been loaned out to a patron
		    	 System.out.println("The book '" + title + "' by '" + author + "' has already been loaned out." );
		    	 return;
		     }
		     
		     String patronID; 
		     System.out.println("Enter the patron ID: ");
		     patronID = readLine();
		     Patron loanPatron = null;
		     for (int i = 0; i < patrons.size(); i++) {
				 if (patronID.equals(patrons.get(i).getPatronID())) {
					 foundPatron = true;
					 loanPatron = patrons.get(i);
					 break;
				 }
				 }
		     
		     if(!foundPatron) {
		    	 System.out.println("The patron with patron ID " + patronID + " was not found.");
		    	 return;
		     }
		     
		     
		     // Let the objects apply their own rules rather than reaching in and setting
		     // fields. Availability was checked above, so this cannot fail.
		     loanBook.checkBookOut();
		     loanPatron.borrowBook();
		     
		     Borrows borrow = new Borrows(loanBook, loanPatron);   //A new borrows object is created after a successful loan
		     loans.add(borrow); // kept in memory so a later return can find and close it
		     writeLoansToFile(LOANS_FILE);
		     System.out.println("The book has been successfully loaned to " + loanPatron.getName() +
		             ". The due date is: " + borrow.getDueDate());
		 }
		 
		 
		 //This method is for librarians to return book for patrons
		 //To keep records of the books that have been borrowed, the borrow item will not be removed from the list after the book is returned.
		 public static void returnBorrowedBook() {
			 String title, author;
			 boolean foundBook = false;
			 
			 Book returnBook = null;
			 
			 System.out.println("\n---------------------");
		     System.out.println("*RETURN BOOK*");
		     System.out.println("---------------------");
			 System.out.println("Enter the title of the book: ");
		     title = readLine();
		     System.out.println("Enter the author of the book: ");
		     author = readLine();
			 for (int i = 0; i < books.size(); i++) {
				 if (title.equals(books.get(i).getTitle()) && author.equals(books.get(i).getAuthor())) {
					 foundBook = true;
					 returnBook = books.get(i);
					 break;
				 }
			 }
		     if(!foundBook) {
		    	 System.out.println("The book '" + title + "' by '" + author + "' was not found." );
		    	 return;
		     }
		     
		     String patronID; 
		     boolean foundPatron = false;
		     Patron returnPatron = null;
		     System.out.println("Enter the patron ID: ");
		     patronID = readLine();
		     
		     for (int i = 0; i < patrons.size(); i++) {
				 if (patronID.equals(patrons.get(i).getPatronID())) {
					 foundPatron = true;
					 returnPatron = patrons.get(i);
					 break;
				 }
				 }
		     
		     if(!foundPatron) {
		    	 System.out.println("The patron with patron ID " + patronID + " was not found.");
		    	 return;
		     }
		     
		  
		        if (returnBook.getAvailability()) {
		            System.out.println("This book was already available in the library.");
		            return;
		        }

		        // Check that THIS patron is the one holding THIS book before changing
		        // anything. Without the check any patron could return any book, and the
		        // count was decremented on whoever happened to be typed in: the borrower
		        // stayed credited with the book forever while an unrelated patron went to
		        // -1 books borrowed, and that negative was written to patronList.csv.
		        Borrows loan = findOpenLoan(returnBook);
		        if (loan == null) {
		            System.out.println("'" + returnBook.getTitle() + "' is marked as out, but there is no "
		                    + "loan record for it. Nothing has been changed.");
		            return;
		        }
		        if (loan.getPatron() != returnPatron) {
		            System.out.println("'" + returnBook.getTitle() + "' is on loan to "
		                    + loan.getPatron().getName() + ", not to " + returnPatron.getName()
		                    + ". Nothing has been changed.");
		            return;
		        }

		        returnBook.returnBook(); // Book is now available again
		        loan.setReturnDate(LocalDate.now()); // the loan is now closed, not just forgotten
		        writeLoansToFile(LOANS_FILE);
		        //The number of books borrowed by the patron is reduced by 1 after returning the book.
		        if (!returnPatron.returnBook()) {
		            // The loan record says this patron holds the book but their count says
		            // they hold nothing. Report it rather than driving the count negative.
		            System.out.println("Note: " + returnPatron.getName() + " was recorded as holding "
		                    + "no books, so the borrow count was left at 0.");
		        }
		        System.out.println("The book '" + returnBook.getTitle() + "' has been returned by "
		                + returnPatron.getName());
		 }
		 
		 

		 //Finds a book by its ISBN, or null.
		 private static Book findBookByISBN(String isbn) {
			 for (Book book : books) {
				 if (book.getISBN().equals(isbn)) return book;
			 }
			 return null;
		 }

		 //Finds a patron by ID, or null.
		 private static Patron findPatronByID(String id) {
			 for (Patron patron : patrons) {
				 if (patron.getPatronID().equals(id)) return patron;
			 }
			 return null;
		 }

		 /**
		  * Finds the open loan of this book, whoever holds it, or null if it is not out.
		  */
		 private static Borrows findOpenLoan(Book book) {
			 for (Borrows loan : loans) {
				 if (loan.isOnLoan() && loan.getBook() == book) return loan;
			 }
			 return null;
		 }

		 /**
		  * Reads the loan history, so the program knows who is holding what.
		  *
		  * Must run AFTER books and patrons are loaded, since each record refers to them.
		  * Records that contradict the rest of the data are reported rather than trusted:
		  * the shipped borrowedBooks.csv says patron 1001 has "1984" out, while
		  * booklist.csv marks that book available and patronList.csv credits 1001 with
		  * zero books. Two sources against one, so the loan is treated as stale history.
		  */
		 public static void readLoansFromFile(String loansFile) {
			 File file = new File(loansFile);
			 if (!file.exists()) return; // no history yet is not an error

			 int lineNumber = 0;
			 try (Scanner sc = new Scanner(file)) {
				 while (sc.hasNextLine()) {
					 String line = sc.nextLine();
					 lineNumber++;
					 if (line.trim().isEmpty()) continue;
					 String[] item = Csv.split(line);
					 if (item.length < 4) {
						 System.out.println("Skipping line " + lineNumber + " of " + loansFile
								 + ": expected at least 4 fields, found " + item.length);
						 continue;
					 }
					 Patron patron = findPatronByID(item[0].trim());
					 Book book = findBookByISBN(item[1].trim());
					 if (patron == null || book == null) {
						 System.out.println("Skipping line " + lineNumber + " of " + loansFile
								 + ": unknown " + (patron == null ? "patron " + item[0].trim()
										 : "ISBN " + item[1].trim()));
						 continue;
					 }
					 LocalDate borrowed;
					 LocalDate due;
					 LocalDate returned = null;
					 try {
						 borrowed = LocalDate.parse(item[2].trim());
						 due = LocalDate.parse(item[3].trim());
						 if (item.length > 4 && !item[4].trim().isEmpty()) {
							 returned = LocalDate.parse(item[4].trim());
						 }
					 } catch (DateTimeParseException e) {
						 System.out.println("Skipping line " + lineNumber + " of " + loansFile
								 + ": " + e.getMessage());
						 continue;
					 }

					 if (returned == null && book.getAvailability()) {
						 System.out.println("Note: " + loansFile + " line " + lineNumber
								 + " shows '" + book.getTitle() + "' out to " + patron.getName()
								 + ", but the catalogue marks it available. Treating it as returned.");
						 returned = due;
					 }
					 loans.add(new Borrows(book, patron, borrowed, due, returned));
				 }
			 } catch (FileNotFoundException e) {
				 System.out.println("File not found: " + loansFile);
			 }
		 }

		 /**
		  * Rewrites the whole loan file. A return has to be able to CHANGE an existing
		  * line, which appending alone cannot do.
		  */
		 private static void writeLoansToFile(String loansFile) {
			 try (BufferedWriter writer = new BufferedWriter(new FileWriter(loansFile))) {
				 for (Borrows loan : loans) {
					 writer.write(loan.toCSVFormat());
					 writer.newLine();
				 }
			 } catch (IOException e) {
				 System.out.println("An error occurred while writing the loans to the file.");
				 e.printStackTrace();
			 }
		 }

		 /**
		  * Writes a data file back, unless doing so would replace a file we never read
		  * with an empty one.
		  *
		  * Refusing is the point. Overwriting is right when the file was loaded and the
		  * user has since emptied it -- that is a real edit. It is wrong when the file
		  * was never there, because then the emptiness is our ignorance rather than the
		  * user's intent, and writing it makes the loss permanent.
		  */
		 private static void saveOnExit(String file, boolean wasLoaded, boolean isEmpty, Runnable write) {
			 if (!wasLoaded && isEmpty) {
				 System.out.println("Not writing " + file + ": it could not be read at startup, and "
						 + "there is nothing in memory to write. The existing file, if any, is untouched.");
				 return;
			 }
			 write.run();
		 }

		 //Method to write the books to book list
		 private static void writeBooksToFile(String booksFile, List<Book> books) {
		        try (BufferedWriter writer = new BufferedWriter(new FileWriter(booksFile))) {
		        	for (int i = 0; i < books.size(); i++) {
		                writer.write(Csv.field(books.get(i).getTitle()) + ","
	                        + Csv.field(books.get(i).getAuthor()) + ","
	                        + Csv.field(books.get(i).getISBN()) + ","
	                        + books.get(i).getAvailability());
		                writer.newLine(); // Move to the next line for the next book
		            }
		        } catch (IOException e) {
		            System.out.println("An error occurred while writing the books to the file.");
		            e.printStackTrace();
		        }
		    }
		
		 
		 //Method to write patrons to patron list
		 private static void writePatronsToFile(String patronsFile, List<Patron> patrons) {
		        try (BufferedWriter writer = new BufferedWriter(new FileWriter(patronsFile))) {
		        	for (int i = 0; i < patrons.size(); i++) {
		                writer.write(Csv.field(patrons.get(i).getName()) + ","
	                        + Csv.field(patrons.get(i).getPatronID()) + ","
	                        + patrons.get(i).getBooksBorrowed());
		                writer.newLine(); // Move to the next line for the next book
		            }
		        } catch (IOException e) {
		            System.out.println("An error occurred while writing the patrons to the file.");
		            e.printStackTrace();
		        }
		    }
		 
			public static void main(String[] args) {
				// TODO Auto-generated method stub
				
				readBooksFromFile(BOOKS_FILE, books); //Book data is read from booklist.csv and stored in bookList arrayList
		        readPatronsFromFile(PATRONS_FILE, patrons); //Patron data is read from patronList.csv and stored in patronList
		        readLoansFromFile(LOANS_FILE); //Must come last: each loan refers to a book and a patron

		        int option = displayMainMenu();

			}

		}


	

