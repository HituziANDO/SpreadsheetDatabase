package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;

public class ChangeTableRequest {

    private final Table table;
    private final SpreadsheetHandler spreadsheetHandler;
    private final ArrayList<Request> requests = new ArrayList<>();

    ChangeTableRequest(SpreadsheetHandler spreadsheetHandler) {
        // The target sheet is Sheet1 initial created.
        this(null, spreadsheetHandler);
    }

    ChangeTableRequest(Table table, SpreadsheetHandler spreadsheetHandler) {
        this.table = table;
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public ChangeTableRequest changeTableName(String newName) {
        if (table != null && table.getName().equals(newName)) {
            // Not change.
            return this;
        }

        SheetProperties sheetProperties = new SheetProperties()
                .setTitle(newName);

        // When sheetId is not set, Sheet1 initial created is default.
        if (table != null) {
            Integer sheetId = spreadsheetHandler.getSheetId(table.getName());

            if (sheetId != null) {
                sheetProperties.setSheetId(sheetId);
            }
        }

        requests.add(new Request()
                .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                        .setProperties(sheetProperties)
                        .setFields("title")));

        return this;
    }

    public void execute() throws IOException {
        if (requests.isEmpty()) {
            return;
        }

        BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);

        BatchUpdateSpreadsheetResponse response = spreadsheetHandler.getService()
                .spreadsheets()
                .batchUpdate(spreadsheetHandler.getSpreadsheetId(), request)
                .execute();
    }
}
