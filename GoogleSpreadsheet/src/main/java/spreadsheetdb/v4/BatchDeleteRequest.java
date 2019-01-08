package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BatchDeleteRequest {

    private final Table table;
    private final SpreadsheetHandler spreadsheetHandler;
    private List<Record> records;

    BatchDeleteRequest(Table table, SpreadsheetHandler spreadsheetHandler) {
        this.table = table;
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public BatchDeleteRequest setRecords(List<Record> records) {
        this.records = records;

        return this;
    }

    public void execute() throws IOException {
        if (records == null || records.isEmpty()) {
            return;
        }

        Integer sheetId = spreadsheetHandler.getSheetId(table.getName());

        if (sheetId != null) {
            // Deletes a row from last row of given records.
            this.records.sort(new Comparator<Record>() {

                @Override
                public int compare(Record record1, Record record2) {
                    if (record1.getRowIndex() == record2.getRowIndex()) {
                        return 0;
                    }

                    return record1.getRowIndex() > record2.getRowIndex() ? -1 : 1;
                }
            });

            ArrayList<Request> requests = new ArrayList<>();

            for (Record record : records) {
                requests.add(new Request()
                        .setDeleteDimension(new DeleteDimensionRequest()
                                .setRange(new DimensionRange()
                                        .setDimension(SpreadsheetHandler.Dimension.ROWS)
                                        .setSheetId(sheetId)
                                        .setStartIndex(record.getRowIndex())
                                        .setEndIndex(record.getRowIndex() + 1))));
            }

            BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            BatchUpdateSpreadsheetResponse response = spreadsheetHandler.getService()
                    .spreadsheets()
                    .batchUpdate(spreadsheetHandler.getSpreadsheetId(), request)
                    .execute();
        }
    }
}
