package jp.hituzi.gdata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * 
 * @author Hituzi ANDO
 * @version 1.0.1
 *
 */
public class GoogleDrive {

	private URL GOOGLE_DRIVE_FEED_URL;
	private DocsService service;
	private Spreadsheet spreadsheet;

	public GoogleDrive (String appName, String userName, String password) throws AuthenticationException, MalformedURLException {
		service = new DocsService(appName);
		service.setUserCredentials(userName, password);
		GOOGLE_DRIVE_FEED_URL = new URL("https://docs.google.com/feeds/default/private/full/");
		spreadsheet = new Spreadsheet(appName, userName, password);
	}

	public Spreadsheet getSpreadsheet () {
		return spreadsheet;
	}
	/**
	 * @param spreadsheetName
	 * @param allowDuplication When you specify true, a new file is created even if specified name is equal to partial name of existing files.
	 * @return true If making a new spreadsheet is successful. Otherwise false.
	 * @throws IOException
	 * @throws ServiceException
	 */
	public boolean createSpreadsheetFile (String spreadsheetName, boolean allowDuplication) throws IOException, ServiceException {
		if (allowDuplication) {
			DocumentListEntry entry = new SpreadsheetEntry();
			entry.setTitle(new PlainTextConstruct(spreadsheetName));
			entry = service.insert(GOOGLE_DRIVE_FEED_URL, entry);
			return true;
		} else {
			SpreadsheetQuery query = new SpreadsheetQuery(FeedURLFactory.getDefault().getSpreadsheetsFeedUrl());
			query.setTitleQuery(spreadsheetName);
			SpreadsheetFeed feed = spreadsheet.getService().query(query, SpreadsheetFeed.class);
			for (com.google.gdata.data.spreadsheet.SpreadsheetEntry entry : feed.getEntries()) {
				if (spreadsheetName.equals(entry.getTitle().getPlainText())) {
					return false;
				}
			}
			DocumentListEntry entry = new SpreadsheetEntry();
			entry.setTitle(new PlainTextConstruct(spreadsheetName));
			entry = service.insert(GOOGLE_DRIVE_FEED_URL, entry);
			return true;
		}
	}

	public void uploadFile (String fileName, String filePath, String mimeType) throws IOException, ServiceException {
		DocumentListEntry entry = new DocumentListEntry();
		entry.setTitle(new PlainTextConstruct(fileName));
		entry.setFile(new File(filePath), mimeType);
		entry = service.insert(GOOGLE_DRIVE_FEED_URL, entry);
	}

}
