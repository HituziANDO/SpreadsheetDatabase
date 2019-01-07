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
    private static HashMap<String, Table> tables = new HashMap<String, Table>();

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
        metaTable = new Table("#meta", Arrays.asList("key", "value"), spreadsheetHandler).create();
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

    public SpreadsheetDatabase createTable(String tableName, List<Object> columns) throws IOException {
        for (Table.Record row : metaTable.selectAll()) {
            // If exists...
            if (row.get(META_TABLE_COLUMN_NAME).equals(tableName)) {
                Table table = new Table(tableName, columns, spreadsheetHandler,
                        Integer.parseInt(row.get(META_TABLE_COLUMN_LAST_ROW_NUM).toString()));

                synchronized (LOCK_OBJECT) {
                    tables.put(tableName, table);
                }

                return this;
            }
        }

        Table table = new Table(tableName, columns, spreadsheetHandler).create();

        synchronized (LOCK_OBJECT) {
            tables.put(tableName, table);
        }

        return this;
    }

    public SpreadsheetDatabase dropTable(String tableName) throws IOException {
        synchronized (LOCK_OBJECT) {
            Table table = tables.remove(tableName);

            if (table != null) {
                table.drop();
            }
        }

        return this;
    }
}
