package ch.web.stock;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
@Service
public class StockServiceImpl implements StockService{
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StockServiceImpl.class);
	@Autowired
	private DataSource dataSource;
	@Autowired
	private StockDao stockDao;
	@Autowired
	private DateService dateService;
	@Autowired
	private StockRecordDao stockRecordDao;
	private java.util.Map<String, String> groupMap;
	@Autowired private ch.web.stock.cache.StockCacheService stockCache;
	@javax.annotation.PostConstruct
	public void init(){
		List<StockInfo> stockInfos = stockDao.getStockInfoAll();
		if (stockInfos == null || stockInfos.size() == 0){
			saveStockInfoAll();
			stockInfos = stockDao.getStockInfoAll();
		}
		if (stockInfos != null && stockInfos.size() > 0){
			for (StockInfo stockInfo : stockInfos){
				STOCK_INFO_MAP.put(stockInfo.getSymbol(), stockInfo);
			}
		}
		groupMap = new java.util.HashMap<String, String>();
		java.io.BufferedReader reader = null;
		try {
			reader = new java.io.BufferedReader(new java.io.FileReader("group_map.txt"));
			String line = null;
			while ((line = reader.readLine()) != null){
				String[] strs = line.split(":");
				if (strs != null && strs.length > 1){
					groupMap.put(strs[0], strs[1]);
				}
			}
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null){
				try {
					reader.close();
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	public boolean createTable() {
		Connection con = null;
		try {
			con = dataSource.getConnection();
			con.prepareStatement(SQL_DROP_STOCK).execute();
			con.prepareStatement(SQL_CREATE_STOCK).execute();
			con.prepareStatement(SQL_DROP_STOCK_STATUS).execute();
			con.prepareStatement(SQL_CREATE_STOCK_STATUS).execute();
			con.prepareStatement(SQL_DROP_STOCK_INFO).execute();
			con.prepareStatement(SQL_CREATE_STOCK_INFO).execute();
			con.prepareStatement("update stock_status set date_day=to_date('2015-5-17','YYYY-MM-DD')").execute();
			con.prepareStatement("update stock_status set status=1").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	@Override
	public List<Stock> getStockBySymbolMonthUrl(int symbol, int yearMonth){
		StockInfo stockInfo = STOCK_INFO_MAP.get(symbol);
		if (stockInfo == null){
			stockInfo = updateStockInfoBySymbol(symbol);
			if (stockInfo != null){
				STOCK_INFO_MAP.put(stockInfo.getSymbol(), stockInfo);
			}
		}
		List<Stock> list = null;
		String html = null;
		if (EX_CH_TSE.equals(STOCK_INFO_MAP.get(symbol).getExCh())){
			html = dateService.getHtml(getUrl(symbol, yearMonth), "UTF-8");
			if (html != null){
				list = getStockByHtml(symbol, html);
			}
		} else if (EX_CH_OTC.equals(STOCK_INFO_MAP.get(symbol).getExCh())){
			html = dateService.getHtml(getOtcUrl(symbol, yearMonth), "UTF-8");
			if (html != null){
				list = getOtcStockByHtml(html);
			}				
		}
		if (html != null){
		}
		return list;
	}
	private String getUrl(int symbol, int yearMonth){
		String year = Integer.toString(yearMonth).substring(0, 4);
		String month = Integer.toString(yearMonth).substring(4, 6);
		String symbolNo = symbol < 100 ? "00" + symbol : symbol < 1000 ? "0" + symbol : Integer.toString(symbol);
		return "http://www.twse.com.tw/ch/trading/exchange/STOCK_DAY/genpage/Report" + yearMonth + "/" +
				yearMonth + "_F3_1_8_" + symbolNo + ".php?STK_NO=" + symbolNo + "&myear=" + year + "&mmon=" + month;
	}
	private String getOtcUrl(int symbol, int yearMonth){
		String year = Integer.toString(Integer.parseInt(Integer.toString(yearMonth).substring(0, 4)) - 1911);
		String month = Integer.toString(yearMonth).substring(4, 6);
		return "http://www.tpex.org.tw/web/stock/aftertrading/daily_trading_info/st43_result.php?l=zh-tw&d=" + year + '/' + month +
				"&stkno=" + symbol;
	}
	private List<Stock> getOtcStockByHtml(String html){
		List<Stock> stocks = new ArrayList<Stock>();
		JSONObject jObj = new JSONObject(html);
		int symbol = jObj.getInt("stkNo");
		String name = jObj.getString("stkName");
		JSONArray jArry = jObj.getJSONArray("aaData");
		if (jArry != null && jArry.length() > 0){
			for (int i = 0; i < jArry.length(); i++){
				JSONArray aArry = jArry.getJSONArray(i);
				if (aArry != null && aArry.length() > 0){
					Stock stock = new Stock();
					stock.setSymbol(symbol);
					stock.setDateDay(dateService.getDateDay(aArry.getString(0)));
					stock.setVolume(Long.parseLong(aArry.getString(1).replace(",","")) * 1000);
					stock.setTurnover(Long.parseLong(aArry.getString(2).replace(",","")) * 1000);
					stock.setOpen(Double.parseDouble(aArry.getString(3)));
					stock.setHigh(Double.parseDouble(aArry.getString(4)));
					stock.setLow(Double.parseDouble(aArry.getString(5)));
					stock.setClose(Double.parseDouble(aArry.getString(6)));
					stock.setChange(Double.parseDouble(aArry.getString(7)));
					stock.setCounts(Long.parseLong(aArry.getString(8).replace(",","")));
					stocks.add(stock);
				}
			}
		}
		return stocks;
	}
	private List<Stock> getStockByHtml(int symbol, String html){
		List<Stock> stocks = new ArrayList<Stock>();
		Matcher matcher0 = Pattern.compile("\"board_trad\">(?s).*?</table>").matcher(html);
		Matcher matcher1 = null;
		Matcher matcher2 = null;
		Matcher matcher3 = null;
		while (matcher0.find()) {
			matcher1 = Pattern.compile("<tr bgcolor='#FFFFFF' class='basic2'>(?s).*?<div align='center'>(\\d{2,3}/\\d{2}/\\d{2})</div>(?s).*?</tr>").matcher(matcher0.group(0));
			while (matcher1.find()) {
				Stock stock = new Stock();
				stock.setSymbol(symbol);
				stock.setDateDay(dateService.getDateDay(matcher1.group(1)));
				matcher2 = Pattern.compile("<td height='20' align='right'>(.*?)</td>").matcher(matcher1.group(0));
				int i = 0;
				while (matcher2.find()) {
					String value = matcher2.group(1).replaceAll(",", "");
					switch (i++){
					case 0:
						stock.setVolume(Long.parseLong(value));
						break;
					case 1:
						stock.setTurnover(Long.parseLong(value));
						break;
					case 2:
						stock.setOpen(Double.parseDouble(value));
						break;
					case 3:
						stock.setHigh(Double.parseDouble(value));
						break;
					case 4:
						stock.setLow(Double.parseDouble(value));
						break;
					case 5:
						stock.setClose(Double.parseDouble(value));
						break;
					case 6:
						stock.setChange(Double.parseDouble(value.replaceAll("X", "")));
						break;
					default:
					}
				}
				matcher3 = Pattern.compile("<td height='20' align='right' class='basic2'>(.*?)</td>").matcher(matcher1.group(0));
				i = 0;
				while (matcher3.find()) {
					String value = matcher3.group(1).replaceAll(",", "");
					switch (i++){
					case 0:
						stock.setCounts(Long.parseLong(value));
						break;
					default:
					}
				}
				stocks.add(stock);
			}
		}
		return stocks;
	}
	@Override
	public List<Stock> getStockBySymbolMonthDb(int symbol, int yearMonth){
		return dateService.sortStock(stockDao.getStockBySymbolMonth(symbol, yearMonth));
	}
	@Override
	public List<Stock> getStockBySymbolMonth(int symbol, int yearMonth){
		List<Stock> stockList = null;
		List<StockStatus> statusList = stockDao.getStockStatusBySymbol(symbol);
		int status = 0;
		Date dateDay = null;
		if (statusList != null && statusList.size() > 0){
			for (StockStatus stockStatus : statusList){
				if (stockStatus.getYearMonth() == yearMonth){
					status = stockStatus.getStatus();
					dateDay = stockStatus.getDateDay();
				}
			}
		}
		if (status <= 2){ // <= 2
			long count = 0;
			boolean fromUrl = true;
			boolean updateStatus = false;
			if (dateService.getCurrentYearMonth().after(dateService.getYearMonth(yearMonth))){
				updateStatus = true;
			} else {
				if (dateDay != null && dateService.getCurrentDay().getTime() == dateDay.getTime()){
					if (status == 2){ // <= 2
						fromUrl = false;
					} else {
						System.out.println("getCurrentDay[" + dateDay + "] = " + status);
					}
				}
			}
			if (fromUrl){
				stockList = getStockBySymbolMonthUrl(symbol, yearMonth);
				if (stockList != null && stockList.size() > 0){
					try {
						System.out.println("gggggggggggggggggggg=" + symbol + ':' + yearMonth);
						count = saveStockListTx(stockList);
					} catch (RuntimeException re){
						count = 0;
						re.printStackTrace();
					}
					StockStatus stockStatus = new StockStatus();
					stockStatus.setSymbol(symbol);
					stockStatus.setYearMonth(yearMonth);
					if (count > 0){ // <= 2
						if (updateStatus){
							stockStatus.setStatus(status + 3);
						} else {
							if (getLastDateDay(stockList).getTime() == dateService.getCurrentDay().getTime()){
								stockStatus.setStatus(2);
							} else {
								stockStatus.setStatus(1);
							}
						}
					} else {
						stockStatus.setStatus(0);
					}
//					stockStatus.setStatus(status + (updateStatus && count > 0 ? 1 : 0));
					stockStatus.setDateDay(getLastDateDay(stockList));
					try {
						saveStockStatusTx(stockStatus);
					} catch (RuntimeException re){
						re.printStackTrace();
					}
				}
				stockList = getStockBySymbolMonthDb(symbol, yearMonth);
			} else {
				stockList = getStockBySymbolMonthDb(symbol, yearMonth);
			}
		} else {
			stockList = getStockBySymbolMonthDb(symbol, yearMonth);
		}
		return dateService.sortStock(stockList);
	}
	@Override
	public List<Stock> updateStockBySymbolMonth(StockStatus stockStatus){
		StockStatus mergeStatus = null;
		if (stockStatus != null){
			mergeStatus = new StockStatus();
			mergeStatus.setSymbol(stockStatus.getSymbol());
			mergeStatus.setYearMonth(stockStatus.getYearMonth());
			mergeStatus.setStatus(stockStatus.getStatus());
		}
		if (mergeStatus != null){
			int symbol = mergeStatus.getSymbol();
			int yearMonth = mergeStatus.getYearMonth();
			List<Stock> stockList = getStockBySymbolMonthUrl(symbol, yearMonth);
			if (stockList != null && stockList.size() > 0){ // update-1
				long count = 0;
				try {
					System.out.println("uuuuuuuuuuuuuuuuuuuu=" + symbol + ':' + yearMonth);
					count = saveStockListTx(stockList);
				} catch (RuntimeException re){
					count = 0;
					re.printStackTrace();
				}
				if (count > 0){ // update-2
					mergeStatus.setStatus(mergeStatus.getStatus() + 3); // <= 1
					mergeStatus.setDateDay(getLastDateDay(stockList));
					if (!dateService.getCurrentYearMonth().after(dateService.getYearMonth(mergeStatus.getYearMonth()))){
						dateService.sortStock(stockList);
						if (dateService.getCurrentDay().getTime() == stockList.get(stockList.size() - 1).getDateDay().getTime()){
							mergeStatus.setStatus(2); // <= 1
						} else {
							mergeStatus.setStatus(1); // <= 1
						}
					}
					System.out.println("saveStockStatus[Y] = " + mergeStatus);
					try {
						count = saveStockStatusTx(mergeStatus);
					} catch (RuntimeException re){
						count = 0;
						re.printStackTrace();
					}
					if (count > 0){ // update-3
						stockList = getStockBySymbolMonthDb(symbol, yearMonth);
						if (stockList != null && stockList.size() > 0){ // update-4
							stockStatus.setStatus(mergeStatus.getStatus()); // <= 1
							return dateService.sortStock(stockList);
						}
					}
				}
			}
			mergeStatus.setStatus(0);
			System.out.println("saveStockStatus[N] = " + mergeStatus);
			try {
				saveStockStatusTx(mergeStatus);
			} catch (RuntimeException re){
				re.printStackTrace();
			}
			stockStatus.setStatus(mergeStatus.getStatus());
		}
		return null;
	}
	private Date getLastDateDay(List<Stock> stocks){
		List<Date> dates = new ArrayList<Date>(); 
		if (stocks != null && stocks.size() > 0){
			for (Stock stock : stocks){
				dates.add(stock.getDateDay());
			}
		}
		Collections.sort(dates);
		return dates.size() > 0 ? dates.get(dates.size() - 1) : null;
	}
	@Override
	public long saveStockTx(Stock stock) {
		return stockDao.saveStock(stock) != null ? 1 : 0;
	}
	@Override
	public long saveStockListTx(List<Stock> stocks) {
		long count = 0;
		if (stocks != null && stocks.size() > 0){
			for (Stock stock : stocks){
				count += stockDao.saveStock(stock) != null ? 1 : 0;
			}
		}
		return count;
	}
	@Override
	public List<Stock> getStocksAll(){
		return dateService.sortStock(stockDao.getStockAll());
	}
	@Override
	public List<StockStatus> getStockStatusBySymbol(int symbol) {
		return dateService.sortStockStatus(stockDao.getStockStatusBySymbol(symbol));
	}
	@Override
	public StockStatus getStockStatusBySymbolMonth(int symbol, int yearMonth){
		return stockDao.getStockStatusBySymbolMonth(symbol, yearMonth);
	}
//	@Override
	private long saveStockStatusTx(StockStatus stockStatus) {
		return stockDao.saveStockStatus(stockStatus) != null ? 1 : 0;
	}
//	@Override
	private long saveStockStatusListTx(List<StockStatus> stockStatus) {
		long count = 0;
		if (stockStatus != null && stockStatus.size() > 0){
			for (StockStatus status : stockStatus){
				count += stockDao.saveStockStatus(status) != null ? 1 : 0;
			}
		}
		return count;
	}
	@Override 
	public List<StockStatus> getStockStatusAll(){
		return dateService.sortStockStatus(stockDao.getStockStatusAll());
	}
	@Override 
	public long testSqliteCount() {
		String libraryPath = "C:\\Users\\hsiang\\Desktop\\0.�a��\\0eclipse\\workspace\\stock\\target\\lib";
		libraryPath = "C:\\Users\\Administrator\\workspace\\stock\\target\\lib";
		SQLite.setLibraryPath(libraryPath);
		SQLiteConnection db = new SQLiteConnection(new File("/tmp/database"));
		System.out.println(db.getDatabaseFile().getAbsolutePath());
		long count = 0;
		try {
			db.open();
			SQLiteStatement st = null;
			try {
				st=db.prepare("create table a(a1 int)");
				st.step();
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				if (st != null && !st.isDisposed()){
					st.dispose();
				}
			}
			try {
				st=db.prepare("insert into a values (2)");
				st.step();
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				if (st != null && !st.isDisposed()){
					st.dispose();
				}
			}
			try {
				st=db.prepare("select count(*) from a");
				st.step();
				count = st.columnLong(0);
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				if (st != null && !st.isDisposed()){
					st.dispose();
				}
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (db != null && !db.isDisposed()){
				db.dispose();
			}
		}
		return count;
	}
	@Override 
	public List<StockNow> getStockNows(StockNow... stockNows){
		List<StockNow> stockNowList = new ArrayList<StockNow>();
		String html = null;
		if (stockNows != null && stockNows.length > 0) {
			String url1 = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=";
			String url2 = ".tw_";
			String url3 = "&json=1&delay=0";
			StringBuffer buffer = new StringBuffer(url1);
			for (StockNow stockNow : stockNows){
				int c = stockNow.getC();
				String exCh = stockNow.getExCh();
				if (EX_CH_TSE.equals(exCh) || EX_CH_OTC.equals(exCh)){
					String symbolNo = c < 100 ? "00" + c : c < 1000 ? "0" + c : Integer.toString(c);
					buffer.append(exCh).append('_').append(symbolNo).append(url2)
					.append(new java.text.SimpleDateFormat("yyyyMMdd").format(new Date())).append('|');
				}
			}
			buffer.setLength(buffer.length() - 1);
			buffer.append(url3);
			buffer.append("&_=").append(new Date().getTime());
			html = dateService.getHtml(buffer.toString(), "UTF-8");
			if (html != null && html.indexOf("\"rtmessage\":\"OK\"") == -1)
				html = dateService.getHtml(buffer.toString(), "UTF-8");
			LOG.info("[html:twse]=" + html);
		}
		try {
		if (html != null){
			JSONObject obj = html.trim().startsWith("{") ? new JSONObject(html) : new JSONObject();
			JSONArray array = obj.has("msgArray") ? obj.getJSONArray("msgArray") : new JSONArray();
			for (int i = 0; i < array.length(); i++){
				JSONObject obj1 = array.getJSONObject(i);
				StockNow stockNow = new StockNow();
				stockNow.setZ(obj1.getDouble("z"));
				stockNow.setV(obj1.getLong("v"));
				stockNow.setO(obj1.getDouble("o"));
				stockNow.setH(obj1.getDouble("h"));
				stockNow.setL(obj1.getDouble("l"));
				stockNow.setY(obj1.getDouble("y"));
				stockNow.setU(obj1.getDouble("u"));
				if (obj1.has("w"))
				stockNow.setW(obj1.getDouble("w"));
				stockNow.setD(obj1.getString("d"));
				stockNow.setT(obj1.getString("t"));
				stockNow.setC(obj1.getInt("c"));
				stockNow.setN(obj1.getString("n"));
				stockNow.setExCh(obj1.getString("tk1").split("_")[1]);
				stockNowList.add(stockNow);
				StockInfo stockInfo = getStockInfoBySymbol(stockNow.getC());
				if (stockInfo != null){
					stockNow.setLevel1(stockInfo.getLevel1());
					stockNow.setLevel2(stockInfo.getLevel2());
					stockNow.setLevel3(stockInfo.getLevel3());
					stockNow.setBsMaxDateDay(stockInfo.getBsMaxDateDay());
					stockNow.setBsMax(stockInfo.getBsMax());
				}
			}
			if (array.length() == 0){
				String url1 = "http://www.google.com/finance/info?infotype=infoquoteall&q=";
				StringBuffer buffer = new StringBuffer(url1);
				for (StockNow stockNow : stockNows)
					if (EX_CH_TSE.equals(stockNow.getExCh()))
						buffer.append("TPE:").append(dateService.strSymbol(stockNow.getC(), 4)).append(',');
				if (buffer.length() > url1.length()){
					buffer.setLength(buffer.length() - 1);
					html = dateService.getHtml(buffer.toString(), "UTF-8");
					LOG.info("[html:google]=" + html);
					if (html != null && html.length() > 1)
					array = new JSONArray(html.substring(2));
					for (int i = 0; i < array.length(); i++){
						JSONObject obj1 = array.getJSONObject(i);
						StockNow stockNow = new StockNow();
						String vo = obj1.getString("vo");
						int k = 1;
						while (vo.indexOf('M') != -1){
							vo = vo.replace("M", "");
							k *= 1000000;
						}
						String dts = obj1.getString("lt_dts");
						// ZVOHLY.UW.DTCNE
						stockNow.setZ(Double.parseDouble(obj1.getString("l_cur").replaceAll("[(NT\\$),]", "")));
						stockNow.setV((long) (Double.parseDouble(vo.replaceAll(",","")) * k / 1000));
						stockNow.setO(Double.parseDouble(obj1.getString("op").replaceAll(",","")));
						stockNow.setH(Double.parseDouble(obj1.getString("hi").replaceAll(",","")));
						stockNow.setL(Double.parseDouble(obj1.getString("lo").replaceAll(",","")));
						stockNow.setY(Double.parseDouble(obj1.getString("pcls_fix").replaceAll(",","")));
						stockNow.setU(0);
						stockNow.setW(0);
						stockNow.setD(dts.substring(0, dts.indexOf('T')).replaceAll("-", ""));
						stockNow.setT(dts.substring(dts.indexOf('T') + 1).replace("Z", ""));
						stockNow.setC(obj1.getInt("t"));
						stockNow.setN(obj1.getString("name"));
						stockNow.setExCh(EX_CH_TSE);
						stockNowList.add(stockNow);
						StockInfo stockInfo = getStockInfoBySymbol(stockNow.getC());
						if (stockInfo != null){
							stockNow.setLevel1(stockInfo.getLevel1());
							stockNow.setLevel2(stockInfo.getLevel2());
							stockNow.setLevel3(stockInfo.getLevel3());
							stockNow.setBsMaxDateDay(stockInfo.getBsMaxDateDay());
							stockNow.setBsMax(stockInfo.getBsMax());
							stockNow.setN(stockInfo.getName().replaceAll("[ ＃＊]", ""));
						}
					}
				}
			}
		}
		} catch (RuntimeException re){
			LOG.error("getStockNows", re);
		}
		return stockNowList;
	}
	@Override 
	public long saveStockInfo(StockInfo stockInfo){
		return stockDao.saveStockInfo(stockInfo) != null ? 1 : 0;		
	}
	@Override 
	public StockInfo getStockInfoBySymbol(int symbol){
		return stockDao.getStockInfoBySymbol(symbol);
	}
	@Override 
	public List<StockInfo> getStockInfoAll(){
		return stockDao.getStockInfoAll();
	}
	@Override
	public List<java.util.Map<String, String>> getStockListMap(List<Stock> stockList){
		List<java.util.Map<String, String>> list = null;
		if (stockList != null && stockList.size() > 0){
			list = new ArrayList<java.util.Map<String, String>>();
			for (Stock stock : stockList){
				java.util.Map<String, String> map = new java.util.HashMap<String, String>();
				map.put("symbol", Integer.toString(stock.getSymbol()));
				String dateDay = Integer.toString(dateService.getRocDate(stock.getDateDay()));
				map.put("dateDay", dateDay.length() < 7 ? '0' + dateDay : dateDay);
				map.put("open", Double.toString(stock.getOpen()));
				map.put("high", Double.toString(stock.getHigh()));
				map.put("low", Double.toString(stock.getLow()));
				map.put("close", Double.toString(stock.getClose()));
				map.put("volume", Long.toString(stock.getVolume()));
				map.put("turnover", Long.toString(stock.getTurnover()));
				map.put("change", Double.toString(stock.getChange()));
				map.put("count", Long.toString(stock.getCounts()));
				if (stock.getM20() != null)
				map.put("m20", Double.toString(stock.getM20()));
				if (stock.getBbh() != null)
				map.put("bbh", Double.toString(stock.getBbh()));
				if (stock.getBbl() != null)
				map.put("bbl", Double.toString(stock.getBbl()));
				list.add(map);
			}
		}
		return list;
	}
	@Override
	public StockInfo updateStockInfoBySymbol(int symbol){
		for (String exCh : new String[]{StockService.EX_CH_TSE, StockService.EX_CH_OTC}){
			List<StockNow> stockNows = getStockNows(new StockNow(symbol, exCh));
			if (stockNows != null && stockNows.size() > 0){
				for (StockNow stockNow : stockNows){
					if (stockNow.getC() == symbol){
						StockInfo stockInfo = new StockInfo();
						stockInfo.setSymbol(symbol);
						stockInfo.setExCh(exCh);
						stockInfo.setName(stockNow.getN());
						if (saveStockInfo(stockInfo) > 0){
							return stockInfo;
						}
					}
				}
			}
		}
		return null;
	}
	private class StockObj{
		String symbol;
		String name;
		String exCh;
		StockObj(String symbol, String name){
			this.symbol = symbol;
			this.name = name;
		}
		@Override
		public String toString(){
			return '[' + symbol + ':' + name + '_' + exCh + ']';
		}
	}
	@Override
	public long saveStockInfoAll(){
		long count = 0;
		String stockTable = dateService.getHtml("http://www.emega.com.tw/js/StockTable.htm", "Big5");
		if (stockTable != null){
			Matcher matcher0 = Pattern.compile("<td nowrap=\"nowrap\" bgcolor=\".*?\">(.*?)&nbsp;</td>").matcher(stockTable);
			List<StockObj> list1 = new java.util.LinkedList<StockObj>();
			List<StockInfo> list2 = new java.util.LinkedList<StockInfo>();
			int n = 21;
			int i = 0;
			String symbol = null;
			String name = null;
			while (matcher0.find()) {
				i = (i + 1) % 2;
				String value = matcher0.group(1).replaceAll("<FONT .*?>", "").replaceAll("</FONT>", "").replaceAll("<B>", "").replaceAll("</B>", "");
				if (i == 1){
					symbol = value;
					name = null;
				} else {
					name = value;
					list1.add(new StockObj(symbol, name));
					symbol = null;
					name = null;
				}
			}
			for (int i1 = 0; i1 < list1.size(); i1++){
				if (list1.get(i1).symbol.matches("\\d+")){
					int i2 = i1;
					while(true){
						i2-=n;
						if (i2 < 0){
							i2 = i2 + list1.size() - 1;
						}
						if (!list1.get(i2).symbol.matches("\\d+")){
							if (list1.get(i2).symbol.equals("上市")){
								list1.get(i1).exCh = StockService.EX_CH_TSE;
								break;
							} else if (list1.get(i2).symbol.equals("上櫃")){
								list1.get(i1).exCh = StockService.EX_CH_OTC;
								break;
							}
						}
						if (i2 == 0){
							break;
						}
					}
					if (list1.get(i1).exCh != null){
						StockInfo stockInfo = getStockInfoBySymbol(Integer.parseInt(list1.get(i1).symbol));
						if (stockInfo == null){
							stockInfo = new StockInfo();
						}
						stockInfo.setSymbol(Integer.parseInt(list1.get(i1).symbol));
						stockInfo.setName(list1.get(i1).name);
						stockInfo.setExCh(list1.get(i1).exCh);
						list2.add(stockInfo);
					}
				}
			}
			for (StockInfo stockInfo : list2){
				count += saveStockInfo(stockInfo);
				System.out.println(stockInfo);
			}
		}
		return count;
	}
	@Override
	public StockInfo saveLevel(int symbol, int dateDay1, int dateDay2){
		StockInfo stockInfo = null;
		List<Stock> stocks = stockDao.saveLevel(symbol, dateDay1, dateDay2);
		if (stocks != null && stocks.size() > 0){
			stockInfo = stockDao.getStockInfoBySymbol(symbol);
			if (stockInfo != null){
				stockInfo.setDateDay1(dateDay1);
				stockInfo.setDateDay2(dateDay2);
				double high = 0;
				double low = 10000;
				Date highD = null;
				Date lowD = null;
				for (Stock stock : stocks){
					if (stock.getHigh() > high){
						high = stock.getHigh();
						highD = stock.getDateDay();
					}
					if (stock.getLow() < low){
						low = stock.getLow();
						lowD = stock.getDateDay();
					}
				}
				double level1 = Math.pow(low, 0.382) * Math.pow(high, 0.618);
				double level2 = Math.pow(low, 0.5) * Math.pow(high, 0.5);
				double level3 = Math.pow(low, 0.618) * Math.pow(high, 0.382);
				if (highD != null && lowD != null){
					if (highD.before(lowD)){
						double level = level1;
						level1 = level3;
						level3 = level;
					}
				}
				stockInfo.setLevel1(level1);
				stockInfo.setLevel2(level2);
				stockInfo.setLevel3(level3);
				stockInfo = stockDao.saveStockInfo(stockInfo);
			}
		}
		return stockInfo;
	}
	@Override
	public StockInfo saveBsMax(int symbol, int bsMaxDateDay, double bsMax){
		StockInfo stockInfo = stockDao.getStockInfoBySymbol(symbol);
		if (stockInfo != null){
			stockInfo.setBsMaxDateDay(bsMaxDateDay);
			stockInfo.setBsMax(bsMax);
			stockInfo = stockDao.saveStockInfo(stockInfo);
		}
		return stockInfo;
	}
	@Override
	public List<String> getGroup() {
		List<String> list = null;
		if (groupMap.size() > 0){
			list = new ArrayList<String>();
			for (String key : groupMap.keySet()){
				list.add(key);
			} 
		}
		return list;
	}
	@Override
	public String getGroup(String group) {
		return groupMap.get(group);
	}
	@Override
	public void saveGroup(String group, String value) {
		if (group != null && !group.trim().isEmpty()){
			groupMap.put(group.trim(), value != null ? value.trim() : value);
			java.io.PrintWriter writer = null;
			try {
				writer = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter("group_map.txt")));
				for (String key : groupMap.keySet()){
					writer.println(key + ':' + groupMap.get(key));
				}
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (writer != null){
					writer.close();
				}
			}
		}
	}
	@Override
	public String removeGroup(String group){
		String value = groupMap.remove(group);
		saveGroup();
		return value;
	}
	private void saveGroup(){
		java.io.PrintWriter writer = null;
		try {
			writer = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter("group_map.txt")));
			for (String key : groupMap.keySet()){
				writer.println(key + ':' + groupMap.get(key));
			}
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null){
				writer.close();
			}
		}
	}
	@Override
	public String removeGroup(String group, String value){
		if (group != null && !group.trim().isEmpty() && value != null && !value.trim().isEmpty()){
			group = group.trim();
			String oValue = groupMap.get(group);
			if (oValue != null && !oValue.trim().isEmpty()){
				java.util.Set<String> set = new java.util.HashSet<String>();
				String[] values = oValue.split(",");
				for (int i = 0; i < values.length; i++){
					if (!values[i].trim().isEmpty()){
						set.add(values[i].trim());
					}
				}
				set.remove(value.trim());
				StringBuffer buffer = new StringBuffer();
				for (String val : set){
					buffer.append(val).append(',');
				}
				if (buffer.length() > 0)
				buffer.setLength(buffer.length() - 1);
				oValue = buffer.toString();
				saveGroup(group, oValue);
			}
		}
		return groupMap.get(group);
	}
	@Override
	public String addGroup(String group, String value){
		if (group != null && !group.trim().isEmpty() && value != null && !value.trim().isEmpty()){
			group = group.trim();
			String oValue = groupMap.get(group);
			if (oValue == null || oValue.trim().isEmpty()){
				oValue = value;
			} else {
				java.util.Set<String> set = new java.util.HashSet<String>();
				String[] values = oValue.split(",");
				for (int i = 0; i < values.length; i++){
					if (!values[i].trim().isEmpty()){
						set.add(values[i].trim());
					}
				}
				set.add(value.trim());
				StringBuffer buffer = new StringBuffer();
				for (String val : set){
					buffer.append(val).append(',');
				}
				buffer.setLength(buffer.length() - 1);
				oValue = buffer.toString();
			}
			saveGroup(group, oValue);
		}
		return groupMap.get(group);
	}
	@Override
	public StockRecord saveStockRecord(int symbol, int dateDay1, double price1, int counts, int buySell){
		StockRecord stockRecord = new StockRecord();
		stockRecord.setSymbol(symbol);
		stockRecord.setDateDay1(dateDay1);
		stockRecord.setPrice1(price1);
		stockRecord.setCounts(counts);
		stockRecord.setBuySell(buySell);
		return stockRecordDao.saveStockRecord(stockRecord);
	}
	@Override
	public StockRecord updateStockRecord(long id, int dateDay2, double price2, int counts){
		StockRecord stockRecord = new StockRecord();
		stockRecord.setId(id);
		stockRecord.setDateDay2(dateDay2);
		stockRecord.setPrice2(price2);
		stockRecord.setCounts(counts);
		return stockRecordDao.updateStockRecord(stockRecord);
	}
	@Override
	public StockRecord getStockRecord(long id){
		return stockRecordDao.getStockRecord(id);
	}
	@Override
	public List<StockRecord> getStockRecordAll(){
		return stockRecordDao.getStockRecordAll();
	}
	@Override
	public StockRecord removeStockRecord(long id){
		return stockRecordDao.removeStockRecord(id);
	}
	@Override
	public List<Integer> getTopV20(){
		List<Integer> list = new ArrayList<Integer>();
		String url0 = "http://www.twse.com.tw/ch/trading/exchange/MI_INDEX20/genpage/Report";
		String url1 = "/A131";
		String url2 = ".php";
		String today = null; 
		for (int i = 0; i < 15; i++){
			today = dateService.getYesterday(today);
			String html = dateService.getHtml(url0 + today.substring(0, 6) + url1 + today + url2, "big5");
			if (html != null){
				Matcher matcher0 = Pattern.compile("'board_trad'>(?s).*?</table>").matcher(html);
				Matcher matcher1 = null;
				boolean flag = false;
				while (matcher0.find()) {
					matcher1 = Pattern.compile("<tr bgcolor='#FFFFFF' class='basic2'>(?s).*?<div align='center'>(\\w+) *</div>(?s).*?</tr>").matcher(matcher0.group(0));
					while (matcher1.find()) {
						if(matcher1.group(1).matches("\\d{4}"))
						{
							list.add(Integer.parseInt(matcher1.group(1)));
						flag = true;}
					}
				}
				if (flag)
				break;
			}
		}
		return list;
	}
	@Override
	public void computeBySymbol(int symbol){
		stockDao.computeBySymbol(symbol);
	}
	@Override
	public long saveStockComputeValueListTx(List<Stock> stocks) {
		long count = 0;
		if (stocks != null && stocks.size() > 0){
			for (Stock stock : stocks){
				count += stockDao.saveStockComputeValue(stock) != null ? 1 : 0;
			}
		}
		return count;
	}
	@Override
	public List<Map<String, String>> getStockNowsComputed(List<StockNow> stockNowList) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if (stockNowList != null && stockNowList.size() > 0){
			for (StockNow stockNow : stockNowList){
				Map<String, String> map = new java.util.HashMap<String, String>();
				map.put(SYMBOL, Integer.toString(stockNow.getC()));
				map.put(DATE_DAY, stockNow.getD());
				map.put(OPEN, Double.toString(stockNow.getO()));
				map.put(HIGH, Double.toString(stockNow.getH()));
				map.put(LOW, Double.toString(stockNow.getL()));
				map.put(CLOSE, Double.toString(stockNow.getZ()));
				map.put(VOLUME, Long.toString(stockNow.getV()) + ",000");
				map.put(TURNOVER, Integer.toString(0));
				if (stockNow.getY() > 0)
				map.put(CHANGE, Double.toString(getDouble(stockNow.getZ() - stockNow.getY(), 2)));
				map.put(COUNTS, Integer.toString(0));
				map.put(EXCH, stockNow.getExCh());
				map.put(NAME, stockNow.getN());
				map.put(TIME, stockNow.getT());
				if (stockNow.getY() > 0){
				map.put(YES, Double.toString(stockNow.getY()));
				map.put(CHANGE_Y, Double.toString(getDouble((stockNow.getZ() - stockNow.getY()) / stockNow.getY() * 100, 2)));
				}
				if (stockNow.getLevel1() != null) map.put(LEVEL1, Double.toString(stockNow.getLevel1()));
				if (stockNow.getLevel2() != null) map.put(LEVEL2, Double.toString(stockNow.getLevel2()));
				if (stockNow.getLevel3() != null) map.put(LEVEL3, Double.toString(stockNow.getLevel3()));
				if (stockNow.getBsMaxDateDay() != null) map.put(BS_MAX_DATE_DAY, Integer.toString(stockNow.getBsMaxDateDay()));
				if (stockNow.getBsMax() != null) map.put(BS_MAX, Double.toString(stockNow.getBsMax()));
				map.put(UP_LIMIT, Double.toString(stockNow.getU()));
				map.put(DOWN_LIMIT, Double.toString(stockNow.getW()));
				List<Stock> stockList = stockCache.getComputeTx(stockNow.getC(), Integer.parseInt(stockNow.getD().substring(0, 6)));
				Stock stock = null;
				if (stockList != null && stockList.size() > 0){
					java.text.DateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
					for (int i = stockList.size() - 1; i >= 0; i--){
						stock = stockList.get(i);
						int dateDay = Integer.parseInt(format.format(stock.getDateDay()));
						if (Integer.parseInt(stockNow.getD()) > dateDay){
							stock = newStock(stockNow);
							stockList.add(i + 1, stock);
							break;
						} else if (Integer.parseInt(stockNow.getD()) == dateDay){
							stock.setClose(stockNow.getZ());
							map.put(TURNOVER, Long.toString(stock.getTurnover()));
							map.put(COUNTS, Long.toString(stock.getCounts()));
							break;
						}
					}
				} else {
					if (stockList == null){
						stockList = new ArrayList<Stock>();
					}
					stock = newStock(stockNow);
					stockList.add(stock);
				}
				stockCache.computeStockList(stockList);
				if (stock != null && stock.getM20() != null){
				map.put(M20, Double.toString(stock.getM20()));
				map.put(BBH, Double.toString(stock.getBbh()));
				map.put(BBL, Double.toString(stock.getBbl()));
				map.put(BIAS, Double.toString(getDouble(bias(stock.getHigh(), stock.getLow(), stock.getM20()) / stock.getM20() * 100, 2)));
				}
				list.add(map);
			}
		}
		return list;
	}
	private Stock newStock(StockNow stockNow){
		Stock stock = new Stock();
		stock.setSymbol(stockNow.getC());
		stock.setDateDay(dateService.getDateDay(stockNow.getD()));
		stock.setOpen(stockNow.getO());
		stock.setHigh(stockNow.getH());
		stock.setLow(stockNow.getL());
		stock.setClose(stockNow.getZ());
		stock.setVolume(stockNow.getV() * 1000);
		stock.setTurnover(0);
		stock.setChange(getDouble(stockNow.getZ() - stockNow.getY(), 2));
		stock.setCounts(0);
		return stock;
	}
	private double getDouble(double value, int dig){return new java.math.BigDecimal(value).setScale(dig, java.math.BigDecimal.ROUND_HALF_UP).doubleValue();}
	@Override
	public List<Map<String, String>> getStockComputedNows(List<Stock> stockList) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if (stockList != null && stockList.size() > 0){
			for (Stock stock : stockList){
				Map<String, String> map = new java.util.HashMap<String, String>();
				map.put(SYMBOL, Integer.toString(stock.getSymbol()));
				String dateDay = Integer.toString(dateService.getRocDate(stock.getDateDay()));
				map.put(DATE_DAY, dateDay.length() < 7 ? '0' + dateDay : dateDay);
				map.put(OPEN, Double.toString(stock.getOpen()));
				map.put(HIGH, Double.toString(stock.getHigh()));
				map.put(LOW, Double.toString(stock.getLow()));
				map.put(CLOSE, Double.toString(stock.getClose()));
				map.put(VOLUME, Long.toString(stock.getVolume()));
				map.put(TURNOVER, Long.toString(stock.getTurnover()));
				map.put(CHANGE, Double.toString(stock.getChange()));
				map.put(COUNTS, Long.toString(stock.getCounts()));
				if (stock.getM20() != null){
					map.put(M20, Double.toString(stock.getM20()));
					map.put(BBH, Double.toString(stock.getBbh()));
					map.put(BBL, Double.toString(stock.getBbl()));
					map.put(BIAS, Double.toString(getDouble(bias(stock.getHigh(), stock.getLow(), stock.getM20()) / stock.getM20() * 100, 2)));
				}
				list.add(map);
			}
		}
		return list;
	}
	@Override
	public List<Map<String, String>> getStockComputedNows(int symbol, int yearMonth, int month){
		int currentYearMonth = dateService.getYearMonthByDateDay(dateService.getCurrentYearMonth());
		if (yearMonth > currentYearMonth) yearMonth = currentYearMonth;
		List<Stock> stockList = stockCache.getComputeTx(symbol, yearMonth);
		if (stockList == null){
			stockList = new ArrayList<Stock>();
		}
		if (yearMonth == dateService.getYearMonthByDateDay(dateService.getCurrentYearMonth())){
			List<StockNow> stockNows = getStockNows(new StockNow(symbol, STOCK_INFO_MAP.get(symbol).getExCh()));
			if (stockNows != null && stockNows.size() == 1){
				StockNow stockNow = stockNows.get(0);
				if (stockList.size() > 0){
					java.text.DateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
					for (int i = stockList.size() - 1; i >= 0; i--){
						Stock stock = stockList.get(i);
						int dateDay = Integer.parseInt(format.format(stock.getDateDay()));
						if (Integer.parseInt(stockNow.getD()) > dateDay){
							stockList.add(i + 1, newStock(stockNow));
							break;
						} else if (Integer.parseInt(stockNow.getD()) == dateDay){
							stock.setClose(stockNow.getZ());
							stock.setHigh(stockNow.getH());
							stock.setLow(stockNow.getL());
							break;
						}
					}
				} else {
					stockList.add(newStock(stockNow));
				}
			}
		}
		for (int i = 0; i < month - 1; i++){
			yearMonth = dateService.getRightYearMonth(yearMonth - 1);
			if (!stockList.addAll(0, stockCache.getComputeTx(symbol, yearMonth))){
				break;
			}
		}
		stockCache.computeStockList(stockList);
		return getStockComputedNows(stockList);
	}
	@Override
	public List<StockNow> getStockNowsOffline(StockNow... stockNows){
		List<StockNow> stockNowList = new ArrayList<StockNow>();
		int yearMonth = dateService.getYearMonthByDateDay(dateService.getCurrentYearMonth());
		for (StockNow stockNow : stockNows){
			StockInfo stockInfo = getStockInfoBySymbol(stockNow.getC());
			if (stockInfo != null){
				stockNow.setLevel1(stockInfo.getLevel1());
				stockNow.setLevel2(stockInfo.getLevel2());
				stockNow.setLevel3(stockInfo.getLevel3());
				stockNow.setBsMaxDateDay(stockInfo.getBsMaxDateDay());
				stockNow.setBsMax(stockInfo.getBsMax());
				stockNow.setN(stockInfo.getName());
			}
			List<Stock> stockList = stockCache.getComputeTx(stockNow.getC(), yearMonth);
			if (stockList == null || stockList.size() == 0){
				stockList = stockCache.getComputeTx(stockNow.getC(), dateService.getRightYearMonth(yearMonth - 1));
			}
			if (stockList != null && stockList.size() > 0){
				Stock stock = stockList.get(stockList.size() - 1);
				stockNow.setD(new java.text.SimpleDateFormat("yyyyMMdd").format(stock.getDateDay()));
				stockNow.setO(stock.getOpen());
				stockNow.setH(stock.getHigh());
				stockNow.setL(stock.getLow());
				stockNow.setZ(stock.getClose());
				stockNow.setV(stock.getVolume());
			}
			stockNowList.add(stockNow);
		}
		return stockNowList;
	}
	private double bias(double high, double low, double m20){
		double bias = high - m20;
		double bias1 = low - m20;
		if (Math.abs(bias) < Math.abs(bias1)) bias = bias1;
		return bias;
	}
}