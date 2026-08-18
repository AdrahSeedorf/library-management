# Library Management System

[![build](https://github.com/AdrahSeedorf/library-management/actions/workflows/build.yml/badge.svg)](https://github.com/AdrahSeedorf/library-management/actions/workflows/build.yml)

A console application for managing a small library — a book catalogue, a patron
register, loans, and a reservation queue, persisted to CSV.

It began as a first-year Java assignment. It has since been treated as **legacy
code**: every defect below was found by compiling and running the program, not by
reading it, and each one has a test that fails against the original commit.

```
$ mvn test

           against the original commit:   8 of 36 shell cases passed
           against the current code:     38 of 38 JUnit tests passed
```

That is the interesting part of this repository. The eleven defects are catalogued
below with the evidence for each, and the git history has one commit per fix,
explaining the failure it caused rather than the line it changed.

---

## The defects

Each was reproduced before it was fixed, and each has a regression test.

### 1. A single mistyped key killed the program

Menus read input with `Scanner.nextInt()`, which throws on anything that is not a
number. Typing a letter — the most likely first mistake anyone makes — ended the
run and lost the session's work.

```
Exception in thread "main" java.util.InputMismatchException
    at LibraryManager.displayMainMenu(LibraryManager.java:60)
```

Reaching the end of input did the same thing, so the program could not be driven
from a script or a pipe at all.

### 2. Every run deleted data

The readers stopped at 20 books and 10 patrons; the exit path rewrites both files
from memory. Starting the program and immediately choosing "Exit" destroyed six
books and five patrons, silently:

```
before: books=26 patrons=15
after:  books=20 patrons=10
```

### 3. The patron file was read under the wrong name

The code asked for `patronlist.csv`; the file is `patronList.csv`. macOS is
case-insensitive by default, so it worked on the machine it was written on and
failed completely anywhere else — no patrons loaded, every loan refused, and an
empty `patronlist.csv` written out beside the real one.

### 4. Removing a book read the list after modifying it

`books.remove(i)` followed by `books.get(i)`. Removing any book but the last
reported the **wrong** title as deleted; removing the last one threw
`IndexOutOfBoundsException` and crashed before the catalogue could be saved.

### 5. Anyone could return anyone's book

The return path never checked who held the book, and the loan file was written but
never read — so it could not have checked. A patron who had borrowed nothing could
return someone else's book and end on **-1** books borrowed, written straight to
`patronList.csv`, while the real borrower stayed credited with it forever.

### 6. Returns were never recorded

`borrowedBooks.csv` had no field for a return date, so it could not represent a
completed loan. Every book ever borrowed stayed outstanding and the file only grew.

### 7. Add Patron crashed on a non-numeric patron ID

IDs are stored as text and the loader accepts any, but `addPatron` ran
`Integer.parseInt` over every existing one to find the highest. A single legacy
membership number was fatal.

### 8. A comma in a title silently corrupted the catalogue

The program lets you type one, and `split(",")` cannot survive it. Adding
*Dune, Part Two* wrote five fields and read back with author, ISBN and availability
each shifted one along — with no error, because the line still had enough fields to
look valid. The book stayed in the catalogue, quietly wrong, which is worse than
losing it.

### 9. A missing data file became an empty one

With `booklist.csv` absent the program reported "File not found", carried on with
nothing, and wrote an empty `booklist.csv` on exit — turning a misplaced file into
a lost one.

### 10. Blank names and titles were accepted

Producing records with an empty key that nothing could ever look up again.

### 11. Duplicate ISBNs were accepted

Loans and returns both match on ISBN, so two books sharing one made every loan
ambiguous.

---

## Running it

Needs a JDK 17 or newer and Maven.

```bash
mvn test                                          # 38 tests
mvn package                                       # builds an executable jar
java -jar target/library-management-1.0.0.jar     # run it
```

By default it reads and writes the CSVs in the working directory. Pass a path to
point it somewhere else, which is also how the tests give each case its own
library:

```bash
java -jar target/library-management-1.0.0.jar /path/to/another/library
```

Without Maven, plain javac still works:

```bash
javac -d out $(find src/main/java -name '*.java')
java -cp out library.LibraryManager
```

## What it does

| Menu | Actions |
|---|---|
| Patron Management | search by ID, list all patrons, add a patron |
| Book Management | check availability, list all books, add a book, remove a book |
| Loans Management | loan a book, return a book, view reservations |

Four CSV files hold the state:

| File | Contents |
|---|---|
| `booklist.csv` | title, author, ISBN, availability |
| `patronList.csv` | name, ID, current borrow count |
| `borrowedBooks.csv` | patron, ISBN, borrowed, due, returned — empty while out |
| `reservations.csv` | patron, ISBN, placed on, status |

They are cross-checked against each other on load and disagreements are reported
rather than believed. The shipped data contains one: `borrowedBooks.csv` records
"1984" as out to patron 1001, while `booklist.csv` marks that book available and
`patronList.csv` credits 1001 with zero books.

## Reservations

Asking for a book that is out used to be a dead end. Now it offers a place in a
queue:

```
The book '1984' by 'George Orwell' has already been loaned out.
1 patron(s) are already waiting for it.
Reserve it? Enter a patron ID, or press Enter to skip: 1004
Ada Lovelace is number 2 in the queue for '1984'.
```

and returning the book serves the front of it:

```
The book '1984' has been returned by Grace Hopper
RESERVED: set '1984' aside for Linus Torvalds (patron 1003), who has been
          waiting since 2026-08-18.
  1 other patron(s) still waiting.
```

Two decisions worth naming. A return **announces** the next reservation rather
than auto-loaning it: a hold is a claim on the next copy, not a loan, and issuing
a book to an absent patron would mark it unavailable while it sat on a shelf. And
the queue is ordered by the date placed rather than held in a map — an order that
depended on iteration order would hand the same book to different people on
different runs, and reproducibility is the whole reason a position can be quoted
back to a patron.

## Beyond the repairs

**Each rule now lives in one place.** `Book.checkBookOut`, `Book.returnBook`,
`Patron.borrowBook` and `Patron.returnBook` existed but were never called — the
manager re-implemented all four inline, so every loan rule existed twice. The
copies had drifted, and that drift caused defect 5: the unused `Patron.returnBook`
guarded against a negative count and the manager's inline copy did not. The methods
used to print to `System.out`, which is why calling them was awkward and
duplicating them was easy; they now change state and return whether it applied.

**The catalogue and register can be viewed.** `outputBookCatalogue` and
`outputListOfPatrons` were written, formatted and unreachable — their only call
sites were commented out.

## Structure

| File | Responsibility |
|---|---|
| `LibraryManager.java` | menus, file I/O, and the operations |
| `Book.java` | title, author, ISBN, availability |
| `Patron.java` | name, ID, current borrow count |
| `Borrows.java` | one loan: book, patron, borrowed/due/return dates |
| `Reservation.java` | one place in the queue for one book |
| `Csv.java` | quote-aware splitting and quoting for the data files |

Tests live in `src/test/java/library`. `ConsoleTest` drives the menus the way a
person does and checks what was printed and what landed in the files; `ModelTest`
exercises the rules and the CSV helpers directly. CI runs both on JDK 17 and 21.

## Known limitations

Honest about what it is not:

- **Lookups are by exact title and author**, and are case-sensitive. A real system
  would search on partial matches.
- **`LibraryManager` is doing too much** — menus, persistence and rules in one
  class, with static state. `reset()` and a configurable data directory make it
  testable, but extracting the rules into their own type would let them be tested
  without driving stdin at all.
- **Persistence is a full rewrite on clean exit**, so a crash loses the session.
  Loans and reservations are written as they happen; books and patrons are not.
- **No overdue handling.** Due dates are recorded but nothing acts on them.
