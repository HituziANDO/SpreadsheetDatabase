import spreadsheetdb.v4.Record;
import spreadsheetdb.v4.SpreadsheetDatabase;
import spreadsheetdb.v4.Table;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final String APPLICATION_NAME = "GoogleSpreadsheetDB";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public static void main(String... args) throws GeneralSecurityException, IOException {
        SpreadsheetDatabase.init(APPLICATION_NAME);

        SpreadsheetDatabase db = SpreadsheetDatabase.newPersonalDatabase(Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH));
//        SpreadsheetDatabase db = SpreadsheetDatabase.getPersonalDatabase("Your Spreadsheet ID",
//                Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH));

        System.out.println("Spreadsheet ID: " + db.getSpreadsheetId());

        // TODO: Migration

        // Create table if it not exists
        db.createTableRequest("member", Arrays.asList("name", "country", "address1", "tel")).execute();

        // Insert
        db.updateRequest("member")
                .insert(new Record(Arrays.asList("Aiko", "Japan", "Tokyo", 111)))
                .insert(new Record(Arrays.asList("Bob", "USA", "New York", 222)))
                .insert(new Record(Arrays.asList("Chris", "USA", "LA", 333)))
                .insert(new Record(Arrays.asList("Dan", "Japan", "Fukuoka", 444)))
                .execute();

        // Select
        List<Record> records = db.queryRequest("member").all().execute();

        Table memberTable = db.getTable("member");

        for (Record record : records) {
            System.out.printf("%s, %s, %s, %d\n",
                    record.getString(memberTable.getColumnIndex("name")),
                    record.getString(memberTable.getColumnIndex("address1")),
                    record.getString(memberTable.getColumnIndex("country")),
                    record.getInt(memberTable.getColumnIndex("tel")));
        }

        Record record1 = records.get(0);
        record1.setValues(Arrays.asList("Akko", "Japan", "Kyoto", 999));

        // Update
        db.updateRequest("member").update(record1).execute();

        // Delete
        db.deleteRequest("member")
                .setRecords(Arrays.asList(records.get(1), records.get(3)))
                .execute();

        // Truncate
//        db.truncateRequest("member").execute();

        db.createTableRequest("class", Arrays.asList("name", "number")).execute();

        // Drop table
        db.dropTableRequest("class").execute();
    }
}
