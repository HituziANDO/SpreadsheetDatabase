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
    private static final HashMap<String, SpreadsheetDatabase> spreadsheetDatabases = new HashMap<>();

    private SpreadsheetHandler spreadsheetHandler;
    private final CredentialsProvider credentialsProvider;
    private final Metadata metadata;
    private final HashMap<String, Table> tables = new HashMap<>();

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

    public static SpreadsheetDatabase newPersonalDatabase(String applicationName,
                                                          CredentialsProvider credentialsProvider) throws GeneralSecurityException, IOException {

        return newPersonalDatabase(applicationName, applicationName, credentialsProvider);
    }

    public static SpreadsheetDatabase newPersonalDatabase(String applicationName,
                                                          String databaseName,
                                                          CredentialsProvider credentialsProvider) throws GeneralSecurityException, IOException {

        SpreadsheetDatabase db;

        if (spreadsheetDatabases.containsKey(databaseName)) {
            db = spreadsheetDatabases.get(databaseName);
        } else {
            db = new SpreadsheetDatabase(null, applicationName, databaseName, credentialsProvider);
            synchronized (LOCK_OBJECT) {
                spreadsheetDatabases.put(databaseName, db);
            }
        }

        return db;
    }

    public static SpreadsheetDatabase getPersonalDatabase(String spreadsheetId,
                                                          String applicationName,
                                                          CredentialsProvider credentialsProvider) throws GeneralSecurityException, IOException {

        return getPersonalDatabase(spreadsheetId, applicationName, applicationName, credentialsProvider);
    }

    public static SpreadsheetDatabase getPersonalDatabase(String spreadsheetId,
                                                          String applicationName,
                                                          String databaseName,
                                                          CredentialsProvider credentialsProvider) throws GeneralSecurityException, IOException {

        SpreadsheetDatabase db;

        if (spreadsheetDatabases.containsKey(databaseName)) {
            db = spreadsheetDatabases.get(databaseName);
        } else {
            db = new SpreadsheetDatabase(spreadsheetId, applicationName, databaseName, credentialsProvider);
            synchronized (LOCK_OBJECT) {
                spreadsheetDatabases.put(databaseName, db);
            }
        }

        return db;
    }

    private SpreadsheetDatabase(String spreadsheetId,
                                String applicationName,
                                String databaseName,
                                CredentialsProvider credentialsProvider) throws GeneralSecurityException, IOException {

        this.credentialsProvider = credentialsProvider;
        spreadsheetHandler = new SpreadsheetHandler(spreadsheetId, applicationName, databaseName, credentialsProvider.getCredentials());
        metadata = Metadata.newInstance(spreadsheetId == null, applicationName, spreadsheetHandler, createTableRequestCallback);
    }

    public String getSpreadsheetId() {
        return spreadsheetHandler.getSpreadsheetId();
    }

    public String getDatabaseName() {
        return spreadsheetHandler.getSpreadsheetTitle();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public CreateTableRequest createTableRequest(String tableName, List<Object> columns) {
        Table table = new Table(tableName, columns);
        tables.put(tableName, table);

        return new CreateTableRequest(tableName, columns, spreadsheetHandler, createTableRequestCallback);
    }

    public DropTableRequest dropTableRequest(String tableName) {
        return new DropTableRequest(tableName, spreadsheetHandler, new DropTableRequest.Callback() {

            @Override
            public void onExecuted() {
                tables.remove(tableName);
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

    public MigrateRequest migrateRequest(String tableName, List<Object> newColumns, int newSchemaVersion) {
        Metadata newMetadata = new Metadata(metadata);
        newMetadata.setSchemaVersion(newSchemaVersion);

        return new MigrateRequest(newMetadata.updateRequest(), getTable(tableName), newColumns, spreadsheetHandler, new MigrateRequest.Callback() {

            @Override
            public void onExecuted() {
                metadata.setSchemaVersion(newSchemaVersion);

                Table table = new Table(tableName, newColumns);
                tables.put(tableName, table);
            }
        });
    }
}
