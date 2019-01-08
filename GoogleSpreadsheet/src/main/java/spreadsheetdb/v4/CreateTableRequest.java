package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateTableRequest {

    private final String tableName;
    private final List<Object> columns;
    private final SpreadsheetHandler spreadsheetHandler;
    private final ArrayList<Request> requests = new ArrayList<>();

    CreateTableRequest(String tableName, List<Object> columns, SpreadsheetHandler spreadsheetHandler) {
        this.tableName = tableName;
        this.columns = columns;
        this.spreadsheetHandler = spreadsheetHandler;

        if (spreadsheetHandler.getSheetId(tableName) == null) {
            requests.add(new Request()
                    .setAddSheet(new AddSheetRequest()
                            .setProperties(new SheetProperties()
                                    .setTitle(tableName))));
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

        // Insert header row
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(columns));
        String range = tableName + "!A1:" + Table.Grid.columnA1Notation(columns.size()) + "1";

        UpdateValuesResponse valuesResponse = spreadsheetHandler.getService()
                .spreadsheets()
                .values()
                .update(spreadsheetHandler.getSpreadsheetId(), range, body)
                .setValueInputOption(SpreadsheetHandler.ValueInputOption.RAW)
                .execute();

        // TODO: serviceを再生成しないとspreadsheets()に追加分が更新されない(?)
    }
}
