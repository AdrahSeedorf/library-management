package library;

import java.util.ArrayList;
import java.util.List;

/**
 * Just enough CSV for this project's three data files.
 *
 * Lives on its own so that both the manager and the model classes can use it
 * without the model having to reach back into the front end for it.
 *
 * @author Seedorf Obeng-Mireku
 */
public final class Csv {

	private Csv() { } // utility class

	/**
	 * Splits one CSV line, honouring double-quoted fields.
	 *
	 * A plain split(",") cannot survive a comma inside a value, and the program
	 * lets you type one: adding "Dune, Part Two" wrote five fields, and reading it
	 * back gave author=" Part Two", isbn="Frank Herbert" and availability=false.
	 * No error was reported, because the line still had four or more fields -- the
	 * book stayed in the catalogue, quietly wrong, which is worse than losing it.
	 *
	 * Deliberately small rather than a full RFC 4180 parser: it handles quoted
	 * fields and doubled quotes inside them, which is what this data needs, and it
	 * does not pretend to handle newlines inside a field.
	 */
	public static String[] split(String line) {
		List<String> fields = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (quoted) {
				if (c == '"') {
					// A doubled quote inside a quoted field is one literal quote.
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
						current.append('"');
						i++;
					} else {
						quoted = false;
					}
				} else {
					current.append(c);
				}
			} else if (c == '"') {
				quoted = true;
			} else if (c == ',') {
				fields.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		fields.add(current.toString().trim());
		return fields.toArray(new String[0]);
	}

	/** Quotes a value for CSV, but only when it needs it. */
	public static String field(String value) {
		if (value == null) return "";
		if (value.indexOf(',') < 0 && value.indexOf('"') < 0) return value;
		return '"' + value.replace("\"", "\"\"") + '"';
	}
}
