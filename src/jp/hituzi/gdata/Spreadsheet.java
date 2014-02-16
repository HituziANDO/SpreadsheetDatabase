package jp.hituzi.gdata;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
/**
 * 
 * @author Hituzi ANDO
 *
 */
public class Spreadsheet implements Serializable {

	private static final long serialVersionUID = 1L;

	private SpreadsheetService service;
	private SpreadsheetEntry spreadsheetEntry;
	private WorksheetEntry worksheetEntry;

	public static Spreadsheet newInstanceByName (
			String appName,
			String userName,
			String password,
			String spreadsheetName) throws IOException, ServiceException {
		Spreadsheet ss = new Spreadsheet(appName, userName, password);
		ss.chooseSpreadsheetByName(spreadsheetName);
		ss.chooseDefaultWorksheet();
		return ss;
	}

	public static Spreadsheet newInstanceByKey (
			String appName,
			String userName,
			String password,
			String spreadsheetKey) throws MalformedURLException, IOException, ServiceException {
		Spreadsheet ss = new Spreadsheet(appName, userName, password);
		ss.chooseSpreadsheetByKey(spreadsheetKey);
		ss.chooseDefaultWorksheet();
		return ss;
	}

	public Spreadsheet (String appName, String userName, String password) throws AuthenticationException {
		service = new SpreadsheetService(appName);
		service.setUserCredentials(userName, password);
	}

	public SpreadsheetService getService () {
		return service;
	}

	public Spreadsheet chooseSpreadsheetByName (String name) throws IOException, ServiceException {
		FeedURLFactory urlFactory = FeedURLFactory.getDefault();
		SpreadsheetQuery query = new SpreadsheetQuery(urlFactory.getSpreadsheetsFeedUrl());
		query.setTitleQuery(name);
		SpreadsheetFeed feed = service.query(query, SpreadsheetFeed.class);
		spreadsheetEntry = feed.getEntries().get(0);
		return this;
	}

	public Spreadsheet chooseSpreadsheetByKey (String key) throws MalformedURLException, IOException, ServiceException {
		spreadsheetEntry = service.getEntry(
				new URL("http://spreadsheets.google.com/feeds/spreadsheets/" + key), SpreadsheetEntry.class);
		return this;
	}

	public String getSpreadsheetName () {
		return spreadsheetEntry.getTitle().getPlainText();
	}

	public String getSpreadsheetKey () {
		return spreadsheetEntry.getKey();
	}

	public Spreadsheet chooseDefaultWorksheet () throws IOException, ServiceException {
		worksheetEntry = spreadsheetEntry.getDefaultWorksheet();
		return this;
	}

	public Spreadsheet chooseWorksheet (String name) throws IOException, ServiceException {
		WorksheetQuery query = new WorksheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
		query.setTitleQuery(name);
		WorksheetFeed feed = spreadsheetEntry.getService().query(query, WorksheetFeed.class);
		worksheetEntry = feed.getEntries().get(0);
		return this;
	}

	public List<ListEntry> getEntries () throws IOException, ServiceException {
		URL url = worksheetEntry.getListFeedUrl();
		ListFeed feed = service.getFeed(url, ListFeed.class);
		return feed.getEntries();
	}

	public List<ListEntry> select (String where) throws IOException, ServiceException {
		ListQuery query = new ListQuery(worksheetEntry.getListFeedUrl());
		query.setSpreadsheetQuery(where);
		ListFeed feed = service.query(query, ListFeed.class);
		List<ListEntry> list = feed.getEntries();
		return list;
	}

	public void insert (Map<String, String> value) throws IOException, ServiceException {
		ListEntry entry = new ListEntry();
		CustomElementCollection elem = entry.getCustomElements();
		for (String key : value.keySet()) {
			elem.setValueLocal(key, value.get(key));
		}
		service.insert(worksheetEntry.getListFeedUrl(), entry);
	}
	/**
	 * @param value
	 * @param where
	 * @return The number of updated entries.
	 * @throws IOException
	 * @throws ServiceException
	 */
	public int update (Map<String, String> value, String where) throws IOException, ServiceException {
		List<ListEntry> list = select(where);
		for (ListEntry entry : list) {
			CustomElementCollection elem = entry.getCustomElements();
			for (String key : value.keySet()) {
				elem.setValueLocal(key, value.get(key));
			}
			entry.update();
		}
		return list.size();
	}
	/**
	 * @param where e.g.) "column_name=value"
	 * @return The number of deleted entries.
	 * @throws ServiceException 
	 * @throws IOException 
	 */
	public int delete (String where) throws IOException, ServiceException {
		ListQuery query = new ListQuery(worksheetEntry.getListFeedUrl());
		query.setSpreadsheetQuery(where);
		ListFeed feed = service.query(query, ListFeed.class);
		List<ListEntry> list = feed.getEntries();
		for (ListEntry entry : list) {
			entry.delete();
		}
		return list.size();
	}
	/**
	 * @param range e.g.) "A1:C1"
	 * @param values New values of specified range.
	 * @param returnEmpty true if empty cell will be set new value too. Otherwise false.
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void changeCells (String range, String[] values, boolean returnEmpty) throws IOException, ServiceException {
		CellQuery query = new CellQuery(worksheetEntry.getCellFeedUrl());
		query.setRange(range);
		query.setReturnEmpty(returnEmpty);
		CellFeed feed = service.query(query, CellFeed.class);
		List<CellEntry> entries = feed.getEntries();
		for (int i = 0, len = entries.size(); i < len; i++) {
			CellEntry entry = entries.get(i);
			entry.changeInputValueLocal(values[i]);
			entry.update();
		}
	}

}
