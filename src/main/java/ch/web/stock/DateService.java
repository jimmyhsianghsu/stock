package ch.web.stock;
import java.util.Date;
import java.util.List;
public interface DateService {
	Date getDateDay(String strDate);
	Date getYearMonth(int yearMonth);
	Date getCurrentYearMonth();
	Date getCurrentDay();
	int getRocDate(Date date);
	String getHtml(String url, String format);
	List<Integer> getYearMonthList(int months);
	List<Stock> sortStock(List<Stock> stocks);
	List<StockStatus> sortStockStatus(List<StockStatus> stockStatus);
	Date getDateDay(int dateDay);
	String getYesterday(String today);
	java.util.Map<Integer, List<Integer>> getYearWeekMap(int year);
	int getYearMonthByDateDay(Date dateDay);
	int getRightYearMonth(int yearMonth);
	void computeStocksAll(List<Stock> stocks);
	void computeStockLast(List<Stock> stocks, Stock lastStock);
	String strSymbol(int symbol, int digit);
}