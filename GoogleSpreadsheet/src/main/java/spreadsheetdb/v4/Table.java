package spreadsheetdb.v4;

import java.util.List;
import java.util.Locale;

public class Table {

    public static class Grid {

        /**
         * Character code (Decimal)
         */
        private static final int A = 65;

        /**
         * @param a1 A column A1 notation's alphabet: A-ZZ.
         * @return An index of given column. e.g.) "A"=0, "Z"=25, "AA"=26
         * @throws IllegalArgumentException
         */
        public static int columnIndex(String a1) {
            a1 = a1.toUpperCase(Locale.US);

            if (!a1.matches("[A-Z][A-Z]|[A-Z]")) {
                throw new IllegalArgumentException("The argument is [A-Z] or [A-Z][A-Z] only.");
            }

            char[] arr = a1.toCharArray();

            if (arr.length == 2) {
                int x = (Integer.valueOf(Integer.toString(arr[0], 10))    // Convert to Character code
                        - A + 1) * 26;
                int y = Integer.valueOf(Integer.toString(arr[1], 10))    // Convert to Character code
                        - A;

                return x + y;
            } else {
                return Integer.valueOf(Integer.toString(arr[0], 10))    // Convert to Character code
                        - A;
            }
        }

        /**
         * @param number The number of a column, not index.
         * @return A column A1 notation's alphabet.
         */
        public static String columnA1Notation(int number) {
            return Character.toString((char) (A + number - 1));
        }
    }

    private final String name;
    private final List<Object> columns;

    public Table(String name, List<Object> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<Object> getColumns() {
        return columns;
    }

    public int getColumnIndex(String column) {
        return columns.indexOf(column);
    }

    public String getColumnA1Notation(String column) {
        return Grid.columnA1Notation(getColumnIndex(column) + 1);
    }

    public String getEndColumnA1Notation() {
        return Grid.columnA1Notation(columns.size());
    }
}
