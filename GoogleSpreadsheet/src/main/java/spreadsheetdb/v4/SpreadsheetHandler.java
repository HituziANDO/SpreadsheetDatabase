package spreadsheetdb.v4;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SpreadsheetHandler {

    public static class BatchUpdateRequest {

        private final ArrayList<Request> requests = new ArrayList<>();

        public BatchUpdateRequest appendSingleRowData(Integer sheetId, List<Object> values) {
            requests.add(new Request()
                    .setAppendCells(new AppendCellsRequest()
                            .setSheetId(sheetId)
                            .setFields("*")
                            .setRows(convertToSingleRowData(values))));

            return this;
        }

        public BatchUpdateRequest updateSingleRowData(Integer sheetId,
                                                      Integer rowIndex,
                                                      Integer columnSize,
                                                      List<Object> values) {

            requests.add(new Request()
                    .setUpdateCells(new UpdateCellsRequest()
                            .setRange(new GridRange()
                                    .setSheetId(sheetId)
                                    .setStartRowIndex(rowIndex)
                                    .setEndRowIndex(rowIndex + 1)
                                    .setStartColumnIndex(0)
                                    .setEndColumnIndex(columnSize))
                            .setFields("*")
                            .setRows(convertToSingleRowData(values))));

            return this;
        }

        private List<RowData> convertToSingleRowData(List<Object> values) {
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

    public static class BatchClearRequest {

        private final ArrayList<String> ranges = new ArrayList<>();

        public BatchClearRequest clearSingleRowData(String sheetTitle, Integer rowIndex, String endColumn) {
            int row = rowIndex + 1;
            ranges.add(sheetTitle + "!" + "A" + row + ":" + endColumn + row);

            return this;
        }
    }

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this library.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private final Sheets service;
    private final Spreadsheet spreadsheet;

    public SpreadsheetHandler(String spreadsheetId,
                              String applicationName,
                              String spreadsheetTitle,
                              InputStream credentialsFile) throws GeneralSecurityException, IOException {

        // Build a new authorized API client service.
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        service = new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(credentialsFile, httpTransport))
                .setApplicationName(applicationName)
                .build();

        Spreadsheet spreadsheet;

        try {
            if (spreadsheetId != null) {
                spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
            } else {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            spreadsheet = new Spreadsheet()
                    .setProperties(new SpreadsheetProperties()
                            .setTitle(spreadsheetTitle));
            spreadsheet = service.spreadsheets().create(spreadsheet)
                    .setFields("spreadsheetId")
                    .execute();
        }

        this.spreadsheet = spreadsheet;
    }

    public String getSpreadsheetId() {
        return spreadsheet.getSpreadsheetId();
    }

    public Integer getSheetId(String sheetTitle) {
        List<Sheet> sheets = spreadsheet.getSheets();

        if (sheets == null) {
            return null;
        }

        for (Sheet sheet : sheets) {
            SheetProperties properties = sheet.getProperties();

            if (properties != null && sheetTitle.equals(properties.getTitle())) {
                return properties.getSheetId();
            }
        }

        return null;
    }

    public void createSheet(String sheetTitle) throws IOException {
        if (getSheetId(sheetTitle) == null) {
            List<Request> requests = new ArrayList<Request>();
            requests.add(new Request()
                    .setAddSheet(new AddSheetRequest()
                            .setProperties(new SheetProperties()
                                    .setTitle(sheetTitle))));

            BatchUpdateSpreadsheetRequest spreadsheetRequest = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            BatchUpdateSpreadsheetResponse spreadsheetResponse = service.spreadsheets()
                    .batchUpdate(spreadsheet.getSpreadsheetId(), spreadsheetRequest)
                    .execute();
        }
    }

    public void deleteSheet(String sheetTitle) throws IOException {
        Integer sheetId = getSheetId(sheetTitle);

        if (sheetId != null) {
            List<Request> requests = new ArrayList<Request>();
            requests.add(new Request()
                    .setDeleteSheet(new DeleteSheetRequest()
                            .setSheetId(sheetId)));

            BatchUpdateSpreadsheetRequest spreadsheetRequest = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            BatchUpdateSpreadsheetResponse spreadsheetResponse = service.spreadsheets()
                    .batchUpdate(spreadsheet.getSpreadsheetId(), spreadsheetRequest)
                    .execute();
        }
    }

    public void appendValues(String sheetTitle, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange()
                .setValues(values);

        AppendValuesResponse response = service.spreadsheets()
                .values()
                .append(spreadsheet.getSpreadsheetId(), sheetTitle + "!A1", body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

    public void updateValues(String sheetTitle, String cellRange, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange()
                .setValues(values);

        UpdateValuesResponse response = service.spreadsheets()
                .values()
                .update(spreadsheet.getSpreadsheetId(), sheetTitle + "!" + cellRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public List<List<Object>> getValues(String sheetTitle, String cellRange) throws IOException {
        ValueRange response = service.spreadsheets()
                .values()
                .get(spreadsheet.getSpreadsheetId(), sheetTitle + "!" + cellRange)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return new ArrayList<List<Object>>();
        } else {
            return values;
        }
    }

    public void clearValues(String sheetTitle, String cellRange) throws IOException {
        ClearValuesResponse response = service.spreadsheets()
                .values()
                .clear(spreadsheet.getSpreadsheetId(), sheetTitle + "!" + cellRange, new ClearValuesRequest())
                .execute();
    }

    public void execute(BatchUpdateRequest request) throws IOException {
        BatchUpdateSpreadsheetRequest spreadsheetRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(request.requests);

        BatchUpdateSpreadsheetResponse spreadsheetResponse = service.spreadsheets()
                .batchUpdate(spreadsheet.getSpreadsheetId(), spreadsheetRequest)
                .execute();
    }

    public void execute(BatchClearRequest request) throws IOException {
        BatchClearValuesRequest valuesRequest = new BatchClearValuesRequest()
                .setRanges(request.ranges);

        BatchClearValuesResponse valuesResponse = service.spreadsheets()
                .values()
                .batchClear(spreadsheet.getSpreadsheetId(), valuesRequest)
                .execute();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param credentialsFile The credentials.json file.
     * @param httpTransport   The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(InputStream credentialsFile,
                                             final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(credentialsFile));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
