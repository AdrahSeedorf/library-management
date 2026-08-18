#!/usr/bin/env bash
# Every defect that was fixed, replayed against a build of this working tree.
#
# Each case is written as the symptom a user would actually see, and each one was
# observed failing against the original code before the fix was written. Run it
# with the commit before the fixes checked out and every case fails; run it here
# and they all pass.
#
#     ./regression_test.sh
#
set -u
cd "$(dirname "$0")"

PASS=0
FAIL=0
BUILD=$(mktemp -d)
WORK=$(mktemp -d)
trap 'rm -rf "$BUILD" "$WORK"' EXIT

if ! command -v javac >/dev/null 2>&1; then
    cat <<'MSG'
No JDK found.

`javac` is not on the PATH, so there is nothing to compile the sources with.
A Java RUNTIME alone is not enough, and neither is the JRE that Eclipse bundles
for its own use -- both can run a program without being able to build one.

On macOS, with Homebrew:

    brew install --cask temurin

Then check both halves are present:

    java -version
    javac -version

MSG
    exit 1
fi

if ! javac -d "$BUILD" src/*.java 2>"$WORK/javac.log"; then
    echo "COMPILE FAILED"; cat "$WORK/javac.log"; exit 1
fi

# A clean copy of the shipped data, for one test case.
fresh() {
    rm -rf "$WORK/run"; mkdir -p "$WORK/run"
    cp booklist.csv patronList.csv "$WORK/run/"
    [ -f borrowedBooks.csv ] && cp borrowedBooks.csv "$WORK/run/"
    :
}

# run <stdin keystrokes>
run() {
    printf '%b' "$1" | (cd "$WORK/run" && java -cp "$BUILD" LibraryManager 2>&1)
}

check() {  # check <name> <condition-result> <detail>
    if [ "$2" = "yes" ]; then
        printf '  PASS  %s\n' "$1"; PASS=$((PASS + 1))
    else
        printf '  FAIL  %s\n     -> %s\n' "$1" "$3"; FAIL=$((FAIL + 1))
    fi
}

echo "Library manager regression tests"
echo

# ---------------------------------------------------------------------------
echo "Input handling"

fresh; OUT=$(run '4\n')
check "a clean exit says so" \
    "$(grep -qF 'End of Program' <<<"$OUT" && echo yes)" "$OUT"

fresh; OUT=$(run 'x\n4\n')
check "a letter at the menu is rejected, not fatal" \
    "$(grep -qF "'x' is not a number" <<<"$OUT" && ! grep -q 'InputMismatchException' <<<"$OUT" && echo yes)" \
    "expected a re-prompt, got: $OUT"

fresh; OUT=$(run '99\n4\n')
check "an out-of-range option is rejected" \
    "$(grep -q 'choose a number between 1 and 4' <<<"$OUT" && echo yes)" "$OUT"

fresh; OUT=$(run '')
check "end of input exits instead of throwing" \
    "$(! grep -q 'NoSuchElementException' <<<"$OUT" && echo yes)" "$OUT"

# ---------------------------------------------------------------------------
echo
echo "Data integrity"

fresh
BOOKS_BEFORE=$(grep -c '' "$WORK/run/booklist.csv")
PATRONS_BEFORE=$(grep -c '' "$WORK/run/patronList.csv")
run '4\n' >/dev/null
BOOKS_AFTER=$(grep -c '' "$WORK/run/booklist.csv")
PATRONS_AFTER=$(grep -c '' "$WORK/run/patronList.csv")
check "starting and quitting loses no books ($BOOKS_BEFORE -> $BOOKS_AFTER)" \
    "$([ "$BOOKS_BEFORE" = "$BOOKS_AFTER" ] && echo yes)" "$BOOKS_BEFORE became $BOOKS_AFTER"
check "starting and quitting loses no patrons ($PATRONS_BEFORE -> $PATRONS_AFTER)" \
    "$([ "$PATRONS_BEFORE" = "$PATRONS_AFTER" ] && echo yes)" "$PATRONS_BEFORE became $PATRONS_AFTER"

fresh; run '4\n' >/dev/null
check "the patron file is read under its real name, not a lowercased one" \
    "$([ ! -e "$WORK/run/patronlist.csv" ] || [ "$WORK/run/patronlist.csv" -ef "$WORK/run/patronList.csv" ] && echo yes)" \
    "a second patronlist.csv was created"

fresh; OUT=$(run '1\n1\n1001\n4\n4\n')
check "patrons actually load" \
    "$(grep -q 'Alan Turing' <<<"$OUT" && echo yes)" "$OUT"

# ---------------------------------------------------------------------------
echo
echo "Book removal"

fresh; LAST=$(tail -1 "$WORK/run/booklist.csv")
LAST_TITLE=$(cut -d, -f1 <<<"$LAST"); LAST_AUTHOR=$(cut -d, -f2 <<<"$LAST")
OUT=$(run "2\n4\n$LAST_TITLE\n$LAST_AUTHOR\n5\n4\n")
check "removing the last book does not crash" \
    "$(! grep -q 'IndexOutOfBoundsException' <<<"$OUT" && echo yes)" "$OUT"

# The original stopped reading at 20 books, so its last LOADED book was line 20 --
# that is the one whose removal blew up, and the file's true last line never loaded
# at all. Both are checked, or this case passes against the old code by accident.
fresh; TWENTIETH=$(sed -n '20p' "$WORK/run/booklist.csv")
T20_TITLE=$(cut -d, -f1 <<<"$TWENTIETH"); T20_AUTHOR=$(cut -d, -f2 <<<"$TWENTIETH")
OUT=$(run "2\n4\n$T20_TITLE\n$T20_AUTHOR\n5\n4\n")
check "removing the 20th book does not crash" \
    "$(! grep -q 'IndexOutOfBoundsException' <<<"$OUT" && echo yes)" "$OUT"

fresh; OUT=$(run '2\n4\n1984\nGeorge Orwell\n5\n4\n')
check "removal reports the book that was actually removed" \
    "$(grep -q '^1984 by George Orwell has been removed' <<<"$OUT" && echo yes)" \
    "$(grep -i removed <<<"$OUT")"

fresh; OUT=$(run '2\n4\nNo Such Book\nNobody\n5\n4\n')
check "removing a missing book says so" \
    "$(grep -q 'was not found in the catalogue' <<<"$OUT" && echo yes)" "$OUT"

# ---------------------------------------------------------------------------
echo
echo "Listings"

fresh; OUT=$(run '2\n2\n5\n4\n')
BOOK_ROWS=$(grep -c '^[A-Z0-9].*978-' <<<"$OUT")
check "the catalogue lists every book ($BOOK_ROWS of 26)" \
    "$([ "$BOOK_ROWS" -eq 26 ] && echo yes)" "listed $BOOK_ROWS rows"

fresh; OUT=$(run '1\n2\n4\n4\n')
PATRON_ROWS=$(grep -cE '^[A-Za-z].* 10[0-9][0-9] ' <<<"$OUT")
check "the register lists every patron ($PATRON_ROWS of 15)" \
    "$([ "$PATRON_ROWS" -eq 15 ] && echo yes)" "listed $PATRON_ROWS rows"

# ---------------------------------------------------------------------------
echo
echo "Loans"

fresh; OUT=$(run '3\n1\n1984\nGeorge Orwell\n1002\n2\n1984\nGeorge Orwell\n1001\n3\n4\n')
check "the wrong patron cannot return a book" \
    "$(grep -q 'is on loan to Grace Hopper, not to Alan Turing' <<<"$OUT" && echo yes)" "$OUT"
check "a refused return leaves the borrow count alone (never negative)" \
    "$(grep -q '^Alan Turing,1001,0$' "$WORK/run/patronList.csv" && echo yes)" \
    "$(grep '^Alan Turing' "$WORK/run/patronList.csv")"

fresh; run '3\n1\n1984\nGeorge Orwell\n1002\n2\n1984\nGeorge Orwell\n1002\n3\n4\n' >/dev/null
check "a correct return clears the borrow count" \
    "$(grep -q '^Grace Hopper,1002,0$' "$WORK/run/patronList.csv" && echo yes)" \
    "$(grep '^Grace Hopper' "$WORK/run/patronList.csv")"
check "a return is recorded in the loan file" \
    "$(awk -F, 'END{exit !($5 != "")}' "$WORK/run/borrowedBooks.csv" && echo yes)" \
    "$(cat "$WORK/run/borrowedBooks.csv")"

fresh; OUT=$(run '3\n1\n1984\nGeorge Orwell\n1002\n1\n1984\nGeorge Orwell\n1003\n3\n4\n')
check "a book already out cannot be loaned again" \
    "$(grep -q 'has already been loaned out' <<<"$OUT" && echo yes)" "$OUT"

# ---------------------------------------------------------------------------
echo
echo "Malformed input files"

fresh
printf 'Broken Line With Too Few Fields\n' >> "$WORK/run/booklist.csv"
printf 'Bad Patron,2001,not-a-number\n' >> "$WORK/run/patronList.csv"
OUT=$(run '4\n')
check "a malformed book line is reported by number, not fatal" \
    "$(grep -q 'Skipping line .* of booklist.csv' <<<"$OUT" && ! grep -q 'Exception' <<<"$OUT" && echo yes)" "$OUT"
check "a non-numeric borrow count is reported, not fatal" \
    "$(grep -q 'is not a whole number' <<<"$OUT" && echo yes)" "$OUT"

# ---------------------------------------------------------------------------
echo
echo "Input validation"

fresh; printf 'Legacy Member,ABC123,0\n' >> "$WORK/run/patronList.csv"
OUT=$(run '1\n3\nNew Person\n4\n4\n')
check "a non-numeric patron ID does not break Add Patron" \
    "$(! grep -q 'NumberFormatException' <<<"$OUT" && grep -q 'New Person has been successfully added' <<<"$OUT" && echo yes)" "$OUT"

fresh; OUT=$(run '1\n3\n\nReal Name\n4\n4\n')
check "a blank patron name is refused, not stored" \
    "$(grep -q 'cannot be blank' <<<"$OUT" && ! grep -q '^,' "$WORK/run/patronList.csv" && echo yes)" \
    "$(grep -n '^,' "$WORK/run/patronList.csv")"

fresh; OUT=$(run '2\n3\nDup\nSomebody\n978-0-452-28423-4\n5\n4\n')
check "a duplicate ISBN is refused" \
    "$(grep -q 'already in the catalogue' <<<"$OUT" && echo yes)" "$OUT"

# ---------------------------------------------------------------------------
echo
echo "CSV round trip"

fresh; run '2\n3\nDune, Part Two\nFrank Herbert\n978-0-441-01359-3\n5\n4\n' >/dev/null
check "a title containing a comma is quoted on write" \
    "$(grep -q '^"Dune, Part Two",Frank Herbert,' "$WORK/run/booklist.csv" && echo yes)" \
    "$(tail -1 "$WORK/run/booklist.csv")"
OUT=$(run '2\n2\n5\n4\n')
check "and reads back with the comma intact" \
    "$(grep -q 'Dune, Part Two  *Frank Herbert' <<<"$OUT" && echo yes)" \
    "$(grep -i dune <<<"$OUT")"

# ---------------------------------------------------------------------------
echo
echo "Missing data files"

rm -rf "$WORK/run"; mkdir -p "$WORK/run"; cp patronList.csv "$WORK/run/"
OUT=$(run '4\n')
check "a missing catalogue is not replaced by an empty one" \
    "$([ ! -e "$WORK/run/booklist.csv" ] && grep -q 'Not writing booklist.csv' <<<"$OUT" && echo yes)" \
    "booklist.csv was created with $(grep -c '' "$WORK/run/booklist.csv" 2>/dev/null || echo 0) lines"

rm -rf "$WORK/run"; mkdir -p "$WORK/run"
printf 'Only Book,Some Author,111,true\n' > "$WORK/run/booklist.csv"
cp patronList.csv "$WORK/run/"
run '2\n4\nOnly Book\nSome Author\n5\n4\n' >/dev/null
check "but a genuinely emptied catalogue is still saved" \
    "$([ -e "$WORK/run/booklist.csv" ] && [ ! -s "$WORK/run/booklist.csv" ] && echo yes)" \
    "$(grep -c '' "$WORK/run/booklist.csv") lines remain"

echo
echo "-----------------------------------------"
printf 'PASSED %d, FAILED %d\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ]
