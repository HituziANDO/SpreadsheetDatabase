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
        Table memberTable = db.createTable("member", Arrays.asList("name", "country", "address1", "tel"))
                .getTable("member");

        // Insert
        Table.BatchUpdate batchInsert = memberTable.batchUpdate()
                .insert(new Table.Record(Arrays.asList("Aiko", "Japan", "Tokyo", 111)))
                .insert(new Table.Record(Arrays.asList("Bob", "USA", "New York", 222)))
                .insert(new Table.Record(Arrays.asList("Chris", "USA", "LA", 333)))
                .insert(new Table.Record(Arrays.asList("Dan", "Japan", "Fukuoka", 444)));
        memberTable.execute(batchInsert);

        // Select
        List<Table.Record> records = memberTable.selectAll();

        for (Table.Record record : records) {
            System.out.printf("%s, %s, %s, %d\n",
                    record.get(memberTable.getColumnIndex("name")),
                    record.get(memberTable.getColumnIndex("address1")),
                    record.get(memberTable.getColumnIndex("country")),
                    record.getInt(memberTable.getColumnIndex("tel")));
        }

        Table.Record record1 = records.get(0);
        record1.setValues(Arrays.asList("Akko", "Japan", "Kyoto", 999));

        // Update
        Table.BatchUpdate batchUpdate = memberTable.batchUpdate().update(record1);
        memberTable.execute(batchUpdate);

        // Delete
        Table.BatchDelete batchDelete = memberTable.batchDelete().delete(records.get(2));
        memberTable.execute(batchDelete);

        for (Table.Record record : records) {
            System.out.printf("%s, %s, %s, %d\n",
                    record.get(memberTable.getColumnIndex("name")),
                    record.get(memberTable.getColumnIndex("address1")),
                    record.get(memberTable.getColumnIndex("country")),
                    record.getInt(memberTable.getColumnIndex("tel")));
        }

        // Truncate
//        memberTable.truncate();

        // Drop table
        db.createTable("class", Arrays.asList("name", "number"));
        db.dropTable("class");
    }
}
