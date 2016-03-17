package ch.web.stock.cache;
import java.util.List;
import java.util.Map;
import ch.web.stock.Stock;
public interface StockCacheService {
	List<Stock> getComputeTx(int symbol, int yearMonth);
	void computeStockList(List<Stock> stockList);
	Map<String, String> getComputeMap();
	void start();
	void stop();
}