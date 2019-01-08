package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.BatchClearValuesRequest;
import com.google.api.services.sheets.v4.model.BatchClearValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchDeleteRequest {

    private final Table table;
    private final String endColumn;
    private final SpreadsheetHandler spreadsheetHandler;
    private final ArrayList<String> ranges = new ArrayList<>();

    BatchDeleteRequest(Table table, SpreadsheetHandler spreadsheetHandler) {
        this.table = table;
        this.endColumn = table.getEndColumnA1Notation();
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public BatchDeleteRequest delete(Record record) {
        int row = record.getRowIndex() + 1;
        ranges.add(table.getName() + "!" + "A" + row + ":" + endColumn + row);

        return this;
    }

    public void execute() throws IOException {
        BatchClearValuesRequest request = new BatchClearValuesRequest()
                .setRanges(ranges);

        BatchClearValuesResponse response = spreadsheetHandler.getService()
                .spreadsheets()
                .values()
                .batchClear(spreadsheetHandler.getSpreadsheetId(), request)
                .execute();

        // Gets current all data excluding empty row.
        List<Record> records = new QueryRequest(table, spreadsheetHandler).all().execute();
        ArrayList<List<Object>> values = new ArrayList<>();

        for (Record r : records) {
            values.add(r.getValues());
        }

        // Deletes all data.
        new TruncateRequest(table, spreadsheetHandler).execute();

        // Writes all data.
        ValueRange body = new ValueRange()
                .setValues(values);

        UpdateValuesResponse valuesResponse = spreadsheetHandler.getService()
                .spreadsheets()
                .values()
                .update(spreadsheetHandler.getSpreadsheetId(), table.getName() + "!A2:" + endColumn, body)
                .setValueInputOption(SpreadsheetHandler.ValueInputOption.RAW)
                .execute();
    }
}
