package spreadsheetdb.v4;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SpreadsheetDatabase {

    private static final int META_TABLE_COLUMN_NAME = 1;
    private static final int META_TABLE_COLUMN_LAST_ROW_NUM = 2;
    private static final Object LOCK_OBJECT = new Object();

    private static String applicationName;
    private static HashMap<String, Table> tables = new HashMap<>();

    private final SpreadsheetHandler spreadsheetHandler;
    private final Table metaTable;

    public static void init(String applicationName) {
        if (SpreadsheetDatabase.applicationName == null) {
            SpreadsheetDatabase.applicationName = applicationName;
        }
    }

    public static SpreadsheetDatabase newPersonalDatabase(InputStream credentialsFile) throws GeneralSecurityException, IOException {

        return newPersonalDatabase(applicationName, credentialsFile);
    }

    public static SpreadsheetDatabase newPersonalDatabase(String databaseName,
                                                          InputStream credentialsFile) throws GeneralSecurityException, IOException {

        return new SpreadsheetDatabase(null, databaseName, credentialsFile);
    }

    public static SpreadsheetDatabase getPersonalDatabase(String spreadsheetId,
                                                          InputStream credentialsFile) throws GeneralSecurityException, IOException {

        return getPersonalDatabase(spreadsheetId, applicationName, credentialsFile);
    }

    public static SpreadsheetDatabase getPersonalDatabase(String spreadsheetId,
                                                          String databaseName,
                                                          InputStream credentialsFile) throws GeneralSecurityException, IOException {

        return new SpreadsheetDatabase(spreadsheetId, databaseName, credentialsFile);
    }

    private SpreadsheetDatabase(String spreadsheetId,
                                String databaseName,
                                InputStream credentialsFile) throws GeneralSecurityException, IOException {

        spreadsheetHandler = new SpreadsheetHandler(spreadsheetId, applicationName, databaseName, credentialsFile);
        metaTable = new Table("#meta", Arrays.asList("key", "value"));

        if (spreadsheetHandler.getSheetId(metaTable.getName()) == null) {
            // Rename Sheet1's title.
            new ChangeTableRequest(spreadsheetHandler).changeTableName(metaTable.getName()).execute();
            // TODO: If Sheet1 is deleted
            // Create the meta table if it does not exist.
//            new CreateTableRequest(metaTable.getName(), metaTable.getColumns(), spreadsheetHandler).execute();
            // TODO: Put meta data
        }
    }

    public static String getApplicationName() {
        return applicationName;
    }

    public String getSpreadsheetId() {
        return spreadsheetHandler.getSpreadsheetId();
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public CreateTableRequest createTableRequest(String tableName, List<Object> columns) {
        Table table = new Table(tableName, columns);

        synchronized (LOCK_OBJECT) {
            tables.put(tableName, table);
        }

        return new CreateTableRequest(tableName, columns, spreadsheetHandler);
    }

    public DropTableRequest dropTableRequest(String tableName) {
        return new DropTableRequest(tableName, spreadsheetHandler, new DropTableRequest.Callback() {

            @Override
            public void onExecuted() {
                synchronized (LOCK_OBJECT) {
                    tables.remove(tableName);
                }
            }
        });
    }

    public QueryRequest queryRequest(String tableName) {
        return new QueryRequest(getTable(tableName), spreadsheetHandler);
    }

    public BatchUpdateRequest updateRequest(String tableName) {
        return new BatchUpdateRequest(getTable(tableName), spreadsheetHandler);
    }

    public BatchDeleteRequest deleteRequest(String tableName) {
        return new BatchDeleteRequest(getTable(tableName), spreadsheetHandler);
    }

    public TruncateRequest truncateRequest(String tableName) {
        return new TruncateRequest(getTable(tableName), spreadsheetHandler);
    }
}
