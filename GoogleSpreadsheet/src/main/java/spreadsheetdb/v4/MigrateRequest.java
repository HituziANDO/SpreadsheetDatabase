package spreadsheetdb.v4;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.List;

public class MigrateRequest extends BatchUpdateRequest {

    public interface MigrationListener {

        List<Record> onMigrate(Table table, List<Object> newColumns, List<Record> oldRecords);
    }

    interface Callback {

        void onExecuted();
    }

    private final List<Object> newColumns;
    private final BatchUpdateRequest metadataUpdateRequest;
    private final Callback callback;

    MigrateRequest(BatchUpdateRequest metadataUpdateRequest,
                   Table table,
                   List<Object> newColumns,
                   SpreadsheetHandler spreadsheetHandler,
                   Callback callback) {

        super(table, spreadsheetHandler);

        this.newColumns = newColumns;
        this.metadataUpdateRequest = metadataUpdateRequest;
        this.callback = callback;
    }

    @Override
    public MigrateRequest insert(Record record) {
        // No operation.
        return this;
    }

    @Override
    public BatchUpdateRequest update(Record record) {
        // No operation if header row is not set.
        if (requests.isEmpty()) {
            return this;
        }

        return super.update(record);
    }

    @Override
    List<Object> getColumns() {
        return newColumns;
    }

    @Override
    public void execute() throws IOException {
        execute(null);
    }

    public void execute(MigrationListener migrationListener) throws IOException {
        if (sheetId == null) {
            return;
        }

        List<Record> records = new QueryRequest(table, spreadsheetHandler).all().execute();

        if (migrationListener != null) {
            records = migrationListener.onMigrate(table, newColumns, records);
        }

        new TruncateRequest(table, spreadsheetHandler).execute();

        // Insert header row.
        GridRange rowRange = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(0)
                .setEndRowIndex(1)
                .setStartColumnIndex(0)
                .setEndColumnIndex(newColumns.size());

        requests.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setRange(rowRange)
                        .setFields("*")
                        .setRows(convertToSingleRowData(newColumns))));

        // Insert new values.
        for (Record record : records) {
            update(record);
        }

        // Update metadata for schema version.
        requests.addAll(metadataUpdateRequest.requests);

        super.execute();

        if (callback != null) {
            callback.onExecuted();
        }
    }
}
