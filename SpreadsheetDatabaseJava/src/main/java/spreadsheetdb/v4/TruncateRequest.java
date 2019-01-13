package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;

import java.io.IOException;

public class TruncateRequest {

    private final Table table;
    private final SpreadsheetHandler spreadsheetHandler;

    TruncateRequest(Table table, SpreadsheetHandler spreadsheetHandler) {
        this.table = table;
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public void execute() throws IOException {
        String range = table.getName() + "!A2:" + table.getEndColumnA1Notation();

        ClearValuesResponse response = spreadsheetHandler.getService()
                .spreadsheets()
                .values()
                .clear(spreadsheetHandler.getSpreadsheetId(), range, new ClearValuesRequest())
                .execute();
    }
}
