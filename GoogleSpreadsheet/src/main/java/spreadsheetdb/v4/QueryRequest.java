package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryRequest {

    private final Table table;
    private final SpreadsheetHandler spreadsheetHandler;
    private String range;
    private int startRowIndex;

    QueryRequest(Table table, SpreadsheetHandler spreadsheetHandler) {
        this.table = table;
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public QueryRequest all() {
        range = table.getName() + "!A2:" + table.getEndColumnA1Notation();
        startRowIndex = 1; // Skip header

        return this;
    }

    public QueryRequest findByRowIndex(int rowIndex) {
        if (rowIndex < 1) {
            throw new IndexOutOfBoundsException("First data's `rowIndex` is 1. `rowIndex` 0 is header.");
        }

        int row = rowIndex + 1;
        range = table.getName() + "!A" + row + ":" + table.getEndColumnA1Notation() + row;
        startRowIndex = rowIndex;

        return this;
    }

    public List<Record> execute() throws IOException {
        if (range == null || startRowIndex <= 0) {
            return new ArrayList<>();
        }

        ValueRange response = spreadsheetHandler.getService()
                .spreadsheets()
                .values()
                .get(spreadsheetHandler.getSpreadsheetId(), range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Record> records = new ArrayList<>();
        int rowIndex = startRowIndex;

        for (List<Object> rowData : values) {
            // Excludes empty row.
            if (!rowData.isEmpty()) {
                records.add(new Record(rowIndex, rowData));
                rowIndex++;
            }
        }

        return records;
    }
}
