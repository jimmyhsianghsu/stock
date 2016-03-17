package ch.web.stock;
import java.util.List;
public interface StockService {
	String EX_CH_TSE = "tse";
	String EX_CH_OTC = "otc";
	String SQL_DROP_STOCK = "drop table stock if exists";
	String SQL_CREATE_STOCK = "create table stock (" +
			"symbol INTEGER, date_day TIMESTAMP," +
			"open DOUBLE, high DOUBLE, low DOUBLE, close DOUBLE," +
			"volume BIGINT, turnover BIGINT, change DOUBLE, counts BIGINT," +
			"primary key (symbol,date_day))";
	String SQL_DROP_STOCK_STATUS = "drop table stock_status if exists";
	String SQL_CREATE_STOCK_STATUS = "create table stock_status (" +
			"symbol INTEGER, year_month INTEGER," +
			"status INTEGER, date_day TIMESTAMP)";
	java.util.Map<Integer, StockInfo> STOCK_INFO_MAP = new java.util.HashMap<Integer, StockInfo>();
	boolean createTable();
	List<Stock> getStockBySymbolMonthUrl(int symbol, int yearMonth);
	List<Stock> getStockBySymbolMonthDb(int symbol, int yearMonth);
	List<Stock> getStockBySymbolMonth(int symbol, int yearMonth);
	List<Stock> updateStockBySymbolMonth(StockStatus stockStatus);
	long saveStockTx(Stock stock);
	long saveStockListTx(List<Stock> stocks);
	List<Stock> getStocksAll();
	List<StockStatus> getStockStatusBySymbol(int symbol);
	StockStatus getStockStatusBySymbolMonth(int symbol, int yearMonth);
//	long saveStockStatusTx(StockStatus stockStatus);
//	long saveStockStatusListTx(List<StockStatus> stockStatus);
	List<StockStatus> getStockStatusAll();
	long testSqliteCount();
	List<StockNow> getStockNows(StockNow... stockNows);
	String SQL_DROP_STOCK_INFO = "drop table stock_info if exists";
	String SQL_CREATE_STOCK_INFO = "create table stock_info (" +
			"symbol INTEGER, ex_ch char(3), name char(20)," +
			"date_day1 INTEGER,date_day2 INTEGER, level_1 DOUBLE, level_2 DOUBLE, level_3 DOUBLE" +
			", bs_max_date_day INTEGER, bs_max DOUBLE)";
	long saveStockInfo(StockInfo stockInfo);
	StockInfo getStockInfoBySymbol(int symbol);
	List<StockInfo> getStockInfoAll();
	List<java.util.Map<String,String>> getStockListMap(List<Stock> stockList);
	StockInfo updateStockInfoBySymbol(int symbol);
	long saveStockInfoAll();
	StockInfo saveLevel(int symbol, int dateDay1, int dateDay2);
	StockInfo saveBsMax(int symbol, int bsMaxDateDay, double bsMax);
	List<String> getGroup();
	String getGroup(String group);
	void saveGroup(String group, String value);
	String removeGroup(String group);
	String removeGroup(String group, String value);
	String addGroup(String group, String value);
	StockRecord saveStockRecord(int symbol, int dateDay1, double price1, int counts, int buySell);
	StockRecord updateStockRecord(long id, int dateDay2, double price2, int counts);
	StockRecord getStockRecord(long id);
	List<StockRecord> getStockRecordAll();
	StockRecord removeStockRecord(long id);
	List<Integer> getTopV20();
	void computeBySymbol(int symbol);
	long saveStockComputeValueListTx(List<Stock> stocks);
	String SYMBOL = "symbol", DATE_DAY = "dateDay";
	String OPEN = "open", HIGH = "high", LOW = "low", CLOSE = "close";
	String VOLUME = "volume", TURNOVER = "turnover", CHANGE = "change", COUNTS = "counts";
	String M20 = "m20", BBH = "bbh", BBL = "bbl", BIAS = "bias";
	String EXCH = "exCh", NAME = "name", TIME = "time";
	String YES = "yes", CHANGE_Y = "changey";
	String LEVEL1 = "level1", LEVEL2 = "level2", LEVEL3 = "level3";
	String BS_MAX_DATE_DAY = "bsMaxDateDay", BS_MAX = "bsMax";
	String UP_LIMIT = "upLimit", DOWN_LIMIT = "downLimit";
	List<java.util.Map<String, String>> getStockNowsComputed(List<StockNow> stockNowList);
	List<java.util.Map<String, String>> getStockComputedNows(List<Stock> stockList);
	List<java.util.Map<String, String>> getStockComputedNows(int symbol, int yearMonth, int month);
	List<StockNow> getStockNowsOffline(StockNow... stockNows);
}