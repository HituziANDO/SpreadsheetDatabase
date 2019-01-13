package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchUpdateRequest {

    final Table table;
    final Integer sheetId;
    final SpreadsheetHandler spreadsheetHandler;
    final ArrayList<Request> requests = new ArrayList<>();

    BatchUpdateRequest(Table table, SpreadsheetHandler spreadsheetHandler) {
        this.table = table;
        this.sheetId = spreadsheetHandler.getSheetId(table.getName());
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public BatchUpdateRequest insert(Record record) {
        requests.add(new Request()
                .setAppendCells(new AppendCellsRequest()
                        .setSheetId(sheetId)
                        .setFields("*")
                        .setRows(convertToSingleRowData(record.getValues()))));

        return this;
    }

    public BatchUpdateRequest update(Record record) {
        GridRange rowRange = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(record.getRowIndex())
                .setEndRowIndex(record.getRowIndex() + 1)
                .setStartColumnIndex(0)
                .setEndColumnIndex(getColumns().size());

        requests.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setRange(rowRange)
                        .setFields("*")
                        .setRows(convertToSingleRowData(record.getValues()))));

        return this;
    }

    public void execute() throws IOException {
        BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);

        BatchUpdateSpreadsheetResponse response = spreadsheetHandler.getService()
                .spreadsheets()
                .batchUpdate(spreadsheetHandler.getSpreadsheetId(), request)
                .execute();
    }

    List<Object> getColumns() {
        return table.getColumns();
    }

    List<RowData> convertToSingleRowData(List<Object> values) {
        ArrayList<CellData> cellDataList = new ArrayList<>();

        for (Object value : values) {
            cellDataList.add(new CellData()
                    .setUserEnteredValue(new ExtendedValue()
                            .setStringValue(value.toString())));
        }

        List<RowData> rowDataList = new ArrayList<>();
        rowDataList.add(new RowData()
                .setValues(cellDataList));

        return rowDataList;
    }
}
