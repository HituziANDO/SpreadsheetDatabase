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
import java.util.Collections;
import java.util.List;

class SpreadsheetHandler {

    /**
     * @see "https://developers.google.com/sheets/api/guides/values#writing"
     */
    public interface ValueInputOption {

        String RAW = "RAW";
        String USER_ENTERED = "USER_ENTERED";
    }

    /**
     * @see "https://developers.google.com/sheets/api/reference/rest/v4/Dimension"
     */
    public interface Dimension {

        String ROWS = "ROWS";
        String COLUMNS = "COLUMNS";
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
    private final String spreadsheetTitle;

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
        this.spreadsheetTitle = spreadsheetTitle;
    }

    public SpreadsheetHandler(SpreadsheetHandler spreadsheetHandler, InputStream credentialsFile) throws GeneralSecurityException, IOException {
        this(spreadsheetHandler.getSpreadsheetId(),
                spreadsheetHandler.getService().getApplicationName(),
                spreadsheetHandler.getSpreadsheetTitle(),
                credentialsFile);
    }

    public Sheets getService() {
        return service;
    }

    public String getSpreadsheetId() {
        return spreadsheet.getSpreadsheetId();
    }

    public String getSpreadsheetTitle() {
        return spreadsheetTitle;
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

    public boolean hasSheet(int sheetId) {
        List<Sheet> sheets = spreadsheet.getSheets();

        if (sheets == null) {
            return false;
        }

        for (Sheet sheet : sheets) {
            SheetProperties properties = sheet.getProperties();

            if (properties != null && properties.getSheetId() == sheetId) {
                return true;
            }
        }

        return false;
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
