# Library Management System

A console application for managing a small library: a book catalogue, a patron
register, and the loans between them, all persisted to CSV.

Written in Java for a Programming Techniques unit, and since **repaired** — six
defects found by compiling and running it rather than by reading it, each with a
regression test that fails against the original code.

```
==========================================
Programming Techniques Library Management
==========================================
1. Patron Management
2. Book Management
3. Loans Management
4. Exit Program
Select an option (1 - 4):
```

## Running it

Needs a JDK (any version from 11). From the project root:

```bash
javac -d out src/*.java
java -cp out LibraryManager_22053376
```

Run it from the project root — it reads and writes the CSVs in the working
directory. Then run the tests:

```bash
./regression_test.sh
```

## What it does

| Menu | Actions |
|---|---|
| Patron Management | search by ID, list all patrons, add a patron (IDs assigned sequentially) |
| Book Management | check availability, list all books, add a book, remove a book |
| Loans Management | loan a book to a patron, return a borrowed book |

Three CSV files hold the state. `booklist.csv` is title, author, ISBN and
availability; `patronList.csv` is name, ID and current borrow count;
`borrowedBooks.csv` is the loan history — patron, ISBN, borrowed date, due date
and return date, the last of which is empty while the book is still out.

The three are cross-checked against each other on load, and disagreements are
reported rather than believed. The shipped data contains one: `borrowedBooks.csv`
records "1984" as out to patron 1001, while `booklist.csv` marks that book
available and `patronList.csv` credits 1001 with zero books borrowed.

## Defects found and fixed

Each was reproduced against the original code before being fixed, and each has a
test in `regression_test.sh`. Running that suite against the first commit gives
6 passed, 13 failed; against the current code, 21 passed.

**1. A mistyped key killed the program.** Menus read input with
`Scanner.nextInt()`, which throws `InputMismatchException` on anything that is not
a number. Typing a letter — the most likely first mistake anyone makes — ended the
run with a stack trace and lost the session's work. End of input did the same via
`NoSuchElementException`.

**2. Every run deleted data.** The file readers stopped at 20 books and 10
patrons, and the exit path rewrites both files from memory. Starting the program
and immediately choosing "Exit" destroyed six books and five patrons, silently:

```
before: books=26 patrons=15
after:  books=20 patrons=10
```

**3. The patron file was read under the wrong name.** The code asked for
`patronlist.csv`; the file is `patronList.csv`. macOS is case-insensitive by
default so it worked on the machine it was written on, and failed completely
anywhere else — no patrons loaded, every loan refused, and an empty
`patronlist.csv` written out beside the real one.

**4. Removing a book read the list after modifying it.** `books.remove(i)`
followed by `books.get(i)` reads whatever shifted into that slot. Removing any
book but the last reported the *wrong* title as deleted; removing the last one
threw `IndexOutOfBoundsException` and crashed before the catalogue could be
saved.

**5. Anyone could return anyone's book.** The return path never checked who was
holding the book, and the loan file was written but never read, so it could not
have checked. A patron who had borrowed nothing could return someone else's book,
ending with **-1** books borrowed — written straight to `patronList.csv` — while
the real borrower stayed credited with it forever.

**6. Returns were never recorded.** `borrowedBooks.csv` had no field for a return
date, so it could not represent a completed loan. Every book ever borrowed stayed
outstanding and the file only ever grew.

Malformed input lines are now reported by line number and skipped rather than
throwing out of `main`.

## Beyond the repairs

Two changes that were not bug fixes:

**Each rule now lives in one place.** `Book.checkBookOut`, `Book.returnBook`,
`Patron.borrowBook` and `Patron.returnBook` existed but were never called — the
manager re-implemented all four inline, so every loan rule existed twice. The two
copies had drifted, and that drift caused defect 5: the unused `Patron.returnBook`
guarded against a negative count, and the manager's inline copy did not. The
methods used to print to `System.out`, which is why calling them was awkward and
duplicating them was easy; they now change state and return whether it applied,
leaving the caller to decide what to say.

**The catalogue and register can be viewed.** `outputBookCatalogue` and
`outputListOfPatrons` were written, formatted and unreachable — their only call
sites were commented out. A library system with no way to list its books now has
one.

## Structure

| File | Responsibility |
|---|---|
| `LibraryManager_22053376.java` | menus, file I/O, and the operations |
| `Book_22053376.java` | title, author, ISBN, availability |
| `Patron_22053376.java` | name, ID, current borrow count |
| `Borrows_22053376.java` | one loan: book, patron, borrowed/due/return dates |

## Known limitations

Honest about what it is not:

- **Titles and authors containing commas** would break the CSV parsing. None of
  the shipped data does, and quoted-field handling was out of scope.
- **Lookups are by exact title and author**, and are case-sensitive. A real
  system would search on partial matches.
- **`main` is static state.** The catalogue, register and loan list are static
  fields, which is workable at this size but is the first thing to change if the
  project grows.
- **No overdue handling.** Due dates are recorded but nothing acts on them.
