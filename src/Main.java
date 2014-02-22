import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.ServiceException;

import jp.hituzi.gdata.GoogleDrive;
import jp.hituzi.gdata.Spreadsheet;


public class Main {

	/**
	 * @param args
	 * @throws ServiceException 
	 * @throws IOException 
	 */
	public static void main (String[] args) throws IOException, ServiceException {
		String appName = "jp.hituzi.gdata-Sample-1";
		String spreadsheetName = "jp_hituzi_gdata_Sampe";
		
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		System.out.print("gmail address > ");
		String userName = br.readLine();
		System.out.print("password > ");
		String password = br.readLine();
		
		System.out.println("Access to Google Drive...");
		
		GoogleDrive drive = new GoogleDrive(appName, userName, password);
		Spreadsheet spreadsheet = drive.getSpreadsheet();
		if (drive.createSpreadsheetFile(spreadsheetName, false)) {
			spreadsheet.chooseSpreadsheetByName(spreadsheetName);
			spreadsheet.chooseDefaultWorksheet();
			// Insert a header row.
			spreadsheet.changeCells("A1:C1", new String[] { "col1", "col2", "col3" }, true);
//			saveSpreadsheetKey(spreadsheet.getSpreadsheetKey());
		} else {
			spreadsheet.chooseSpreadsheetByName(spreadsheetName);
//			String spreadsheetKey = loadSpreadsheetKey();
//			spreadsheet.chooseSpreadsheetByKey(spreadsheetKey);
			spreadsheet.chooseDefaultWorksheet();
		}
		
		System.out.println("SpreadsheetName=" + spreadsheet.getSpreadsheetName());
		System.out.println("SpreadsheetKey=" + spreadsheet.getSpreadsheetKey());
		
		System.out.println("Insert");
		
		Map<String, String> value1 = new HashMap<String, String>();
		value1.put("col1", "hoge");
		value1.put("col2", "foo");
		value1.put("col3", "bar");
		spreadsheet.insert(value1);
		Map<String, String> value2 = new HashMap<String, String>();
		value2.put("col1", "apple");
		value2.put("col2", "orange");
		value2.put("col3", "banana");
		spreadsheet.insert(value2);
		
		printListEntries(spreadsheet.getEntries());
		
		System.out.println("Update");
		
		Map<String, String> value3 = new HashMap<String, String>();
		value3.put("col1", "grape");
		int count = spreadsheet.update(value3, "col1=apple");
		
		System.out.println("Updated! " + count);
		
		printListEntries(spreadsheet.getEntries());
		
		System.out.println("Select");
		
		List<ListEntry> list = spreadsheet.select("col1=grape");
		printListEntries(list);
		
		System.out.println("Delete");
		
		count = spreadsheet.delete("col1=hoge");
		
		System.out.println("Deleted! " + count);
		
		printListEntries(spreadsheet.getEntries());
	}

//	private static void saveSpreadsheetKey (String key) {
//		// TODO
//	}

//	private static String loadSpreadsheetKey () {
//		// TODO
//		return null;
//	}

	private static void printListEntries (List<ListEntry> entries) throws IOException, ServiceException {
		for (ListEntry entry : entries) {
			CustomElementCollection elem = entry.getCustomElements();
			System.out.println("col1=" + elem.getValue("col1"));
			System.out.println("col2=" + elem.getValue("col2"));
			System.out.println("col3=" + elem.getValue("col3"));
		}
	}

}
