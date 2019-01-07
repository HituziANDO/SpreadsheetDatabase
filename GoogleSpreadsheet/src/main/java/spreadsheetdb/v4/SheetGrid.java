package spreadsheetdb.v4;

import java.util.Locale;

public class SheetGrid {

    /**
     * Character code (Decimal)
     */
    private static final int A = 65;

    /**
     * @param str A column string: A-ZZ.
     * @return A number of given column string. e.g.) "A"=1, "Z"=26, "AA"=27
     * @throws IllegalArgumentException
     */
    public static int columnNumber(String str) {
        str = str.toUpperCase(Locale.US);

        if (!str.matches("[A-Z][A-Z]|[A-Z]")) {
            throw new IllegalArgumentException("The argument is [A-Z] or [A-Z][A-Z] only.");
        }

        char[] arr = str.toCharArray();

        if (arr.length == 2) {
            int x = (Integer.valueOf(Integer.toString(arr[0], 10))    // Convert to Character code
                    - A + 1) * 26;
            int y = Integer.valueOf(Integer.toString(arr[1], 10))    // Convert to Character code
                    - A;

            return x + y + 1;
        } else {
            return Integer.valueOf(Integer.toString(arr[0], 10))    // Convert to Character code
                    - A + 1;
        }
    }

    /**
     * @param number A number of a column.
     * @return A Column string.
     */
    public static String columnString(int number) {
        return Character.toString((char) (A + number - 1));
    }
}
