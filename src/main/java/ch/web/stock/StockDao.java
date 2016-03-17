package ch.web.stock;
import java.util.List;
public interface StockDao {
	Stock saveStock(Stock stock);
	List<Stock> getStockBySymbolMonth(int symbol, int yearMonth);
	List<Stock> getStockAll();
	long getStockCount();
	StockStatus saveStockStatus(StockStatus stockStatus);
	List<StockStatus> getStockStatusBySymbol(int symbol);
	StockStatus getStockStatusBySymbolMonth(int symbol, int yearMonth);
	List<StockStatus> getStockStatusAll();
	long getStockStatusCount();
	StockInfo saveStockInfo(StockInfo stockInfo);
	StockInfo getStockInfoBySymbol(int symbol);
	List<StockInfo> getStockInfoAll();
	List<Stock> saveLevel(int symbol, int dateDay1, int dateDay2);
	int computeBySymbol(int symbol);
	Stock saveStockComputeValue(Stock stock);
}