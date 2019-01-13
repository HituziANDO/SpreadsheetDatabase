package spreadsheetdb.v4;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Metadata extends Table {

    private static final String TABLE_NAME = "#meta";
    private static final String COLUMN_NAME_KEY = "key";
    private static final String COLUMN_NAME_VALUE = "value";

    private final String applicationName;
    private final SpreadsheetHandler spreadsheetHandler;
    private String description;
    private int schemaVersion = 1;

    static Metadata newInstance(boolean createDb,
                                String applicationName,
                                SpreadsheetHandler spreadsheetHandler,
                                CreateTableRequest.Callback callback) throws IOException {

        Metadata metadata = new Metadata(applicationName, spreadsheetHandler);

        if (spreadsheetHandler.getSheetId(TABLE_NAME) == null) {
            // If the meta table is not found, renames Sheet1's title.
            if (!createDb || !spreadsheetHandler.hasSheet(0)) {
                new ChangeTableRequest(spreadsheetHandler).changeTableName(TABLE_NAME)
                        .execute();
            } else {
                // If default sheet is deleted, creates new sheet for the meta table.
                new CreateTableRequest(TABLE_NAME, metadata.getColumns(), spreadsheetHandler, callback)
                        .execute();
            }
        }

        List<Record> records = new QueryRequest(metadata, spreadsheetHandler).all().execute();

        try {
            metadata.setDescription(records.get(1).getString(metadata.getColumnIndex(COLUMN_NAME_VALUE)));
            metadata.setSchemaVersion(records.get(2).getInt(metadata.getColumnIndex(COLUMN_NAME_VALUE)));
        } catch (Exception e) {
        }

        return metadata;
    }

    private Metadata(String applicationName, SpreadsheetHandler spreadsheetHandler) {
        super(TABLE_NAME, Arrays.asList(COLUMN_NAME_KEY, COLUMN_NAME_VALUE));

        this.applicationName = applicationName;
        this.spreadsheetHandler = spreadsheetHandler;
    }

    Metadata(Metadata metadata) {
        super(metadata.getName(), metadata.getColumns());

        this.applicationName = metadata.applicationName;
        this.spreadsheetHandler = metadata.spreadsheetHandler;
        this.description = metadata.description;
        this.schemaVersion = metadata.schemaVersion;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void update() throws IOException {
        updateRequest().execute();
    }

    BatchUpdateRequest updateRequest() {
        return new BatchUpdateRequest(this, spreadsheetHandler)
                .update(new Record(0, getColumns()))
                .update(new Record(1, Arrays.asList("applicationName", applicationName)))
                .update(new Record(2, Arrays.asList("description", description)))
                .update(new Record(3, Arrays.asList("schemaVersion", schemaVersion)));
    }
}
