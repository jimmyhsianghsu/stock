package ch.web.stock.cache;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import ch.web.stock.DateService;
import ch.web.stock.Stock;
import ch.web.stock.StockService;
@Service
public class StockCacheServiceImpl implements StockCacheService {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StockCacheServiceImpl.class);
	@Autowired
	private StockService stockService;
	@Autowired
	private DateService dateService;
	@Autowired
	private StockCacheLoader stockCacheLoader;
	private LoadingCache<StockKey, List<Stock>> dayCache;
	private Thread thread;
	private Runceasable runnable;
	private ConcurrentMap<StockKey, Integer> computeMap = new ConcurrentHashMap<StockKey, Integer>();
	@javax.annotation.PostConstruct
	public void init(){
		dayCache = CacheBuilder.newBuilder().maximumSize(1000).refreshAfterWrite(1, TimeUnit.MINUTES).build(stockCacheLoader);
	}
	private List<Stock> cacheGet(int symbol, int yearMonth) {
		List<Stock> stockList = null;
		StockKey stockKey = new StockKey(symbol, yearMonth);
		try {
			dayCache.refresh(stockKey);
			stockList = dayCache.get(stockKey);
		} catch (ExecutionException ee) {
			LOG.error("cacheGet", ee);
		} catch (RuntimeException re){
			LOG.error("cacheGet", re);
		}
		putComputeMap(symbol, yearMonth, stockList != null && stockList.size() > 0 ? 0 : -2);
		return dateService.sortStock(stockList);
	}
	private void putComputeMap(int symbol, int yearMonth, int value){
		StockKey stockKey = new StockKey(symbol, yearMonth);
		switch (value){
		case 0:
		case -2:
			Integer oriValue = computeMap.putIfAbsent(stockKey, value);
			if (oriValue != null){
				computeMap.replace(stockKey, -2, value);
			}
			break;
		case 2:
		case -1:
			computeMap.replace(stockKey, 0, value);
			if (value == -1){
				computeMap.replace(stockKey, 2, value);	
			}
			break;
		case 1:
			computeMap.replace(stockKey, -1, value);
			break;
		default:
		}
	}
	@Override
	@Transactional
	public List<Stock> getComputeTx(int symbol, int yearMonth) {
		List<Stock> stockList = cacheGet(symbol, yearMonth);
		Integer value = computeMap.get(new StockKey(symbol, yearMonth));
		if (value == null || value < 1 || needCompute(stockList))
		computeStockList(stockList);
		putComputeMap(symbol, yearMonth, 2);
		return stockList != null ? new java.util.LinkedList<Stock>(stockList) : null;
	}
	@Override
	public void computeStockList(List<Stock> stockList) {
		if (stockList != null && stockList.size() > 0){
			int p20 = 20;
			List<Stock> stocks = new LinkedList<Stock>(stockList);
			for (int i = stockList.size() - 1; i >= 0; i--){
				Stock[] stockArry = new Stock[p20];
				int i1;
				for (i1 = 0; i1 < p20; i1++){
					int i2 = i - p20 + i1 + 1;
					Stock stock = null;
					if (i2 < 0){
						if (stocks.size() - stockList.size() + i2 < 0){
							stocks.addAll(0, getPrevStocks(stocks.get(0).getSymbol(), dateService.getYearMonthByDateDay(stocks.get(0).getDateDay()), -i2));
						}
						if (stocks.size() - stockList.size() + i2 >= 0){
							stock = stocks.get(stocks.size() - stockList.size() + i2);
						}
					} else {
						stock = stockList.get(i2);
					}
					if (stock != null){
						stockArry[i1] = stock; 
					} else {
						break;
					}
				}
				if (i1 == p20){
					computeStocks(stockArry);
				} else {
					break;
				}
			}
		}
	}
	private List<Stock> getPrevStocks(int symbol, int yearMonth, int count){
		List<Stock> stocks = new LinkedList<Stock>();
		while (stocks.size() < count){
			yearMonth = dateService.getRightYearMonth(yearMonth - 1);
			List<Stock> getStocks = cacheGet(symbol, yearMonth);
			putComputeMap(symbol, yearMonth, -1);
			if (getStocks != null && getStocks.size() > 0){
				stocks.addAll(0, getStocks);
			} else {
				break;
			}
		}
		return stocks;
	}
	private void computeStocks(Stock... stocks){
		if (stocks != null && stocks.length > 0){
			double ma = 0;
			for (Stock stock : stocks){
				ma+= stock.getClose();
			}
			ma = ma / stocks.length;
			double bb = 0;
			for (Stock stock : stocks){
				bb += Math.pow(stock.getClose() - ma, 2);
			}
			bb = Math.pow(bb / stocks.length, 0.5);
			double bbh = ma + bb * 2;
			double bbl = ma - bb * 2;
			Stock stock = stocks[stocks.length - 1];
			stock.setM20(ma);
			stock.setBbh(bbh);
			stock.setBbl(bbl);
		}
	}
	@Override
	public Map<String, String> getComputeMap(){
		Map<String, String> map = new TreeMap<String, String>();
		if (computeMap.size() > 0){
			for (StockKey stockKey : computeMap.keySet()){
				map.put(stockKey.toString(), computeMap.get(stockKey).toString());
			}
		}
		return map;
	}
	private class Runceasable implements Runnable {
		private boolean ceased = false;
		@Override
		public void run() {
			while (!ceased){
				for (StockKey stockKey : computeMap.keySet()){
					if (computeMap.get(stockKey) == -1){
						stockService.saveStockComputeValueListTx(getComputeTx(stockKey.getSymbol(), stockKey.getYearMonth()));
						putComputeMap(stockKey.getSymbol(), stockKey.getYearMonth(), 1);
					}
				}
			}
		}
	}
	@Override
	public void start() {
		if (thread == null || !thread.isAlive()){
			runnable = new Runceasable();
			thread = new Thread(runnable);
			thread.start();
		}
	}
	@Override
	public void stop() {
		if (runnable != null && thread != null && thread.isAlive()){
			runnable.ceased = true;
		}
	}
	private boolean needCompute(List<Stock> stockList){
		if (stockList != null && stockList.size() > 0){
			for (Stock stock : stockList){
				if (stock.getM20() == null || stock.getM20() == 0){
					putComputeMap(stock.getSymbol(), dateService.getYearMonthByDateDay(stock.getDateDay()), -1);
					return true;
				}
			}
		}
		return false;
	}
}