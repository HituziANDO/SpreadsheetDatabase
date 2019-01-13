# SpreadsheetDatabase

***SpreadsheetDatabase is a simple database using [Google Sheets API v4](https://developers.google.com/sheets/api/).***

## Install

1. Sets up your project with reference to [Java Quickstart: Step1-Step2](https://developers.google.com/sheets/api/quickstart/java)
1. Puts `SpreadsheetDatabaseJava/src/main/java/spreadsheetdb/v4/*` in your project

## Usage

1. Create Database
	
	```java
	private static final String APPLICATION_NAME = "Your App Name";
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	private static final SpreadsheetDatabase.CredentialsProvider CREDENTIALS_PROVIDER = new SpreadsheetDatabase.CredentialsProvider() {
	
	    @Override
	    public InputStream getCredentials() {
	        return Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	    }
	};

	public static void main(String... args) throws GeneralSecurityException, IOException {
	    SpreadsheetDatabase db = SpreadsheetDatabase.newPersonalDatabase(APPLICATION_NAME, CREDENTIALS_PROVIDER);
	    
	    ...
   }
	```
	
1. Create Table If Not Exist
	
	```java
	db.createTableRequest("member", Arrays.asList("name", "country", "address1", "tel")).execute();
	```
	
1. Insert
	
	```java
	db.updateRequest("member")
	    .insert(new Record(Arrays.asList("Aiko", "Japan", "Tokyo", 111)))
	    .insert(new Record(Arrays.asList("Bob", "USA", "New York", 222)))
	    .execute();
	```
	
1. Select All
	
	```java
	List<Record> records = db.queryRequest("member").all().execute();
	```
	
1. Find By Row Index
	
	```java
	Record record1 = db.queryRequest("member").findByRowIndex(1).execute().get(0);
	```
		
1. Update
	
	```java
	record1.setValues(Arrays.asList("Akko", "Japan", "Kyoto", 333));
	db.updateRequest("member").update(record1).execute();
	```
	
1. Delete
	
	```java
	db.deleteRequest("member")
	    .setRecords(Arrays.asList(new Record(1)))
	    .execute();
	```
	
1. Truncate
	
	```java
	db.truncateRequest("member").execute();
	```
	
1. Drop Table
	
	```java
	db.dropTableRequest("member").execute();
	```
	
1. Migrate
	
	```java
	int newSchemaVersion = 2;
	
	if (db.getMetadata().getSchemaVersion() < newSchemaVersion) {
	    db.migrateRequest("member", Arrays.asList("name", "country", "address1", "tel", "score"), newSchemaVersion).execute(new MigrateRequest.MigrationListener() {
	        
	        @Override
	        public List<Record> onMigrate(Table table, List<Object> newColumns, List<Record> oldRecords) {
	            for (Record record : oldRecords) {
	                record.setValues(Arrays.asList(
	                    record.getString(table.getColumnIndex("name")),
	                    record.getString(table.getColumnIndex("country")),
	                    record.getString(table.getColumnIndex("address1")),
	                    record.getInt(table.getColumnIndex("tel")),
	                    99.9    // Set default value
	                ));
	            }
	
	            // Return new values
	            return oldRecords;
	        }
	    });
	}
	```
	
More info, see my [sample code](https://github.com/HituziANDO/GoogleDriveSpreadsheet/blob/master/SpreadsheetDatabaseJava/src/main/java/Main.java).
