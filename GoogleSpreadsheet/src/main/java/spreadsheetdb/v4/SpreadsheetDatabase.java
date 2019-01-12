package spreadsheetdb.v4;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

public class SpreadsheetDatabase {

    public interface CredentialsProvider {

        InputStream getCredentials();
    }

    private static final Object LOCK_OBJECT = new Object();

    private static String applicationName;
    private static CredentialsProvider credentialsProvider;
    private static HashMap<String, Table> tables = new HashMap<>();

    private SpreadsheetHandler spreadsheetHandler;
    private final Metadata metadata;

    private final CreateTableRequest.Callback createTableRequestCallback = new CreateTableRequest.Callback() {

        @Override
        public void onExecuted() {
            try {
                spreadsheetHandler = new SpreadsheetHandler(spreadsheetHandler, credentialsProvider.getCredentials());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static void init(String applicationName, CredentialsProvider credentialsProvider) {
        SpreadsheetDatabase.applicationName = applicationName;
        SpreadsheetDatabase.credentialsProvider = credentialsProvider;
    }

    public static SpreadsheetDatabase newPersonalDatabase() throws GeneralSecurityException, IOException {
        return newPersonalDatabase(applicationName);
    }

    public static SpreadsheetDatabase newPersonalDatabase(String databaseName) throws GeneralSecurityException, IOException {
        return new SpreadsheetDatabase(true, null, databaseName);
    }

    public static SpreadsheetDatabase getPersonalDatabase(String spreadsheetId) throws GeneralSecurityException, IOException {
        return getPersonalDatabase(spreadsheetId, applicationName);
    }

    public static SpreadsheetDatabase getPersonalDatabase(String spreadsheetId, String databaseName) throws GeneralSecurityException, IOException {
        return new SpreadsheetDatabase(false, spreadsheetId, databaseName);
    }

    private SpreadsheetDatabase(boolean createDb, String spreadsheetId, String databaseName) throws GeneralSecurityException, IOException {
        spreadsheetHandler = new SpreadsheetHandler(spreadsheetId, applicationName, databaseName, credentialsProvider.getCredentials());
        metadata = Metadata.newInstance(createDb, applicationName, spreadsheetHandler, createTableRequestCallback);
    }

    public static String getApplicationName() {
        return applicationName;
    }

    public String getSpreadsheetId() {
        return spreadsheetHandler.getSpreadsheetId();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public CreateTableRequest createTableRequest(String tableName, List<Object> columns) {
        Table table = new Table(tableName, columns);

        synchronized (LOCK_OBJECT) {
            tables.put(tableName, table);
        }

        return new CreateTableRequest(tableName, columns, spreadsheetHandler, createTableRequestCallback);
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
