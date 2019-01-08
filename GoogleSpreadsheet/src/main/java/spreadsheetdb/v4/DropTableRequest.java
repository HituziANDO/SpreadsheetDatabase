package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.Request;

import java.io.IOException;
import java.util.ArrayList;

public class DropTableRequest {

    interface Callback {

        void onExecuted();
    }

    private final String tableName;
    private final SpreadsheetHandler spreadsheetHandler;
    private final Callback callback;
    private final ArrayList<Request> requests = new ArrayList<>();

    DropTableRequest(String tableName, SpreadsheetHandler spreadsheetHandler, Callback callback) {
        this.tableName = tableName;
        this.spreadsheetHandler = spreadsheetHandler;
        this.callback = callback;

        Integer sheetId = spreadsheetHandler.getSheetId(tableName);

        if (sheetId != null) {
            requests.add(new Request()
                    .setDeleteSheet(new DeleteSheetRequest()
                            .setSheetId(sheetId)));
        }
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

        if (callback != null) {
            callback.onExecuted();
        }
    }
}
