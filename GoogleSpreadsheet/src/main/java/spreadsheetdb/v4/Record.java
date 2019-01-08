package spreadsheetdb.v4;

import java.util.ArrayList;
import java.util.List;

public class Record {

    private final int rowIndex;
    private List<Object> values;

    public Record(int rowIndex) {
        this(rowIndex, new ArrayList<>());
    }

    public Record(List<Object> values) {
        this(0, values);
    }

    public Record(int rowIndex, List<Object> values) {
        this.rowIndex = rowIndex;
        this.values = values;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public Object get(int columnIndex) {
        return values.get(columnIndex);
    }

    public int getInt(int columnIndex) {
        return Integer.parseInt(get(columnIndex).toString());
    }

    public String getString(int columnIndex) {
        return (String) values.get(columnIndex);
    }
}
