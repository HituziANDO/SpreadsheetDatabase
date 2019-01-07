package spreadsheetdb.v4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {

    public static class Record {

        private final int rowNumber;
        private List<Object> values;

        public Record(int rowNumber) {
            this(rowNumber, new ArrayList<Object>());
        }

        public Record(List<Object> values) {
            this(0, values);
        }

        public Record(int rowNumber, List<Object> values) {
            this.rowNumber = rowNumber;
            this.values = values;
        }

        public int getRowNumber() {
            return rowNumber;
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
    }

    public static class BatchUpdate {

        private final int sheetId;
        private final int columnSize;
        private final SpreadsheetHandler.BatchUpdateRequest batchRequest = new SpreadsheetHandler.BatchUpdateRequest();

        public BatchUpdate(int sheetId, int columnSize) {
            this.sheetId = sheetId;
            this.columnSize = columnSize;
        }

        public BatchUpdate insert(Record record) {
            batchRequest.appendSingleRowData(sheetId, record.getValues());

            return this;
        }

        public BatchUpdate update(Record record) {
            batchRequest.updateSingleRowData(sheetId,
                    record.getRowNumber() - 1,
                    columnSize,
                    record.getValues());

            return this;
        }
    }

    public static class BatchDelete {

        private final String tableName;
        private final String endColumn;
        private final SpreadsheetHandler.BatchClearRequest batchRequest = new SpreadsheetHandler.BatchClearRequest();

        public BatchDelete(String tableName, String endColumn) {
            this.tableName = tableName;
            this.endColumn = endColumn;
        }

        public BatchDelete delete(Record record) {
            batchRequest.clearSingleRowData(tableName, record.getRowNumber() - 1, endColumn);

            return this;
        }
    }

    private final String name;
    private final List<Object> columns;
    private final SpreadsheetHandler spreadsheetHandler;
    private int lastRowNumber = 0;

    public Table(String name, List<Object> columns, SpreadsheetHandler spreadsheetHandler) {
        this.name = name;
        this.columns = columns;
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public Table(String name, List<Object> columns, SpreadsheetHandler spreadsheetHandler, int lastRowNumber) {
        this(name, columns, spreadsheetHandler);

        this.lastRowNumber = lastRowNumber;
    }

    public long getLastRowNumber() {
        return lastRowNumber;
    }

    public List<Object> getColumns() {
        return columns;
    }

    public int getColumnIndex(String column) {
        return columns.indexOf(column);
    }

    public BatchUpdate batchUpdate() {
        return new BatchUpdate(spreadsheetHandler.getSheetId(name), columns.size());
    }

    public BatchDelete batchDelete() {
        return new BatchDelete(name, SheetGrid.columnString(columns.size()));
    }

    Table create() throws IOException {
        spreadsheetHandler.createSheet(name);

        List<List<Object>> headers = Collections.singletonList(columns);
        spreadsheetHandler.updateValues(name, "A1", headers);

        lastRowNumber = 1;

        return this;
    }

    void drop() throws IOException {
        spreadsheetHandler.deleteSheet(name);
    }

    public List<Record> selectAll() throws IOException {
        ArrayList<Record> records = new ArrayList<Record>();
        String range = "A2:" + SheetGrid.columnString(columns.size());
        int rowNumber = 2; // skip header

        for (List<Object> values : spreadsheetHandler.getValues(name, range)) {
            // Excludes empty row.
            if (!values.isEmpty()) {
                records.add(new Record(rowNumber, values));
                rowNumber++;
            }
        }

        return records;
    }

    public Table insert(Record record) throws IOException {
        lastRowNumber++;
        spreadsheetHandler.appendValues(name, Collections.singletonList(record.getValues()));

        return this;
    }

    public Table update(Record record) throws IOException {
        spreadsheetHandler.updateValues(name, "A" + record.getRowNumber(), Collections.singletonList(record.getValues()));

        return this;
    }

    public Table delete(Record record) throws IOException {
        String range = "A" + record.getRowNumber() + ":" + SheetGrid.columnString(columns.size()) + record.getRowNumber();
        spreadsheetHandler.clearValues(name, range);

        ArrayList<List<Object>> values = new ArrayList<>();

        for (Record r : selectAll()) {
            values.add(r.getValues());
        }

        truncate();
        spreadsheetHandler.updateValues(name, "A2:" + SheetGrid.columnString(columns.size()), values);

        return this;
    }

    public Table truncate() throws IOException {
        String range = "A2:" + SheetGrid.columnString(columns.size());
        spreadsheetHandler.clearValues(name, range);

        return this;
    }

    public Table execute(BatchUpdate batchUpdate) throws IOException {
        spreadsheetHandler.execute(batchUpdate.batchRequest);

        return this;
    }

    public Table execute(BatchDelete batchDelete) throws IOException {
        spreadsheetHandler.execute(batchDelete.batchRequest);

        ArrayList<List<Object>> values = new ArrayList<>();

        for (Record r : selectAll()) {
            values.add(r.getValues());
        }

        truncate();
        spreadsheetHandler.updateValues(name, "A2:" + SheetGrid.columnString(columns.size()), values);

        return this;
    }
}
